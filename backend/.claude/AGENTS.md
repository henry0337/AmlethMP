# Instructions for AI Coding Agents

## Project rules
- Read `CODING_STANDARDS.md` and follow it for Python/FastAPI changes.
- Inspect the existing project structure, `pyproject.toml`, lock files, Python version, test configuration and current patterns before editing.
- Preserve the project's existing package manager, framework versions, architecture and naming unless the task explicitly requires a change.
- Prefer small, focused changes; do not refactor unrelated code or introduce abstractions/dependencies without a clear reason.
- Keep API request/response contracts backward-compatible unless the task explicitly changes them.
- Validate untrusted input, preserve authorization checks, and never expose secrets, credentials or sensitive internal details.
- Use the project's existing patterns for dependency injection, database sessions, transactions, settings and error handling.
- For async code, verify that called I/O is async-compatible; do not block the event loop.
- Add or update tests for behavior changes, validation, error cases and permissions as appropriate.
- Do not claim a command or test passed unless it was actually run and passed.

## Before running commands
- Determine the package manager from lock files/configuration (for example `uv.lock`, `poetry.lock` or project-specific setup).
- Inspect existing scripts and configured tools before inventing commands.
- Do not run destructive database, migration, deployment or data-cleanup commands without explicit authorization.

## Verification
- Run the repository's configured formatter/linter, type checker and tests when available and relevant.
- If a check cannot be run, state which check was skipped and why.
- Review the final diff for unrelated edits, accidental secrets, debug prints and unsafe error details.

## Final response
- Summarize the key changes.
- List the checks actually run and their results.
- Mention relevant limitations, follow-up work or compatibility considerations.
