# Pitfalls

Janitor looks like JavaScript and borrows from Python, but many details differ. This list collects the mistakes that
people and AI models make most often when they come from those languages. Each item links to the full description.

<a id="checklist"></a>

## Checklist

Before you hand over a script, check it against this list:

1. Variables that are first assigned inside a block are gone after the block. ([details](#block-scope))
2. Map keys in literals are identifiers or strings; methods hide keys with the same name. ([details](#map-keys))
3. `import name;` and `import "NAME" as alias;` find different modules. ([details](#two-imports))
4. `indexOf` has no start index, and too many arguments are an error. ([details](#indexof))
5. Use `x not in list`, `not`, `and`, `or`, not the deprecated `!`, `&&`, `||`. ([details](#not-in))
6. `return` at the top level ends the script and sets its result. ([details](#top-level-return))
7. There is no string interpolation. ([details](#no-interpolation))
8. Methods that change a list return `null`. ([details](#null-returning-methods))
9. Functions must be defined before they are called, and named arguments are bound by position. ([details](#no-hoisting))
10. Only use modules that the host registers. ([details](#modules-are-optional))

<a id="block-scope"></a>

## Block scope

A variable that is first assigned inside `{ ... }` (an `if`, a loop, `try`, `catch`, a function) does not exist after
the block. In Python it would; in JavaScript it would with `var`. Assign the variable before the block.
([language.md](language.md#block-scope))

```janitor error
for (n in [1, 2, 3]) {
    last = n;
}
print(last);
```

```error
name 'last' is not defined
```

```janitor
last = null;
for (n in [1, 2, 3]) {
    last = n;
}
print(last);
```

```output
3
```

The opposite also differs from Python: inside a function, assigning to a variable that exists outside changes the
outside variable. There is no `global` or `nonlocal`. ([language.md](language.md#outer-variables))

<a id="map-keys"></a>

## Map keys

- Keys in a map literal are identifiers or strings: `{name: "Ada"}` and `{"name": "Ada"}` are the same. Numbers cannot
  be keys in a literal: `{1: "a"}` is a syntax error; write `m = {}; m[1] = "a";`.
- `m["key"]` is the official way to read an entry; `m.key` is a shorthand that does not work for keys that are names of
  map methods (`size`, `keys`, `values`, `get`, `put`, `remove`, ...) or `class`, because methods take precedence.
- A map does not keep the order of its keys.
- A lone `{}` or `{ ... }` at the start of a statement is a block, not a map. `return {};` and `x = {};` are maps.

([language.md](language.md#maps))

```janitor
counts = {size: 10};
print(counts.size());
print(counts["size"]);
```

```output
1
10
```

<a id="two-imports"></a>

## The two forms of import

`import collections;` loads a module that the host registered under that name. `import "crm" as crm;` asks the host to
resolve the name, which is how host-specific modules are often loaded. The two do not find each other's modules:
`import "collections" as c;` fails even when `import collections;` works. Imports are only allowed at the top level.
([modules.md](modules.md#import))

<a id="indexof"></a>

## indexOf without a start index

`text.indexOf(part)` has exactly one argument. A second argument, like JavaScript's or Java's `fromIndex`, is an error
("requires exactly 1 arguments"). To search from a position, search in a slice and add the offset.
([builtins.md](builtins.md#type-string))

```janitor
text = "banana";
start = 2;
print(start + text[start:].indexOf("a"));
```

```output
3
```

```janitor error
print("banana".indexOf("a", 2));
```

```error
requires exactly 1 arguments
```

Lists have no `indexOf` at all; use `contains` or a loop.

<a id="not-in"></a>

## not in, not, and, or

Janitor has `x in container` and `x not in container`, like Python, for lists, sets, map keys and substrings. `not`,
`and` and `or` replace `!`, `&&` and `||`, which still work but are deprecated and will be removed in Janitor 1.0.0;
`!=` and `!~` stay. Two details differ from both JavaScript and Python
([language.md](language.md#logic)):

- `not` (and `!`) bind weaker than `==`: `not a == b` is `not (a == b)`, and so is `!a == b`, which in JavaScript
  would be `(!a) == b`.
- `a and b` returns a boolean, not one of its operands. `a or b` does return an operand, so `value or "default"` works.

```janitor
print("b" not in ["a", "c"]);
print(not 1 == 2);
print("x" and "y");
print(null or "default");
```

```output
true
true
true
default
```

<a id="top-level-return"></a>

## return at the top level

`return` is allowed outside of functions. At the top level it ends the whole script, and its value becomes the script's
result, which the host uses. Without a `return`, the result is the value of the last top-level expression statement.
([index.md](index.md#script-result))

```janitor
order = {total: 0};
if (order.total == 0) {
    return "empty order";
}
return "total: " + order.total;
```

```result
empty order
```

<a id="no-interpolation"></a>

## No string interpolation

Neither `` `${x}` `` (JavaScript; backticks are a syntax error) nor `f"{x}"` (Python) exist, and `"${x}"` is a plain
string. Use `+`, `"%s".format(x)` or `"${x}".expand({x: x})`. ([language.md](language.md#strings))

<a id="null-returning-methods"></a>

## Methods that return null

`list.add`, `addAll`, `put`, `remove`, `sort` and `clear` change the list and return `null`, so they cannot be
chained, and `sorted = items.sort();` gives `null`. ([builtins.md](builtins.md#type-list))

```janitor
items = [3, 1, 2];
result = items.sort();
print(result);
print(items);
```

```output
null
[1, 2, 3]
```

<a id="names-of-methods"></a>

## Names of common methods

| You might write | In Janitor |
| --- | --- |
| `s.length`, `len(s)` | `s.length()` (a string) |
| `list.length`, `len(list)`, `list.size` | `list.size()` |
| `list.push(x)`, `list.append(x)` | `list.add(x)` |
| `list.indexOf(x)`, `list.includes(x)` | `list.contains(x)` or `x in list` |
| `list.reverse()` | `list[::-1]` (a new list) |
| `list.forEach(f)` | `for (x in list) { ... }` |
| `s.strip()` | `s.trim()` |
| `str(x)`, `String(x)`, `x.toString()` | `"" + x` |
| `int(s)`, `parseInt(s)` | `s.toInt()` |
| `JSON.parse(text)`, `json.loads(text)` | `{}.parseJson(text)` or `[].parseJson(text)` |
| `JSON.stringify(x)`, `json.dumps(x)` | `x.toJson()` (lists and maps) |
| `typeof x`, `type(x)` | `x.class` (a string like `"int"`) |
| `x === y`, `x is y` | `x == y` |
| `None`, `undefined`, `True`, `False` | `null`, `true`, `false` |

Without parentheses, a method is not called: `"abc".length` is the method itself, not 3. Calling a method that does not
exist is a `NameException`. ([builtins.md](builtins.md))

<a id="no-hoisting"></a>

## Functions are not hoisted

A `function` statement defines the function when it runs. Calling it earlier in the script is an error, unlike in
JavaScript. ([language.md](language.md#function-declarations))

```janitor error
print(square(3));
function square(x) {
    return x * x;
}
```

```error
invalid callable: square
```

Also note:

- Named arguments are bound by position, not by name: `f(b = 1, a = 2)` passes `1` to the first parameter. Write
  arguments in the order of the parameters, and use `name = value` only for parameters with default values.
  ([language.md](language.md#function-declarations))
- Lambdas use `->`, not `=>`, and there is no `lambda` keyword and no `function (x) { ... }` expression.
- A lambda cannot be called where it is written, `((x) -> x)(1)`; store it in a variable first.
- Calling a function or method with too many arguments is an error, as in Python and Java (JavaScript ignores them).
  A function that should accept more declares `*rest` or `**options`.

<a id="syntax-differences"></a>

## Syntax that does not exist

- Declarations: `var`, `let`, `const`. Just assign.
- `for (i = 0; i < n; i++)`: use `for (i from 0 to n - 1)`. The range includes both ends and only counts upwards.
- `for (x of list)`, `for x in list:`: write `for (x in list) { ... }`.
- `switch`, `case`, `elif`: use `if` / `else if`.
- `if (x) doSomething();` without braces: braces are required.
- `x ** 2`, `x ??= y`, `x?.[0]`, `...spread`, destructuring `[a, b] = list`.
- Comprehensions like `[x * 2 for x in xs]`: use `xs.map(x -> x * 2)` and `filter`.
- Classes: there are no classes in scripts; use maps and functions, or objects that the host provides.
- `#` comments: use `//` and `/* */`.
- Set literals `{1, 2}`: use `[1, 2].toSet()`.

<a id="types-and-comparisons"></a>

## Types and comparisons

- `7 / 2` is `3`: dividing integers gives an integer. Write `7 / 2.0` for `3.5`.
- `"1" == 1` is `false`, and `1 < "2"` is an error: there is no implicit conversion except for `+` with a string.
- `[1] + [2]` is an error; use `addAll`.
- Empty lists, maps and strings are falsy (as in Python, unlike JavaScript); `"0"` is truthy, `0` is falsy.
- Strings cannot be iterated with `for`; use `text.split("")`.
- `text ~ "pattern"` is a wildcard match (`*`, `?`) that ignores case, not a regular expression and not a substring test:
  `"abc" ~ "b"` is `false`; write `"abc" ~ "*b*"` or `"b" in "abc"`. For regular expressions, use `text ~ re/.../`.
  ([language.md](language.md#matching))

```janitor
print(7 / 2);
print(7 / 2.0);
print("1" == 1);
print([] ? "truthy" : "falsy");
```

```output
3
3.5
false
falsy
```

<a id="statements-in-blocks"></a>

## Expression statements in blocks

At the top level, a lone expression is allowed and sets the script's result. Inside a block, a statement must be a call,
an assignment or `++`/`--`; an expression that has no effect, like `x;`, `list[0];` or `x == 1;` (a typo for `x = 1;`),
is a compile error. ([language.md](language.md#expression-statements))

```janitor error
count = 0;
for (n in [1, 2, 3]) {
    count == count + n;
}
```

```error
this expression has no effect
```

<a id="errors"></a>

## Errors

- `catch` always needs a name in parentheses: `catch (e) { ... }`. There is only one catch clause, and it catches all
  runtime errors.
- `throw` accepts any value. In `catch`, the value is `e.value`, not `e` itself as in JavaScript; keys of a thrown map
  can also be read directly, as `e.code`. `e.message` is the text without the traceback, `e.traceback` the whole text.
  ([language.md](language.md#throw))
- `assert(condition, "message")` with a message fails; use `assert(condition)` or `if (...) { throw "message"; }`.
  ([builtins.md](builtins.md#global-assert))

<a id="dates"></a>

## Dates and durations

- Dates are literals: `@2024-03-05`, `@2024-03-05-14:30`, `@today`, `@now`. Month, day and hour need two digits, and
  the date and time must exist (`@2024-02-30` is a compile error).
- Minutes are `@15mi`, months are `@3mo`. `@15m` is not a duration.
- `date.year` is a property, `datetime.year()` a method.

([language.md](language.md#date-literals))

<a id="modules-are-optional"></a>

## Modules are optional

Every module, including the standard ones like `files`, `httpclient` and `collections`, is only available if the host
registers it. A script that works in one host can fail with `Module not found` in another. Prefer the built-in types and
functions, and check the host's documentation for its modules. ([modules.md](modules.md#availability))

<a id="output"></a>

## Output

`print` belongs to the host. With several arguments, the separator depends on the host (a space in some, nothing in
others), so build one string: `print("a: " + a)` instead of `print("a:", a)`. ([builtins.md](builtins.md#global-print))
