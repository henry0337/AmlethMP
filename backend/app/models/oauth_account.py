from enum import StrEnum
from uuid import UUID

from sqlalchemy import Enum, ForeignKey, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.models.user import User


class OAuthProvider(StrEnum):
    """Identity providers supported by the application."""

    GOOGLE = "google"
    DISCORD = "discord"


class OAuthAccount(Base):
    """An external OAuth identity linked to a user."""

    __tablename__ = "oauth_accounts"
    __table_args__ = (
        UniqueConstraint(
            "provider",
            "provider_account_id",
            name="uq_oauth_accounts_provider_account",
        ),
        UniqueConstraint(
            "user_id",
            "provider",
            name="uq_oauth_accounts_user_provider",
        ),
    )

    user_id: Mapped[UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
    )
    provider: Mapped[OAuthProvider] = mapped_column(
        Enum(
            OAuthProvider,
            name="oauth_provider",
            values_callable=lambda providers: [provider.value for provider in providers],
        )
    )
    provider_account_id: Mapped[str] = mapped_column(String(255))
    user: Mapped[User] = relationship(back_populates="oauth_accounts")
