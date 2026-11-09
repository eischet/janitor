# Notepad++ Syntax Highlighting for Janitor

`Janitor.udl.xml` is a User Defined Language (UDL 2.1) for Notepad++. It highlights keywords, constants,
word operators (`and`, `or`, `not`), comments, strings (`'...'`, `"..."`, `'''...'''`, `"""..."""`),
regex literals (`re/.../`), numbers and `@` literals, and folds `{ }` and `[ ]` blocks.

## Installation

Run `install.bat` (double-click is fine), or copy `Janitor.udl.xml` yourself to `%APPDATA%\Notepad++\userDefineLangs\` and restart Notepad++, or use
*Language → User Defined Language → Define your language... → Import...*.

Files ending in `.janitor` are then highlighted automatically; for other files choose *Language → Janitor*.

## Limitations

UDL is a simple, keyword-based highlighter, so a few things are approximations:

* Date and time literals are split at `-` and `:`, so in `@2024-12-31` only `@2024` gets the literal colour.
* `#` starts a line comment anywhere, not only as a shebang in the first line.
* `from`, `to`, `in` and `as` are always highlighted, even where they are used as identifiers.
* The colours are meant for a light theme; adjust them in the UDL dialog for a dark theme.
