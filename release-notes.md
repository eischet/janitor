# 0.9.7, work in progress

Added basic syntax highlighters for ACE, TextMate and VS Code.
Add a logo.


# 0.9.6, 2024-08-13

Added a number of unit tests.
Converted JString into a composite instead of a wrapper.
Durations can now be added and subtracted.
The AST can now be written to JSON.
Lots of names, signatures, comments cleaned up.


# 0.9.5, 2024-08-07

Merged addModule/registerModule in the Environment interface, of which one was only an empty stub.
Small fixes for Scopes.


# 0.9.4, 2024-08-02

Fixed some design mistakes with JsonOutputStream; a number of helper methods for writing optional values have
had their signature changes to key, value instead of the surprising value, key, and all have been renamed to "optional".

Enable mapping true|false|null for Java Boolean fields in dispatch tables.

Added some tests.


# 0.9.3, 2024-07-30

New JSR223 implementation. See Jsr223TestCase for a simple example.
Contexts are not implemented yet, but bindings should work reasonably well.
Note that this is currently just a by-product, "because we can", not our main artifact.

The "baseDispatch" in the builtin types interface can now be used to add properties/methods to all builtin
objects; all dispatch tables now support simple inheritance.


# 0.9.2, 2024-07-27

An older "class" implementation  is removed and replace by a new "dispatch table" approach, which is easier to extend.
Most built-in classes have been updated to use dispatch tables instead of classes or even hand-written dispatch logic.
A number of implementation details have been moved out of the "api" into the "lang" implementation package.


# Initial Release 0.9.1, 2024-07-17

This is the first public release, after the language grew within a custom application for a couple of years.
