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


class CurrentUserResponse(BaseModel):
    id: UUID
    email: str | None
    display_name: str | None
    avatar_url: str | None
