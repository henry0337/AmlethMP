import hashlib
import secrets
import uuid
from datetime import UTC, datetime, timedelta

import jwt

from app.core.config import Settings
from app.models import UserRole


def create_access_token(
    *,
    user_id: uuid.UUID,
    role: UserRole,
    settings: Settings,
    now: datetime | None = None,
) -> tuple[str, int]:
    """Sign a short-lived access JWT and return it with its lifetime in seconds."""
    issued_at = now or datetime.now(UTC)
    lifetime = timedelta(minutes=settings.access_token_ttl_minutes)
    claims = {
        "sub": str(user_id),
        "role": role.value,
        "iat": issued_at,
        "exp": issued_at + lifetime,
    }
    token = jwt.encode(
        claims, settings.jwt_secret.get_secret_value(), algorithm="HS256"
    )
    return token, int(lifetime.total_seconds())


def generate_refresh_token() -> str:
    return secrets.token_urlsafe(32)


def hash_refresh_token(token: str) -> str:
    """SHA-256 is enough here: the token is 256 random bits, so there is nothing to brute-force."""
    return hashlib.sha256(token.encode()).hexdigest()
