# Janitor – TODO list for Bugs and Features

- **JList has no value equality.** `JList` (via `JanitorComposed`) does not override `equals`/`hashCode`, so lists only compare by identity. Two lists with the same elements are not equal, and neither are two `JMap`s containing such lists (e.g. the same JSON parsed twice), although `JMap` itself compares by content. Decide whether `JList` should compare by elements (keeping in mind that lists are mutable and may be used as map keys or set elements) and implement `equals`/`hashCode` accordingly.
- we need to consolidate a number of "tool" and "util" classes.
  Candidates found so far: `StringHelpers` (janitor-toolbox) has duplicate methods (`nullOrEmpty` / `isNullOrEmpty` / `isEmpty`,
  `notEmpty` / `isNotNullOrEmpty`); `ObjectUtilities` (janitor-api); `FlexiDateParser` (janitor-lang, Gson data binding, only used
  by Cockpit's `GsonHelper`), probably to be removed rather than moved. Renaming or moving `StringHelpers` or `ObjectUtilities`
  requires rebuilding ajui-v8 and ajui-vx, which use them.
- **The time zone is global.** `DateTimeUtils.setTimeZoneSource` configures one time zone for the whole JVM. Attach it to the
  `JanitorEnvironment` instead, or even to the running script process, which would be more local; `DateTimeUtils` would then
  need the zone passed in, or look it up from the current process.
