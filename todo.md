# Janitor – Open Bugs

Findings from an ongoing code review across `janitor-lang` (compiler/runtime),
`janitor-api` (type system), and `janitor-modules`/`janitor-toolbox` (stdlib,
modules). Resolved items have been removed from this file; their rationale
now lives as comments in the relevant source files (and in git history).

Legend: 🔴 high / 🟡 medium / ⚪ low · 🔒 security-relevant

---

## Modules & stdlib (`janitor-modules`)

### 🟡 `ZipFile.addFile` leaks a `FileInputStream` on exceptions
[ZipFile.java:98-106](janitor-modules/files/src/main/java/com/eischet/janitor/modules/files/ZipFile.java)

No try-with-resources/finally around `fis`. If `zos.putNextEntry(...)`
(e.g. duplicate entry name) or `fis.transferTo(zos)` throws, `fis.close()`
is never reached. Since scripts can call `addFile()` in loops, this is a
realistic path to file-handle exhaustion.
