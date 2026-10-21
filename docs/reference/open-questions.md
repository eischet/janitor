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

### `datetime.formatAtTimezone(zone, pattern)`

- Without a pattern, the zoned value is never computed, and the method fails with a `NullPointerException`.
- With a pattern, it does not convert: it calls `LocalDateTime.atZone(zone)`, which attaches the zone to the same clock
  time, so `@2024-03-05-14:30.formatAtTimezone("UTC", "HH:mm")` gives `"14:30"` in every host time zone; only patterns
  that print the zone (`z`, `VV`, `XXX`) show a difference. The name suggests "this time, seen in zone X", i.e. a
  conversion from the host's time zone to X.

Both are planned to be fixed on top of the [configurable time zone](#time-zone), which defines what "the host's time
zone" is. (`JDateTimeClass.__formatAtTimezone`)

<a id="locale"></a>

### Locale of `format` and date patterns

Scripts get two kinds of formatting, depending on the method:

- `string.format(...)` uses Java's `String.formatted(...)`, i.e. the JVM's default locale: `"%.2f".format(0.5)` is
  `0,50` on one server and `0.50` on another.
- Date patterns (`date.format("...")`, `datetime.string("...")`, `formatAtTimezone`) and `parseDate`/`parseDateTime` use
  `DateTimeFormatter.ofPattern(...)` without a locale, i.e. also the JVM's default locale, for month and weekday names
  (`MMMM`, `EEEE`) in output and input.
- Without a pattern (`date.format()`, `datetime.time()`), the environment's `JanitorFormatting` formats, e.g.
  `JanitorFormattingLocale(locale)` or `JanitorFormattingGerman`.
- The interface `JanitorFormatting` does not expose a locale; only `JanitorFormattingLocale` has one internally.

So the same script can produce different text on different servers, and a host with German formatting on a server with
an English default locale gets a mixture.

The proposal (not decided yet): **one locale per environment.** `JanitorFormatting` gets a method `getLocale()`, as a
default method that returns the JVM's default locale, so that existing implementations keep compiling and working;
`JanitorFormattingLocale` returns its locale, `JanitorFormattingGerman` returns German. `format`, all date patterns and
`parseDate`/`parseDateTime` then use that locale. Later, a process could get its own locale, like the
[time zone](#time-zone), e.g. for users with their own language. Cockpit, which embeds Janitor, should be checked for its
own `JanitorFormatting` implementation: the default method would not break it, but would return the JVM's locale
instead of the one it formats with.

<a id="dir"></a>

### `dir(value)`

`dir` includes internal names like `__get__` and `__in__`, which scripts should not call.

<a id="modules"></a>

## Modules

<a id="module-documentation"></a>

### Documentation of the larger modules

`pdf`, `mustang` and `docusign` mirror large Java libraries. Their members are listed in
[module-types.md](module-types.md), but the arguments of their functions are not described, and their dispatch tables
have no HELP texts. The same holds for most members of the other modules. HELP texts in the module code would make the
generated tables useful.

This is left open on purpose for now: the modules are still rather experimental.

<a id="http-test-method"></a>

### Test method in the HTTP client

The HTTP client object has a method `_test_throwNativeException()`, which exists for tests and is visible to scripts.
(`JanitorHttpClient`)

<a id="hosting"></a>

## Hosting

<a id="time-zone"></a>

### A configurable time zone for environments and processes

Dates and datetimes in Janitor have no time zone; where one is needed, a time zone of the host is used. Today, that is
not one well-defined zone:

- `DateTimeUtils` (janitor-toolbox) holds a single, JVM-wide time zone in a static field. It comes from a static
  `TimeZoneSource` if a host sets one, else from the JVM's default time zone, and is cached on first use. It is used for
  `datetime.epoch` and `toEpoch()`, `int.epoch`, the conversion of `java.util.Date` and `java.sql.Timestamp` values,
  and for writing datetimes to JSON.
- `@now` and `@today` do not use it: they call `LocalDateTime.now()` and `LocalDate.now()`, i.e. always the JVM's
  default time zone. If a host sets a different zone through `TimeZoneSource`, `@now.epoch` is off by the difference.
- A host that serves users in different time zones cannot give each script run its own zone.

The plan:

- The **environment** gets a time zone: by default from the existing `TimeZoneSource`, else from the system, or set
  explicitly by the host.
- A **script process** inherits the environment's time zone, unless the host sets one explicitly for that process,
  e.g. from the current user's profile (like `setMaxInstructionCount` today).
- Everything that scripts see uses the process's time zone: `@now`, `@today`, `epoch`, `formatAtTimezone`, and the
  conversion of host date values.

Open design points:

1. Code without a process (the JSON support in janitor-toolbox, `nativeToScript`, the JSON support of dispatch tables)
   needs the environment's zone, or keeps `DateTimeUtils` as the fallback.
2. New methods on `JanitorEnvironment` and `JanitorScriptProcess` (e.g. `getZoneId()` / `setZoneId(...)`) should be
   default methods, so that implementations outside of Janitor do not break; Cockpit, which embeds Janitor, should be
   checked.
3. Instead of a time zone alone, the environment could hold a `java.time.Clock` (zone plus current time), so that tests
   can also fix `@now`.
4. Hosts that set a `TimeZoneSource` today must keep working.

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
