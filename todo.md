# Things to do

TODO: *args and **kwargs are now implemented in the Grammar and the compiler, but not in the runtime yet.


The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.

Some chained .getEnviroment().getFoo().getBar() calls should be untangled. Those are left over from some
refactorings.

Lots of unit tests will have to be added.

Two things are called "builtin" at the moment: default variables in a script's scope, and environment-supplied data
types like list, map, integer, string... Not good, the names should be more recognizable.

