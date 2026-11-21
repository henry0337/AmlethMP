# Instructions for Coding Agents

<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->

## Working rules

- Inspect the relevant source files and nearby patterns before editing; keep changes focused and avoid unrelated refactors.
- For React, Next.js, or TypeScript changes, follow the applicable rules in [`CODING_STANDARDS.md`](./CODING_STANDARDS.md). Read the relevant sections rather than copying the whole standard into this file.
- Treat `CODING_STANDARDS.md` as the detailed coding-standard reference. Do not duplicate its rules here; update the source document when an agreed convention changes.
- Prefer existing project patterns and installed dependencies over introducing new abstractions or packages. If existing code conflicts with the standard, assess whether it is an intentional legacy decision; flag meaningful conflicts instead of rewriting broadly.
- Inspect `package.json` and the repository configuration to identify the actual package manager and available scripts. Do not assume commands that are not defined by the project.
- After making changes, run the relevant available checks (for example lint, typecheck, tests, or build). Report which checks were run and their results; never claim a check passed if it was not run.
- Do not hard-code secrets or expose server-only data to client code. Validate untrusted input and verify authorization on the server for protected operations.
- Update project documentation or coding standards when a change intentionally alters an established convention or workflow.

## Change priorities

1. Correctness and security.
2. Consistency with the existing architecture and coding standards.
3. Small, readable, testable changes.
4. Performance improvements supported by a clear need or measurement.
