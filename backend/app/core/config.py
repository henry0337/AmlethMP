from functools import lru_cache
from pathlib import Path
from typing import Literal

from pydantic import Field, SecretStr, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

PROJECT_ROOT = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    """Runtime configuration loaded from environment variables and `.env`."""

    model_config = SettingsConfigDict(
        env_file=PROJECT_ROOT / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    app_name: str = "AmlethMP API"
    app_env: Literal["development", "test", "production"] = "development"
    database_url: str = Field(repr=False)
    database_echo: bool = False
    database_connect_timeout: int = Field(default=5, ge=1, le=30)
    database_pool_size: int = Field(default=5, ge=1)
    database_max_overflow: int = Field(default=10, ge=0)

    # Web-type OAuth client ID: the "aud" claim of every Google ID token the Android app obtains.
    google_web_client_id: str = Field(min_length=1)
    # HS256 key for our own access tokens. Generate one with `openssl rand -hex 32`.
    jwt_secret: SecretStr = Field(min_length=32, repr=False)
    access_token_ttl_minutes: int = Field(default=15, ge=1, le=1440)
    refresh_token_ttl_days: int = Field(default=30, ge=1, le=365)

    @field_validator("database_url")
    @classmethod
    def database_url_uses_asyncpg(cls, value: str) -> str:
        if not value.startswith("postgresql+asyncpg://"):
            raise ValueError(
                "DATABASE_URL must use the postgresql+asyncpg:// scheme."
            )
        return value


@lru_cache
def get_settings() -> Settings:
    return Settings()
