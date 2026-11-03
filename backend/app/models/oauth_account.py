import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    Boolean,
    DateTime,
    ForeignKey,
    String,
    UniqueConstraint,
    func,
    text,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.types import string_enum


class OAuthProvider(enum.StrEnum):
    GOOGLE = "google"
    DISCORD = "discord"


# TODO(account-linking): for now every (provider, provider_user_id) is its own user, so signing in
# with Google and with Discord using the same email creates two users. Email is deliberately not
# used to merge them: a provider may report an unverified email, and trusting it would let someone
# take over another person's account. Merging comes later as an explicit "link account" feature,
# where the user proves ownership by signing in with both providers. That feature adds a row here
# for the existing user_id instead of creating a new user.
class OAuthAccount(Base):
    __tablename__ = "oauth_accounts"
    __table_args__ = (
        UniqueConstraint(
            "provider", "provider_user_id", name="uq_oauth_accounts_provider_user"
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid7,
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    provider: Mapped[OAuthProvider] = mapped_column(
        string_enum(OAuthProvider, name="provider")
    )
    # The provider's stable id: "sub" for Google, the user id for Discord. Not the email.
    provider_user_id: Mapped[str] = mapped_column(String(255))
    # Snapshot taken at the last sign-in. The provider's access and refresh tokens are not stored.
    provider_email: Mapped[str | None] = mapped_column(String(320))
    email_verified: Mapped[bool] = mapped_column(
        Boolean, default=False, server_default=text("false")
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    last_login_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
