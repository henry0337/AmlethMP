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
