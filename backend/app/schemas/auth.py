from typing import Literal

from pydantic import BaseModel, Field, field_validator

from app.models import ClientPlatform


class GoogleSignInRequest(BaseModel):
    id_token: str = Field(min_length=1, max_length=4096)
    client: ClientPlatform
    device_name: str | None = Field(default=None, max_length=100)

    @field_validator("device_name")
    @classmethod
    def blank_device_name_is_none(cls, value: str | None) -> str | None:
        if value is None or not value.strip():
            return None
        return value.strip()


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: Literal["Bearer"] = "Bearer"
    # Seconds until the access token expires. A duration rather than a timestamp, so the client's
    # clock does not matter.
    expires_in: int
