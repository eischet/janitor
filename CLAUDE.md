# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

Janitor is an embedded scripting and expression language for JVM applications (Java 21). It is designed to be sandboxed by default, easy to embed, and accessible to non-expert users. See `README.md` and `overview.md` for language and embedding examples.

## Build & Test Commands

```bash
# Run all tests
mvn -q test

# Run tests for a single module
mvn -pl janitor-tests test
mvn -pl janitor-lang test

# Run a specific test class
mvn -pl janitor-tests test -Dtest=JStringTestCase

# Run a specific test method
mvn -pl janitor-tests test -Dtest=JStringTestCase#basics

# Build without running tests
mvn -DskipTests package
```

No linter or auto-formatter is enforced. Follow 4-space indentation, braces on the same line, packages under `com.eischet.janitor.*`.

## Module Structure

| Module | Purpose |
|---|---|
| `janitor-api` | Public API interfaces and type system (`JanitorObject`, dispatch) |
| `janitor-lang` | Core compiler, ANTLR4 grammar, and runtime (`BaseRuntime`, `JanitorScript`) |
| `janitor-toolbox` | Shared utilities |
| `janitor-tests` | Main integration test suite |
| `janitor-demo` | Embedding example — start here to understand host integration |
| `janitor-jsr223` | JSR 223 `ScriptEngine` adapter |
| `janitor-repl` | REPL tooling |
| `janitor-logging` | Logging integration |
| `janitor-dbxs` / `janitor-orm` | Database and ORM support |
| `janitor-modules` | Optional plugin modules (`httpclient`, `files`, `os`, `brrr`, `mustang`) |
| `janitor-maven-plugin` | Maven plugin (uses JUnit 4, unlike other modules) |

## Architecture: Compilation & Execution Pipeline

```
Source text
  → ANTLR4 (Janitor.g4)  →  JanitorParser / JanitorLexer
  → JanitorAntlrCompiler  →  AST / RunnableScript
  → BaseRuntime subclass  →  JanitorScriptProcess (execution)
```

Key classes:
- `JanitorRuntime` (interface) / `BaseRuntime` (abstract) — host provides a concrete subclass
- `JanitorEnvironment` — sandbox, module registry, host-exposed globals
- `RunnableScript` — compiled artifact; call `script.run(g -> g.bind("name", value))` to execute
- `JanitorObject` — root of the type system; all Janitor values implement this
- `Scope` / `ScriptModule` — variable scoping and module isolation

The grammar lives at `janitor-lang/src/main/antlr4/com/eischet/janitor/lang/Janitor.g4`. Modifying the grammar requires re-running ANTLR (Maven does this automatically on build).

## Testing Infrastructure

Tests extend `JanitorTest` (abstract base in `janitor-tests`) which wires up `OutputCatchingTestRuntime` and `TestEnv`. Test classes use `*TestCase` or `*Test` suffixes. Use JUnit Jupiter for all new tests except in `janitor-maven-plugin` (JUnit 4).

`sample-scripts/` contains example Janitor scripts used by tests and documentation.

## Security & Dependencies

Changes that touch sandboxing or security behavior should be reviewed against `SECURITY.md`. Avoid new external dependencies without clear justification.
