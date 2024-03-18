# Janitor 

Janitor is an embedded scripting and expression language for (Java) applications.

This is what a script can look like:

```
for (i in [1,2,3]) {
    print("Did you stick a penny in there?");
}
```

This is what an expression can look like:

```
x % 2 == 0 ? "even" : "odd" // determine the row's CSS class
```

## Main Goals

* Be accessible to programming novices and casual developers by providing simple and familiar syntax.
* Be easy to embed into existing code by application developers.
* Be reasonably safe by sandboxing everything by default.
* Be free (MIT License).



