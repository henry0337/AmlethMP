import os

# Settings are read when `app` is first imported, so the variables must exist before that.
os.environ.setdefault("APP_ENV", "test")
os.environ.setdefault(
    "DATABASE_URL", "postgresql+asyncpg://unused:unused@localhost:5432/unused"
)
os.environ.setdefault(
    "GOOGLE_WEB_CLIENT_ID", "test-web-client.apps.googleusercontent.com"
)
os.environ.setdefault("JWT_SECRET", "test-secret-test-secret-test-secret-0123")
