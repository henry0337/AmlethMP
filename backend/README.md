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

## Project structure

Code is grouped by feature under `app/modules/<module>/` (`models.py`, `schemas.py`,
`service.py`, `router.py`, ...). Shared infrastructure lives in `app/core/` (settings) and
`app/db/` (base class, session, and `models.py`, which imports every module's models for
Alembic). Tests mirror this layout under `tests/`. See `CODING_STANDARDS.md` section 11 for
how to add a module.

## Code quality and dependencies

Run these commands from `backend/`. `uv sync` installs the dev tools (Ruff,
Pyright and pytest) together with the runtime dependencies.

| Command                                         | Purpose                                         |
| ----------------------------------------------- | ----------------------------------------------- |
| `uv run ruff check`                             | Lint; the rule set is in `pyproject.toml`.      |
| `uv run python scripts/format.py`               | Sort imports, format, remove trailing commas.   |
| `uv run python scripts/format.py --check`       | Check formatting without changing files.        |
| `uv run pyright`                                | Strict type check (not `scripts`, migrations).  |
| `uv run pytest`                                 | Run the tests.                                  |
| `uv audit`                                      | Check locked dependencies for known advisories. |

Do not use trailing commas. Ruff (like Black) always adds one when it puts each element on its own
line and cannot be configured to stop, so `scripts/format.py` runs Ruff and then removes the
trailing commas that do not change the program (the one-element tuple `(value,)` keeps its comma).
Use it instead of `ruff format` and `ruff check --fix`: `ruff format --check` always fails on this
code, and import sorting (`I`) is done by the script because `ruff check` would require trailing
commas in wrapped imports. Editors that format on save with Ruff will add the commas back, so run
the script before committing.

Before opening a pull request, run the lint, format check, type check and test
commands. In CI, use `uv sync --locked` so a stale `uv.lock` fails the build.

Dependency pinning follows these rules:

- Pin exact versions for FastAPI, Ruff and Pyright, because their minor or
  patch releases can change behavior or diagnostics.
- Pin the minor version of `0.x` packages (`httpx`, `asyncpg`) and of
  SQLAlchemy; keep a major-version range for the other stable libraries.
- Add new dependencies with `uv add`. `uv.lock` is the source of the exact
  installed versions; commit it with `pyproject.toml`.
- `requires-python` allows any Python 3.12 release; `.python-version` selects
  the interpreter used for local development.

## Google sign-in (local development)

Create a Google OAuth client of type **Web application**, then add
`http://localhost:8000/auth/google/callback` as its authorized redirect URI.
Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and a random
`AUTH_SESSION_SECRET` in `.env`. The API stores only an opaque, signed,
HttpOnly browser session; the Google client secret never belongs in the web app.

For production, use HTTPS for the API callback and web origin, set
`AUTH_SESSION_COOKIE_SECURE=true`, and register the exact production callback
URL with Google.
