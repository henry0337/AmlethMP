import asyncio
from typing import Any, cast
from uuid import uuid4

import pytest
from fastapi import HTTPException
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.auth.exceptions import OAuthError
from app.modules.auth.models import OAuthAccount, OAuthProvider
from app.modules.auth.schemas import DiscordIdentity
from app.modules.auth.service import find_or_create_discord_user
from app.modules.users.models import User


class FakeSession:
    """Just enough of `AsyncSession` for `find_or_create_oauth_user`."""

    def __init__(self, *lookups: object, commit_error: Exception | None = None) -> None:
        self.lookups = list(lookups)
        self.commit_error = commit_error
        self.added: list[object] = []
        self.rolled_back = False

    async def scalar(self, _statement: object) -> object:
        return self.lookups.pop(0)

    def add(self, instance: object) -> None:
        self.added.append(instance)

    async def flush(self) -> None:
        for instance in self.added:
            if isinstance(instance, User):
                instance.id = uuid4()

    async def commit(self) -> None:
        if self.commit_error is not None:
            raise self.commit_error

    async def rollback(self) -> None:
        self.rolled_back = True


def sign_in(session: FakeSession, **overrides: object) -> User:
    payload: dict[str, Any] = {
        "id": "80351110224678912",
        "username": "nelly",
        "global_name": "Nelly",
        "avatar": "abc123",
        "email": "Nelly@Example.com",
        "verified": True
    }
    identity = DiscordIdentity.model_validate({**payload, **overrides})
    return asyncio.run(find_or_create_discord_user(cast(AsyncSession, session), identity))


def test_a_new_discord_user_is_created_with_a_lowercased_verified_email() -> None:
    session = FakeSession(None, None)

    user = sign_in(session)

    assert user.email == "nelly@example.com"
    assert user.display_name == "Nelly"
    assert user.avatar_url == "https://cdn.discordapp.com/avatars/80351110224678912/abc123.png"
    account = next(item for item in session.added if isinstance(item, OAuthAccount))
    assert account.provider == OAuthProvider.DISCORD
    assert account.provider_account_id == "80351110224678912"
    assert account.user_id == user.id


@pytest.mark.parametrize(
    "overrides",
    [
        pytest.param({"email": None}, id="no-email"),
        pytest.param({"verified": False}, id="unverified-email")
    ]
)
def test_a_user_without_a_verified_email_is_created_without_one(
    overrides: dict[str, object]
) -> None:
    session = FakeSession(None)

    user = sign_in(session, **overrides)

    assert user.email is None
    assert session.lookups == []


def test_an_existing_discord_account_returns_its_user_without_creating_one() -> None:
    owner = User(email="owner@example.com", is_active=True)
    account = OAuthAccount(provider=OAuthProvider.DISCORD, provider_account_id="1")
    account.user = owner
    session = FakeSession(account)

    assert sign_in(session) is owner
    assert session.added == []


def test_a_disabled_account_cannot_sign_in() -> None:
    account = OAuthAccount(provider=OAuthProvider.DISCORD, provider_account_id="1")
    account.user = User(email="owner@example.com", is_active=False)

    with pytest.raises(HTTPException) as error:
        sign_in(FakeSession(account))

    assert error.value.status_code == 403


def test_a_verified_email_owned_by_another_user_is_a_conflict() -> None:
    session = FakeSession(None, User(email="nelly@example.com"))

    with pytest.raises(HTTPException) as error:
        sign_in(session)

    assert error.value.status_code == 409
    assert session.added == []


def test_a_concurrent_insert_rolls_back_and_raises_an_oauth_error() -> None:
    session = FakeSession(None, None, commit_error=IntegrityError("insert", {}, Exception()))

    with pytest.raises(OAuthError):
        sign_in(session)

    assert session.rolled_back
