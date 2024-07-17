# Janitor 

Janitor is an embedded scripting and expression language for (Java) applications.

## Main Goals

* Be accessible to programming novices and casual developers by providing simple and familiar syntax.
* Be easy to embed into existing code by application developers.
* Be reasonably safe by sandboxing everything by default.
* Be free (MIT License).

## What does a script look like?

```
for (i in [1,2,3]) {
    print("Did you stick a penny in there?");
}
```

There's a growing collection of sample scripts in the folder sample-scripts.


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

Versions 0.9.x are uploaded to Github Packages only to avoid spamming Maven Central with rapidly changing JARs.
(https://docs.github.com/en/packages/learn-github-packages/introduction-to-github-packages)


