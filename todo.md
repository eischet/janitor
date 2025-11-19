# Things to do

TODO: many "type cast" are not complete enough, because they fail to unpack all the possible cases and look at the first level only.
  That, however, fails when e.g. the JanitorObject passed in to such a method is a JAssignable (lvalue they call it in C I think), which
  is not itself e.g. a string or a number but contains one.


TODO: *args and **kwargs are now implemented in the Grammar and the compiler, but not in the runtime yet.


The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.

Some chained .getEnviroment().getFoo().getBar() calls should be untangled. Those are left over from some
refactorings.

Lots of unit tests will have to be added.

Two things are called "builtin" at the moment: default variables in a script's scope, and environment-supplied data
types like list, map, integer, string... Not good, the names should be more recognizable.

