# Janitor language reference

<a id="about"></a>

## About this reference

This is the reference of the Janitor scripting language, as every application that embeds Janitor provides it. It is
written for people and for AI models. No model knows Janitor from training data, so the reference is precise about the
points where Janitor differs from JavaScript and Python, which it resembles. When you write or read a Janitor script,
do not assume that something works as in those languages; check it here.

Applications that embed Janitor are called **hosts**. A host adds its own modules, global variables and object types,
and describes them in documents of its own. Those documents build on this reference. They refer to the extension points
in [Hosting Janitor](hosting.md#extension-points) and need not repeat anything that is described here.

The reference has these parts:

| File | Content |
| --- | --- |
| [index.md](index.md) | This overview: what Janitor is, how scripts run, the sandbox |
| [language.md](language.md) | Syntax and semantics: literals, variables and scopes, operators, control flow, functions, errors |
| [builtins.md](builtins.md) | Global functions and the built-in types with their methods and properties |
| [modules.md](modules.md) | `import`, and the standard modules of the Janitor project |
| [module-types.md](module-types.md) | Generated list of the members of the objects that the standard modules create |
| [pitfalls.md](pitfalls.md) | Common mistakes, especially for readers who know JavaScript or Python |
| [hosting.md](hosting.md) | For hosts: registering modules, binding globals, documenting both |
| [open-questions.md](open-questions.md) | Known gaps and behavior that looks unintended |

Some parts of [builtins.md](builtins.md) and [modules.md](modules.md), and all of [module-types.md](module-types.md), are
generated from the interpreter's own tables, and every example in the reference is run as a test. The examples show
their output in a separate block.

<a id="what-is-janitor"></a>

## What Janitor is

Janitor is a small scripting and expression language for applications that run on the Java virtual machine. Typical uses
are rules, filters, computed fields, small automations and glue code that the users of an application write themselves.
Janitor aims to be:

- **embedded**: a script runs inside a host application and works with the objects that the host gives it;
- **sandboxed by default**: a script can only do what the host allows (see [The sandbox](#sandbox));
- **easy**: the syntax looks like JavaScript with a few Python ideas (`and`, `or`, `not`, `in`), plus literals for
  dates and durations.

Values are dynamically typed. Variables are not declared: there is no `var`, `let` or `const`.

```janitor
for (name in ["Ada", "Grace"]) {
    print("Hello, " + name + "!");
}
```

```output
Hello, Ada!
Hello, Grace!
```

<a id="execution-model"></a>

## Execution model

<a id="scripts"></a>

### Scripts

A **script** is a piece of source text that the host compiles and runs. It consists of statements. `import` statements
may only appear at the top level, outside of all blocks, functions and loops. The host decides when a script runs, and
how often: for example once per click, once per database record, or once per incoming message.

Every run starts with fresh variables: a script cannot keep a value from one run to the next, unless the host provides a
way to store it.

<a id="script-result"></a>

### The result of a script

A script can hand a value back to the host, its **result**. The host decides what the result means; a filter, for
example, might keep a record if the result is truthy. The result is:

1. the value of a `return` statement at the top level, which also ends the script, or else
2. the value of the last expression statement at the top level that ran, or else
3. `null`.

```janitor
price = 120;
discount = 0.1;
return price * (1 - discount);
```

```result
108.0
```

A script that consists of a single expression returns its value, so `return` is optional for expressions:

```janitor
3 > 2 and "a" < "b"
```

```result
true
```

A function call is an expression statement, too. `print(...)` returns `null`, so a script that ends with a `print` call
has the result `null`.

<a id="globals"></a>

### Global variables

Before a script runs, the host can **bind global variables**: values that the script can use without defining them,
such as the record that a filter looks at, or the current user. Which globals exist depends entirely on the host and on
the situation; the host documents them (see [hosting.md](hosting.md#host-provided-globals)). Besides these, every script
sees the [built-in functions](builtins.md#globals) `print`, `assert`, `help` and `dir`.

Reading a name that is not defined is a runtime error (`NameException`), not `undefined` or `None`.

<a id="output"></a>

### Output

`print(...)` hands its arguments to the host, which decides what happens with them: a console, a log, a text field. With
several arguments, the host also decides how they are separated; to be safe, pass one string.

<a id="sandbox"></a>

## The sandbox

A Janitor script has no access to the outside world by itself. There are no built-in functions for files, network,
processes, environment variables, threads or Java classes. A script can only use

- the built-in types and functions described in this reference,
- the global variables that the host binds, and
- the modules that the host registers, and that the script imports.

So the host decides what scripts may do. Some standard modules give access to the file system (`files`), the operating
system (`os`) or the network (`httpclient`, `msgraph`, `mcp`, …); a host registers them only for scripts that may use
them. A module that is not registered cannot be imported. See [modules.md](modules.md#availability).

A host can also limit how many instructions a script may run, so that an endless loop ends with an error.

<a id="versions"></a>

## Versions

This reference describes the version of Janitor that it was published with. Hosts that read it from the class path
(artifact `com.eischet.janitor:janitor-docs`) get that version from `JanitorDocs.version()`; see
[hosting.md](hosting.md#shipping-the-reference).
