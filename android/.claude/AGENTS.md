# Instructions for AI Coding Agents

## Before changing code

- Read `CODING_STANDARDS.md` before making Kotlin, Android, or Jetpack Compose changes.
- Inspect the repository structure, nearby files, Gradle modules, version catalog, and existing patterns first; do not assume a specific package, DI framework, navigation library, or build variant.
- Treat existing project configuration as authoritative unless the task explicitly asks to change it. Do not migrate architecture or upgrade dependencies as an unrelated cleanup.

## Implementation rules

- Follow `CODING_STANDARDS.md`; follow the official Kotlin style and Compose API conventions.
- Prefer unidirectional data flow: immutable UI state flows down, user actions flow up through callbacks/state holders.
- Keep Composables focused on rendering; put screen/business logic in the appropriate ViewModel/state holder and access data through the repository layer used by this project.
- Use lifecycle-aware state collection for ViewModel flows where appropriate; do not introduce `GlobalScope`, UI-thread blocking, or unnecessary side effects.
- For Composables that emit UI, use a single `modifier: Modifier = Modifier` as the first optional parameter and apply it to the root layout.
- Preserve accessibility, localization, error/loading/empty states, and security/privacy requirements.
- Make the smallest complete change that solves the task. Avoid unrelated refactors, new abstractions, or dependencies without a concrete reason.
- Never hard-code secrets or log credentials/personal data. Validate untrusted input and check authorization at the appropriate boundary.

## Verification

- Determine available Gradle tasks and relevant module/variant before running checks; do not assume task names or package layout.
- Run the most relevant unit tests, Compose/UI tests, lint, and/or assemble task that the environment supports.
- Do not claim a command or test passed unless it was actually run and passed. Clearly report failures and checks that could not be run.
- Review the final diff for accidental changes, generated files, secrets, dependency churn, and formatting issues.

## Response

- Summarize the files/behavior changed and why.
- List the checks actually run and their results; state any known limitations concisely.
- If a convention conflicts with an established repository pattern or the requested behavior, prioritize correctness and the task, then explain the deviation.
