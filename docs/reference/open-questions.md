# Open questions

This page lists behavior that the reference documents as it is, but that looks unintended, is incomplete, or is not
decided yet. Scripts should not rely on these points; they may change. Each item says what happens today and where in
the code it comes from.

<a id="language"></a>

## Language

<a id="named-arguments"></a>

### Named arguments are bound by position

In a call, `name = value` does not bind the value to the parameter `name`. Parameters are filled in the order of the
arguments, whatever their names; for a parameter with a default value, a named argument of the same name is used if
there is no positional one. So `f(b = "B", a = "A")` for `function f(a, b)` binds `a` to `"B"`, and with a default for
`b` the same call fails with "Default parameters are not yet implemented". `*rest` also receives the values of named
arguments, which `**options` gets, too. The code has a TODO for the missing cases. (`ScriptFunction`, argument binding)

<a id="deprecated-operators"></a>

### Deprecated operators `&&`, `||`, `!` and `<>`

`&&`, `||` and `!` exist only so that scripts taken over from JavaScript keep working, and `<>` is a second spelling of
`!=`. They are planned to be removed in Janitor 1.0.0, so that there is one spelling for each operator: `and`, `or`,
`not`, `!=`. `!` is also a trap for JavaScript users, because it binds weaker than `==` (`!a == b` is `!(a == b)`).
`!=` and `!~` are separate operators and stay. The plan:

1. In a 0.9.x version, compiling a script that uses one of them gives a warning.
2. In 1.0.0, they are compile errors whose message names the replacement, e.g. "`&&` is not supported; write `and`",
   so that people and AI models can fix scripts in one step.

Hosts should check their stored scripts with `checkCompile` before upgrading.

<a id="builtins"></a>

## Built-in functions and types

<a id="format-at-timezone"></a>

### `datetime.formatAtTimezone(zone)` without a pattern

Without a pattern, the zoned value is never computed, and the method fails with a `NullPointerException`.
(`JDateTimeClass.__formatAtTimezone`)

<a id="locale"></a>

### Locale of `format`

`string.format(...)` and date patterns with names (`MMMM`, `EEEE`) use the default locale of the Java VM, while
`date.format()` without a pattern uses the host's `JanitorFormatting`. So the same script can produce different text on
different servers.

<a id="dir"></a>

### `dir(value)`

`dir` returns an empty list for numbers, booleans, maps and some module objects, because it only looks at values that
implement `HasDispatcher`, and it includes internal names like `__get__`.

<a id="cut-filename"></a>

### `string.cutFilename(maxLength)`

The code has a note that this method serves a single application and should be removed from the language.
(`JStringClass.cutFilename`)

<a id="modules"></a>

## Modules

<a id="module-documentation"></a>

### Documentation of the larger modules

`pdf`, `mustang` and `docusign` mirror large Java libraries. Their members are listed in
[module-types.md](module-types.md), but the arguments of their functions are not described, and their dispatch tables
have no HELP texts. The same holds for most members of the other modules. HELP texts in the module code would make the
generated tables useful.

<a id="native-modules"></a>

### Modules without dispatch tables

`collections` and `httpclient` implement `janitorGetAttribute` directly instead of using a dispatch table, so neither
`dir()` nor the generator of this reference can list their functions.

<a id="files-lastmod"></a>

### `files.lastmod(path)`

`File.lastModified()` returns milliseconds, but the value is converted as if it were seconds, so the date is far in the
future. (`FilesModule.lastMod`)

<a id="zip-add-text"></a>

### Encoding argument of `Zip.addText`

`addText(text, entryName, encoding)` reads the encoding from the fourth argument instead of the third. (`ZipFile`)

<a id="http-test-method"></a>

### Test method in the HTTP client

The HTTP client object has a method `_test_throwNativeException()`, which exists for tests and is visible to scripts.
(`JanitorHttpClient`)

<a id="hosting"></a>

## Hosting

<a id="instruction-limit"></a>

### Setting the instruction limit

The limit is set on the script process (`JanitorScriptProcess.setMaxInstructionCount`), which hosts only get hold of
when they create the process themselves; `RunnableScript.run` has no option for it.

<a id="print-separator"></a>

### Separator of `print`

How `print` separates several arguments is up to each runtime: the test runtime uses a space, the demo runtime nothing.
A defined default would make scripts portable.

<a id="older-documents"></a>

### Older introductions

`scripting-intro.md` in the repository says that semicolons are required and that there is a C-style `for(;;)` loop.
Neither is true for the current version; this reference describes the actual behavior.
