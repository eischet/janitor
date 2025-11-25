# Things to do

TODO: *args and **kwargs are now implemented in the Grammar and the compiler, but not in the runtime yet.

TODO: templates need unit tests. TemplatingTestCase.java is a small start.
  The main upstream app uses them extensively, so I know they do work, but I'd rather have unit tests to prove it to myself. :)

The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.

Lots of unit tests will have to be added.

Two things are called "builtin" at the moment: default variables in a script's scope, and environment-supplied data
types like list, map, integer, string... Not good, the names should be more recognizable.

