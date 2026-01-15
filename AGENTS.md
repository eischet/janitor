# Repository Guidelines

## Project Structure & Module Organization
Janitor is a Maven multi-module project. Each top-level `janitor-*` directory is a module with standard Maven layout:
`src/main/java` for production code, `src/test/java` for tests, and module-specific resources under `src/main/resources`.
Key locations:
- `janitor-lang/`: core language, parser, and runtime; grammar in `janitor-lang/src/main/antlr4`.
- `janitor-api/`, `janitor-commons/`, `janitor-toolbox/`: public API and shared utilities.
- `janitor-demo/`: embedding examples.
- `janitor-repl/`: REPL tooling.
- `sample-scripts/`: example scripts used by tests and docs.
- `logos/`, `overview.md`, `scripting-intro.md`: assets and documentation.

## Build, Test, and Development Commands
Use Maven from the repo root:
- `mvn -q test`: run all module tests.
- `mvn -pl janitor-lang test`: run tests for a single module.
- `mvn -DskipTests package`: build all modules without tests.
- `mvn -pl janitor-demo package`: build the demo module.

## Coding Style & Naming Conventions
Java follows conventional formatting: 4-space indentation, braces on the same line, and package names under
`com.eischet.janitor.*`. Test classes typically use `*TestCase` or `*Test` suffixes (see `janitor-lang/src/test/java`).
Keep changes consistent with existing style; no auto-formatter is enforced in this repo.

## Testing Guidelines
Tests use JUnit Jupiter (JUnit 5) in most modules, with a small amount of JUnit 4 in `janitor-maven-plugin`.
Prefer JUnit Jupiter for new tests unless the module already uses JUnit 4. Test suites live alongside code in
`src/test/java`. There is no explicit coverage gate, but new features should include focused unit tests.

## Commit & Pull Request Guidelines
Recent commits are short, imperative, and lowercase (e.g., “sync versions”, “orm tweaks”), with release tags like
`v0.9.41`. Keep commit subjects concise and specific. PRs should describe the change, mention impacted modules, and
include test evidence (command output or notes). If behavior changes, link to an issue or provide a short rationale.

## Security & Configuration Tips
If your change touches security or sandboxing behavior, review `SECURITY.md` and highlight any risk implications in
the PR. Avoid introducing new external dependencies without a clear justification.
