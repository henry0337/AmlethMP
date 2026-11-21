import logging

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import joinedload

from app.modules.auth.exceptions import OAuthError
from app.modules.auth.models import OAuthAccount, OAuthProvider
from app.modules.auth.schemas import DiscordIdentity, GoogleIdentity
from app.modules.users.models import User

logger = logging.getLogger(__name__)


async def find_or_create_google_user(session: AsyncSession, identity: GoogleIdentity) -> User:
    """Find the Google account owner or create a new user with that identity."""
    return await find_or_create_oauth_user(
        session,
        provider=OAuthProvider.GOOGLE,
        provider_account_id=identity.sub,
        email=identity.email,
        display_name=identity.name,
        avatar_url=identity.picture
    )


async def find_or_create_discord_user(session: AsyncSession, identity: DiscordIdentity) -> User:
    """Find the Discord account owner or create a new user with that identity."""
    return await find_or_create_oauth_user(
        session,
        provider=OAuthProvider.DISCORD,
        provider_account_id=identity.id,
        email=identity.verified_email,
        display_name=identity.display_name,
        avatar_url=identity.avatar_url
    )


async def find_or_create_oauth_user(
    session: AsyncSession,
    *,
    provider: OAuthProvider,
    provider_account_id: str,
    email: str | None,
    display_name: str | None,
    avatar_url: str | None
) -> User:
    """Find the owner of an external account or create a user for it.

    Accounts are never merged by email: a verified email that already belongs to another user
    is a conflict. `email` must be None when the provider has not verified it.
    """
    account = await session.scalar(
        select(OAuthAccount)
        .options(joinedload(OAuthAccount.user))
        .where(
            OAuthAccount.provider == provider,
            OAuthAccount.provider_account_id == provider_account_id
        )
    )
    if account is not None:
        if not account.user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN, detail="This account is disabled."
            )
        return account.user

    normalized_email = email.lower() if email else None
    if normalized_email is not None:
        existing_user = await session.scalar(select(User).where(User.email == normalized_email))
        if existing_user is not None:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="An account with this email already exists."
            )

    user = User(email=normalized_email, display_name=display_name, avatar_url=avatar_url)
    session.add(user)
    await session.flush()
    session.add(
        OAuthAccount(user_id=user.id, provider=provider, provider_account_id=provider_account_id)
    )

    try:
        await session.commit()
    except IntegrityError as error:
        await session.rollback()
        logger.warning("Could not create an OAuth account because of a concurrent update.")
        raise OAuthError from error

    return user
