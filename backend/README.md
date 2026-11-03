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

## Google sign-in

`POST /auth/google` exchanges a Google ID token for this API's own tokens. Set
`GOOGLE_WEB_CLIENT_ID` (the OAuth client of type **Web application**, the same
value the Android app uses) and `JWT_SECRET` in `.env`, then:

```json
{ "id_token": "<from Google>", "client": "android", "device_name": "Pixel 9" }
```

It answers `{ "access_token", "refresh_token", "token_type": "Bearer", "expires_in" }`
(`expires_in` is in seconds). A bad token gives `401`, a disabled or deleted account
`403`, and `503` means Google's signing keys could not be fetched.

To reach the API from an Android emulator or a phone, listen on all interfaces:
`uv run fastapi dev --host 0.0.0.0`. The emulator sees the host as `10.0.2.2`; for a
USB-connected phone run `adb reverse tcp:8000 tcp:8000`.

## Tests

```bash
uv run pytest
```

The endpoint tests in `tests/test_auth_google.py` need a real PostgreSQL database and are
skipped unless `TEST_DATABASE_URL` is set (for example
`postgresql+asyncpg://user:password@localhost:5432/amlethmp_test`). The database name must
end in `_test`: its tables are dropped and recreated around every test.
