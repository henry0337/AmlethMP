"""Endpoint tests against a real PostgreSQL database.

Set TEST_DATABASE_URL to a postgresql+asyncpg:// URL whose database name ends in `_test`; the
tables are created and dropped around every test, so never point it at a database with real data.
"""

import os
import uuid
from collections.abc import AsyncIterator
from datetime import UTC, datetime

import jwt
import pytest
from httpx import ASGITransport, AsyncClient
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncEngine, async_sessionmaker, create_async_engine
from sqlalchemy.pool import NullPool

from app.api.deps import get_google_verifier
from app.core.config import get_settings
from app.core.security import hash_refresh_token
from app.db.base import Base
from app.db.session import get_db_session
from app.main import app
from app.models import OAuthAccount, RefreshToken, User, UserStatus
from app.services.google_id_token import (
    GoogleCertsUnavailableError,
    GoogleIdentity,
    InvalidGoogleTokenError,
)

TEST_DATABASE_URL = os.environ.get("TEST_DATABASE_URL")

pytestmark = pytest.mark.skipif(
    not TEST_DATABASE_URL or not TEST_DATABASE_URL.rsplit("/", 1)[-1].endswith("_test"),
    reason="TEST_DATABASE_URL must point at a database whose name ends in _test",
)

SIGN_IN_BODY = {
    "id_token": "google-id-token",
    "client": "android",
    "device_name": "Pixel 9",
}


class FakeVerifier:
    def __init__(self) -> None:
        self.identity = GoogleIdentity(
            subject="google-sub-1",
            email="listener@example.com",
            email_verified=True,
            name="Listener One",
            picture="https://example.com/avatar.png",
        )
        self.error: Exception | None = None

    async def verify(self, id_token: str) -> GoogleIdentity:
        if self.error is not None:
            raise self.error
        return self.identity


@pytest.fixture
async def engine() -> AsyncIterator[AsyncEngine]:
    assert TEST_DATABASE_URL is not None
    test_engine = create_async_engine(TEST_DATABASE_URL, poolclass=NullPool)
    async with test_engine.begin() as connection:
        await connection.run_sync(Base.metadata.drop_all)
        await connection.run_sync(Base.metadata.create_all)
    yield test_engine
    async with test_engine.begin() as connection:
        await connection.run_sync(Base.metadata.drop_all)
    await test_engine.dispose()


@pytest.fixture
def session_factory(engine: AsyncEngine) -> async_sessionmaker:
    return async_sessionmaker(engine, expire_on_commit=False, autoflush=False)


@pytest.fixture
def verifier() -> FakeVerifier:
    return FakeVerifier()


@pytest.fixture
async def client(
    session_factory: async_sessionmaker, verifier: FakeVerifier
) -> AsyncIterator[AsyncClient]:
    async def override_db_session() -> AsyncIterator:
        async with session_factory() as session:
            yield session

    app.dependency_overrides[get_db_session] = override_db_session
    app.dependency_overrides[get_google_verifier] = lambda: verifier
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as http_client:
        yield http_client
    app.dependency_overrides.clear()


async def test_first_sign_in_creates_the_user_and_returns_tokens(
    client: AsyncClient, session_factory: async_sessionmaker
) -> None:
    response = await client.post("/auth/google", json=SIGN_IN_BODY)

    assert response.status_code == 200
    body = response.json()
    assert body["token_type"] == "Bearer"
    assert body["expires_in"] == get_settings().access_token_ttl_minutes * 60

    async with session_factory() as session:
        user = (await session.execute(select(User))).scalar_one()
        account = (await session.execute(select(OAuthAccount))).scalar_one()
        refresh = (await session.execute(select(RefreshToken))).scalar_one()

    assert user.id.version == 7
    assert user.display_name == "Listener One"
    assert user.email == "listener@example.com"
    assert user.last_login_at is not None
    assert (account.provider.value, account.provider_user_id) == (
        "google",
        "google-sub-1",
    )
    assert account.user_id == user.id

    claims = jwt.decode(
        body["access_token"],
        get_settings().jwt_secret.get_secret_value(),
        algorithms=["HS256"],
    )
    assert claims["sub"] == str(user.id)
    assert claims["role"] == "member"

    assert refresh.user_id == user.id
    assert refresh.token_hash == hash_refresh_token(body["refresh_token"])
    assert refresh.token_hash != body["refresh_token"]
    assert refresh.client.value == "android"
    assert refresh.device_name == "Pixel 9"
    assert refresh.expires_at > datetime.now(UTC)


async def test_second_sign_in_reuses_the_user_and_opens_a_new_session(
    client: AsyncClient, session_factory: async_sessionmaker
) -> None:
    first = (await client.post("/auth/google", json=SIGN_IN_BODY)).json()
    second = (await client.post("/auth/google", json=SIGN_IN_BODY)).json()

    assert first["refresh_token"] != second["refresh_token"]
    async with session_factory() as session:
        assert await session.scalar(select(func.count()).select_from(User)) == 1
        assert await session.scalar(select(func.count()).select_from(OAuthAccount)) == 1
        refresh_tokens = (await session.execute(select(RefreshToken))).scalars().all()
    assert len(refresh_tokens) == 2
    assert len({token.family_id for token in refresh_tokens}) == 2


async def test_sign_in_refreshes_the_avatar_but_keeps_the_edited_name(
    client: AsyncClient, session_factory: async_sessionmaker, verifier: FakeVerifier
) -> None:
    await client.post("/auth/google", json=SIGN_IN_BODY)
    async with session_factory() as session:
        user = (await session.execute(select(User))).scalar_one()
        user.display_name = "Chosen Name"
        await session.commit()

    verifier.identity = GoogleIdentity(
        subject="google-sub-1",
        email="listener@example.com",
        email_verified=True,
        name="Google Name Changed",
        picture="https://example.com/new-avatar.png",
    )
    await client.post("/auth/google", json=SIGN_IN_BODY)

    async with session_factory() as session:
        user = (await session.execute(select(User))).scalar_one()
    assert user.display_name == "Chosen Name"
    assert user.avatar_url == "https://example.com/new-avatar.png"


async def test_same_email_with_another_google_account_is_a_separate_user(
    client: AsyncClient, session_factory: async_sessionmaker, verifier: FakeVerifier
) -> None:
    await client.post("/auth/google", json=SIGN_IN_BODY)
    verifier.identity = GoogleIdentity(
        subject="google-sub-2",
        email="listener@example.com",
        email_verified=True,
        name="Listener Two",
        picture=None,
    )
    await client.post("/auth/google", json=SIGN_IN_BODY)

    async with session_factory() as session:
        assert await session.scalar(select(func.count()).select_from(User)) == 2


async def test_missing_name_falls_back_to_the_email_prefix(
    client: AsyncClient, session_factory: async_sessionmaker, verifier: FakeVerifier
) -> None:
    verifier.identity = GoogleIdentity(
        subject="google-sub-3",
        email="nameless@example.com",
        email_verified=False,
        name=None,
        picture=None,
    )

    await client.post("/auth/google", json=SIGN_IN_BODY)

    async with session_factory() as session:
        user = (await session.execute(select(User))).scalar_one()
    assert user.display_name == "nameless"
    assert user.email_verified is False


@pytest.mark.parametrize("change", ["disabled", "deleted"])
async def test_unavailable_account_is_forbidden_and_gets_no_tokens(
    change: str, client: AsyncClient, session_factory: async_sessionmaker
) -> None:
    await client.post("/auth/google", json=SIGN_IN_BODY)
    async with session_factory() as session:
        user = (await session.execute(select(User))).scalar_one()
        if change == "disabled":
            user.status = UserStatus.DISABLED
        else:
            user.deleted_at = datetime.now(UTC)
        await session.commit()

    response = await client.post("/auth/google", json=SIGN_IN_BODY)

    assert response.status_code == 403
    assert "refresh_token" not in response.json()
    async with session_factory() as session:
        assert await session.scalar(select(func.count()).select_from(RefreshToken)) == 1


async def test_invalid_google_token_is_unauthorized_and_creates_nothing(
    client: AsyncClient, session_factory: async_sessionmaker, verifier: FakeVerifier
) -> None:
    verifier.error = InvalidGoogleTokenError("expired")

    response = await client.post("/auth/google", json=SIGN_IN_BODY)

    assert response.status_code == 401
    assert response.json() == {"detail": "Invalid Google ID token."}
    async with session_factory() as session:
        assert await session.scalar(select(func.count()).select_from(User)) == 0


async def test_unreachable_google_is_service_unavailable(
    client: AsyncClient, verifier: FakeVerifier
) -> None:
    verifier.error = GoogleCertsUnavailableError()

    response = await client.post("/auth/google", json=SIGN_IN_BODY)

    assert response.status_code == 503


@pytest.mark.parametrize(
    "body",
    [
        {"client": "android"},
        {"id_token": "", "client": "android"},
        {"id_token": "x", "client": "playstation"},
        {"id_token": "x"},
        {"id_token": "x", "client": "android", "device_name": "d" * 101},
    ],
)
async def test_invalid_request_body_is_rejected(
    client: AsyncClient, body: dict[str, str]
) -> None:
    response = await client.post("/auth/google", json=body)

    assert response.status_code == 422


async def test_refresh_token_ids_are_uuid7(
    client: AsyncClient, session_factory: async_sessionmaker
) -> None:
    await client.post("/auth/google", json=SIGN_IN_BODY)
    async with session_factory() as session:
        refresh_ids = [
            token.id
            for token in (await session.execute(select(RefreshToken))).scalars()
        ]

    assert all(
        isinstance(token_id, uuid.UUID) and token_id.version == 7
        for token_id in refresh_ids
    )
