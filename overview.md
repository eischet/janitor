# Language Overview

For a formal grammar in ANTLR4 format, see [Janitor.g4](https://github.com/eischet/janitor/blob/main/janitor-lang/src/main/antlr4/com/eischet/janitor/lang/Janitor.g4).

## Script

At the top level, a script consists of a series of statements, which can be import or block statements.

An import statement, like "import foo;", loads/makes available a "module" provided by the environment.
A very small number of modules is included with core Janitor at the moment, because an app is supposed to provide 
its own modules for its own functionality. These statements can only occur at the top level of a script, preferably
at the top.



