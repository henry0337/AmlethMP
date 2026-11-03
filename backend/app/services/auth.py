import uuid
from datetime import UTC, datetime, timedelta

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import Settings
from app.core.security import (
    create_access_token,
    generate_refresh_token,
    hash_refresh_token,
)
from app.models import (
    ClientPlatform,
    OAuthAccount,
    OAuthProvider,
    RefreshToken,
    User,
    UserStatus,
)
from app.schemas.auth import TokenResponse
from app.services.google_id_token import GoogleIdentity

DISPLAY_NAME_MAX_LENGTH = 100
FALLBACK_DISPLAY_NAME = "User"


class AccountUnavailableError(Exception):
    """The account is disabled or has been deleted, so it may not sign in."""


async def sign_in_with_google(
    session: AsyncSession,
    identity: GoogleIdentity,
    *,
    client: ClientPlatform,
    device_name: str | None,
    settings: Settings,
) -> TokenResponse:
    """Find or create the user behind a verified Google identity and open a new session."""
    now = datetime.now(UTC)
    user, account = await _find_or_create(session, identity)

    if user.status is not UserStatus.ACTIVE or user.deleted_at is not None:
        raise AccountUnavailableError

    user.avatar_url = identity.picture
    user.email = identity.email
    user.email_verified = identity.email_verified
    user.last_login_at = now
    account.provider_email = identity.email
    account.email_verified = identity.email_verified
    account.last_login_at = now

    refresh_token = generate_refresh_token()
    session.add(
        RefreshToken(
            user_id=user.id,
            token_hash=hash_refresh_token(refresh_token),
            family_id=uuid.uuid7(),
            client=client,
            device_name=device_name,
            expires_at=now + timedelta(days=settings.refresh_token_ttl_days),
        )
    )
    await session.commit()

    access_token, expires_in = create_access_token(
        user_id=user.id, role=user.role, settings=settings, now=now
    )
    return TokenResponse(
        access_token=access_token, refresh_token=refresh_token, expires_in=expires_in
    )


async def _find_or_create(
    session: AsyncSession, identity: GoogleIdentity
) -> tuple[User, OAuthAccount]:
    found = await _find(session, identity.subject)
    if found is not None:
        return found

    user = User(
        display_name=_display_name(identity),
        avatar_url=identity.picture,
        email=identity.email,
        email_verified=identity.email_verified,
    )
    account = OAuthAccount(
        provider=OAuthProvider.GOOGLE,
        provider_user_id=identity.subject,
        provider_email=identity.email,
        email_verified=identity.email_verified,
    )
    try:
        # A savepoint, so losing a race with a concurrent first sign-in of the same account
        # does not poison the outer transaction.
        async with session.begin_nested():
            session.add(user)
            await session.flush()
            account.user_id = user.id
            session.add(account)
            await session.flush()
    except IntegrityError:
        found = await _find(session, identity.subject)
        if found is None:
            raise
        return found

    return user, account


async def _find(
    session: AsyncSession, subject: str
) -> tuple[User, OAuthAccount] | None:
    result = await session.execute(
        select(User, OAuthAccount)
        .join(OAuthAccount, OAuthAccount.user_id == User.id)
        .where(
            OAuthAccount.provider == OAuthProvider.GOOGLE,
            OAuthAccount.provider_user_id == subject,
        )
    )
    row = result.one_or_none()
    return None if row is None else (row[0], row[1])


def _display_name(identity: GoogleIdentity) -> str:
    candidate = (
        identity.name or (identity.email or "").split("@")[0] or FALLBACK_DISPLAY_NAME
    )
    return candidate[:DISPLAY_NAME_MAX_LENGTH]
