# Janitor – List of Potential/Likely Bugs

Result of a code review across `janitor-lang` (compiler/runtime), `janitor-api`
(type system), and `janitor-modules`/`janitor-toolbox` (stdlib, modules).
Findings marked with ✅ were verified against the source code and confirmed;
the others come from the review and were not separately re-verified
(confidence level as noted).

Legend: 🔴 high / 🟡 medium / ⚪ low · 🔒 security-relevant

---

## 1. Language core (compiler & runtime)

(No open items at the moment — the closure-scope bugs, `||` short-circuit,
`try`/`finally` control-flow skip, and list/string slice-indexing gaps that
used to live here have all been fixed; see git history and the test suites
under `janitor-tests/src/test/java/com/eischet/janitor/internals/` and
`janitor-tests/src/test/java/com/eischet/janitor/types/` for details.)

---

## 2. Type system (`janitor-api`)

### 🟡 `JCallArgs.getRequiredIntValue` only checks the upper overflow bound
[JCallArgs.java:171-180](janitor-api/src/main/java/com/eischet/janitor/api/types/functions/JCallArgs.java)

Only `num.toLong() > Integer.MAX_VALUE` is checked, not `Integer.MIN_VALUE`.
A strongly negative `JInt` (e.g. `-5_000_000_000`) is silently truncated by
the cast to `int` instead of throwing a `JanitorArgumentException`.

### 🟡 `JList`: inconsistent handling of negative indices between read/single-index-write and `add`/`put`
[JList.java](janitor-api/src/main/java/com/eischet/janitor/api/types/builtin/JList.java) – `get`/`getIndexed` vs. `add`/`put`

`get`/`getIndexed` route through `toIndex()` and support negative indices
(`list[-1]`). `add(JInt, ...)` and `put(JInt, ...)`, on the other hand, use
`index.janitorGetHostValue().intValue()` directly without `toIndex()` —
these are only reachable via explicit method calls (`list.add(i, x)`,
`list.put(i, x)`), not via `list[i] = x` syntax (which now goes through
`getIndexed()`, already fixed), so this is lower-impact than it looked
originally, but still an inconsistency worth cleaning up.

### ⚪ `JNumber.compareTo` / `Semantics.areEquals` compare large numbers via `double`
`JNumber.java:37-39`

Comparison via `Double.compare(toDouble(), ...)`. For `long` values beyond
2^53 this can lose precision, so two distinct large integers can compare as
equal. Possibly an accepted tradeoff of the double-based numeric model, but
affects both `compareTo` and equality.

---

## 3. Modules & stdlib (`janitor-modules`, `janitor-toolbox`)

### 🔴 🔒 `ignoreSecurityIssues(true)` disables TLS hostname verification **JVM-wide and permanently**
[JanitorHttpClient.java:235](janitor-modules/httpclient/src/main/java/com/eischet/janitor/modules/httpclient/JanitorHttpClient.java) (setter exposed via `DISPATCH.addBuilderMethod("ignoreSecurityIssues", ...)`, line 62)

```java
System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
```
This system property applies to **every** `java.net.http.HttpClient` in the
whole process, not just the instance the script is currently building, and
is never reset. A single (possibly less trusted) script can therefore
permanently disable hostname verification for all HTTPS connections in the
process — including the host application itself and other, concurrently
running scripts. In a multi-tenant embedding this is a serious,
cross-tenant MITM risk and a clear contradiction of the "sandboxed by
default" goal.

### ✅ RESOLVED — `janitorCleanup()` reintroduced exactly the hang `close()` was meant to avoid
[JanitorHttpClient.java](janitor-modules/httpclient/src/main/java/com/eischet/janitor/modules/httpclient/JanitorHttpClient.java)

`close()` carries a comment saying "`client.close` can hang for up to a full
day, hence the workaround via a shutdown thread using `shutdownNow()`".
`janitorCleanup()` — invoked synchronously during script-process teardown
via `AbstractScriptProcess.processCleanups()` — used to call
`builtClient.close()` directly, the exact blocking call `close()`
deliberately avoided.

While investigating, we also found and fixed a closely related, independent
bug: `JanitorScript.run(Consumer<Scope>)` used to call
`globalScope.janitorLeaveScope()` a second, redundant time after
`RunningScriptProcess.run()` had already done so on the very same scope
instance, so every ref-counted object (like `JanitorHttpClient`, via
`com.eischet.janitor.toolbox.memory.RefCounter`) got `janitorLeaveScope()`
called twice for one `janitorEnterScope()` — logging a spurious "RefCounter
was already zeroed" warning on essentially every script run that used an
HTTP client (see
[ScopeEnterLeaveBalanceTestCase.java](janitor-tests/src/test/java/com/eischet/janitor/internals/ScopeEnterLeaveBalanceTestCase.java)).
Fixed by removing the redundant call.

That investigation also surfaced a deeper design question: a `JanitorHttpClient`
bound at a *module* scope (via `runAndKeepGlobals`, cached and reused across
many, possibly concurrent, script runs) can have its resources torn down as
soon as the module is first loaded/compiled, regardless of the ref-counter
fix above, and can be shut down while other scripts are still using it.
Rather than trying to get scope-lifecycle timing exactly right for a
shared/cached module (which turned out to be architecturally ambiguous —
there's no single script whose "completion" a shared module's lifetime could
be tied to), we made `JanitorHttpClient` self-healing instead: `close()` now
resets `builtClient`/`mustRebuild` (and does so under a lock, since the
client can genuinely be shared across concurrently running scripts), so
`buildClient()`'s existing lazy-rebuild check transparently constructs a
fresh client on the next real use, no matter when or how many times `close()`
fires. `janitorCleanup()` now simply delegates to `close()` instead of
calling the blocking `builtClient.close()` directly, which resolves this
item. See
[JanitorHttpClientCloseTestCase.java](janitor-modules/httpclient/src/test/java/com/eischet/janitor/modules/httpclient/JanitorHttpClientCloseTestCase.java).
<br>
Note: `JanitorHttpClient` is (deliberately) the only resource-owning,
`janitorCleanup()`-using builtin type in the codebase; this kind of
self-healing design is the recommended pattern for any future type that owns
an external resource, rather than relying on precise scope-lifecycle timing.

### 🔴 🔒/DoS `os.exec` can deadlock on a full pipe buffer
[OperatingSystemModule.java:52-53, 65-66](janitor-modules/os/src/main/java/com/eischet/janitor/modules/os/OperatingSystemModule.java)

```java
Process osProc = Runtime.getRuntime().exec(...);
int result = osProc.waitFor();
```
Neither `getInputStream()` nor `getErrorStream()` is read. If the child
process writes more than the OS pipe buffer (~64 KB) to stdout/stderr, it
blocks on the write, and `waitFor()` blocks forever — a classic
`Runtime.exec` deadlock. Any script that runs a command with non-trivial
output can hang the calling thread permanently. (The `JList` branch, at
least, correctly builds an argv array instead of a shell string, so it is
not vulnerable to command injection.)

### 🟡 `ZipFile.addFile` leaks a `FileInputStream` on exceptions
[ZipFile.java:98-106](janitor-modules/files/src/main/java/com/eischet/janitor/modules/files/ZipFile.java)

No try-with-resources/finally around `fis`. If `zos.putNextEntry(...)`
(e.g. duplicate entry name) or `fis.transferTo(zos)` throws, `fis.close()`
is never reached. Since scripts can call `addFile()` in loops, this is a
realistic path to file-handle exhaustion.

### ✅ RESOLVED (accepted, not a bug) — `files`/`os` modules have no built-in sandbox restriction
[FilesModule.java](janitor-modules/files/src/main/java/com/eischet/janitor/modules/files/FilesModule.java)

Arbitrary absolute paths, arbitrary env var names, arbitrary commands —
`normalize()` only does `getCanonicalPath()`, no containment check against a
base directory. This is intentional: these modules are privileged and only
ever available to a script if the host explicitly registers them (they are
never auto-registered). Checked for prior art on flagging "dangerous"
modules in other embedded scripting/polyglot runtimes (GraalVM Polyglot,
Luau) — neither annotates modules as dangerous; they either gate individual
capabilities behind explicit opt-in flags at the runtime/context level
(GraalVM's `allowIO`/`allowCreateProcess`/...) or simply omit unsafe
functionality from the sandboxed environment entirely (Luau). Janitor's
existing opt-in module registration already matches the latter pattern, at
module granularity. Decision: no code change; document the risk (host is
responsible for not registering `files`/`os` for untrusted scripts) and
leave it at that.

---

## Prioritized summary (tackle first)

1. `ignoreSecurityIssues` disables TLS verification JVM-wide ([JanitorHttpClient.java:235](janitor-modules/httpclient/src/main/java/com/eischet/janitor/modules/httpclient/JanitorHttpClient.java)) – security hole in multi-tenant scenarios.
2. `os.exec` deadlock on heavy process output ([OperatingSystemModule.java](janitor-modules/os/src/main/java/com/eischet/janitor/modules/os/OperatingSystemModule.java)) – potential DoS vector.
3. `ZipFile.addFile` leaks a `FileInputStream` on exceptions ([ZipFile.java:98-106](janitor-modules/files/src/main/java/com/eischet/janitor/modules/files/ZipFile.java)).
