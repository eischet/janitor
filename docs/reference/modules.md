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
| `args` | property, list, read-only |  |
| `host` | property, string, read-only |  |
| `revision` | property, string, read-only |  |
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
| `Generator(...)` | method |  |
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
| `Message(...)` | method |  |
| `send(...)` | method |  |
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
| `Zip(...)` | method |  |
| `copy(...)` | method |  |
| `delete(...)` | method |  |
| `exists(...)` | method |  |
| `lastmod(...)` | method |  |
| `list(...)` | method |  |
| `mkdirs(...)` | method |  |
| `move(...)` | method |  |
| `normalize(...)` | method |  |
| `read(...)` | method |  |
| `readBinary(...)` | method |  |
| `readXml(...)` | method |  |
| `write(...)` | method |  |
| `writeBinary(...)` | method |  |
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
| `exec(...)` | method |  |
| `getenv(...)` | method |  |
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
| `BlockQuote(...)` | method |  |
| `BulletList(...)` | method |  |
| `Document(...)` | method |  |
| `Emphasis(...)` | method |  |
| `FencedCodeBlock(...)` | method |  |
| `HardLineBreak(...)` | method |  |
| `Heading(...)` | method |  |
| `HtmlBlock(...)` | method |  |
| `HtmlInline(...)` | method |  |
| `Image(...)` | method |  |
| `IndentedCodeBlock(...)` | method |  |
| `Link(...)` | method |  |
| `LinkReferenceDefinition(...)` | method |  |
| `ListItem(...)` | method |  |
| `OrderedList(...)` | method |  |
| `Paragraph(...)` | method |  |
| `SoftLineBreak(...)` | method |  |
| `StrongEmphasis(...)` | method |  |
| `Text(...)` | method |  |
| `ThematicBreak(...)` | method |  |
| `parse(...)` | method |  |
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
| `ALIGN_BOTTOM` | property, int, read-only |  |
| `ALIGN_CENTER` | property, int, read-only |  |
| `ALIGN_JUSTIFIED` | property, int, read-only |  |
| `ALIGN_LEFT` | property, int, read-only |  |
| `ALIGN_MIDDLE` | property, int, read-only |  |
| `ALIGN_RIGHT` | property, int, read-only |  |
| `ALIGN_TOP` | property, int, read-only |  |
| `Anchor(...)` | method |  |
| `Annotation(...)` | method |  |
| `BOLD` | property, int, read-only |  |
| `BOLDITALIC` | property, int, read-only |  |
| `BORDER_BOTTOM` | property, int, read-only |  |
| `BORDER_BOX` | property, int, read-only |  |
| `BORDER_LEFT` | property, int, read-only |  |
| `BORDER_NONE` | property, int, read-only |  |
| `BORDER_RIGHT` | property, int, read-only |  |
| `BORDER_TOP` | property, int, read-only |  |
| `BaseFont(...)` | method |  |
| `COURIER` | property, int, read-only |  |
| `Chapter(...)` | method |  |
| `Chunk(...)` | method |  |
| `Color(...)` | method |  |
| `Document(...)` | method |  |
| `Font(...)` | method |  |
| `HELVETICA` | property, int, read-only |  |
| `HeaderFooter(...)` | method |  |
| `ITALIC` | property, int, read-only |  |
| `Image(...)` | method |  |
| `List(...)` | method |  |
| `ListItem(...)` | method |  |
| `Meta(...)` | method |  |
| `NORMAL` | property, int, read-only |  |
| `PCell(...)` | method |  |
| `PTable(...)` | method |  |
| `PageSize(...)` | method |  |
| `Paragraph(...)` | method |  |
| `Phrase(...)` | method |  |
| `Reader(...)` | method |  |
| `Rectangle(...)` | method |  |
| `STRIKETHRU` | property, int, read-only |  |
| `SYMBOL` | property, int, read-only |  |
| `TIMES_ROMAN` | property, int, read-only |  |
| `UNDERLINE` | property, int, read-only |  |
| `Writer(...)` | method |  |
| `ZAPFDINGBATS` | property, int, read-only |  |
| `concat(...)` | method |  |
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
| `Allowance(...)` | method |  |
| `BankDetails(...)` | method |  |
| `CashDiscount(...)` | method |  |
| `Charge(...)` | method |  |
| `Contact(...)` | method |  |
| `DirectDebit(...)` | method |  |
| `Exporter(...)` | method |  |
| `FileAttachment(...)` | method |  |
| `IncludedNote(...)` | method |  |
| `Invoice(...)` | method |  |
| `Item(...)` | method |  |
| `LegalOrganisation(...)` | method |  |
| `PaymentTerms(...)` | method |  |
| `Product(...)` | method |  |
| `ReferencedDocument(...)` | method |  |
| `SchemedID(...)` | method |  |
| `TradeParty(...)` | method |  |
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
| `CarbonCopy(...)` | method |  |
| `Client(...)` | method |  |
| `Document(...)` | method |  |
| `EnvelopeDefinition(...)` | method |  |
| `Recipients(...)` | method |  |
| `SignHere(...)` | method |  |
| `Signer(...)` | method |  |
| `Tabs(...)` | method |  |
| `TemplateRole(...)` | method |  |
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
| `connect(...)` | method |  |
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
| `connect(...)` | method |  |
<!-- generated:end -->

<a id="host-modules"></a>

## Modules of the host

Hosts add modules of their own, either registered under a name (`import name;`) or resolved by a string
(`import "NAME" as alias;`). They are described in the host's documentation, which refers to
[hosting.md](hosting.md#host-provided-modules).
