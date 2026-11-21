from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from starlette.middleware.sessions import SessionMiddleware

from app.core.config import get_settings
from app.db.session import engine, shutdown_database
from app.modules.auth.router import router as auth_router

settings = get_settings()


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    await shutdown_database()


app = FastAPI(title=settings.app_name, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.web_origin],
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type"]
)

if settings.auth_session_secret is not None:
    app.add_middleware(
        SessionMiddleware,
        secret_key=settings.auth_session_secret.get_secret_value(),
        session_cookie="amlethmp_session",
        max_age=settings.auth_session_max_age_seconds,
        same_site="lax",
        https_only=settings.auth_session_cookie_secure
    )

app.include_router(auth_router)


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
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database is unavailable."
        ) from error

    return {"status": "ok", "database": "ok"}
