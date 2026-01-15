# Janitor 

Janitor is an embedded scripting and expression language for (Java) applications.

![foo](logos/janitor64.png)

## Main Goals

* Be accessible to programming novices and casual developers by providing simple and familiar syntax.
* Be easy to embed into existing code by application developers.
* Be reasonably safe by sandboxing everything by default.
* Be free (MIT License).


## Some Use Cases

* Letting your users extend and customize your application with scripts.
* Letting your users automate tasks with scripts.
* Letting your users configure your application with scripts.

Some concrete cases that are in production use right now:

* Let users fully customize table views on SQL queries and/or web service calls by letting them write "column scripts"
  and "filter scripts" that transform and organize the data into the form they want to see.
* Let users write simple scripts to import and export data to and from APIs, Database, Excel and CSV files, using APIs 
  made accessible by a host application.
* Let users author complex and changing permission rules for an app by enabling them to evaluate user accounts and records in a script.
* Read mails from a mailbox and turn them into data, e.g. by importing these mails into an IT support system.


## What does a script look like?

```
for (i in [1,2,3]) {
    print("Did you stick a penny in there?");
}
```

There's a growing collection of sample scripts in the folder sample-scripts.

The unit tests in the janitor-lang module contain a number of examples, too. 

The grammar in ANTLR4 format is here: janitor-lang/src/main/antlr4/com/eischet/janitor/lang/Janitor.g4


## What does it look like on the Java Side?

```
final DemoEnvironment env = new DemoEnvironment();
final DemoRuntime runtime = new DemoRuntime(env);
final RunnableScript script = runtime.compile(name, "print(foo);");
script.run(g -> g.bind("foo", "bar"));
```

This will print the Text "bar" to the console.

See the janitor-demo module for a simple example of how to embed Janitor into a Java application.


# Releases

Starting with 1.0.0, we'll upload to Maven Central.

# IntelliJ Plugin

There's a rudimentary plugin with syntax highlighting and a REPL available here: https://github.com/eischet/janitor-idea

