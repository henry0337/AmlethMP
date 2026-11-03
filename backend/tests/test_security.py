import uuid
from datetime import UTC, datetime, timedelta

import jwt
import pytest

from app.core.config import get_settings
from app.core.security import (
    create_access_token,
    generate_refresh_token,
    hash_refresh_token,
)
from app.models import UserRole


def test_access_token_carries_subject_role_and_expiry() -> None:
    settings = get_settings()
    user_id = uuid.uuid7()
    now = datetime.now(UTC).replace(microsecond=0)

    token, expires_in = create_access_token(
        user_id=user_id, role=UserRole.MEMBER, settings=settings, now=now
    )

    claims = jwt.decode(
        token,
        settings.jwt_secret.get_secret_value(),
        algorithms=["HS256"],
        options={"verify_exp": False},
    )
    assert claims["sub"] == str(user_id)
    assert claims["role"] == "member"
    assert claims["exp"] - claims["iat"] == expires_in
    assert expires_in == int(
        timedelta(minutes=settings.access_token_ttl_minutes).total_seconds()
    )


def test_access_token_signed_with_another_secret_is_rejected() -> None:
    token, _ = create_access_token(
        user_id=uuid.uuid7(), role=UserRole.MEMBER, settings=get_settings()
    )

    with pytest.raises(jwt.InvalidSignatureError):
        jwt.decode(token, "another-secret-another-secret-0123456", algorithms=["HS256"])


def test_refresh_tokens_are_unique_and_only_their_hash_is_comparable() -> None:
    first = generate_refresh_token()
    second = generate_refresh_token()

    assert first != second
    assert len(hash_refresh_token(first)) == 64
    assert hash_refresh_token(first) == hash_refresh_token(first)
    assert hash_refresh_token(first) != hash_refresh_token(second)
    assert first not in hash_refresh_token(first)
