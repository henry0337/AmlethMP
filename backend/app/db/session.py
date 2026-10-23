from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.core.config import get_settings

settings = get_settings()

engine = create_async_engine(
    settings.database_url,
    echo=settings.database_echo,
    pool_pre_ping=True,
    connect_args={"timeout": settings.database_connect_timeout},
    pool_size=settings.database_pool_size,
    max_overflow=settings.database_max_overflow,
)

async_session_factory = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autoflush=False,
)


async def get_db_session() -> AsyncGenerator[AsyncSession, None]:
    """Provide one session per request; transaction commits stay explicit."""
    async with async_session_factory() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise


async def shutdown_database() -> None:
    """Close pooled database connections during application shutdown."""
    await engine.dispose()
