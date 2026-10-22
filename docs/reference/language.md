# Syntax and semantics

This part describes the language itself. The methods of the built-in types are in [builtins.md](builtins.md), the
`import` statement and the modules in [modules.md](modules.md).

<a id="lexical-structure"></a>

## Lexical structure

<a id="comments"></a>

### Comments

```janitor
// a line comment
/* a block comment,
   which can span lines */
print("comments are ignored"); // also at the end of a line
```

```output
comments are ignored
```

`#` does not start a comment. It is only allowed on the very first line of a script, as a Unix "shebang" line
(`#!/usr/bin/env janitor`), which is then ignored.

<a id="statements"></a>

### Statements and semicolons

A statement ends with a semicolon or at the end of its line. A line break ends a statement only where the statement
would be complete; an expression that is obviously unfinished (open brackets, a trailing operator) continues on the next
line. Using semicolons everywhere is recommended and is the style of this reference.

```janitor
a = 1
b = [1, 2,
     3]
c = a +
    b.size()
print(c)
```

```output
4
```

Blocks are written with braces `{ ... }`. Braces are required after `if`, `else`, `for`, `while`, `do`, `try`, `catch`,
`finally` and function heads, even for a single statement. Indentation has no meaning.

<a id="identifiers"></a>

### Identifiers and keywords

Identifiers consist of letters, digits, `_` and `$`, and do not start with a digit. They are case-sensitive.

Reserved words: `function`, `return`, `if`, `then`, `else`, `for`, `while`, `do`, `break`, `continue`, `try`, `catch`,
`finally`, `throw`, `import`, `true`, `false`, `null`, `and`, `or`, `not`. The words `from`, `to` and `in` have a
special meaning in some places, but can also be used as names.

Janitor does **not** have the keywords `var`, `let`, `const`, `class`, `new`, `this`, `switch`, `case`, `typeof`,
`instanceof`, `async`, `await`, `yield`, `lambda`, `def`, `elif`, `None`, `True`, `False` or `undefined`.

<a id="literals"></a>

## Literals

<a id="json"></a>

### JSON is valid Janitor

Every JSON value can be written as a Janitor literal: `null`, `true`, `false`, numbers, strings, arrays (lists) and
objects (maps), with all escape sequences of JSON, including `\u00e4` and `\/`. Janitor also allows things that
JSON does not: map keys without quotes, single-quoted strings, trailing commas, comments.

```janitor
order = {
    "id": 17,
    "customer": {"name": "Ada", "vip": true},
    "items": [{"sku": "A-1", "qty": 2}, {"sku": "B-7", "qty": 1}],
    "note": null
};
print(order.customer.name);
print(order.items[1].sku);
```

```output
Ada
B-7
```

<a id="null-and-booleans"></a>

### null and booleans

`null` is the absence of a value; `true` and `false` are the booleans. Their classes are `null` and `bool`.

<a id="numbers"></a>

### Numbers

There are two number types: `int` (64-bit integer) and `float` (64-bit floating point).

```janitor
print(42);
print(-7);
print(1.5);
print(.5);
print(1.5e3);
print((42).class + " " + (1.5).class);
```

```output
42
-7
1.5
0.5
1500.0
int float
```

Integer literals can be written in decimal, hexadecimal (`0x`) or binary (`0b`), and `_` can separate digits in all
of them, also in floats. Hexadecimal and binary literals are bit patterns of up to 64 bits, as in Java, so
`0xFFFF_FFFF_FFFF_FFFF` is `-1`.

```janitor
print(1_000_000);
print(0x1F);
print(0xff_ff);
print(0b1010);
print(-0x10);
print(0xFFFF_FFFF_FFFF_FFFF);
print(1_000.25);
```

```output
1000000
31
65535
10
-16
-1
1000.25
```

These forms are **not** valid:

- Octal literals: a leading zero, as in `017` or `00`, is a compile error, because in Java `010` means 8. Write `17`.
- Suffixes for integers like Java's `10L`: all integers are 64 bits wide anyway. (Floats accept the suffixes `f` and
  `d`, as in `2.5f`, which change nothing.)
- Decimal literals above `9223372036854775807`, and hexadecimal or binary literals with more than 64 bits, are compile
  errors.

```janitor error
x = 017;
```

```error
octal literals are not supported
```

A method call on an integer literal needs parentheses: `(42).class`; `42.class` is a syntax error.

<a id="strings"></a>

### Strings

Strings are written in single or double quotes; both mean the same. A string is a sequence of characters and cannot be
changed; string methods return new strings.

```janitor
print('single "quotes" inside');
print("double 'quotes' inside");
print("tab:\tend");
print("quote: \" backslash: \\");
```

```output
single "quotes" inside
double 'quotes' inside
tab:	end
quote: " backslash: \
```

Escape sequences are those of Java and JSON: `\t`, `\n`, `\r`, `\b`, `\f`, `\"`, `\'`, `\\`,
octal escapes like `\101`, unicode escapes (a backslash, `u` and four hex digits, like `\u00e4`), and `\/` for a
slash, which JSON allows. A backslash before any other character is a syntax error. Strings can also contain any
Unicode character directly.

```janitor
print("Grüße, äöü €");
print("\u00e4" == "ä");
print("\u20ac".length());
```

```output
Grüße, äöü €
true
1
```

```janitor
print("https:\/\/example.com");
```

```output
https://example.com
```

Triple quotes (`'''...'''` or `"""..."""`) make strings that can span lines and contain quotes:

```janitor
text = """first line
second line with "quotes"
""";
print(text.splitLines().size());
```

```output
2
```

There is **no string interpolation**: `"${x}"` is just these characters. Use `+`, `format` or `expand`:

```janitor
name = "Ada";
count = 3;
print("Hello, " + name + "! You have " + count + " messages.");
print("Hello, %s! You have %d messages.".format(name, count));
print("Hello, ${who}!".expand({who: name}));
print("${name}");
```

```output
Hello, Ada! You have 3 messages.
Hello, Ada! You have 3 messages.
Hello, Ada!
${name}
```

<a id="lists"></a>

### Lists

A list is an ordered, changeable sequence of values of any type, written in square brackets. A trailing comma is allowed.

```janitor
numbers = [3, 1, 2,];
mixed = [1, "two", [3], {four: 4}, null];
numbers.add(4);
print(numbers);
print(numbers.size() + mixed.size());
```

```output
[3, 1, 2, 4]
9
```

<a id="maps"></a>

### Maps

A map stores values under keys, written in braces. A key in a literal is either an identifier, which is used as a
string key, or a string literal. Numbers and other expressions cannot be keys in a literal, but any value can be a key
when you assign with brackets; `1` and `"1"` are different keys.

Maps do **not** keep the order in which keys were added, unlike JavaScript objects and Python dicts, and this is by
design: `keys()`, `values()`, `for` loops, `print` and `toJson()` show the entries in an unspecified order, and `keys()`
may even list them in a different order than a `for` loop. Sort the keys if the order matters.

```janitor
person = {name: "Ada", "year of birth": 1815};
person.city = "London";
person["languages"] = ["English"];
print(person.name);
print(person["year of birth"]);
keys = person.keys();
keys.sort();
print(keys);
```

```output
Ada
1815
[city, languages, name, year of birth]
```

The official way to read and write entries is `map["key"]`; a missing key gives `null`. `map.key` is a shorthand for
string keys. Methods take precedence over the shorthand, by design, so that a map's methods always work, whatever keys it
contains: for keys that are also names of map methods, such as `size`, `keys` or `get`, and for `class`, `map.size` is
the method. Use `map["size"]` for such keys, and generally for data whose keys you do not know in advance.

```janitor
m = {size: 5};
print(m["size"]);
print(m.size());
print(m.missing);
```

```output
5
1
null
```

Since a map literal uses braces, Janitor has **no set literal**: `{1, 2}` is a syntax error. Use
`collections.Set(...)` or `list.toSet()` (see [builtins.md](builtins.md#type-set)).

<a id="date-literals"></a>

### Dates, datetimes and durations

Janitor has literals for points in time, which start with `@`:

| Literal | Type | Meaning |
| --- | --- | --- |
| `@2024-03-05` | `date` | a calendar date |
| `@2024-03-05-14:30`, `@2024-03-05-14:30:15` | `datetime` | a date with a time, without time zone |
| `@today` | `date` | the current date, when the expression is evaluated |
| `@now` | `datetime` | the current date and time, when the expression is evaluated |

Month, day and hour must have two digits: `@2024-3-5` and `@2024-03-05-9:30` are errors, and so is the ISO form
`@2024-03-05T14:30`. A literal must name a date and time that exist: `@2024-02-30`, `@2023-02-29`, `@2024-01-01-24:00`
and `@0000-01-01` are compile errors. Years range from 0001 to 9999; there are no negative years. (`parseDate` and
`parseDateTime`, which read text at runtime, are lenient instead: they turn the 30th of February into the last day of
February.)

```janitor error
deadline = @2024-02-30;
```

```error
invalid date literal: @2024-02-30
```

Durations are written as `@` followed by a whole number and a unit:

| Literal | Unit |
| --- | --- |
| `@30s` | seconds |
| `@15mi` | minutes (`mi`, not `m`) |
| `@2h` | hours |
| `@3d` | days |
| `@1w` | weeks |
| `@6mo` | months |
| `@1y` | years |

Dates and datetimes can be compared, and durations can be added to or subtracted from them; the difference of two dates
is a duration.

```janitor
start = @2024-01-31;
print(start + @1d);
print(start + @1mo);
print(@2024-03-05 - @2024-03-01);
print(@2024-01-01-10:00 + @90mi);
print(@2024-03-05 < @2024-03-06);
print(@2024-03-05.format("dd.MM.yyyy"));
```

```output
@2024-02-01
@2024-02-29
@4d
@2024-01-01-11:30:00
true
05.03.2024
```

There are three ways to turn a date into text, with different results: `print(date)` shows the literal form
(`@2024-03-05`), `"" + date` gives the ISO form (`2024-03-05`, or `2024-03-05T10:00` for a datetime), and
`date.format()` without a pattern uses a format that the host chooses. Pass a pattern, as in `format("dd.MM.yyyy")`, if
the exact text matters.

```janitor
day = @2024-03-05;
print(day);
print("" + day);
print("" + @2024-03-05-10:00);
```

```output
@2024-03-05
2024-03-05
2024-03-05T10:00
```

<a id="regex-literals"></a>

### Regular expressions

`re/pattern/` is a regular expression literal, in Java regex syntax. Backslashes are written once: `re/\d+/`.

```janitor
print(re/(\d+)-(\d+)/.extract("pages 10-20"));
print("2024-03-05".split(re/-/));
```

```output
10
[2024, 03, 05]
```

<a id="variables"></a>

## Variables and scopes

<a id="assignment"></a>

### Assignment creates variables

A variable comes into existence when a value is first assigned to it. There is no declaration.

```janitor
x = 1;
x = "now a string";
print(x);
```

```output
now a string
```

Besides `=`, there are `+=`, `-=`, `*=`, `/=` and `%=`, and `++` and `--` before or after a variable. `x++` returns the
old value, `++x` the new one. There is no `**`, `??=`, `&&=` or `||=`.

<a id="block-scope"></a>

### Block scope

Every block (`{ ... }`, including the blocks of `if`, `else`, `for`, `while`, `do`, `try`, `catch`, `finally` and
functions) has its own scope. **A variable that is first assigned inside a block exists only in that block.** After the
block, it is gone. This is different from Python, where such a variable stays visible, and from JavaScript's `var`.

```janitor error
if (true) {
    found = "yes";
}
print(found);
```

```error
name 'found' is not defined
```

To use a value after the block, assign the variable before the block:

```janitor
found = "no";
if (true) {
    found = "yes";
}
print(found);
```

```output
yes
```

The same applies to loops: the loop variable and everything first assigned in the loop body are gone after the loop.

```janitor
total = 0;
last = null;
for (n in [5, 6, 7]) {
    total += n;
    last = n;
}
print(total);
print(last);
```

```output
18
7
```

<a id="outer-variables"></a>

### Assigning to outer variables

Assigning to a name that already exists in an enclosing scope changes that variable; it does not create a new local one.
This also holds inside functions, for global variables: unlike in Python, there is no `global` or `nonlocal`, and none
is needed.

```janitor
counter = 0;
function increment() {
    counter = counter + 1;
}
increment();
increment();
print(counter);
```

```output
2
```

A variable that is first assigned inside a function is local to the function call:

```janitor error
function setup() {
    temporary = 1;
}
setup();
print(temporary);
```

```error
name 'temporary' is not defined
```

<a id="operators"></a>

## Operators

<a id="precedence"></a>

### Precedence

From the strongest binding to the weakest:

| Operators | Meaning |
| --- | --- |
| `a.b`, `a?.b`, `a(...)`, `a[...]`, `a++`, `a--` | member access, call, index, postfix increment |
| `-a`, `+a`, `++a`, `--a` | negation, unary plus, prefix increment |
| `*`, `/`, `%` | multiplication, division, remainder |
| `+`, `-` | addition, subtraction |
| `<`, `<=`, `>`, `>=` | comparison |
| `in`, `not in` | membership |
| `==`, `!=`, `<>`, `~=`, `~`, `!~` | equality, matching |
| `not a`, `!a` | logical not |
| `and`, `&&` | logical and |
| `or`, `\|\|` | logical or |
| `c ? a : b` | conditional |
| `x -> ...`, `if c then a else b` | lambda, conditional expression |

Note that `not` (and the deprecated `!`) bind **weaker** than `==`: `not a == b` means `not (a == b)`; in JavaScript,
`!a == b` would mean `(!a) == b`. Use parentheses. The symbol forms `&&`, `||`, `!` and `<>` are deprecated, see
[open questions](open-questions.md#deprecated-operators).
Unary `+` is the counterpart of `-`: `+x` is the number `x` unchanged. Unlike in JavaScript, it does not convert
strings to numbers; `+"3"` is an error, use `"3".toInt()`.

<a id="arithmetic"></a>

### Arithmetic

```janitor
print(7 / 2);
print(-7 / 2);
print(7 % 3);
print(-7 % 3);
print(7 / 2.0);
print(1 + 2.5);
print(10 - 2 * 3);
print(+7 - -2);
```

```output
3
-3
1
-1
3.5
3.5
4
9
```

- Dividing two integers gives an integer, rounded towards zero. If one operand is a float, the result is a float.
- `%` has the sign of the left operand.
- Integer overflow wraps around silently; dividing an integer by zero is an `ArithmeticException`.
- There is no power operator `**`.

`+` with a string on either side converts the other side to a string and concatenates. `*` with a string and an integer
repeats the string. Lists and maps cannot be added with `+`; use `list.addAll(other)`.

```janitor
print("a" + 1);
print(1 + "a");
print("ab" * 3);
```

```output
a1
1a
ababab
```

<a id="comparison"></a>

### Comparison and equality

`==` and `!=` compare values, not identities: lists and maps are equal if their contents are equal, and an
integer equals a float with the same value. Values of different kinds are not equal: `"1" == 1` is `false`. `~=`
compares strings ignoring case.

`<`, `<=`, `>` and `>=` work for numbers, strings, dates, datetimes and durations. Comparing values that cannot be
compared, like a number and a string, is an error. Comparisons cannot be chained: `1 < x < 3` is an error; write
`1 < x and x < 3`.

```janitor
print([1, 2] == [1, 2]);
print({a: 1} == {a: 1});
print(1 == 1.0);
print("1" == 1);
print("ABC" ~= "abc");
print("apple" < "banana");
```

```output
true
true
true
false
true
true
```

<a id="truthiness"></a>

### Truthiness

Conditions accept any value. These values count as false: `false`, `null`, `0`, `0.0`, the empty string, the empty list
and the empty map. Everything else is true.

```janitor
for (value in [false, null, 0, 0.0, "", [], {}, "0", [0], 1]) {
    if (value) {
        print("truthy");
    } else {
        print("falsy");
    }
}
```

```output
falsy
falsy
falsy
falsy
falsy
falsy
falsy
truthy
truthy
truthy
```

<a id="logic"></a>

### and, or, not

`and`, `or` and `not` are the logical operators. `and` and `or` only evaluate their right side when needed. The
symbols `&&`, `||` and `!` mean the same, but are **deprecated** and will be removed in Janitor 1.0.0 (see
[open questions](open-questions.md#deprecated-operators)); always write the words.

- `a or b` returns `a` itself if it is truthy, and `b` otherwise. This makes `value or default` the usual way to replace
  empty values (`null`, `""`, `0`, empty lists and maps) with a default.
- `a and b` always returns a boolean: `true` if both sides are truthy, `false` otherwise. Unlike in Python, it never
  returns one of its operands, so conditions like `x > 1 and y < 2` give the host a real boolean. To read a property of
  something that may be `null`, use `x?.name` instead of Python's `x and x.name`.

The two operators differ on purpose: `or` is for default values, `and` is for conditions.

```janitor
nickname = null;
print(nickname or "anonymous");
print("x" and "y");
print(not (1 > 2));
```

```output
anonymous
true
true
```

<a id="membership"></a>

### in and not in

`x in container` tests membership, `x not in container` its opposite:

| Container | `x in container` is true if |
| --- | --- |
| list | an element equals `x` |
| set | the set contains `x` |
| map | `x` is a **key** of the map (not a value) |
| string | `x` is a substring |

```janitor
print(2 in [1, 2, 3]);
print("b" in {a: 1, b: 2});
print(2 in {a: 1, b: 2});
print("ell" in "hello");
print("z" not in "hello");
```

```output
true
true
false
true
true
```

<a id="matching"></a>

### Pattern matching: ~ and !~

`text ~ pattern` matches a string against a wildcard pattern, as known from file names: `*` stands for any number of
characters (also none), `?` for exactly one character, and every other character stands for itself, also `.`, `(` or
`[`. The pattern must match the whole string, and upper and lower case are not distinguished. `!~` is the negation.
`"*"` matches every string, `""` only the empty string.

With a regular expression literal on the right, the whole string must match the regular expression, case-sensitively.

```janitor
print("report-2024.pdf" ~ "*.pdf");
print("Report-2024.PDF" ~ "report-*");
print("abc" ~ "a?c");
print("abc" ~ "a.c");
print("abc" ~ "*b*");
print("a1" ~ re/[a-z]\d/);
print("abc" !~ "x*");
```

```output
true
true
true
false
true
true
true
```

<a id="optional-chaining"></a>

### Optional member access: ?.

`a?.b` and `a?.b(...)` give `null` if `a` is `null`, instead of failing. Plain `a.b` on `null` is an error.

```janitor
customer = null;
print(customer?.name);
print(customer?.name?.toUpperCase());
```

```output
null
null
```

<a id="indexing"></a>

### Indexes and slices

Lists and strings are indexed from 0; negative indexes count from the end. A slice `[from:to]` contains the elements
from `from` up to, but not including, `to`; either bound can be left out, and a third part is a step. Reading a single
index outside of the list or string is an error, while slices are cut to the available range.

```janitor
letters = ["a", "b", "c", "d"];
print(letters[0]);
print(letters[-1]);
print(letters[1:3]);
print(letters[2:]);
print(letters[::-1]);
print("hello"[1:3]);
print(letters[10:]);
```

```output
a
d
[b, c]
[c, d]
[d, c, b, a]
el
[]
```

Assigning to an index changes a list or a map; assigning to a slice replaces that part of a list. Strings cannot be
changed.

```janitor
numbers = [1, 2, 3];
numbers[0] = 10;
numbers[1:2] = [20, 21];
print(numbers);
```

```output
[10, 20, 21, 3]
```

<a id="control-flow"></a>

## Control flow

<a id="if"></a>

### if and else

The condition is always in parentheses, and the branches are always blocks. `else if` chains conditions; there is no
`elif` and no `switch`.

```janitor
score = 72;
if (score >= 90) {
    print("excellent");
} else if (score >= 50) {
    print("passed");
} else {
    print("failed");
}
```

```output
passed
```

<a id="conditional-expressions"></a>

### Conditional expressions

There are two forms: `condition ? a : b`, and `if condition then a else b`. Without `else`, the second form gives
`null` when the condition is false. Python's `a if condition else b` does not exist.

```janitor
n = 3;
print(n % 2 == 0 ? "even" : "odd");
print(if n > 2 then "big" else "small");
print(if n > 5 then "huge");
```

```output
odd
big
null
```

<a id="for-in"></a>

### for (x in ...)

`for (name in collection) { ... }` runs the block once for each element of a list or set, or once for each key of a map.
The keys of a map come in no particular order. Strings cannot be iterated; use `text.split("")` to get a list of
characters. Do not add or remove elements of a list while looping over it; that is an error.

```janitor
prices = {apple: 1.2, pear: 0.8};
total = 0;
for (fruit in prices) {
    total += prices[fruit];
}
print(total);
for (c in "abc".split("")) {
    print(c);
}
```

```output
2.0
a
b
c
```

<a id="for-range"></a>

### for (i from a to b)

`for (name from first to last) { ... }` counts from `first` up to `last`, **including** `last`. It only counts upwards:
if `first` is greater than `last`, the block does not run at all. There is no C-style `for (i = 0; i < n; i++)`.

```janitor
for (i from 1 to 3) {
    print(i);
}
for (i from 3 to 1) {
    print("never printed");
}
```

```output
1
2
3
```

<a id="while"></a>

### while and do-while

```janitor
i = 0;
while (i < 3) {
    i++;
}
do {
    i--;
} while (i > 1);
print(i);
```

```output
1
```

<a id="break-continue"></a>

### break and continue

`break` leaves the innermost loop, `continue` starts its next iteration. There are no labels.

```janitor
for (n in [1, 2, 3, 4, 5]) {
    if (n == 2) {
        continue;
    }
    if (n == 4) {
        break;
    }
    print(n);
}
```

```output
1
3
```

<a id="return"></a>

### return

`return value;` or `return;` ends a function, or, at the top level, the whole script (see
[the result of a script](index.md#script-result)). Code after a top-level `return` does not run.

```janitor
items = [];
if (items.isEmpty()) {
    return "nothing to do";
}
print("this is not printed");
```

```result
nothing to do
```

<a id="expression-statements"></a>

### Expressions as statements

At the top level, any expression can stand alone as a statement; its value becomes the script's result.

Inside a block (of `if`, a loop, a function, ...), a statement must do something: it must be a call, an assignment, or
`++`/`--`. An expression whose value would just be dropped, such as `x;`, `list[0];` or `x == 1;`, is a compile error
("this expression has no effect"). This catches a common typo: `x == 1;` where `x = 1;` was meant.

```janitor
x = 0;
if (true) {
    x = 1;
    x++;
    print(x);
}
x == 2
```

```output
2
```

```result
true
```

```janitor error
x = 0;
if (true) {
    x == 1;
}
```

```error
this expression has no effect
```

Calls inside blocks do not change the script's result; only top-level expression statements and `return` do.

<a id="functions"></a>

## Functions

<a id="function-declarations"></a>

### Declaring functions

```janitor
function greet(name, greeting = "Hello") {
    return greeting + ", " + name + "!";
}
print(greet("Ada"));
print(greet("Grace", "Hi"));
print(greet("Linus", greeting = "Welcome"));
```

```output
Hello, Ada!
Hi, Grace!
Welcome, Linus!
```

- A function is defined when its `function` statement runs. It must be defined **before** it is called; there is no
  hoisting.
- A function without a `return`, or with a bare `return;`, returns `null`.
- Parameters can have default values (`greeting = "Hello"`). Parameters without a default come first.
- Too few and too many arguments are errors (`JanitorArgumentException`), as in Python and Java; JavaScript would
  ignore additional arguments. A function that should accept more declares `*rest` or `**options`, see below. This also
  holds for lambdas passed to methods: `list.map((a, b) -> a)` fails, because `map` passes one argument.
- Functions are values: they can be stored in variables, lists and maps, and passed to other functions.

**Named arguments** (`name = value` in a call) are not matched by name as in Python. Arguments are bound to the
parameters **in the order in which they are written**, named or not; the name only lets a parameter with a default value
be skipped. So write named arguments in the order of the parameters, after the positional ones, and use them only for
parameters with defaults (see [open questions](open-questions.md#named-arguments)):

```janitor
function range(from, to, step = 1) {
    return from + ".." + to + " by " + step;
}
print(range(1, 10, step = 2));
print(range(to = 10, from = 1));
```

```output
1..10 by 2
10..1 by 1
```

`*rest` collects the remaining arguments in a list, and `**options` collects the named arguments in a map. Named
arguments end up in both:

```janitor
function describe(first, *rest, **options) {
    return first + " " + rest + " " + options;
}
print(describe(1, 2, 3));
print(describe(1, 2, sep = "-"));
```

```output
1 [2, 3] {}
1 [2, -] {sep=-}
```

<a id="lambdas"></a>

### Lambdas

A lambda is an anonymous function, written with `->`. One parameter can be written without parentheses. The body is an
expression, or a block with `return`. JavaScript's `=>` and Python's `lambda` do not exist, and neither does
`function (...) { ... }` as an expression.

```janitor
double = x -> x * 2;
add = (a, b) -> a + b;
classify = n -> {
    if (n < 0) {
        return "negative";
    }
    return "not negative";
};
print(double(21));
print(add(1, 2));
print(classify(-5));
print([1, 2, 3].map(x -> x * x));
print([1, 2, 3, 4].filter(x -> x % 2 == 0));
```

```output
42
3
negative
[1, 4, 9]
[2, 4]
```

A lambda must be stored in a variable or passed as an argument before it can be called: `((a, b) -> a * b)(2, 3)` is a
syntax error.

<a id="closures"></a>

### Closures

Functions and lambdas can use the variables of the scope in which they were defined, also after that scope has ended.
They see the variable itself, not a copy of its value at the time of definition.

```janitor
function makeCounter() {
    count = 0;
    return () -> {
        count = count + 1;
        return count;
    };
}
next = makeCounter();
next();
next();
print(next());

x = 1;
showX = () -> x;
x = 2;
print(showX());
```

```output
3
2
```

<a id="methods-as-values"></a>

### Methods

Methods are called with a dot: `"abc".toUpperCase()`. Leaving out the parentheses does not call the method, but gives
the method itself as a value: `"abc".length` is a method, not the number 3.

<a id="errors"></a>

## Errors

<a id="error-kinds"></a>

### Kinds of errors

Errors are either **compile errors**, found before the script runs (syntax errors, a lone name as a statement in a
block, ...), or **runtime errors**. A compile error means that the script does not run at all; it cannot be caught. A
runtime error ends the script, unless a `try` block catches it. The error message contains a traceback with the
script's line and column.

These are the runtime errors of the language itself; modules and hosts can add their own:

| `e.type` | Cause |
| --- | --- |
| `JanitorNameException` | an undefined variable, or a member that the object does not have |
| `JanitorArgumentException` | wrong number or type of arguments for a function or method |
| `JanitorTypeException` | an operator that does not work for these types, e.g. `[1] + [2]` |
| `JanitorNotImplementedException` | values that cannot be compared, e.g. `1 < "2"` |
| `JanitorArithmeticException` | division by zero |
| `JanitorAssertionException` | a failed `assert(...)` |
| `JanitorScriptThrownException` | a value thrown with `throw` |
| `JanitorNativeException` | an error inside a built-in method or module, e.g. an index out of range |
| `JanitorInstructionLimitExceededException` | the script ran more instructions than the host allows; it cannot be caught |
| `JanitorTimeLimitExceededException` | the script ran longer than the host allows; it cannot be caught |
| `JanitorCancelledException` | the host cancelled the script; it cannot be caught |

<a id="try-catch"></a>

### try, catch and finally

```janitor
function parseAmount(text) {
    try {
        return text.toInt();
    } catch (e) {
        print("not a number: " + text);
        return 0;
    } finally {
        print("checked " + text);
    }
}
print(parseAmount("12") + parseAmount("x"));
```

```output
checked 12
not a number: x
checked x
12
```

- `catch (e)` catches every runtime error except the ones with which the host aborts the script, such as
  `JanitorInstructionLimitExceededException` or `JanitorCancelledException`; there are no typed catch clauses. The parentheses and the name are required.
- `finally` runs in every case, also after `return`, `break` and `continue`. Either `catch` or `finally` can be left out.
- The variable in `catch` is an error object with these properties:

  | Property | Value |
  | --- | --- |
  | `e.message` | the error text without the traceback, e.g. `name 'x' is not defined`; for `throw value`, the text of the value |
  | `e.type` | the kind of error, see the table above |
  | `e.value` | the value of `throw value`; `null` for all other errors |
  | `e.traceback` | the whole error text, with the script's lines and columns |

- Errors from modules can have more properties; for example, failed HTTP requests have `e.code`.

<a id="throw"></a>

### throw

`throw value;` raises an error with any value: a string, a number, a map, or an object. In `catch`, `e.value` is that
value, and properties that errors do not have themselves are read from it, so a thrown map's keys can be read as
`e.code`. The properties of errors (`message`, `type`, `traceback`) take precedence: a key `message` of a thrown map is
read as `e.value.message`. `throw e;` inside `catch` raises the caught error again, unchanged.

```janitor
function withdraw(balance, amount) {
    if (amount > balance) {
        throw {code: "FUNDS", missing: amount - balance};
    }
    return balance - amount;
}
try {
    withdraw(10, 50);
} catch (e) {
    print(e.type);
    print(e.code + ": " + e.missing + " missing");
}
try {
    throw "insufficient funds";
} catch (e) {
    print(e.message);
    print(e.value == "insufficient funds");
}
```

```output
JanitorScriptThrownException
FUNDS: 40 missing
insufficient funds
true
```

<a id="assert"></a>

### assert

`assert(condition)` and `assert(condition, message)` raise a `JanitorAssertionException` if the condition is not
truthy. See [builtins.md](builtins.md#global-assert).
