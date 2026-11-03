import enum
import uuid
from datetime import datetime

from sqlalchemy import Boolean, DateTime, String, func, text
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.types import string_enum


class UserStatus(enum.StrEnum):
    ACTIVE = "active"
    DISABLED = "disabled"


class UserRole(enum.StrEnum):
    MEMBER = "member"
    ADMIN = "admin"


# Guests never get a row here: their session is a short-lived JWT with the guest role.
class User(Base):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid7,
    )
    # Copied from the provider on the first sign-in, then editable by the user.
    display_name: Mapped[str] = mapped_column(String(100))
    # Refreshed from the provider on every sign-in.
    avatar_url: Mapped[str | None] = mapped_column(String(2048))
    # For display and contact only. It is not unique and never identifies a user: that is
    # the job of (provider, provider_user_id) in oauth_accounts.
    email: Mapped[str | None] = mapped_column(String(320))
    email_verified: Mapped[bool] = mapped_column(
        Boolean, default=False, server_default=text("false")
    )
    status: Mapped[UserStatus] = mapped_column(
        string_enum(UserStatus, name="status"),
        default=UserStatus.ACTIVE,
        server_default=UserStatus.ACTIVE.value,
    )
    role: Mapped[UserRole] = mapped_column(
        string_enum(UserRole, name="role"),
        default=UserRole.MEMBER,
        server_default=UserRole.MEMBER.value,
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
    )
    last_login_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    # Set when the user deletes the account; kept so the data can be purged after a grace period.
    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
