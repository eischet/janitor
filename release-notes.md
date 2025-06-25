
# 0.9.19, 2025-06-xx

Add a regex matcher class, in Java style, e.g. `m = re/(foo)/.matcher(text);`
Add arbitrary meta data to dispatch tables.
Add rudimentary support for Pythonic "dir" and "help" functions as default builtins.


# 0.9.18, 2025-04-24

Add os.getenv(string): string to the os module.
When serializing objects to JSON, using the DispatchTable, omit empty strings by default.
Skipped 0.9.17 because of build settings mistake.

# 0.9.16, 2025-04-23

Improve JSON output in the "GenericDispatchTable" implementation: ignore defaults like 0, "", 0.0d instead of writing them; automatically output lists.

JanitorDefaultEnvironment now provides a static "create" method that allows creating envs as one-liners, for use in unit tests, like this:

```java
    @BeforeAll
    public static void setUp() {
        SomeSingleton.setJanitorEnvironment(JanitorDefaultEnvironment.create(new JanitorFormattingGerman(), System.err::println));
    }
```

# 0.9.15, 2025-03-28

Update the maven plugin to add os.exec, for another project that uses this plugin.


# 0.9.14, 2025-03-19

New string method `split` splits a string into a list of substrings by the given separator.
Usually, a separator is taken as a string: `'1,2,3'.split(',')` returns `['1', '2', '3']`.
This string can be empty, splitting by each character: `'abc'.split('')` returns `['a', 'b', 'c']`.
If the separator is a regular expression, it is used to split the string: `'1,2,3'.split(/,/)`. This is very similar to how split works in Java.

''.encode(charset) encodes a string to a binary representation using the given charset, which defaults to UTF-8 when omitted.



# 0.9.13, 2025-02-20

Tweak JSON parsing to return integers for numbers that are integers, and doubles only for numbers that are not.
This improves interoperability with many JSON APIs, which seem to have issues with e.g. an ID value of 5.0 where 5 is 
expected, even though there are actually no integers in the JSON specification.


# 0.9.12, 2024-11-28

Fix maps throwing a null pointer exception when non-existent keys are accessed as d.foo instead of d["foo"].
This makes map.foo behave like map?.foo in call cases now, which is probably more readable, especially when working with JSON-like data.


# 0.9.11, 2024-10-29

Add some properties to the maven plugin.


# 0.9.10, 2024-10-07

Provide automatic conversion to/from JSON in classes that extends JanitorComposed<T>, base on the existing dispatch tables.  Basic cases work, but overall the feature needs some more thought.


# 0.9.9, 2024-09-11

New 'md5' and 'sha256' methods on binary values to calculate checksums.
Provide easier access to the dispatch tables for builtin types, though I'm not happy with having those implementation details in the API.


# 0.9.8, 2024-09-05

0.9.7 broke filter scripts with implicit objects in an app; restoring old behaviour.


# 0.9.7, 2024-09-05

Added basic syntax highlighters for ACE, TextMate and VS Code. Added a logo.
Added several example scripts, some implementing challenges from sampleprograms.io.
Remove the "CompilerSettings", which allowed to automatically turn "." into "?.", which
was a bad idea from the start and caused subtle concurrency bugs in unit tests.
Lots of test improvements, some refactorings.


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
