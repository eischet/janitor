# 0.9.3, 2024-08-xx (work in progress)

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
