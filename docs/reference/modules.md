# Modules

<a id="import"></a>

## The import statement

A module is an object with functions and values, which a script loads with `import`. There are two forms, which find
modules in different ways:

| Form | Finds | Binds |
| --- | --- | --- |
| `import name;` | a module that the host **registered** under this name, e.g. one of the [standard modules](#standard-modules) | `name` |
| `import name as alias;` | the same | `alias` |
| `import "NAME" as alias;` | a module that the host **resolves by name**, e.g. a module of the host application, or a script stored in the host | `alias` (required) |

```janitor
import collections;
import collections as c;
print(collections.List(1, 2));
print(c.Set(1, 1, 2).size());
```

```output
[1, 2]
2
```

The string form asks the host's module resolvers (see [hosting.md](hosting.md#module-resolvers)). It does not find the
registered modules of the first form, and the first form does not find modules that only a resolver knows:

```janitor error
import "collections" as c;
```

```error
Module not found: 'collections'
```

Which names the string form accepts, and what the modules contain, is described in the host's documentation (see
[hosting.md](hosting.md#host-provided-modules)). A typical host module might be used like this:

```janitor compile-only
import "crm" as crm;
customer = crm.findCustomer("C-1001");
```

Rules:

- `import` statements are only allowed at the top level of a script, not inside blocks, functions or loops. Put them at
  the start of the script.
- An import is executed when the script reaches it, so a missing module is a runtime error (`Module not found`), not a
  compile error.
- Several modules can be imported in one statement: `import collections, janitor;`.
- A registered module has a simple name. Dotted names like `import a.b;` are a syntax error; a host that wants
  hierarchical names offers them through the string form, e.g. `import "acme.crm" as crm;`.
- There is no `from module import name`, no `require(...)`, and no `import * as`. Members are always used through the
  module: `collections.Set()`.

<a id="availability"></a>

## Availability

**Every module is only available if the host registers it.** The Janitor project provides the standard modules below,
but a host decides which of them its scripts get; many hosts register none of them, or only a few, and add their own
modules instead. Some standard modules give scripts access to files, processes or the network; hosts register those only
for trusted scripts. If a module is not registered, `import` fails with `Module not found`.

So before you use a module in a script for a particular host, check the host's documentation. Where the host is
unknown, prefer the [built-in types and functions](builtins.md), which are always available.

<a id="standard-modules"></a>

## Standard modules

<!-- generated:begin modules/index -->
| Module | Maven artifact | Java registration | Registered by `JanitorModulesCommon.registerCommonModules(env, includingNonSandboxed)` |
| --- | --- | --- | --- |
| [`collections`](modules.md#module-collections) | `com.eischet.janitor:janitor-lang` | `CollectionsModule.REGISTRATION` | always |
| [`janitor`](modules.md#module-janitor) | `com.eischet.janitor.modules:janitor` | `JanitorInternalsModule.REGISTRATION` | always |
| [`generator`](modules.md#module-generator) | `com.eischet.janitor:janitor-generator` | `GeneratorModule.REGISTRATION` | always |
| [`httpclient`](modules.md#module-httpclient) | `com.eischet.janitor.modules:httpclient` | `HttpClientModule.REGISTRATION` | always |
| [`brrr`](modules.md#module-brrr) | `com.eischet.janitor.modules:brrr` | `BrrrModule.REGISTRATION` | always |
| [`files`](modules.md#module-files) | `com.eischet.janitor.modules:files` | `FilesModule.REGISTRATION` | only with `includingNonSandboxed = true` |
| [`os`](modules.md#module-os) | `com.eischet.janitor.modules:os` | `OperatingSystemModule.REGISTRATION` | only with `includingNonSandboxed = true` |
| [`commonmark`](modules.md#module-commonmark) | `com.eischet.janitor.modules:commonmark` | `CommonMarkModule.REGISTRATION` | no; register it explicitly |
| [`pdf`](modules.md#module-pdf) | `com.eischet.janitor.modules:pdf` | `PdfModule.REGISTRATION` | no; register it explicitly |
| [`mustang`](modules.md#module-mustang) | `com.eischet.janitor.modules:mustang` | `MustangModule.REGISTRATION` | no; register it explicitly |
| [`docusign`](modules.md#module-docusign) | `com.eischet.janitor.modules:docusign` | `DocusignModule.REGISTRATION` | no; register it explicitly |
| [`msgraph`](modules.md#module-msgraph) | `com.eischet.janitor.modules:msgraph` | `MsGraphModule.REGISTRATION` | no; register it explicitly |
| [`mcp`](modules.md#module-mcp) | `com.eischet.janitor.modules:mcp` | `McpModule.REGISTRATION` | no; register it explicitly |
<!-- generated:end -->

For each module, the following sections describe its functions; the generated table at the end of each section lists all
members of the module object, and [module-types.md](module-types.md) lists the members of the objects that the module
creates. A host registers a module like this (see [hosting.md](hosting.md#registering-modules)):

```java
env.addModule(FilesModule.REGISTRATION);
```

<a id="module-collections"></a>

### collections

Factory functions for collections. Part of `janitor-lang`, so it is always on the class path, but it still needs to be
registered.

| Function | Returns |
| --- | --- |
| `Set(values...)` | a new [set](builtins.md#type-set) with the values |
| `List(values...)` | a new [list](builtins.md#type-list) with the values |
| `Map()` | a new, empty [map](builtins.md#type-map); takes no arguments |

Like classes in Java, the names are capitalized. `set`, `list` and `map` work as aliases, but do not appear in
`dir(collections)`.

```janitor
import collections;
seen = collections.Set();
for (word in ["a", "b", "a"]) {
    seen.add(word);
}
print(seen.size());
```

```output
2
```

<!-- generated:begin modules/collections -->
| Member | Kind | Description |
| --- | --- | --- |
| `List(values...)` | method | Returns a new list with the values. Also available as list(...). |
| `Map()` | method | Returns a new, empty map; takes no arguments. Also available as map(). |
| `Set(values...)` | method | Returns a new set with the values. Also available as set(...). |
<!-- generated:end -->

<a id="module-janitor"></a>

### janitor

Information about the Janitor runtime.

| Property | Value |
| --- | --- |
| `revision` | the Janitor version, a string like `"0.9.70"` |
| `host` | a name for the host application, set by the host; `"unknown"` if it does not set one |
| `args` | the command line arguments as a list of strings, if the host passes them (e.g. the standalone interpreter); otherwise an empty list |

```janitor
import janitor;
print(janitor.revision.class);
print(janitor.args.size());
```

```output
string
0
```

<!-- generated:begin modules/janitor -->
| Member | Kind | Description |
| --- | --- | --- |
| `args` | property, list, read-only | List of the command line arguments as strings; empty if there are none; read-only. |
| `host` | property, string, read-only | Name of the host application that runs the script, e.g. "standalone" or "maven", or "unknown"; read-only. |
| `revision` | property, string, read-only | The revision (version) of the Janitor runtime; read-only. |
<!-- generated:end -->

<a id="module-generator"></a>

### generator

Generates Java source code, e.g. entity classes with their dispatch tables, from a description written in a script. It
is a tool for Janitor's own developers and for hosts that generate code at build time; ordinary scripts do not need it.

| Function | Returns |
| --- | --- |
| `Generator(options)` | a new generator; `options` is an optional map of properties to set |

The objects and their members are listed in [module-types.md](module-types.md#types-generator).

```janitor
import generator;
gen = generator.Generator();
print(gen.class);
```

```output
Generator
```

<!-- generated:begin modules/generator -->
| Member | Kind | Description |
| --- | --- | --- |
| `Generator(properties)` | method | Creates a new Java code generator; an optional map argument is applied to its properties. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
<!-- generated:end -->

<a id="module-httpclient"></a>

### httpclient

HTTP requests. Gives scripts network access, so hosts register it only for scripts that may use it.

| Function | Returns |
| --- | --- |
| `fetch(url)` | the body of a GET request, as a string |
| `build()` | a new client object, for requests with headers, authentication, JSON and so on |

The client's methods for requests take the URL as the first argument and return the response body as a string (or
binary, for `getBinary`); JSON responses are strings, too, so parse them with `{}.parseJson(text)` or
`[].parseJson(text)`:

| Method | Request |
| --- | --- |
| `getString(url)`, `getJson(url)`, `getBinary(url)` | GET |
| `postJson(url, data)`, `putJson(url, data)`, `patchJson(url, data)` | POST, PUT, PATCH with a JSON body; `data` is a map, a list or a JSON string |
| `postForm(url, map)` | POST with a form body |
| `delete(url)` | DELETE; returns `null` |

The configuration methods change the client and return it, so they can be chained: `addHeader(name, value)`,
`setBearerToken(token)`, `basic(user, password)`, `proxy(host, port)`, `setTimeout(seconds)` (connect timeout),
`setRequestTimeout(seconds)`, `allowCookies()`, `verbose()`, `ignoreSecurityIssues()` (accepts any TLS certificate; avoid),
`setPrettyJson(flag)`, `convertJsonToPureAscii(flag)`. `close()` releases the client.

A status from 200 to 399 counts as success. Other statuses and network errors raise an error, which has the properties
`code` (the status, or 0 for network errors), `body`, `headers` (with lower-case names), `method` and `url`:

```janitor compile-only
import httpclient;
client = httpclient.build().setBearerToken("secret-token").setTimeout(10);
try {
    order = {}.parseJson(client.getJson("https://shop.example.com/api/orders/17"));
    print(order.status);
    client.postJson("https://shop.example.com/api/orders/17/notes", {text: "checked"});
} catch (e) {
    print("request failed with status " + e.code + ": " + e.body);
}
```

<!-- generated:begin modules/httpclient -->
| Member | Kind | Description |
| --- | --- | --- |
| `build()` | method | Returns a new HTTP client, for requests with headers, authentication, JSON and so on. |
| `fetch(url)` | method | Sends a GET request and returns the body of the response as a string; a failed request is an error with code, body, headers, method and url. |
<!-- generated:end -->

<a id="module-brrr"></a>

### brrr

Sends push notifications through the [Brrr](https://brrr.now/) service. Needs network access.

| Function | Returns |
| --- | --- |
| `send(message)` | sends to the webhook URL in the environment variable `BRRR_URL` |
| `send(url, message)` | sends to the given webhook URL |
| `Message(properties)` | a message object; `properties` is an optional map, e.g. `{title: "...", message: "..."}` |

`message` is a string, a map of message properties, or a message object. The properties (`title`, `subtitle`, `message`,
`sound`, `open_url`, `image_url`, `thread_id`, `interruption_level`, `expiration_date`, `filter_criteria`) are listed in
[module-types.md](module-types.md#types-brrr-brrrmessage).

```janitor compile-only
import brrr;
brrr.send({title: "Backup", message: "The nightly backup has finished."});
```

<!-- generated:begin modules/brrr -->
| Member | Kind | Description |
| --- | --- | --- |
| `Message(properties)` | method | Creates a new push notification message; an optional map argument is applied to its properties. |
| `send(url, message)` | method | Sends a message, given as a Message, a map of its properties or a string with the text, and returns null; the URL is optional and defaults to the BRRR_URL environment variable. |
<!-- generated:end -->

<a id="module-files"></a>

### files

Access to the file system, with any path the Java process may access. Hosts register it only for trusted scripts; it is
not restricted to a directory. Paths are strings.

| Function | Returns |
| --- | --- |
| `exists(path)` | `true` if the file or folder exists |
| `read(path, charset)` | the content as a string; `charset` is optional, UTF-8 by default |
| `write(path, text, charset)` | `null`; writes the text, UTF-8 by default |
| `readBinary(path)` | the content as a [binary](builtins.md#type-binary) value |
| `writeBinary(path, data)` | `null`; writes a binary value |
| `readXml(path, charset)` | the root [XML element](builtins.md#type-xml-element) of the file |
| `list(folder)` | a list of the names of the entries in the folder; empty if it does not exist |
| `mkdirs(folder)` | `true` if the folder was created, including missing parents |
| `delete(path)` | `true` if the file or empty folder was deleted |
| `move(source, target)`, `copy(source, target)` | `null` |
| `normalize(path)` | the canonical absolute path |
| `lastmod(path)` | the time of the last change, as a datetime in the host's time zone; `null` if the file does not exist |
| `Zip(path)` | a zip file object for writing a new zip archive |

The zip file object has `addFile(path, entryName)`, `addText(text, entryName, encoding)` (the encoding is optional,
UTF-8 by default), `addBinary(data, entryName)`,
`setLevel(level)`, `setComment(text)` and `close()`; the `add` methods return the object, so they can be chained.

```janitor compile-only
import files;
folder = "/tmp/janitor-example";
files.mkdirs(folder);
files.write(folder + "/hello.txt", "Hello!");
print(files.read(folder + "/hello.txt"));
zip = files.Zip(folder + "/archive.zip");
zip.addFile(folder + "/hello.txt", "hello.txt").addText("more text", "more.txt");
zip.close();
print(files.list(folder));
```

<!-- generated:begin modules/files -->
| Member | Kind | Description |
| --- | --- | --- |
| `Zip(path)` | method | Creates a new ZIP file at the path and returns it for adding entries; it is closed when the script ends. |
| `copy(source, target)` | method | Copies a file; fails if the target already exists. |
| `delete(path)` | method | Deletes a file or an empty folder; returns true if it was deleted. |
| `exists(path)` | method | Returns true if a file or folder exists at the path. |
| `lastmod(path)` | method | Returns the last modification time of a file as a date-time, or null if it does not exist. |
| `list(folder)` | method | Returns a list of the names of the entries in a folder; empty if the folder does not exist. |
| `mkdirs(folder)` | method | Creates a folder including missing parent folders; returns true if it was created. |
| `move(source, target)` | method | Moves a file; fails if the target already exists. |
| `normalize(path)` | method | Returns the canonical absolute form of the path. |
| `read(path, charset)` | method | Returns the contents of a file as text; charset defaults to UTF-8. |
| `readBinary(path)` | method | Returns the contents of a file as binary data. |
| `readXml(path, charset)` | method | Reads an XML file and returns its root element; charset defaults to UTF-8. |
| `write(path, text, charset)` | method | Writes the text to a file, replacing its contents; charset defaults to UTF-8. Returns null. |
| `writeBinary(path, data)` | method | Writes binary data to a file, replacing its contents. Returns null. |
<!-- generated:end -->

<a id="module-os"></a>

### os

Access to the operating system. Hosts register it only for trusted scripts.

| Function | Returns |
| --- | --- |
| `exec(command)` | runs a command and returns its exit code; `command` is a string, or a list with the program and its arguments. The output of the command is discarded. |
| `getenv(name)` | the value of an environment variable, or `null` |

```janitor compile-only
import os;
code = os.exec(["git", "--version"]);
print("exit code: " + code);
print(os.getenv("HOME") or "no HOME variable");
```

<!-- generated:begin modules/os -->
| Member | Kind | Description |
| --- | --- | --- |
| `exec(command)` | method | Runs an operating system command, given as a string or as a list of program and arguments, waits for it to finish and returns its exit code; the output is discarded. |
| `getenv(name)` | method | Returns the value of the environment variable, or null if it is not set. |
<!-- generated:end -->

<a id="module-commonmark"></a>

### commonmark

Reads, changes and writes Markdown documents ([CommonMark](https://commonmark.org/)).

| Function | Returns |
| --- | --- |
| `parse(text)` | the document node of the Markdown text |
| `Document()`, `Heading()`, `Paragraph()`, `Text()`, ... | a new, empty node of that type, for building documents; an optional map sets its properties |

Every node has `type`, `parent`, `firstChild`, `lastChild`, `next`, `previous`, the methods `appendChild(node)`,
`prependChild(node)`, `insertAfter(node)`, `insertBefore(node)`, `unlink()` and `visit(function)`, and converts itself
with `toHtml()`, `toMarkdown()` and `toText()`. The node types and their properties (`level` of a heading, `literal` of a
text, `destination` of a link, ...) are listed in [module-types.md](module-types.md#types-commonmark).

```janitor
import commonmark;
doc = commonmark.parse("# Title\n\nSome *emphasis*.");
heading = doc.firstChild;
print(heading.type + " " + heading.level);
print(heading.next.toText().trim());
```

```output
Heading 1
Some emphasis.
```

<!-- generated:begin modules/commonmark -->
| Member | Kind | Description |
| --- | --- | --- |
| `BlockQuote(properties)` | method | Creates a new, empty block quote node; an optional map argument is applied to its properties. |
| `BulletList(properties)` | method | Creates a new, empty bullet list node; an optional map argument is applied to its properties. |
| `Document(properties)` | method | Creates a new, empty document node; an optional map argument is applied to its properties. |
| `Emphasis(properties)` | method | Creates a new, empty emphasis node; an optional map argument is applied to its properties. |
| `FencedCodeBlock(properties)` | method | Creates a new, empty fenced code block node; an optional map argument is applied to its properties. |
| `HardLineBreak(properties)` | method | Creates a new, empty hard line break node; an optional map argument is applied to its properties. |
| `Heading(properties)` | method | Creates a new, empty heading node; an optional map argument is applied to its properties. |
| `HtmlBlock(properties)` | method | Creates a new, empty HTML block node; an optional map argument is applied to its properties. |
| `HtmlInline(properties)` | method | Creates a new, empty inline HTML node; an optional map argument is applied to its properties. |
| `Image(properties)` | method | Creates a new, empty image node; an optional map argument is applied to its properties. |
| `IndentedCodeBlock(properties)` | method | Creates a new, empty indented code block node; an optional map argument is applied to its properties. |
| `Link(properties)` | method | Creates a new, empty link node; an optional map argument is applied to its properties. |
| `LinkReferenceDefinition(properties)` | method | Creates a new, empty link reference definition node; an optional map argument is applied to its properties. |
| `ListItem(properties)` | method | Creates a new, empty list item node; an optional map argument is applied to its properties. |
| `OrderedList(properties)` | method | Creates a new, empty ordered list node; an optional map argument is applied to its properties. |
| `Paragraph(properties)` | method | Creates a new, empty paragraph node; an optional map argument is applied to its properties. |
| `SoftLineBreak(properties)` | method | Creates a new, empty soft line break node; an optional map argument is applied to its properties. |
| `StrongEmphasis(properties)` | method | Creates a new, empty strong emphasis node; an optional map argument is applied to its properties. |
| `Text(properties)` | method | Creates a new, empty text node; an optional map argument is applied to its properties. |
| `ThematicBreak(properties)` | method | Creates a new, empty thematic break node; an optional map argument is applied to its properties. |
| `parse(markdown)` | method | Parses the Markdown text and returns the document node of the resulting tree. |
<!-- generated:end -->

<a id="module-pdf"></a>

### pdf

Creates and reads PDF documents, based on the OpenPDF library. The functions create document elements, named like the
library's classes: `Document`, `Writer`, `Reader`, `Paragraph`, `Phrase`, `Chunk`, `Anchor`, `List`, `ListItem`,
`Chapter`, `Section`, `PTable`, `PCell`, `Image`, `Font`, `BaseFont`, `Color`, `Rectangle`, `PageSize(name)`,
`HeaderFooter`, `Annotation` and `Meta`; `concat(...)` joins PDF files. Constants like `pdf.BOLD`, `pdf.ALIGN_CENTER` or
`pdf.BORDER_BOX` are properties of the module. The elements and their members are listed in
[module-types.md](module-types.md#types-pdf).

The arguments of the element functions follow the constructors of OpenPDF; they are not described in more detail here
(see [open questions](open-questions.md#module-documentation)). `Document()` takes no arguments, a page size, or a
page size and four margins (left, right, top, bottom).

```janitor compile-only
import pdf;
doc = pdf.Document(pdf.PageSize("A4"));
writer = pdf.Writer(doc, "/tmp/hello.pdf");
doc.open();
font = pdf.Font(pdf.HELVETICA, 18, pdf.BOLD);
doc.add(pdf.Paragraph("Hello, world!", font));
doc.close();
```

<!-- generated:begin modules/pdf -->
| Member | Kind | Description |
| --- | --- | --- |
| `ALIGN_BOTTOM` | property, int, read-only | Vertical alignment constant: bottom. |
| `ALIGN_CENTER` | property, int, read-only | Horizontal alignment constant: centered. |
| `ALIGN_JUSTIFIED` | property, int, read-only | Horizontal alignment constant: justified. |
| `ALIGN_LEFT` | property, int, read-only | Horizontal alignment constant: left. |
| `ALIGN_MIDDLE` | property, int, read-only | Vertical alignment constant: middle. |
| `ALIGN_RIGHT` | property, int, read-only | Horizontal alignment constant: right. |
| `ALIGN_TOP` | property, int, read-only | Vertical alignment constant: top. |
| `Anchor(text, font)` | method | Creates an Anchor, a phrase that can be a link or a named destination; text and font are optional. |
| `Annotation(title, text)` | method | Creates a popup text note; Annotation(llx, lly, urx, ury, url) instead creates a link to the URL over the given area. |
| `BOLD` | property, int, read-only | Font style flag for bold text; combine flags with \|. |
| `BOLDITALIC` | property, int, read-only | Font style constant for bold italic text, the same as BOLD \| ITALIC. |
| `BORDER_BOTTOM` | property, int, read-only | Border flag for the bottom side; combine flags with \|. |
| `BORDER_BOX` | property, int, read-only | Border constant for all four sides. |
| `BORDER_LEFT` | property, int, read-only | Border flag for the left side; combine flags with \|. |
| `BORDER_NONE` | property, int, read-only | Border constant: no border. |
| `BORDER_RIGHT` | property, int, read-only | Border flag for the right side; combine flags with \|. |
| `BORDER_TOP` | property, int, read-only | Border flag for the top side; combine flags with \|. |
| `BaseFont(name, encoding, embedded)` | method | Creates a BaseFont from a standard font name or a TrueType/OpenType file path; encoding defaults to Cp1252 (WinAnsi), embedded to false. |
| `COURIER` | property, int, read-only | Font family constant for Courier. |
| `Chapter(title, number)` | method | Creates a chapter with the chapter number; the title is optional. |
| `Chunk(text, font)` | method | Creates a Chunk, the smallest piece of text with a Font; text and font are optional. |
| `Color(red, green, blue, alpha)` | method | Creates a Color from components 0 to 255; alpha is optional. |
| `Document(pageSize, marginLeft, marginRight, marginTop, marginBottom)` | method | Creates a new PDF document; pageSize (a Rectangle) and the margins in points are optional, but the margins only together with a page size. |
| `Font(family, size, style, color)` | method | Creates a Font; all arguments are optional, e.g. pdf.Font(pdf.HELVETICA, 12, pdf.BOLD, pdf.Color(0, 0, 255)). |
| `HELVETICA` | property, int, read-only | Font family constant for Helvetica. |
| `HeaderFooter(before, numberedOrAfter)` | method | Creates a page header or footer from the Phrase before and either a boolean (show the page number after it) or a Phrase shown after the page number. |
| `ITALIC` | property, int, read-only | Font style flag for italic text; combine flags with \|. |
| `Image(source)` | method | Loads an Image from a local file path or from binary data. |
| `List(numbered, lettered)` | method | Creates a List of ListItems; both arguments are optional booleans, without them the list is bulleted. |
| `ListItem(text, font)` | method | Creates a ListItem for a List; text and font are optional. |
| `Meta(tag, content)` | method | Creates a piece of document meta-information. |
| `NORMAL` | property, int, read-only | Font style constant for normal text. |
| `PCell(content)` | method | Creates a table cell; content is optional and can be a Phrase, an Image or a nested PTable. |
| `PTable(columns)` | method | Creates a table with the given number of columns, or with one column per entry of a list of relative column widths. |
| `PageSize(name)` | method | Returns the read-only Rectangle of a standard page size like "A4", "A5", "LETTER" or "LEGAL" (case-insensitive); unknown names are an error. |
| `Paragraph(text, font)` | method | Creates a Paragraph, a phrase with alignment, indentation and spacing; text and font are optional. |
| `Phrase(text, font)` | method | Creates a Phrase, a series of chunks; text and font are optional. |
| `Reader(source)` | method | Reads an existing PDF from a file path or from binary data. |
| `Rectangle(width, height)` | method | Creates a Rectangle of the size in points; Rectangle(llx, lly, urx, ury) instead takes the lower left and upper right corners. |
| `STRIKETHRU` | property, int, read-only | Font style flag for struck-through text; combine flags with \|. |
| `SYMBOL` | property, int, read-only | Font family constant for the Symbol font. |
| `TIMES_ROMAN` | property, int, read-only | Font family constant for Times Roman. |
| `UNDERLINE` | property, int, read-only | Font style flag for underlined text; combine flags with \|. |
| `Writer(document, path)` | method | Creates a writer that writes the document to the file path; without a path, the PDF is kept in memory and available as writer.bytes after closing the document. |
| `ZAPFDINGBATS` | property, int, read-only | Font family constant for ZapfDingbats. |
| `concat(targetFile, sourceFiles)` | method | Concatenates the pages of the PDF files in the list sourceFiles into the new file targetFile. |
<!-- generated:end -->

<a id="module-mustang"></a>

### mustang

Creates electronic invoices in the ZUGFeRD / Factur-X / XRechnung formats, based on the Mustang project. The functions
are constructors for the parts of an invoice: `Invoice`, `Item`, `Product`, `TradeParty`, `Contact`, `BankDetails`,
`LegalOrganisation`, `SchemedID`, `DirectDebit`, `CashDiscount`, `PaymentTerms`, `IncludedNote`, `ReferencedDocument`,
`FileAttachment`, `Charge`, `Allowance` and `Exporter`. Each takes an optional map of properties. The properties are
listed in [module-types.md](module-types.md#types-mustang).

```janitor compile-only
import mustang;
seller = mustang.TradeParty({name: "Example GmbH"});
invoice = mustang.Invoice({number: "2024-0001"});
```

The objects mirror the classes of the Mustang library, and their properties are not described in more detail here (see
[open questions](open-questions.md#module-documentation)).

<!-- generated:begin modules/mustang -->
| Member | Kind | Description |
| --- | --- | --- |
| `Allowance()` | method | Creates a new, empty allowance (discount) on item or document level. |
| `BankDetails()` | method | Creates new, empty bank details of a trade party. |
| `CashDiscount()` | method | Creates a new, empty early-payment discount. |
| `Charge()` | method | Creates a new, empty charge (surcharge) on item or document level. |
| `Contact()` | method | Creates a new, empty contact person of a trade party. |
| `DirectDebit()` | method | Creates new, empty SEPA direct debit details. |
| `Exporter()` | method | Creates a new exporter, which embeds invoice data into PDF files. |
| `FileAttachment()` | method | Creates a new, empty file attachment for an invoice. |
| `IncludedNote()` | method | Creates a new, empty free-text note for an invoice or item. |
| `Invoice()` | method | Creates a new, empty electronic invoice (ZUGFeRD/Factur-X). |
| `Item()` | method | Creates a new, empty invoice item (line). |
| `LegalOrganisation()` | method | Creates a new, empty legal registration of a trade party. |
| `PaymentTerms()` | method | Creates new, empty payment terms. |
| `Product()` | method | Creates a new, empty product for an invoice item. |
| `ReferencedDocument()` | method | Creates a new, empty reference to another document. |
| `SchemedID()` | method | Creates a new, empty scheme-qualified identifier. |
| `TradeParty()` | method | Creates a new, empty trade party, i.e. the sender or recipient of an invoice. |
<!-- generated:end -->

<a id="module-docusign"></a>

### docusign

Sends documents for electronic signature through DocuSign. Needs network access and DocuSign credentials. The functions
`Client`, `EnvelopeDefinition`, `Document`, `Signer`, `CarbonCopy`, `Recipients`, `Tabs`, `SignHere` and `TemplateRole`
create objects; each takes an optional map of properties. The objects are listed in
[module-types.md](module-types.md#types-docusign); their names follow the DocuSign eSignature SDK.

```janitor compile-only
import docusign;
signer = docusign.Signer({email: "ada@example.com", name: "Ada Lovelace", recipientId: "1"});
recipients = docusign.Recipients();
```

<!-- generated:begin modules/docusign -->
| Member | Kind | Description |
| --- | --- | --- |
| `CarbonCopy(properties)` | method | Creates a new carbon copy recipient; an optional map argument is applied to its properties. |
| `Client(properties)` | method | Creates a new DocuSign API client; an optional map argument is applied to its properties. |
| `Document(properties)` | method | Creates a new document for an envelope; an optional map argument is applied to its properties. |
| `EnvelopeDefinition(properties)` | method | Creates a new envelope definition; an optional map argument is applied to its properties. |
| `Recipients(properties)` | method | Creates a new, empty set of recipients; an optional map argument is applied to its properties. |
| `SignHere(properties)` | method | Creates a new sign here tab; an optional map argument is applied to its properties. |
| `Signer(properties)` | method | Creates a new signer recipient; an optional map argument is applied to its properties. |
| `Tabs(properties)` | method | Creates a new, empty set of tabs; an optional map argument is applied to its properties. |
| `TemplateRole(properties)` | method | Creates a new template role; an optional map argument is applied to its properties. |
<!-- generated:end -->

<a id="module-msgraph"></a>

### msgraph

Reads and organizes e-mail in Microsoft 365 mailboxes through Microsoft Graph. Needs network access and an Entra ID
(Azure AD) application with a client secret.

| Function | Returns |
| --- | --- |
| `connect(clientId, clientSecret, tenantId, options)` | a client; `options` is an optional map, see `MsGraphOptions` in [module-types.md](module-types.md#types-msgraph) |

The client's methods take the mailbox (a user's e-mail address or id) as the first argument:

| Method | Returns |
| --- | --- |
| `folder(user, folderId)` | a mail folder |
| `folders(user, parentId)` | a list of the folders below the parent, or the top-level folders without `parentId` |
| `allFolders(user)` | a list of all folders |
| `findFolder(user, displayName, parentId)` | the folder with that name, or `null`; `parentId` is optional |
| `createFolder(user, displayName, parentId)` | the new folder; `parentId` is optional |
| `deleteFolder(user, folderId)` | `null` |
| `messages(user, folderId, query)` | a list of messages; `folderId` defaults to `"inbox"`, `query` is an optional map, see `MessageQuery` |
| `mime(user, messageId)` | the message in MIME format, as a string |
| `move(user, messageId, destinationFolderId)` | the id of the moved message |
| `update(user, messageId, changes)` | `null`; `changes` is a map, see `MessageUpdate` |

```janitor compile-only
import msgraph;
graph = msgraph.connect(clientId, clientSecret, tenantId);
for (message in graph.messages("support@example.com", "inbox")) {
    print(message.subject);
}
```

<!-- generated:begin modules/msgraph -->
| Member | Kind | Description |
| --- | --- | --- |
| `connect(clientId, clientSecret, tenantId, options)` | method | Returns a Microsoft Graph client that authenticates with the client secret; options is an optional map with baseUrl, proxyHost, proxyPort, tokenCache, timeoutSeconds and maxRetries. |
<!-- generated:end -->

<a id="module-mcp"></a>

### mcp

A client for servers of the Model Context Protocol (MCP), which offer tools that can be called. Needs network access.

| Function | Returns |
| --- | --- |
| `connect(url, options)` | a connection; `options` is an optional map with `headers` (a map of strings) and the properties of `McpOptions` in [module-types.md](module-types.md#types-mcp-mcpoptions) |

| Member of a connection | Returns |
| --- | --- |
| `tools()` | a list of maps that describe the server's tools |
| `call(name, arguments)` | the result of calling a tool; `arguments` is an optional map. The result has `text`, `content`, `structured` and `isError`. |
| `serverName`, `serverVersion`, `protocolVersion`, `instructions`, `capabilities` | information about the server |
| `close()` | ends the connection; it is also closed when the script ends |

```janitor compile-only
import mcp;
server = mcp.connect("https://tools.example.com/mcp", {headers: {Authorization: "Bearer secret-token"}});
result = server.call("search", {text: "printer"});
print(result.text);
```

<!-- generated:begin modules/mcp -->
| Member | Kind | Description |
| --- | --- | --- |
| `connect(url, options)` | method | Connects to the MCP server at the URL and returns the connection; options is an optional map with headers, timeoutSeconds, clientName, clientVersion, transport and throwOnError. |
<!-- generated:end -->

<a id="host-modules"></a>

## Modules of the host

Hosts add modules of their own, either registered under a name (`import name;`) or resolved by a string
(`import "NAME" as alias;`). They are described in the host's documentation, which refers to
[hosting.md](hosting.md#host-provided-modules).
