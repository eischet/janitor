# Built-in functions and types

The tables of members in this file are generated from the interpreter's dispatch tables (see
[About this reference](index.md#about)), so they list exactly what exists. The text around them explains how the types behave.

How to read the tables: a **method** is called with parentheses, `value.name(arguments)`; a **property** is read
without them, `value.name`. Arguments in the call form are positional. Unless a description says otherwise, methods do
not change the value they are called on.

<a id="globals"></a>

## Global functions

These names are defined in every script:

<!-- generated:begin builtins/globals -->
`__builtin__`, `assert`, `dir`, `help`, `print`
<!-- generated:end -->

Hosts usually add more globals; see [hosting.md](hosting.md#host-provided-globals).

<a id="global-print"></a>

### `print(values...)`

Hands the values to the host as output and returns `null`. What happens with the output depends on the host: it may go
to a console, a log, or nowhere. With several arguments, the separator also depends on the host; pass a single string
to be sure of the result.

<a id="global-assert"></a>

### `assert(condition, message)`

Raises a `JanitorAssertionException` if `condition` is not truthy; otherwise returns `condition`. The message is
optional and must be a string; it becomes part of the error text: "assertion failed: message", or "assertion failed!"
without one.

```janitor
total = 3 + 4;
assert(total == 7);
assert(total > 0, "the total must be positive");
try {
    assert(total > 10, "the total is too small");
} catch (e) {
    print(e.message);
}
```

```output
assertion failed: the total is too small
```

<a id="global-help"></a>

### `help(value)`

Returns the help text of a value, a method or a type as a string, or `null` if there is none. The help texts are the
descriptions in the tables below.

```janitor
print(help("text".toUpperCase));
```

```output
toUpperCase(): Returns the string in upper case.
```

<a id="global-dir"></a>

### `dir(value)`

Returns a list with the names of the methods and properties of a value, sorted by name. For a map, these are the map's
methods, not its keys; use `map.keys()` for those. `true`, `false` and `null` have no members, and some module objects
do not list theirs (see [open questions](open-questions.md#dir)). The list contains internal names starting with `__`,
which scripts should not use.

```janitor
print(dir(1));
```

```output
[apply, class, epoch, int]
```

<a id="global-__builtin__"></a>

### `__builtin__`

The scope that holds the global functions. It is an implementation detail; scripts should not use it.

<a id="common-members"></a>

## Members of all values

Values of the built-in types have these members:

<!-- generated:begin builtins/common -->
| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `class` | property, string, read-only | The name of the value's type, e.g. "string", "int" or "list". |
<!-- generated:end -->

`apply(map)` is meant for objects with writable properties, such as objects from modules; see
[module-types.md](module-types.md).

<a id="type-null"></a>

## null

`null` has no members. Reading a member of `null` is an error; use `?.` to avoid it (see
[optional member access](language.md#optional-chaining)). `null.class` is `"null"`.

<a id="type-bool"></a>

## bool

`true` and `false`. They have no members besides `class`, which is `"bool"`.

<a id="type-string"></a>

## string

Strings cannot be changed; every method returns a new value. Indexes start at 0. Most methods require their arguments
to be strings, so convert numbers first: `"Total: " + 3` works, but `"a,b".split(1)` does not.

```janitor
s = "  Hello, World  ";
t = s.trim();
print(t.toUpperCase());
print(t.length());
print(t.indexOf("o"));
print(t.replace("World", "Janitor"));
print(t.split(", "));
print(t.startsWith("Hello") and t.contains("World"));
print("42".toInt() + 1);
print("a-b-c".split("-").join("+"));
```

```output
HELLO, WORLD
12
4
Hello, Janitor
[Hello, World]
true
43
a+b+c
```

Note that `replace` treats its first argument literally, while `replaceAll` and `replaceFirst` treat it as a regular
expression, as in Java:

```janitor
print("1.2.3".replace(".", "-"));
print("1.2.3".replaceAll(".", "-"));
print("1.2.3".replaceAll("\\.", "-"));
```

```output
1-2-3
-----
1-2-3
```

<!-- generated:begin builtins/string -->
| Member | Kind | Description |
| --- | --- | --- |
| `contains(text)` | method | Returns true if the string contains the text. Same as the operator: text in string. |
| `containsIgnoreCase(text)` | method | Returns true if the string contains the text, ignoring upper and lower case. |
| `count(text)` | method | Returns how often the text occurs in the string, without overlaps. |
| `cutFilename(maxLength)` | method | Takes the file name part of a path and shortens it to maxLength characters, keeping the extension. |
| `decodeBase64()` | method | Decodes the Base64 string and returns the bytes as a binary value. |
| `empty()` | method | Returns true if the string has no characters. |
| `encode(charset)` | method | Encodes the string with the named character set, UTF-8 if none is given, and returns the bytes as a binary value. |
| `endsWith(text)` | method | Returns true if the string ends with the text. An empty text never matches. |
| `expand(map)` | method | Replaces placeholders like ${name} or ${a.b} by values from the map. |
| `format(args...)` | method | Uses the string as a Java format string (%s, %d, %.2f, ...) and fills in the arguments. |
| `get(index)` | method | Same as string[index]; get(from, to) and get(from, to, step) are the same as string[from:to] and string[from:to:step]. |
| `indexOf(text)` | method | Returns the index of the first occurrence of the text, or -1. There is no start index: indexOf(text, from) is an error. |
| `int()` | method | Same as toInt(). |
| `isNumeric()` | method | Returns true if the string is not empty and consists of the digits 0-9 only. |
| `lastIndexOf(text)` | method | Returns the index of the last occurrence of the text, or -1. |
| `length()` | method | Returns the number of characters in the string. |
| `parseDate(pattern)` | method | Parses the string as a date, using a Java DateTimeFormatter pattern like "dd.MM.yyyy". Leniently: a day that does not exist in the month becomes the last day of the month. Text that does not match the pattern gives null and a warning to the host. |
| `parseDateTime(pattern)` | method | Parses the string as a datetime, using a Java DateTimeFormatter pattern like "yyyy-MM-dd HH:mm". Leniently, like parseDate; text that does not match the pattern gives null and a warning to the host. |
| `removeLeadingZeros()` | method | Returns the string without the zeros at its start, e.g. "007" becomes "7". |
| `replace(text, replacement)` | method | Replaces every occurrence of the literal text by the replacement. |
| `replaceAll(regex, replacement)` | method | Replaces every match of the regular expression, given as a string, by the replacement. |
| `replaceFirst(regex, replacement)` | method | Replaces the first match of the regular expression, given as a string, by the replacement. |
| `split(separator)` | method | Splits the string into a list. The separator is a literal string or a regex literal; an empty string splits into single characters. |
| `splitLines()` | method | Splits the string into a list of lines; accepts both Unix and Windows line breaks. |
| `startsWith(text)` | method | Returns true if the string starts with the text. An empty text never matches. |
| `startsWithNumbers()` | method | Returns true if the string starts with a digit. |
| `substring(from, to)` | method | Returns the characters from index from (inclusive) to index to (exclusive); without to, up to the end. |
| `toBinaryUtf8()` | method | Encodes the string as UTF-8 and returns the bytes as a binary value. |
| `toCamelCase()` | method | Converts words separated by spaces, underscores or other characters to camel case, e.g. "FOO_BAR" becomes "fooBar". |
| `toConstantCase()` | method | Converts words separated by spaces or other characters to an upper case constant name, e.g. "foo bar" becomes "FOO_BAR". |
| `toFloat()` | method | Parses the string as a floating point number; a blank string gives 0.0, any other non-number is an error. |
| `toInt()` | method | Parses the string as a decimal integer; a blank string gives 0, any other non-number is an error. |
| `toLowerCase()` | method | Returns the string in lower case. |
| `toUpperCase()` | method | Returns the string in upper case. |
| `trim()` | method | Returns the string with leading and trailing whitespace removed. |
| `urlDecode()` | method | Decodes a string that was encoded for a URL query, in UTF-8; + becomes a space. |
| `urlEncode()` | method | Encodes the string for a URL query, in UTF-8; a space becomes +. |
<!-- generated:end -->

<a id="type-int"></a>

## int

Whole numbers from -2<sup>63</sup> to 2<sup>63</sup>-1. Arithmetic is described in
[language.md](language.md#arithmetic). To convert a string, use `"42".toInt()`; to format a number, use
`"%05d".format(n)` or `"%.2f".format(x)`.

<!-- generated:begin builtins/int -->
| Member | Kind | Description |
| --- | --- | --- |
| `epoch` | property, read-only | Reads the number as seconds since 1970-01-01 00:00 UTC and returns that point in time as a datetime, in the host's time zone. |
| `int` | property, int, read-only | The number itself. |
<!-- generated:end -->

<a id="type-float"></a>

## float

Floating point numbers (Java `double`). Like everywhere, they are not exact: `0.1 + 0.2` is `0.30000000000000004`.

```janitor
x = 7.9;
print(x.int);
print((-7.9).int);
print(2.0 / 4);
```

```output
7
-7
0.5
```

`"%.2f".format(x)` rounds to two decimals, but uses the default locale of the Java VM, so the decimal separator may be a
comma (`0,67`) or a point (`0.67`).

<!-- generated:begin builtins/float -->
| Member | Kind | Description |
| --- | --- | --- |
| `int` | property, int, read-only | The number as an integer, with the fraction cut off (towards zero). |
<!-- generated:end -->

<a id="type-list"></a>

## list

Lists are changeable. Methods that change a list in place (`add`, `addAll`, `put`, `remove`, `sort`, `clear`) return
`null`, not the list, so they cannot be chained: write `items.sort();` and then use `items`.

```janitor
items = [3, 1, 2];
items.add(4);
items.sort();
print(items);
print(items.map(x -> x * 10).filter(x -> x > 15));
print(items.contains(2));
print(items.join(", "));
print(items.size());
copy = items.toList();
copy.clear();
print(items.isEmpty());
```

```output
[1, 2, 3, 4]
[20, 30, 40]
true
1, 2, 3, 4
4
false
```

There are no methods `push`, `pop`, `length`, `indexOf`, `reduce`, `forEach` or `reverse`; use `add`, `size()`, a `for`
loop, and the slice `list[::-1]`.

<!-- generated:begin builtins/list -->
| Member | Kind | Description |
| --- | --- | --- |
| `add(x)` | method | Appends x; add(index, x) inserts x at the index instead. Returns null. |
| `addAll(other)` | method | Appends all elements of the other list; returns null. |
| `clear()` | method | Removes all elements; returns null. |
| `contains(x)` | method | Returns true if an element is equal to x. Same as the operator: x in list. |
| `count(x)` | method | Returns how many elements are equal to x. |
| `filter(function)` | method | Returns a new list with the elements for which the function returns a truthy value. |
| `get(index)` | method | Same as list[index]; get(from, to) and get(from, to, step) return a read-only slice like list[from:to:step]. |
| `isEmpty()` | method | Returns true if the list has no elements. |
| `join(separator)` | method | Returns the string forms of the elements, joined by the separator; a single space if none is given. |
| `map(function)` | method | Returns a new list with the results of calling the function on each element. |
| `parseJson(json)` | method | Parses a JSON array and appends its elements to this list; returns this list. Usage: [].parseJson(text). |
| `put(index, x)` | method | Replaces the element at the index, like list[index] = x; returns null. |
| `randomSublist(n)` | method | Returns a new list with n elements of this list, picked at random. |
| `remove(x)` | method | Removes every element that is equal to x; returns null. |
| `removeAll(other)` | method | Removes every element that is equal to an element of the other list; returns null. |
| `size()` | method | Returns the number of elements. |
| `sort()` | method | Sorts the list in place, in natural order, and returns null; the elements must be comparable with each other. |
| `toJson()` | method | Returns the list as a JSON string. |
| `toList()` | method | Returns a shallow copy of this list. |
| `toSet()` | method | Returns a new set with the elements of this list. |
<!-- generated:end -->

<a id="type-map"></a>

## map

Maps are changeable. Keys can be any values. Read and write entries with `map["key"]`; for string keys, `map.key` is a
shorthand, except for keys that are names of map methods, because methods take precedence (see
[language.md](language.md#maps)). Reading a missing key gives `null`. The order
of the entries is not specified. `map.class` is always `"map"`, even if the map has a key `"class"`.

```janitor
stock = {apples: 3};
stock.pears = 5;
stock["plums"] = 0;
print(stock.get("apples") + stock.pears);
print("plums" in stock);
print(stock.containsValue(5));
stock.remove("plums");
print(stock.size());
print({a: [1, 2]}.toJson());
print({}.parseJson("{\"x\": 1}").x);
```

```output
8
true
true
2
{"a":[1,2]}
1
```

<!-- generated:begin builtins/map -->
| Member | Kind | Description |
| --- | --- | --- |
| `clear()` | method | Removes all entries. |
| `containsKey(key)` | method | Returns true if the map has an entry for the key. Same as the operator: key in map. |
| `containsValue(value)` | method | Returns true if an entry has a value equal to the argument. |
| `copy()` | method | Returns a shallow copy of the map. |
| `get(key)` | method | Returns the value for the key, or null if there is none. |
| `isEmpty()` | method | Returns true if the map has no entries. |
| `keys()` | method | Returns a list of the keys. |
| `parseJson(json)` | method | Parses a JSON object and puts its entries into this map; returns this map. Usage: {}.parseJson(text). |
| `put(key, value)` | method | Stores the value under the key, like map[key] = value; returns null. |
| `remove(key)` | method | Removes the entry for the key and returns its value, or null if there was none. |
| `size()` | method | Returns the number of entries. |
| `toJson()` | method | Returns the map as a JSON string. |
| `values()` | method | Returns a list of the values. |
<!-- generated:end -->

<a id="type-set"></a>

## set

A set holds each value at most once, in no particular order. There is no set literal; create sets with
`list.toSet()`, or with the [`collections`](modules.md#module-collections) module if the host registers it.

```janitor
tags = ["a", "b", "a"].toSet();
tags.add("c");
print(tags.size());
print("a" in tags);
```

```output
3
true
```

<!-- generated:begin builtins/set -->
| Member | Kind | Description |
| --- | --- | --- |
| `add(x)` | method | Adds x; returns true if the set did not contain it before. |
| `contains(x)` | method | Returns true if the set contains x. Same as the operator: x in set. |
| `isEmpty()` | method | Returns true if the set has no elements. |
| `remove(x)` | method | Removes x; returns true if the set contained it. |
| `size()` | method | Returns the number of elements. |
| `toList()` | method | Returns a new list with the elements, in no particular order. |
| `toSet()` | method | Returns a copy of the set. |
<!-- generated:end -->

<a id="type-binary"></a>

## binary

A sequence of bytes, e.g. the content of a file or an HTTP response. Create one from a string with `toBinaryUtf8()`,
`encode(charset)` or `decodeBase64()`.

```janitor
data = "hello".toBinaryUtf8();
print(data.size());
print(data.encodeBase64());
print(data.decode());
```

```output
5
aGVsbG8=
hello
```

<!-- generated:begin builtins/binary -->
| Member | Kind | Description |
| --- | --- | --- |
| `decode(charset)` | method | Decodes the bytes to a string with the named character set, UTF-8 if none is given. |
| `encodeBase64()` | method | Returns the bytes as a Base64 string. |
| `length` | property, int, read-only | The number of bytes, same as size(). |
| `sha256()` | method | Returns the SHA-256 hash of the bytes as a lower case hex string, without leading zeros; an empty string for no bytes. |
| `size()` | method | Returns the number of bytes. |
| `string` | property, string, read-only | The bytes decoded with the JVM's default character set; prefer decode(). |
| `toString(charset)` | method | Same as decode(charset). |
<!-- generated:end -->

<a id="type-regex"></a>

## regex

Regular expressions, written as literals `re/.../` (see [language.md](language.md#regex-literals)). The syntax is that
of Java's `java.util.regex`. To test whether a whole string matches, use the operator `text ~ re/.../`.

```janitor
print(re/(\d+)/.extractAll("a1 b22 c333"));
print(re/\s+/.replaceAll("a  b   c", " "));
print(re/(\w+)@(\w+)/.replaceFirst("mail ada@example now", "$2 at $1"));
```

```output
[1, 22, 333]
a b c
mail example at ada now
```

<!-- generated:begin builtins/regex -->
| Member | Kind | Description |
| --- | --- | --- |
| `extract(text)` | method | Finds the first match in the text and returns its first capture group, or null if nothing matches. The pattern needs a group, e.g. re/(\d+)/. |
| `extractAll(text)` | method | Returns a list with the first capture group of every match in the text. The pattern needs a group. |
| `matcher(text)` | method | Returns a matcher for the text, to step through the matches with find() and read groups by number or name. |
| `replaceAll(text, replacement)` | method | Replaces every match in the text; the replacement may refer to groups as $1, $2, ... |
| `replaceFirst(text, replacement)` | method | Replaces the first match in the text; the replacement may refer to groups as $1, $2, ... |
| `split(text)` | method | Splits the text at every match and returns the parts as a list. |
<!-- generated:end -->

<a id="type-matcher"></a>

## matcher

The result of `regex.matcher(text)`, for when `extract` and `extractAll` are not enough: it steps through the matches
of a pattern and gives access to every capture group, by number or by name (`(?<name>...)`), and to positions. Its class
name is `JMatcherWrapper`.

`find()` moves to the next match and returns `false` when there is none, so it drives a `while` loop; `matches()` tests
the whole text instead. Afterwards, `group`, `start` and `end` describe the current match; before a successful `find()`
or `matches()`, they are an error.

```janitor
m = re/(?<key>\w+)=(?<value>\d+)/.matcher("a=1, b=22");
while (m.find()) {
    print(m.group("key") + " -> " + m.group("value") + " at " + m.start());
}
print(re/\d+/.matcher("123").matches());
```

```output
a -> 1 at 0
b -> 22 at 5
true
```

<!-- generated:begin builtins/matcher -->
| Member | Kind | Description |
| --- | --- | --- |
| `end(group)` | method | Returns the index after the end of the current match, or of a capture group given by number or name; -1 if the group did not take part in the match. |
| `find()` | method | Moves to the next match in the text and returns true, or returns false if there is none. Use it in a loop: while (m.find()) { ... }. |
| `group(group)` | method | Returns the text of the current match, or of a capture group given by number or name; null if the group did not take part in the match. group() and group(0) are the whole match. |
| `groupCount()` | method | Returns the number of capture groups in the pattern. |
| `matches()` | method | Returns true if the whole text matches the pattern; then the whole text is the current match. |
| `start(group)` | method | Returns the index where the current match, or a capture group given by number or name, starts; -1 if the group did not take part in the match. |
<!-- generated:end -->

<a id="type-duration"></a>

## duration

A length of time, written as a literal like `@3d` (see [language.md](language.md#date-literals)). Durations can be
compared and added. A month counts as 2629746 seconds (an average month), and a year as 365 days.

```janitor
print(@2h.minutes);
print(@1w.days);
print(@1d == @24h);
```

```output
120
7
true
```

<!-- generated:begin builtins/duration -->
| Member | Kind | Description |
| --- | --- | --- |
| `days` | property, int, read-only | The length of the duration in whole days, rounded down. |
| `hours` | property, int, read-only | The length of the duration in whole hours, rounded down. |
| `minutes` | property, int, read-only | The length of the duration in whole minutes, rounded down. |
| `seconds` | property, int, read-only | The length of the duration in whole seconds. |
| `weeks` | property, int, read-only | The length of the duration in whole weeks, rounded down. |
<!-- generated:end -->

<a id="type-date"></a>

## date

A calendar date without time. Note that `year`, `month` and `day` are properties of a date, while a datetime has a
method `year()`.

```janitor
d = @2024-03-05;
print(d.year);
print(d.month);
print(d.format("dd.MM.yyyy"));
print(@today.class);
```

```output
2024
3
05.03.2024
date
```

Patterns with names of months or weekdays (`MMMM`, `EEEE`) use the default locale of the Java VM.

<!-- generated:begin builtins/date -->
| Member | Kind | Description |
| --- | --- | --- |
| `day` | property, int, read-only | The day of the month, 1 to 31. |
| `format(pattern)` | method | Formats the date with a Java DateTimeFormatter pattern like "dd.MM.yyyy"; without a pattern, in the host's default format. |
| `month` | property, int, read-only | The month, 1 to 12. |
| `string(pattern)` | method | Same as format(pattern). |
| `year` | property, int, read-only | The year, e.g. 2024. |
<!-- generated:end -->

<a id="type-datetime"></a>

## datetime

A date with a time of day, without a time zone; the host's time zone is used where one is needed (`epoch`).

```janitor
t = @2024-03-05-14:30;
print(t.date());
print(t.year());
print(t.format("yyyy-MM-dd HH:mm"));
print((t + @45mi).format("HH:mm"));
```

```output
@2024-03-05
2024
2024-03-05 14:30
15:15
```

<!-- generated:begin builtins/datetime -->
| Member | Kind | Description |
| --- | --- | --- |
| `date()` | method | Returns the date part as a date. |
| `epoch` | property, int, read-only | The number of seconds since 1970-01-01 00:00 UTC, reading the datetime in the host's time zone. |
| `format(pattern)` | method | Same as string(pattern). |
| `formatAtTimezone(zone, pattern)` | method | Formats the datetime at a time zone like "Europe/Berlin" with a DateTimeFormatter pattern; the pattern is required. |
| `kw()` | method | Returns the calendar week (German: Kalenderwoche) as a two-digit string, using German week rules. |
| `string(pattern)` | method | Formats the datetime with a Java DateTimeFormatter pattern; without a pattern, in the host's default format. |
| `time()` | method | Returns the time part as a string, formatted by the host, e.g. "14:30". |
| `toEpoch()` | method | Same as the property epoch. |
| `year()` | method | Returns the year as an integer. Note: for a date, year is a property, not a method. |
<!-- generated:end -->

<a id="type-xml-element"></a>

## XML element

Elements of a parsed XML document, as returned by `files.readXml(path)` (see [modules.md](modules.md#module-files)).
Their class name is `JElement`.

<!-- generated:begin builtins/xml-element -->
| Member | Kind | Description |
| --- | --- | --- |
| `attrs` | property, read-only | The attributes of the element, as a map from name to value. |
| `children` | property, list, read-only | The list of child elements. |
| `name` | property, string, writable | The tag name of the element; writable. |
| `text` | property, string, writable | The text content of the element; writable. |
<!-- generated:end -->

<a id="host-types"></a>

## Types provided by the host

Hosts add types of their own, e.g. for their business objects. Such objects behave like the objects of modules: they
have methods and properties, and often `apply(map)`. They are described in the host's documentation; see
[hosting.md](hosting.md#host-provided-types).
