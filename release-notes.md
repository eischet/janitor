# 0.9.36-SNAPSHOT, 2025-10-xx

- fix snapshot repo for automatic builds on GitHub Actions


# 0.9.35, 2025-10-21

Updated grammar:
- Syntax is prepared for *args and **kwargs, but they are not fully implemented yet.
  Notably, these are currently only available on functions, not on lambdas.
- Semicolons are now optional at the end of a statement, similar to JavaScript
- Unused lexer symbols were removed: "|", "#".
- Function calls were simplified in the AST compiler.
- The syntax has been updated to see assignments as statements, not expressions, which
  was intended from the start, but I somehow never got around to fixing. As a side effect,
  the language is now stricter with unbalanced parentheses, which I have no real explanation
  for, but which is a good thing.
- The throw statement has been implemented.


# 0.9.30-33, 2025-09-18

0.9.30: Packaging changes only. Janitor is now deployed to a small public maven repo; see pom.xml if interested.
0.9.31: moved the project under a new common parent POM for simpler dependency management.
0.9.32: release with updated POM, no changes.
0.9.33: the Maven plugin did stupid things to the class path; fixed by setting all dependencies to "provided".


# 0.9.29, 2025-09-15

Defend against null pointer exceptions coming from unset locations, as reported by an upstream project customer.

Lists without a specified element type can now be read from JSON. Objects within the list will be returned as Maps.

Generic Maps can now be read from JSON.

Add factory methods for nullable ints and longs to the Janitor interface.

JDateTime was missing equals/hashCode.

JDateTime and JDate can now be properly written to JSON, and basic support for reading from JSON has been added.


# 0.9.28, 2025-08-25

Add missing list methods "remove" and "removeAll" that remove elements (by equality) from the list.

Add a "value expander" that allows you to auto-translate values assigned to object properties.
E.g. your client code can detect "a.foo = 'bar'" and turn the string 'bar' into an object instance that can actually be assigned to the property.
This works great with database lookups (in the not-yet-public janitor-orm sister project) and will be useful for enums, too, where you could
turn the 'bar' string into an actual enum instance without much ado.

The wildcard matching operator now handles null values more consistently. Instead of failing, a "null" string will make the operators return false.

(0.9.27 was skipped because of a premature internal package deployment.)


# 0.9.26, 2025-08-21

Add a value converter to object properties in the GenericDispatchTable, so you can chose to convert script values in app object.
This is used in a project to enable assignments like `foo.bar = 'frobnicate'` to automatically look up a database object by the string value and assign *that*
to the property instead of the string value. This is very useful in classes that have "foreign key" properties. Value conversion is handled by the client,
so you can make it work however you want. The default implementation simply casts to the expected type.


# 0.9.25, 2025-08-20

Wildcard matching operator: if either side of the operator is null, return false. This applies to both the "~" and "!~" operators in the same way, as SQL does it with "LIKE" and "NOT LIKE". 

List: the 'remove' method was not added to the default dispatch table, and it was actually "removeAll". Fixed both.

Skipped 0.9.24 because I deployed a broken version internally by mistake.


# 0.9.23, 2025-08-14

`"foo"[:8]` now returns `"foo"` instead of failing with an exception, like it was always meant to.

Script processes now always have a name, which an upstream project uses for auditing data access.
The name is filled with the module name, which was always mandatory, and should not influence existing client code.

A number of @Nullable/@NotNull annotations were added to clarify the API auf the JanitorScriptProcess.


# 0.9.22, 2025-08-05

JanitorEnvironment: enable auto-discovery of modules via the Service Loader mechanism; this is an opt-in feature.

Maven Plugin: opt in to this new auto-discovery. Use case: I'm auto-generating TypeScript definitions for Java Code in a web project.

Relax some parts of the GenericDispatchTable, which were rejecting valid values since 0.9.21, e.g. null for a nullable string property.

General toolbox thing: Add a very simple i18n tool that handles a single resource bundle for translations.

JList is now JanitorComposed, not wrapped, to hide the inner list object. This enables us to more easily implement list properties that, when changed,
"write back" to the original object. E.g. you can now "foo.entries.add('bar')", which would've manipulated a temporary list and had no visible effect
before when a simple addListProperty(getter) call was used to define "entries".

Dispatch tables now take an optional, but recommended, java default constructor. Having this enables reading objects from JSON lists, for example.


# 0.9.21, 2025-07-18

Many helper functions, e.g. for creating script objects like integers and floats, have been centralized in the new "Janitor" class.
The plan is to move most client-facing code into that class. Instead of getting the builtins from somewhere, you can now simply write
`Janitor.string(...)` to create a string instance, for example. That's both nicer to read and easier to remember.

Behind the scenes, it is still possible to provide a custom Environment, either discovered via the service loader mechanism or manually, but your code
doesn't have to reference it all over the place.

Made the REPL work and added a standalone REPL jar. Some logging issues remain, where SLF4J bindings are not picked up; still figuring that one out.

New MetaData Key TYPE_HINT is automatically populated by the DispatchTable where possible. This means that Java code can now more easily introspect an unknown object.

We now have distinct "glue" and "runtime" exceptions, where the new glue exception can be converted to runtime in order to get a proper script stack trace.
This makes code working with Janitor types much simpler, mainly because it's now unnecessary to carry around references to a running script process or to
the runtime. 


# 0.9.20, 2025-07-02

The runtime now supports arbitrary callbacks to be executed; those are JCallable (and usually JanitorObject) instances that are retrieved from another script
and then can be called by the Java side when needed.


# 0.9.19, 2025-06-25

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
