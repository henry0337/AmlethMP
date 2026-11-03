import enum
import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, func
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.types import string_enum


class ClientPlatform(enum.StrEnum):
    ANDROID = "android"
    WEB = "web"
    WINDOWS = "windows"
    LINUX = "linux"


# Only members have refresh tokens; guests get a short-lived access token and nothing else.
class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid7,
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    # SHA-256 hex digest of the token. The token itself is never stored.
    token_hash: Mapped[str] = mapped_column(String(64), unique=True)
    # Every rotation keeps the family. Presenting an already replaced token revokes the whole family.
    family_id: Mapped[uuid.UUID] = mapped_column(index=True)
    replaced_by_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("refresh_tokens.id", ondelete="SET NULL")
    )
    client: Mapped[ClientPlatform] = mapped_column(
        string_enum(ClientPlatform, name="client")
    )
    # Label for a future "signed-in devices" screen, for example the phone model.
    device_name: Mapped[str | None] = mapped_column(String(100))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
