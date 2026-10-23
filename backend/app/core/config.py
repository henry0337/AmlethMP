from functools import lru_cache
from pathlib import Path
from typing import Literal

from pydantic import Field, field_validator
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
