from functools import lru_cache
from pathlib import Path
from typing import Literal

from pydantic import Field, SecretStr, field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

PROJECT_ROOT = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    """Runtime configuration loaded from environment variables and `.env`."""

    model_config = SettingsConfigDict(
        env_file=PROJECT_ROOT / ".env", env_file_encoding="utf-8", extra="ignore"
    )

    app_name: str = "AmlethMP API"
    app_env: Literal["development", "test", "production"] = "development"
    database_url: str = Field(repr=False)
    database_echo: bool = False
    database_connect_timeout: int = Field(default=5, ge=1, le=30)
    database_pool_size: int = Field(default=5, ge=1)
    database_max_overflow: int = Field(default=10, ge=0)
    web_origin: str = "http://localhost:3000"
    google_client_id: str | None = None
    google_client_secret: SecretStr | None = Field(default=None, repr=False)
    google_redirect_uri: str = "http://localhost:8000/auth/google/callback"
    discord_client_id: str | None = None
    discord_client_secret: SecretStr | None = Field(default=None, repr=False)
    discord_redirect_uri: str = "http://localhost:8000/auth/discord/callback"
    auth_session_secret: SecretStr | None = Field(default=None, repr=False)
    auth_session_max_age_seconds: int = Field(default=60 * 60 * 24 * 7, ge=60)
    auth_session_cookie_secure: bool = False

    @field_validator("database_url")
    @classmethod
    def database_url_uses_asyncpg(cls, value: str) -> str:
        if not value.startswith("postgresql+asyncpg://"):
            raise ValueError("DATABASE_URL must use the postgresql+asyncpg:// scheme.")
        return value

    @model_validator(mode="after")
    def production_sessions_require_secure_cookies(self) -> "Settings":
        if self.app_env == "production" and not self.auth_session_cookie_secure:
            raise ValueError("AUTH_SESSION_COOKIE_SECURE must be true in production.")
        return self

    @property
    def google_login_is_configured(self) -> bool:
        return all((self.google_client_id, self.google_client_secret, self.auth_session_secret))

    @property
    def discord_login_is_configured(self) -> bool:
        return all((self.discord_client_id, self.discord_client_secret, self.auth_session_secret))


@lru_cache
def get_settings() -> Settings:
    # Required fields are loaded from the environment, not passed as arguments.
    return Settings()  # pyright: ignore[reportCallIssue]
