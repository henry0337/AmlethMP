import logging

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import joinedload

from app.modules.auth.exceptions import GoogleOAuthError
from app.modules.auth.models import OAuthAccount, OAuthProvider
from app.modules.auth.schemas import GoogleIdentity
from app.modules.users.models import User

logger = logging.getLogger(__name__)


async def find_or_create_google_user(session: AsyncSession, identity: GoogleIdentity) -> User:
    """Find the Google account owner or create a new user with that identity."""
    account = await session.scalar(
        select(OAuthAccount)
        .options(joinedload(OAuthAccount.user))
        .where(
            OAuthAccount.provider == OAuthProvider.GOOGLE,
            OAuthAccount.provider_account_id == identity.sub
        )
    )
    if account is not None:
        if not account.user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN, detail="This account is disabled."
            )
        return account.user

    existing_user = await session.scalar(select(User).where(User.email == identity.email.lower()))
    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this email already exists."
        )

    user = User(
        email=identity.email.lower(), display_name=identity.name, avatar_url=identity.picture
    )
    session.add(user)
    await session.flush()
    session.add(
        OAuthAccount(
            user_id=user.id, provider=OAuthProvider.GOOGLE, provider_account_id=identity.sub
        )
    )

    try:
        await session.commit()
    except IntegrityError as error:
        await session.rollback()
        logger.warning("Could not create a Google OAuth account because of a concurrent update.")
        raise GoogleOAuthError from error

    return user
