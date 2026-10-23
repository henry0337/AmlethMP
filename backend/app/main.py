from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError

from app.core.config import get_settings
from app.db.session import engine, shutdown_database

settings = get_settings()


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    await shutdown_database()


app = FastAPI(title=settings.app_name, lifespan=lifespan)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/health/ready")
async def readiness() -> dict[str, str]:
    """Verify that the API can obtain a connection to PostgreSQL."""
    try:
        async with engine.connect() as connection:
            await connection.execute(text("SELECT 1"))
    except SQLAlchemyError as error:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database is unavailable.",
        ) from error

    return {"status": "ok", "database": "ok"}
