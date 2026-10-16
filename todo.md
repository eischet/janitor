# Janitor – TODO list for Bugs and Features

- **JList has no value equality.** `JList` (via `JanitorComposed`) does not override `equals`/`hashCode`, so lists only compare by identity. Two lists with the same elements are not equal, and neither are two `JMap`s containing such lists (e.g. the same JSON parsed twice), although `JMap` itself compares by content. Decide whether `JList` should compare by elements (keeping in mind that lists are mutable and may be used as map keys or set elements) and implement `equals`/`hashCode` accordingly.
