# Things to do

TODO: *args and **kwargs are now implemented in the Grammar and the compiler, but not in the runtime yet.

TODO: templates need unit tests. TemplatingTestCase.java is a small start.
  The main upstream app uses them extensively, so I know they do work, but I'd rather have unit tests to prove it to myself. :)

The "Toolbox" contains some leftover code that is not actually used here, but by the original mother app. Remove.

Lots of unit tests will have to be added.

Two things are called "builtin" at the moment: default variables in a script's scope, and environment-supplied data
types like list, map, integer, string... Not good, the names should be more recognizable.




# Bugs

TODO: There's a difference between these two approaches of loading JSON into a map, but there shouldn't be.

            final JMap newData = Janitor.map();
            JMapClass.parseJson(newData, jsonContents, Janitor.current()); // Map is filled
            log.info("loaded json into map: {}", newData.exportToJson(Janitor.current()));

            newData.readJson(Janitor.current().getLenientJsonConsumer(jsonContents)); // Map stays empty
            log.info("loaded json into map: {}", newData.exportToJson(Janitor.current()));
    