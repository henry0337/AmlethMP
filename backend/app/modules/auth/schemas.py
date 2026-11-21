from uuid import UUID

from pydantic import BaseModel, Field


class GoogleIdentity(BaseModel):
    """The subset of OpenID Connect user info required by AmlethMP."""

    sub: str = Field(min_length=1, max_length=255)
    email: str = Field(min_length=1, max_length=320)
    email_verified: bool = False
    name: str | None = Field(default=None, max_length=100)
    picture: str | None = Field(default=None, max_length=2048)


class GoogleTokenResponse(BaseModel):
    """The part of Google's token endpoint response needed to call the user info endpoint."""

    access_token: str = Field(min_length=1)


class DiscordIdentity(BaseModel):
    """The subset of Discord's `/users/@me` response required by AmlethMP."""

    id: str = Field(pattern=r"^\d{1,32}$")
    username: str = Field(min_length=1, max_length=100)
    global_name: str | None = Field(default=None, max_length=100)
    avatar: str | None = Field(default=None, pattern=r"^[A-Za-z0-9_]{1,64}$")
    email: str | None = Field(default=None, max_length=320)
    verified: bool = False

    @property
    def verified_email(self) -> str | None:
        """Return the email only when Discord confirms the user owns it."""
        if self.email and self.verified:
            return self.email
        return None

    @property
    def display_name(self) -> str:
        return self.global_name or self.username

    @property
    def avatar_url(self) -> str | None:
        if self.avatar is None:
            return None
        return f"https://cdn.discordapp.com/avatars/{self.id}/{self.avatar}.png"


class DiscordTokenResponse(BaseModel):
    """The part of Discord's token endpoint response needed to call the user endpoint."""

    access_token: str = Field(min_length=1)


class CurrentUserResponse(BaseModel):
    id: UUID
    email: str | None
    display_name: str | None
    avatar_url: str | None
