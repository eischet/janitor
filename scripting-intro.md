# Scripting Introduction

The basic idea is that the people writing the scripts and the people writing the application are *two different groups of people*.
We'll concentrate on the scripting side here. See below for the embedding side.

The syntax is similar to C and JavaScript, with some simplifications.
The runtime is inspired by Java, Python and JavaScript.

This is what a script can look like:

```
for (i in [1,2,3]) {
    print("Did you stick a penny in there?");
}
```

Some things to note:
* Statements end with a semicolon, which is required.
* Blocks are denoted by curly braces. (Indentation is not significant, though recommended for longer scripts.)
* A `print` function is supposed to be provided by the host application. (What it actually *does* is up to the host application!)
* List (and Map) literals are available, just like in Python and JavaScript. (In fact, any valid JSON expression is a valid Janitor expression!)
* The main means of iteration is the for-in loop, like in modern Java. We've got for(;;) and while() loops too, of course.
* Variables come into existence when they are first assigned to, and no types are specified. No var, const, let. We're very similar to Python in this regard.


This is what an expression can look like:

```
return x % 2 == 0 ? "even" : "odd" // determine the row's CSS class
```

Some things to note:
* Scripts can return results from their top level, which can then be received by the host application.
* In an expression context, the final semicolon is optional.

In this case, the *x* comes from outside of the script and the result is used outside of the script.
That's what "embedded" means in this context.

Another expression:

``` 
x > 17 and not y < 42
```

* The `return` keyword is optional, too, in an expression context.
* 'and', 'or', 'not' are preferred because these are easier to read than '&&', '||', '!'. Both styles are supported, though.


One more thing: date literals. Most languages don't have any, which is strange for such a common type. Python example:

```python
import datetime
christmas = datetime.date(2024, 12, 24)
christmas_eve = datetime.datetime(2024, 12, 24, 18, 0)
now = datetime.datetime.now()
today = datetime.date.today()
````

Janitor simplifies this quite a bit:

```
christmas = @2024-12-24;
christmas_eve = @2024-12-24-18:00;
now = @now;
today = @today;
```

Note that the Python code is already much better than, say, the equivalent Oracle SQL with TO_DATE('2024-12-24', 'YYYY-MM-DD') or what some other languages put you through when you want to specify a date value.


# Embedding Introduction

These are the steps required to run a script:
* Create a runtime instance. This will implement the print function and, for example, provide modules that a script can import.
* Compile a script from text into a JanitorScript instance.
* Run the script, optionally providing a set of global variables. The script can then access these variables. The run method will return the result of the script, if any.

That's how it looks like in code:

```java
public class SimplestEmbeddingTestCase {

    @Test
    public void simplestTestPossible() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorScript script = rt.compile("main", "print('Hello, ' + person + '!');");
        script.run(globals -> globals.bind("person", "JD"));
        assertEquals("Hello, JD!\n", rt.getAllOutput());
    }

    @Test
    public void simpleExpressionTest() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorScript script = rt.compile("main", "3 * x + 1");
        final JanitorObject result = script.run(globals -> globals.bind("x", 2));
        assertEquals(7L, result.janitorGetHostValue());
    }

}
```

Looking at this simple unit test, we can see two main ways of using Janitor:
* Run a script and fetch its *output*. Scripts mainly causing side effects will probably want to print (= log) something.
* Run a script and fetch its *result*. When used as an expression language, that's what we're usually interested in.
  The implementation does not really differentiate between the two styles. The term "expression" is only used to describe the context in which a script is used.
  You have to decide which style is more appropriate for your specific use case.

You can reuse script instances as often as you like, and this saves time by avoiding having to recompile a script again and again.

You can reuse runtime instances as often as you like when they are implemented in a stateless way. The OutputCatchingTestRuntime is an example of a stateful runtime,
which catches all print statement output in a StringBuffer. This is not a good candidate for reuse, because the output would accumulate over time.

In your own application, you'll usually implement at least one type of runtime, which will provide a common set of modules and functions to all scripts.

You'll also want to take a look at the *JanitorObject* interface, which classes need to implement in order to be used by scripts.
A number of built-in types are provided, which all implement that interface, including Strings, Booleans, Dates, Numbers, Lists and Maps.

By overriding the JanitorObject interface's "janitorGetAttribute" method, you can provide properties and methods to scripts. For example. this is how the JFloat class (builtin floating point numbers)
implements the "int" property, which returns an integer value by truncating the floating point number:

```java
    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if (Objects.equals(name, "int")) {
            return JInt.of((long) number);
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }
```


In a script, it's used like this:

```
myFloat = 17.3 * 2;
myInt = myFloat.int;
assert(myInt == 34);
```

Everything else builds on this simple, string-based dispatch API.
Finally, let's have a look at how the String class implements its toUpperCase function to see how to implement Java code that can be called by scripts:

```java
    public static JString __toUpperCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.string.toUpperCase(Locale.ROOT));
    }
```

A typical callable, in this case a string method, will receive the object it was called on, the running script, and the call arguments.

The JanitorScriptProcess represents the "process" that is currently executing. These objects are created by the run() method on JanitorScript instances.
This is where a script's internal state lives during execution.


# Sandboxing and Security

Janitor does not allow scripts to access any platform-specific functionality by default. This includes file system access, network access, and especially reflection.
A Java developer has to explicitly enable access by either adding a module to the runtime or by binding objects into a script's global scope.
The worst a script can do "out of the box" is to consume CPU time and memory, and maybe run into an endless loop.

This is vastly different from other scripting languages for the JVM, e.g. Jython, Rhino/Nashorn or Groovy, which operate as "first-class" languages.
If you're looking for a language to write your whole JVM application in, instead of Java, Janitor is very probably *not* what you're looking for.


