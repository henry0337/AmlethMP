# Getting Started

## Local PostgreSQL

1. Copy `.env.example` to `.env` and replace `change-me` with the password of
   the local `amlethmp_app` role. The `.env` file is ignored by Git.
2. Install the locked dependencies with `uv sync`.
3. Start the API with `uv run fastapi dev`. The FastAPI CLI entrypoint is
   configured in `pyproject.toml`.

`GET /health` is a liveness check and does not access PostgreSQL. Use
`GET /health/ready` to verify database connectivity.

Schema changes are managed by Alembic. After adding an ORM model, create and
apply a migration from this directory:

```bash
uv run alembic revision --autogenerate -m "describe the change"
uv run alembic upgrade head
```

Do not call `Base.metadata.create_all()` in the application; keeping schema
changes in migrations makes local, staging, and production environments
reproducible.

## Google sign-in (local development)

Create a Google OAuth client of type **Web application**, then add
`http://localhost:8000/auth/google/callback` as its authorized redirect URI.
Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and a random
`AUTH_SESSION_SECRET` in `.env`. The API stores only an opaque, signed,
HttpOnly browser session; the Google client secret never belongs in the web app.

For production, use HTTPS for the API callback and web origin, set
`AUTH_SESSION_COOKIE_SECURE=true`, and register the exact production callback
URL with Google.
