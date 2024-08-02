# Things to do

The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.

Some chained .getEnviroment().getFoo().getBar() calls should be untangled. Those are left over from some
refactorings.

Lots of unit tests will have to be added.

Two things are called "builtin" at the moment: default variables in a script's scope, and environment-supplied data
types like list, map, integer, string... Not good, the names should be more recognizable.


# Later

Write a C# version: https://tomassetti.me/getting-started-with-antlr-in-csharp/

Consider adding Janitor to https://github.com/nst/JSONTestSuite because it should be able to parse most valid JSON
files as valid code, or at least borrow their test files and run them through the parser in out unit tests.
