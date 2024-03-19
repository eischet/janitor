# Janitor 

Janitor is an embedded scripting and expression language for (Java) applications.


## Main Goals

* Be accessible to programming novices and casual developers by providing simple and familiar syntax.
* Be easy to embed into existing code by application developers.
* Be reasonably safe by sandboxing everything by default.
* Be free (MIT License).


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

```
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

You can reuse script instances as often as you like, and this saves time by avoiding having to recompile a script again and again.

You can reuse runtime instances as often as you like when they are implemented in a stateless way. The OutputCatchingTestRuntime is an example of a stateful runtime,
which catches all print statement output in a StringBuffer. This is not a good candidate for reuse, because the output would accumulate over time.

In your own application, you'll usually implement at least one type of runtime, which will provide a common set of modules and functions to all scripts.

You'll also want to take a look at the *JanitorObject* interface, which classes need to implement in order to be used by scripts.
A number of built-in types are provided, which all implement that interface, including Strings, Booleans, Dates, Numbers, Lists and Maps.

