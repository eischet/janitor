<!-- generated file: do not edit, see README.md -->

# Object types of the standard modules

This file lists the members of the objects that the [standard modules](modules.md) create, as their dispatch tables
define them. It is generated from the code, so it is complete but terse: a description is shown where the code has
one. Each module is only available if the host registers it. Members that every object has, like `apply(map)`, are
listed, too.

<a id="types-collections"></a>

## collections

This module defines no object types of its own.

<a id="types-janitor"></a>

## janitor

This module defines no object types of its own.

<a id="types-generator"></a>

## generator

<a id="types-generator-generatedclass"></a>

### GeneratedClass

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `name` | property, string, read-only | The name of the class to generate; read-only. |

<a id="types-generator-generatedfield"></a>

### GeneratedField

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `name` | property, string, read-only | The name of the field; read-only. |

<a id="types-generator-generator"></a>

### Generator

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-httpclient"></a>

## httpclient

<a id="types-httpclient-httpexception-wrapper"></a>

### HttpException.Wrapper

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `body` | property, string, read-only | The body of the server's response, or null if there is none; read-only. |
| `code` | property, int, read-only | The HTTP status code, or 0 if there was no response; read-only. |
| `headers` | property, read-only | The headers of the server's response as a map; empty if there was no response; read-only. |
| `method` | property, string, read-only | The HTTP method of the failed request, e.g. GET; read-only. |
| `url` | property, string, read-only | The URL of the failed request; read-only. |

<a id="types-httpclient-janitorhttpclient"></a>

### JanitorHttpClient

| Member | Kind | Description |
| --- | --- | --- |
| `addHeader(name, value)` | method | Adds a header that is sent with every request. Returns this client. |
| `allowCookies()` | method | Makes the client remember cookies set by servers and send them with later requests. Returns this client. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basic(username, password)` | method | Enables HTTP basic authentication; an empty user name disables it. Returns this client. |
| `close()` | method | Closes the underlying connection; the client can still be used and opens a new one when needed. |
| `convertJsonToPureAscii(yes)` | method | Escapes all non-ASCII characters in JSON request bodies as \uXXXX when true. Returns this client. |
| `delete(url)` | method | Sends a DELETE request; a failed request is an error. |
| `getBinary(url)` | method | Sends a GET request and returns the response body as binary data, or null if it is empty; a failed request is an error. |
| `getJson(url)` | method | Sends a GET request that accepts JSON and returns the response body as a string; a failed request is an error. |
| `getString(url)` | method | Sends a GET request and returns the response body as a string; a failed request is an error. |
| `ignoreSecurityIssues(yes)` | method | Accepts invalid certificates and host names when true; insecure, and an error unless the host application allows it. Returns this client. |
| `patchJson(url, body)` | method | Sends a PATCH request with a JSON body (a JSON string or a value that can be written as JSON) and returns the response body as a string; a failed request is an error. |
| `postForm(url, fields)` | method | Sends a POST request with the map of fields as URL-encoded form data and returns the response body as a string; a failed request is an error. |
| `postJson(url, body)` | method | Sends a POST request with a JSON body (a JSON string or a value that can be written as JSON) and returns the response body as a string; a failed request is an error. |
| `prettyJson` | property, bool, writable | Whether JSON that the client creates from script values is indented; writable. |
| `proxy(host, port)` | method | Sends requests through the given HTTP proxy. Returns this client. |
| `putJson(url, body)` | method | Sends a PUT request with a JSON body (a JSON string or a value that can be written as JSON) and returns the response body as a string; a failed request is an error. |
| `setBearerToken(token)` | method | Sends the token as "Authorization: Bearer" header with every request. Returns this client. |
| `setPrettyJson(yes)` | method | Indents JSON that the client creates from script values when true. Returns this client. |
| `setRequestTimeout(seconds)` | method | Sets the timeout for a whole request (default 300 seconds). Returns this client. |
| `setTimeout(seconds)` | method | Sets the timeout for connecting (default 60 seconds). Returns this client. |
| `verbose()` | method | Logs every request and response in detail. Returns this client. |

<a id="types-brrr"></a>

## brrr

<a id="types-brrr-brrrmessage"></a>

### BrrrMessage

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `expiration_date` | property, datetime, writable | The datetime after which the notification expires; writable. |
| `filter_criteria` | property, string, writable | The filter criteria sent with the notification; writable. |
| `image_url` | property, string, writable | The URL of an image to show in the notification; writable. |
| `interruption_level` | property, string, writable | The interruption level, "passive", "active" or "time-sensitive"; writable, unknown values clear it. |
| `message` | property, string, writable | The text of the notification; writable, null is stored as an empty string. |
| `open_url` | property, string, writable | The URL that opens when the notification is tapped; writable. |
| `send(url)` | method | Sends the message to the Brrr webhook URL and returns null; without a URL, the BRRR_URL environment variable is used, and the URL "disabled" only logs a warning. |
| `sound` | property, string, writable | The name of the notification sound, e.g. "default", "brrr" or "cat_meow"; writable, unknown or blank names select "default". |
| `subtitle` | property, string, writable | The subtitle of the notification; writable. |
| `thread_id` | property, string, writable | The ID of the thread that the notification belongs to; writable. |
| `title` | property, string, writable | The title of the notification; writable. |

<a id="types-files"></a>

## files

<a id="types-files-zipfile"></a>

### ZipFile

| Member | Kind | Description |
| --- | --- | --- |
| `addBinary(data, entryName)` | method | Adds binary data as an entry. Returns this ZIP file. |
| `addFile(path, entryName)` | method | Adds a file from the file system; without an entry name, the file's name is used. Returns this ZIP file. |
| `addText(text, entryName, charset)` | method | Adds the text as an entry; charset defaults to UTF-8. Returns this ZIP file. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close()` | method | Finishes and closes the ZIP file; no entries can be added afterwards. Calling it twice is harmless. |
| `setComment(comment)` | method | Sets the comment of the ZIP file. Returns this ZIP file. |
| `setLevel(level)` | method | Sets the compression level (0-9) for the following entries. Returns this ZIP file. |
| `setMethod(method)` | method | Sets the compression method for the following entries as a number (8 = deflated, 0 = stored). Returns this ZIP file. |

<a id="types-os"></a>

## os

This module defines no object types of its own.

<a id="types-commonmark"></a>

## commonmark

<a id="types-commonmark-cmblock"></a>

### CMBlock

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-commonmark-cmblockquote"></a>

### CMBlockQuote

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmbulletlist"></a>

### CMBulletList

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `marker` | property, string | The character used as the bullet marker, e.g. "-", "*" or "+"; writable. |
| `tight` | property |  |

<a id="types-commonmark-cmcode"></a>

### CMCode

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The text of the inline code span; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmcustomblock"></a>

### CMCustomBlock

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-commonmark-cmcustomnode"></a>

### CMCustomNode

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-commonmark-cmdocument"></a>

### CMDocument

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmemphasis"></a>

### CMEmphasis

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `closingDelimiter` | property, string | The delimiter that closes the emphasis, "*" or "_"; read-only. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `openingDelimiter` | property, string | The delimiter that opens the emphasis, "*" or "_"; read-only. |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmfencedcodeblock"></a>

### CMFencedCodeBlock

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `closingFenceLength` | property, int | The number of fence characters in the closing fence; writable, may be null. |
| `fenceCharacter` | property, string | The character of the fence, "`" or "~"; writable. |
| `fenceIndent` | property, int | The indentation of the opening fence, in spaces; writable. |
| `fenceLength` | property, int | The number of fence characters in the opening fence; writable. |
| `firstChild` | property |  |
| `info` | property, string | The info string after the opening fence, usually the language of the code; writable. |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The code inside the block; writable. |
| `next` | property |  |
| `openingFenceLength` | property, int | The number of fence characters in the opening fence; writable, may be null. |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmhardlinebreak"></a>

### CMHardLineBreak

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmheading"></a>

### CMHeading

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `level` | property, int | The level of the heading, from 1 to 6; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmhtmlblock"></a>

### CMHtmlBlock

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The raw HTML of the block; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmhtmlinline"></a>

### CMHtmlInline

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The raw inline HTML; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmimage"></a>

### CMImage

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `destination` | property, string | The URL of the image; writable. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string | The title of the image; writable. |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmindentedcodeblock"></a>

### CMIndentedCodeBlock

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The code inside the block; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmlink"></a>

### CMLink

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `destination` | property, string | The URL that the link points to; writable. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string | The title of the link; writable. |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmlinkreferencedefinition"></a>

### CMLinkReferenceDefinition

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `destination` | property, string | The URL that the definition points to; writable. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `label` | property, string | The label that links use to refer to this definition; writable. |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string | The title of the definition; writable. |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmlistblock"></a>

### CMListBlock

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `tight` | property, bool, writable | True if the list is tight, i.e. its items are not separated by blank lines; writable. |

<a id="types-commonmark-cmlistitem"></a>

### CMListItem

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `contentIndent` | property, int | The indentation of the item's content, in columns; writable, may be null. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `markerIndent` | property, int | The indentation of the list marker, in columns; writable, may be null. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmnode"></a>

### CMNode

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild(node)` | method | Adds the given node as the last child of this node. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property, read-only | The first child node, or null if the node has no children; read-only. |
| `insertAfter(node)` | method | Inserts the given node as the next sibling of this node. |
| `insertBefore(node)` | method | Inserts the given node as the previous sibling of this node. |
| `lastChild` | property, read-only | The last child node, or null if the node has no children; read-only. |
| `next` | property, read-only | The next sibling node, or null if there is none; read-only. |
| `parent` | property, read-only | The parent node, or null if the node has none; read-only. |
| `prependChild(node)` | method | Adds the given node as the first child of this node. |
| `previous` | property, read-only | The previous sibling node, or null if there is none; read-only. |
| `toHtml(...)` | method | Converts the node to html representation |
| `toMarkdown(...)` | method | Converts the node to markdown representation |
| `toText(...)` | method | Converts the node to plain text representation |
| `type` | property, string, read-only | The kind of node, e.g. "Paragraph", "Heading" or "Text"; read-only. |
| `unlink()` | method | Removes the node, with its children, from its parent and siblings. |
| `visit(...)` | method |  |

<a id="types-commonmark-cmorderedlist"></a>

### CMOrderedList

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `markerDelimiter` | property, string | The delimiter after the item numbers, "." or ")"; writable. |
| `markerStartNumber` | property, int | The number of the first item of the list; writable, may be null. |
| `startNumber` | property, int | The number of the first item of the list; writable. |
| `tight` | property |  |

<a id="types-commonmark-cmparagraph"></a>

### CMParagraph

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmsoftlinebreak"></a>

### CMSoftLineBreak

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmstrongemphasis"></a>

### CMStrongEmphasis

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `closingDelimiter` | property, string | The delimiter that closes the strong emphasis, "**" or "__"; read-only. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `openingDelimiter` | property, string | The delimiter that opens the strong emphasis, "**" or "__"; read-only. |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmtext"></a>

### CMText

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The text of the node; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-commonmark-cmthematicbreak"></a>

### CMThematicBreak

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string | The source characters of the thematic break, e.g. "---"; writable. |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `toHtml` | property |  |
| `toMarkdown` | property |  |
| `toText` | property |  |
| `type` | property |  |
| `unlink` | property |  |
| `visit` | property |  |

<a id="types-pdf"></a>

## pdf

<a id="types-pdf-jpdfanchor"></a>

### JPDFAnchor

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Chunk, Phrase, ...) to the phrase; returns the phrase. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only | True if the phrase contains no content; read-only. |
| `font` | property, writable | The Font of the phrase; writable. |
| `leading` | property, float, writable | The line spacing (distance between baselines) in points; writable. |
| `name` | property, string, writable | The name of the anchor as a named destination, the target of e.g. Chunk.setLocalGoto(name); writable. |
| `reference` | property, string, writable | The URL the anchor links to; writable. |
| `size` | property, int, read-only | The number of elements in the phrase; read-only. |

<a id="types-pdf-jpdfannotation"></a>

### JPDFAnnotation

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, read-only | The text content of the annotation; read-only. |
| `llx` | property, float, read-only | The x coordinate of the lower left corner of the annotation area; read-only, see setDimensions(). |
| `lly` | property, float, read-only | The y coordinate of the lower left corner of the annotation area; read-only, see setDimensions(). |
| `setDimensions(llx, lly, urx, ury)` | method | Sets the area of the annotation by its lower left and upper right corners; returns the annotation. |
| `title` | property, string, read-only | The title of the annotation; read-only. |
| `urx` | property, float, read-only | The x coordinate of the upper right corner of the annotation area; read-only, see setDimensions(). |
| `ury` | property, float, read-only | The y coordinate of the upper right corner of the annotation area; read-only, see setDimensions(). |

<a id="types-pdf-jpdfbasefont"></a>

### JPDFBaseFont

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `getFont(size)` | method | Returns a Font using this base font at the given size; without a size, the size is left undefined. |
| `getWidthPoint(text, fontSize)` | method | Returns the width of the text in points when set in this font at the given size. |
| `postscriptFontName` | property, string, read-only | The PostScript name of the font; read-only. |

<a id="types-pdf-jpdfchapter"></a>

### JPDFChapter

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Paragraph, List, PTable, ...) to the section; returns the section. |
| `addSection(title, numberDepth)` | method | Adds a nested Section with the title and returns it; numberDepth is optional, see numberDepth. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bookmarkOpen` | property, bool, writable | True if the section's bookmark (outline entry) is shown expanded; writable. |
| `indentation` | property, float, writable | The indentation of the section content in points; writable. |
| `indentationLeft` | property, float, writable | The left indentation in points; writable. |
| `indentationRight` | property, float, writable | The right indentation in points; writable. |
| `numberDepth` | property, int, writable | How many levels of the section number are shown in the title, e.g. 2 for "1.2."; 0 shows no number; writable. |
| `numberStyle` | property, int, writable |  |
| `title` | property, writable | The title of the section as a Paragraph; writable. |
| `triggerNewPage` | property, bool, writable | True if the section starts on a new page; writable. |

<a id="types-pdf-jpdfchunk"></a>

### JPDFChunk

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `characterSpacing` | property, float, writable | The extra space between characters in points; writable. |
| `content` | property, string, read-only | The text of the chunk; read-only. |
| `empty` | property, bool, read-only | True if the chunk has no content; read-only. |
| `font` | property, writable | The Font of the chunk; writable. |
| `horizontalScaling` | property, float, writable | The horizontal scaling factor of the text, 1 for normal width; writable. |
| `setAnchor(url)` | method | Makes the chunk a link to the URL; returns the chunk. |
| `setBackground(color)` | method | Sets the background Color of the chunk; returns the chunk. |
| `setGenericTag(tag)` | method | Sets a generic tag on the chunk, which is passed to page events; returns the chunk. |
| `setLocalDestination(name)` | method | Makes the chunk a named destination, the target of setLocalGoto(name); returns the chunk. |
| `setLocalGoto(name)` | method | Makes the chunk a link to the named destination within the document; returns the chunk. |
| `setNewPage()` | method | Makes the chunk start a new page; returns the chunk. |
| `setUnderline(thickness, yPosition)` | method | Underlines the chunk with a line of the given thickness at the given offset from the baseline; the defaults are 1 and -2; returns the chunk. |
| `textRise` | property, float, writable | The vertical shift of the text from the baseline in points, e.g. for superscript (positive) or subscript (negative); writable. |

<a id="types-pdf-jpdfcolor"></a>

### JPDFColor

| Member | Kind | Description |
| --- | --- | --- |
| `alpha` | property, int, read-only | The alpha (opacity) component of the color, 0 to 255; read-only. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `blue` | property, int, read-only | The blue component of the color, 0 to 255; read-only. |
| `green` | property, int, read-only | The green component of the color, 0 to 255; read-only. |
| `red` | property, int, read-only | The red component of the color, 0 to 255; read-only. |
| `rgb` | property, string, read-only | The color as a hex string like "#FF8000", without alpha; read-only. |

<a id="types-pdf-jpdfdocument"></a>

### JPDFDocument

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Paragraph, PTable, Image, ...) to the document, which must be open; returns the document. |
| `addAuthor(author)` | method | Sets the author in the document's meta-information; returns the document. |
| `addCreator(creator)` | method | Sets the creator (the creating application) in the document's meta-information; returns the document. |
| `addHeader(name, content)` | method | Adds a custom entry to the document's meta-information (not a page header, see setHeader()); returns the document. |
| `addKeywords(keywords)` | method | Sets the keywords in the document's meta-information; returns the document. |
| `addSubject(subject)` | method | Sets the subject in the document's meta-information; returns the document. |
| `addTitle(title)` | method | Sets the title in the document's meta-information; returns the document. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close()` | method | Closes the document, which finishes writing the PDF. |
| `isOpen` | property, bool, read-only | True if the document is open; read-only. |
| `marginBottom` | property, float, read-only | The bottom page margin in points; read-only, see setMargins(). |
| `marginLeft` | property, float, read-only | The left page margin in points; read-only, see setMargins(). |
| `marginRight` | property, float, read-only | The right page margin in points; read-only, see setMargins(). |
| `marginTop` | property, float, read-only | The top page margin in points; read-only, see setMargins(). |
| `newPage()` | method | Starts a new page; returns true if a new page was started. |
| `open()` | method | Opens the document for adding content; create the Writer before. |
| `pageNumber` | property, int, read-only | The number of the current page; read-only. |
| `pageSize` | property, writable | The page size as a Rectangle; writable, e.g. doc.pageSize = pdf.PageSize("A4"). |
| `resetFooter()` | method | Removes the page footer. |
| `resetHeader()` | method | Removes the page header. |
| `setFooter(headerFooter)` | method | Sets the HeaderFooter shown at the bottom of each page; returns the document. |
| `setHeader(headerFooter)` | method | Sets the HeaderFooter shown at the top of each page; returns the document. |
| `setMarginMirroring(mirror)` | method | If true, the left and right margins are swapped on even pages, e.g. for double-sided printing; returns the document. |
| `setMargins(left, right, top, bottom)` | method | Sets the page margins in points; returns the document. |

<a id="types-pdf-jpdffont"></a>

### JPDFFont

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bold` | property, bool, read-only | True if the font is bold; read-only, see style. |
| `calculatedSize` | property, float, read-only | The effective font size in points, with the default size if none is set; read-only. |
| `color` | property, writable | The Color of the font, or null; writable. |
| `family` | property, int, writable | The font family as one of pdf.COURIER, HELVETICA, TIMES_ROMAN, SYMBOL or ZAPFDINGBATS; writable, other values set Helvetica. |
| `familyname` | property, string, read-only | The name of the font family; read-only. |
| `italic` | property, bool, read-only | True if the font is italic; read-only, see style. |
| `size` | property, float, writable | The font size in points; writable. |
| `strikethru` | property, bool, read-only | True if the font is struck through; read-only, see style. |
| `style` | property, int, writable | The font style as bit flags, e.g. pdf.BOLD \| pdf.ITALIC; writable. |
| `underlined` | property, bool, read-only | True if the font is underlined; read-only, see style. |

<a id="types-pdf-jpdfheaderfooter"></a>

### JPDFHeaderFooter

| Member | Kind | Description |
| --- | --- | --- |
| `addPadding(padding)` | method | Adds the given amount in points to the padding; returns the header/footer. |
| `after` | property, read-only | The Phrase shown after the page number; read-only. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `before` | property, read-only | The Phrase shown before the page number; read-only. |
| `numbered` | property, bool, read-only | True if the page number is shown after the "before" text; read-only. |
| `setAlignment(alignment)` | method | Sets the horizontal alignment, e.g. pdf.ALIGN_CENTER; returns the header/footer. |
| `setPadding(padding)` | method | Sets the padding in points; returns the header/footer. |
| `setPageNumber(number)` | method | Sets the page number shown; returns the header/footer. |

<a id="types-pdf-jpdfimage"></a>

### JPDFImage

| Member | Kind | Description |
| --- | --- | --- |
| `absoluteX` | property, float, read-only | The absolute x position on the page in points; read-only, see setAbsolutePosition(). |
| `absoluteY` | property, float, read-only | The absolute y position on the page in points; read-only, see setAbsolutePosition(). |
| `alignment` | property, int, writable | The horizontal alignment of the image, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable. |
| `alt` | property, string, writable | The alternative text of the image; write-only, reading it always returns null. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `indentationLeft` | property, float, writable | The left indentation in points; writable. |
| `indentationRight` | property, float, writable | The right indentation in points; writable. |
| `plainHeight` | property, float, read-only | The height of the image in points after scaling, without rotation; read-only. |
| `plainWidth` | property, float, read-only | The width of the image in points after scaling, without rotation; read-only. |
| `scaleAbsolute(width, height)` | method | Scales the image to exactly this width and height in points; returns the image. |
| `scalePercent(percentX, percentY)` | method | Scales the image by a percentage of its original size; with one argument, both directions use it; returns the image. |
| `scaleToFit(width, height)` | method | Scales the image proportionally to fit into the given width and height in points; returns the image. |
| `scaledHeight` | property, float, read-only | The height of the image in points after scaling and rotation; read-only. |
| `scaledWidth` | property, float, read-only | The width of the image in points after scaling and rotation; read-only. |
| `setAbsolutePosition(x, y)` | method | Places the image at a fixed position on the page, in points from the lower left corner; returns the image. |
| `setRotationDegrees(degrees)` | method | Rotates the image by the given angle in degrees; returns the image. |
| `spacingAfter` | property, float, writable | The space after the image in points; writable. |
| `spacingBefore` | property, float, writable | The space before the image in points; writable. |
| `widthPercentage` | property, float, writable | The width of the image as a percentage of the available width; writable. |

<a id="types-pdf-jpdflist"></a>

### JPDFList

| Member | Kind | Description |
| --- | --- | --- |
| `add(item)` | method | Adds a ListItem to the list; returns the list. |
| `alignindent` | property, bool, writable | True if the indentation of all items is aligned; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoindent` | property, bool, writable | True if the symbol indentation is calculated from the widest list symbol; writable. |
| `empty` | property, bool, read-only | True if the list has no items; read-only. |
| `first` | property, int, writable | The number (or letter position) an ordered list starts with; writable. |
| `indentationLeft` | property, float, writable | The left indentation of the list in points; writable. |
| `indentationRight` | property, float, writable | The right indentation of the list in points; writable. |
| `lettered` | property, bool, writable | True if an ordered list uses letters instead of numbers; writable. |
| `lowercase` | property, bool, writable | True if a lettered list uses lowercase letters; writable. |
| `numbered` | property, bool, writable | True for an ordered list with numbers (or letters), false for a bulleted list; writable. |
| `setListSymbol(symbol)` | method | Sets the symbol (e.g. a bullet) used for the items of an unordered list; returns the list. |
| `setPostSymbol(text)` | method | Sets the text shown after the number or letter of an ordered list; returns the list. |
| `setPreSymbol(text)` | method | Sets the text shown before the number or letter of an ordered list; returns the list. |
| `size` | property, int, read-only | The number of items in the list; read-only. |
| `symbolIndent` | property, float, writable | The indentation reserved for the list symbol in points; writable. |

<a id="types-pdf-jpdflistitem"></a>

### JPDFListItem

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Chunk, Phrase, ...) to the phrase; returns the phrase. |
| `alignment` | property, int, writable | The horizontal alignment, one of pdf.ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT or ALIGN_JUSTIFIED; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only | True if the phrase contains no content; read-only. |
| `font` | property, writable | The Font of the phrase; writable. |
| `indentationLeft` | property, float, writable | The left indentation in points; writable. |
| `indentationRight` | property, float, writable | The right indentation in points; writable. |
| `leading` | property, float, writable | The line spacing (distance between baselines) in points; writable. |
| `listSymbol` | property, writable | The symbol (bullet or number) in front of the item, as a Chunk; writable. |
| `size` | property, int, read-only | The number of elements in the phrase; read-only. |

<a id="types-pdf-jpdfmeta"></a>

### JPDFMeta

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, read-only | The value of the meta-information; read-only. |
| `name` | property, string, read-only | The name (tag) of the meta-information, e.g. title or author; read-only. |

<a id="types-pdf-jpdfpcell"></a>

### JPDFPCell

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `backgroundColor` | property, writable | The background Color of the cell, or null; writable. |
| `border` | property, int, writable | The sides that have a border, as pdf.BORDER_* flags combined with \|; writable. |
| `borderWidth` | property, float, writable | The width of the border in points; writable. |
| `colspan` | property, int, writable | The number of columns the cell spans; writable. |
| `fixedHeight` | property, float, writable | The fixed height of the cell in points; writable. |
| `horizontalAlignment` | property, int, writable | The horizontal alignment of the content, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable. |
| `minimumHeight` | property, float, writable | The minimum height of the cell in points; writable. |
| `noWrap` | property, bool, writable | True if the text in the cell is not wrapped; writable. |
| `paddingBottom` | property, float, writable | The bottom padding in points; writable. |
| `paddingLeft` | property, float, writable | The left padding in points; writable. |
| `paddingRight` | property, float, writable | The right padding in points; writable. |
| `paddingTop` | property, float, writable | The top padding in points; writable. |
| `rowspan` | property, int, writable | The number of rows the cell spans; writable. |
| `setImage(image)` | method | Sets the content of the cell to the Image; returns the cell. |
| `setPadding(padding)` | method | Sets the padding on all four sides in points; returns the cell. |
| `setPhrase(phrase)` | method | Sets the content of the cell to the Phrase; returns the cell. |
| `useAscender` | property, bool, writable | True if the font's ascender is used to position the first line of text; writable. |
| `useBorderPadding` | property, bool, writable | True if the border width is added to the padding; writable. |
| `useDescender` | property, bool, writable | True if the font's descender is taken into account below the last line of text; writable. |
| `verticalAlignment` | property, int, writable | The vertical alignment of the content, one of pdf.ALIGN_TOP, ALIGN_MIDDLE or ALIGN_BOTTOM; writable. |

<a id="types-pdf-jpdfptable"></a>

### JPDFPTable

| Member | Kind | Description |
| --- | --- | --- |
| `addCell(cell)` | method | Adds the next cell, given as a PCell, Phrase or string; returns the table. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `completeRow()` | method | Fills the current, incomplete row with empty default cells. |
| `deleteBodyRows()` | method | Deletes all rows except the header rows. |
| `deleteLastRow()` | method | Deletes the last row; returns true if a row was deleted. |
| `deleteRow(index)` | method | Deletes the row with the zero-based index; returns true if a row was deleted. |
| `headerRows` | property, int, writable | The number of leading rows that are repeated as a header on every page; writable. |
| `horizontalAlignment` | property, int, writable | The horizontal alignment of the table on the page, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable. |
| `lockedWidth` | property, bool, writable | True if the table uses its totalWidth instead of widthPercentage; writable. |
| `numberOfColumns` | property, int, read-only | The number of columns of the table; read-only. |
| `setSpacingAfter(spacing)` | method | Sets the space after the table in points; returns the table. |
| `setSpacingBefore(spacing)` | method | Sets the space before the table in points; returns the table. |
| `setTotalWidth(width)` | method | Sets the absolute width of the table in points, used when lockedWidth is true; returns the table. |
| `setWidths(widths)` | method | Sets the relative widths of the columns from a list of numbers, one per column; returns the table. |
| `size` | property, int, read-only | The number of complete rows in the table; read-only. |
| `skipFirstHeader` | property, bool, writable | True if the header rows are not shown on the first page of the table; writable. |
| `skipLastFooter` | property, bool, writable | True if the footer rows are not shown on the last page of the table; writable. |
| `splitRows` | property, bool, writable | True if a row may be split across pages; writable. |
| `totalWidth` | property, float, read-only | The absolute width of the table in points; read-only, see setTotalWidth(). |
| `widthPercentage` | property, float, writable | The width of the table as a percentage of the available width; writable. |

<a id="types-pdf-jpdfparagraph"></a>

### JPDFParagraph

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Chunk, Phrase, ...) to the phrase; returns the phrase. |
| `alignment` | property, int, writable | The horizontal alignment, one of pdf.ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT or ALIGN_JUSTIFIED; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only | True if the phrase contains no content; read-only. |
| `extraParagraphSpace` | property, float, writable |  |
| `firstLineIndent` | property, float, writable | The extra indentation of the first line in points; writable. |
| `font` | property, writable | The Font of the phrase; writable. |
| `indentationLeft` | property, float, writable | The left indentation in points; writable. |
| `indentationRight` | property, float, writable | The right indentation in points; writable. |
| `keepTogether` | property, bool, writable | True if the paragraph should not be split across pages; writable. |
| `leading` | property, float, writable | The line spacing (distance between baselines) in points; writable. |
| `multipliedLeading` | property, float, writable | The line spacing as a factor of the font size; writable. |
| `runDirection` | property, int, writable |  |
| `size` | property, int, read-only | The number of elements in the phrase; read-only. |
| `spacingAfter` | property, float, writable | The space after the paragraph in points; writable. |
| `spacingBefore` | property, float, writable | The space before the paragraph in points; writable. |

<a id="types-pdf-jpdfphrase"></a>

### JPDFPhrase

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Chunk, Phrase, ...) to the phrase; returns the phrase. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only | True if the phrase contains no content; read-only. |
| `font` | property, writable | The Font of the phrase; writable. |
| `leading` | property, float, writable | The line spacing (distance between baselines) in points; writable. |
| `size` | property, int, read-only | The number of elements in the phrase; read-only. |

<a id="types-pdf-jpdfreader"></a>

### JPDFReader

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close()` | method | Closes the reader and releases the PDF. |
| `encrypted` | property, bool, read-only | True if the PDF is encrypted; read-only. |
| `numberOfPages` | property, int, read-only | The number of pages of the PDF; read-only. |
| `pdfVersion` | property, string, read-only | The PDF version from the file header, e.g. "4" for PDF 1.4; read-only. |
| `rebuilt` | property, bool, read-only | True if the reader had to rebuild the file's cross-reference table, i.e. the PDF was damaged; read-only. |

<a id="types-pdf-jpdfrectangle"></a>

### JPDFRectangle

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `backgroundColor` | property, writable | The background Color, or null; writable, except for page sizes. |
| `border` | property, int, writable | The sides that have a border, as pdf.BORDER_* flags combined with \|; writable, except for page sizes. |
| `borderColor` | property, writable | The Color of the border, or null; writable, except for page sizes. |
| `borderWidth` | property, float, writable | The width of the border in points; writable, except for page sizes. |
| `bottom` | property, float, writable | The y coordinate of the bottom edge in points; writable, except for page sizes. |
| `height` | property, float, read-only | The height of the rectangle in points; read-only. |
| `left` | property, float, writable | The x coordinate of the left edge in points; writable, except for page sizes. |
| `right` | property, float, writable | The x coordinate of the right edge in points; writable, except for page sizes. |
| `rotate()` | method | Returns a new rectangle rotated by 90 degrees, e.g. to turn a page size into landscape. |
| `rotation` | property, int, writable | The rotation in degrees; writable, except for page sizes. |
| `top` | property, float, writable | The y coordinate of the top edge in points; writable, except for page sizes. |
| `width` | property, float, read-only | The width of the rectangle in points; read-only. |

<a id="types-pdf-jpdfsection"></a>

### JPDFSection

| Member | Kind | Description |
| --- | --- | --- |
| `add(element)` | method | Adds a pdf element (Paragraph, List, PTable, ...) to the section; returns the section. |
| `addSection(title, numberDepth)` | method | Adds a nested Section with the title and returns it; numberDepth is optional, see numberDepth. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bookmarkOpen` | property, bool, writable | True if the section's bookmark (outline entry) is shown expanded; writable. |
| `indentation` | property, float, writable | The indentation of the section content in points; writable. |
| `indentationLeft` | property, float, writable | The left indentation in points; writable. |
| `indentationRight` | property, float, writable | The right indentation in points; writable. |
| `numberDepth` | property, int, writable | How many levels of the section number are shown in the title, e.g. 2 for "1.2."; 0 shows no number; writable. |
| `numberStyle` | property, int, writable |  |
| `title` | property, writable | The title of the section as a Paragraph; writable. |
| `triggerNewPage` | property, bool, writable | True if the section starts on a new page; writable. |

<a id="types-pdf-jpdfwriter"></a>

### JPDFWriter

| Member | Kind | Description |
| --- | --- | --- |
| `addJavaScript(code)` | method | Adds document-level JavaScript code to the PDF; returns the writer. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bytes` | property, read-only | The finished PDF as binary for a writer created without a path, available once the document has been closed; null for a writer that writes to a file; read-only. |
| `close()` | method | Closes the writer. |

<a id="types-mustang"></a>

## mustang

<a id="types-mustang-mustangallowance"></a>

### MustangAllowance

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable | The amount the percentage applies to; writable. |
| `basisQuantity` | property, int, writable |  |
| `isCharge` | property, bool, read-only | True for a charge (surcharge), false for an allowance (discount); read-only. |
| `percent` | property, int, writable | The amount as a percentage of the basis amount; writable. |
| `reason` | property, string, writable | The reason as free text; writable. |
| `reasonCode` | property, string, writable | The reason as a code; writable. |
| `sequenceNumeric` | property, int, writable |  |
| `setBasisAmount(amount)` | method | Sets the amount the percentage applies to. Returns this charge or allowance. |
| `setBasisQuantity(...)` | method |  |
| `setPercent(percent)` | method | Sets the amount as a percentage of the basis amount. Returns this charge or allowance. |
| `setReason(text)` | method | Sets the reason as free text. Returns this charge or allowance. |
| `setReasonCode(code)` | method | Sets the reason as a code. Returns this charge or allowance. |
| `setSequenceNumeric(...)` | method |  |
| `setTaxAllowanceChargeBasisAmount(...)` | method |  |
| `setTaxBasisAmount(...)` | method |  |
| `setTaxCalculatedAmount(...)` | method |  |
| `setTaxCategoryCode(code)` | method | Sets the VAT category code, e.g. "S" for the standard rate. Returns this charge or allowance. |
| `setTaxDueDateTypeCode(...)` | method |  |
| `setTaxExemptionReason(text)` | method | Sets the reason for a VAT exemption as free text. Returns this charge or allowance. |
| `setTaxExemptionReasonCode(code)` | method | Sets the reason for a VAT exemption as a code. Returns this charge or allowance. |
| `setTaxLineTotalBasisAmount(...)` | method |  |
| `setTaxPointDate(...)` | method |  |
| `setTaxRateApplicablePercent(percent)` | method | Sets the VAT rate in percent. Returns this charge or allowance. |
| `setTotalAmount(amount)` | method | Sets the absolute amount. Returns this charge or allowance. |
| `taxAllowanceChargeBasisAmount` | property, int, writable |  |
| `taxBasisAmount` | property, int, writable |  |
| `taxCalculatedAmount` | property, int, writable |  |
| `taxCategoryCode` | property, string, writable | The VAT category code, e.g. "S" for the standard rate; writable. |
| `taxDueDateTypeCode` | property, string, writable |  |
| `taxExemptionReason` | property, string, writable | The reason for a VAT exemption as free text; writable. |
| `taxExemptionReasonCode` | property, string, writable | The reason for a VAT exemption as a code; writable. |
| `taxLineTotalBasisAmount` | property, int, writable |  |
| `taxPointDate` | property, date, writable |  |
| `taxRateApplicablePercent` | property, int, writable | The VAT rate in percent; writable. |
| `totalAmount` | property, int, writable | The absolute amount, or null while neither it nor a percentage is set; writable. |

<a id="types-mustang-mustangbankdetails"></a>

### MustangBankDetails

| Member | Kind | Description |
| --- | --- | --- |
| `accountName` | property, string, writable | The name of the account holder; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bic` | property, string, writable | The BIC of the bank; writable. |
| `iban` | property, string, writable | The IBAN of the account; writable. |
| `paymentMeansCode` | property, string, writable | The UNTDID 4461 payment means code, e.g. "58" for a SEPA credit transfer; writable. |
| `paymentMeansInformation` | property, string, writable | A free-text description of the payment means; writable. |
| `setAccountName(name)` | method | Sets the name of the account holder. Returns these bank details. |
| `setBic(bic)` | method | Sets the BIC of the bank. Returns these bank details. |
| `setIban(iban)` | method | Sets the IBAN of the account. Returns these bank details. |
| `setPaymentMeansCode(code)` | method | Sets the UNTDID 4461 payment means code, e.g. "58" for a SEPA credit transfer. Returns these bank details. |
| `setPaymentMeansInformation(text)` | method | Sets a free-text description of the payment means. Returns these bank details. |

<a id="types-mustang-mustangcashdiscount"></a>

### MustangCashDiscount

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable | The amount the discount percentage applies to; writable. |
| `days` | property, int, writable | The number of days within which the payment must be made to get the discount, or null; writable. |
| `percent` | property, int, writable | The discount in percent; writable. |
| `setBasisAmount(amount)` | method | Sets the amount the discount percentage applies to. Returns this cash discount. |
| `setDays(days)` | method | Sets the number of days within which the payment must be made to get the discount. Returns this cash discount. |
| `setPercent(percent)` | method | Sets the discount in percent. Returns this cash discount. |

<a id="types-mustang-mustangcharge"></a>

### MustangCharge

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable | The amount the percentage applies to; writable. |
| `basisQuantity` | property, int, writable |  |
| `isCharge` | property, bool, read-only | True for a charge (surcharge), false for an allowance (discount); read-only. |
| `percent` | property, int, writable | The amount as a percentage of the basis amount; writable. |
| `reason` | property, string, writable | The reason as free text; writable. |
| `reasonCode` | property, string, writable | The reason as a code; writable. |
| `sequenceNumeric` | property, int, writable |  |
| `setBasisAmount(amount)` | method | Sets the amount the percentage applies to. Returns this charge or allowance. |
| `setBasisQuantity(...)` | method |  |
| `setPercent(percent)` | method | Sets the amount as a percentage of the basis amount. Returns this charge or allowance. |
| `setReason(text)` | method | Sets the reason as free text. Returns this charge or allowance. |
| `setReasonCode(code)` | method | Sets the reason as a code. Returns this charge or allowance. |
| `setSequenceNumeric(...)` | method |  |
| `setTaxAllowanceChargeBasisAmount(...)` | method |  |
| `setTaxBasisAmount(...)` | method |  |
| `setTaxCalculatedAmount(...)` | method |  |
| `setTaxCategoryCode(code)` | method | Sets the VAT category code, e.g. "S" for the standard rate. Returns this charge or allowance. |
| `setTaxDueDateTypeCode(...)` | method |  |
| `setTaxExemptionReason(text)` | method | Sets the reason for a VAT exemption as free text. Returns this charge or allowance. |
| `setTaxExemptionReasonCode(code)` | method | Sets the reason for a VAT exemption as a code. Returns this charge or allowance. |
| `setTaxLineTotalBasisAmount(...)` | method |  |
| `setTaxPointDate(...)` | method |  |
| `setTaxRateApplicablePercent(percent)` | method | Sets the VAT rate in percent. Returns this charge or allowance. |
| `setTotalAmount(amount)` | method | Sets the absolute amount. Returns this charge or allowance. |
| `taxAllowanceChargeBasisAmount` | property, int, writable |  |
| `taxBasisAmount` | property, int, writable |  |
| `taxCalculatedAmount` | property, int, writable |  |
| `taxCategoryCode` | property, string, writable | The VAT category code, e.g. "S" for the standard rate; writable. |
| `taxDueDateTypeCode` | property, string, writable |  |
| `taxExemptionReason` | property, string, writable | The reason for a VAT exemption as free text; writable. |
| `taxExemptionReasonCode` | property, string, writable | The reason for a VAT exemption as a code; writable. |
| `taxLineTotalBasisAmount` | property, int, writable |  |
| `taxPointDate` | property, date, writable |  |
| `taxRateApplicablePercent` | property, int, writable | The VAT rate in percent; writable. |
| `totalAmount` | property, int, writable | The absolute amount, or null while neither it nor a percentage is set; writable. |

<a id="types-mustang-mustangcontact"></a>

### MustangContact

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `country` | property, string, writable | The country code, e.g. "DE"; writable. |
| `email` | property, string, writable | The email address; writable. |
| `fax` | property, string, writable | The fax number; writable. |
| `location` | property, string, writable | The city; writable. |
| `name` | property, string, writable | The contact person's name; writable. |
| `phone` | property, string, writable | The phone number; writable. |
| `setCountry(country)` | method | Sets the country code, e.g. "DE". Returns this contact. |
| `setEmail(email)` | method | Sets the email address. Returns this contact. |
| `setFax(fax)` | method | Sets the fax number. Returns this contact. |
| `setLocation(city)` | method | Sets the city. Returns this contact. |
| `setName(name)` | method | Sets the contact person's name. Returns this contact. |
| `setPhone(phone)` | method | Sets the phone number. Returns this contact. |
| `setStreet(street)` | method | Sets the street. Returns this contact. |
| `setZip(zip)` | method | Sets the postal code. Returns this contact. |
| `street` | property, string, writable | The street; writable. |
| `zip` | property, string, writable | The postal code; writable. |

<a id="types-mustang-mustangdirectdebit"></a>

### MustangDirectDebit

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `iban` | property, string, writable | The IBAN of the debited account; writable. |
| `mandate` | property, string, writable | The reference of the mandate that authorizes the debit; writable. |
| `paymentMeansCode` | property, string, writable | The UNTDID 4461 payment means code, e.g. "59" for a SEPA direct debit; writable. |
| `paymentMeansInformation` | property, string, writable | A free-text description of the payment means; writable. |
| `setIban(iban)` | method | Sets the IBAN of the debited account. Returns this direct debit. |
| `setMandate(mandate)` | method | Sets the reference of the mandate that authorizes the debit. Returns this direct debit. |
| `setPaymentMeansCode(code)` | method | Sets the UNTDID 4461 payment means code, e.g. "59" for a SEPA direct debit. Returns this direct debit. |
| `setPaymentMeansInformation(text)` | method | Sets a free-text description of the payment means. Returns this direct debit. |

<a id="types-mustang-mustangexporter"></a>

### MustangExporter

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `attachFile(attachment)` | method | Attaches an additional file to the PDF. Returns this exporter. |
| `close()` | method | Closes the exporter and its PDF. |
| `disableAutoClose(yes)` | method | If true, the PDF is not closed after export, so close() must be called. Returns this exporter. |
| `disableFacturX(...)` | method |  |
| `export(path)` | method | Writes the PDF with the embedded invoice data to a file. |
| `ignorePDFAErrors()` | method | Accepts a source PDF that is not valid PDF/A; only works for exporters from PDF/A-3, otherwise it only logs a warning. Returns this exporter. |
| `load(path)` | method | Loads the PDF file to embed the invoice into. Returns this exporter. |
| `setCreator(creator)` | method | Sets the creator in the PDF's metadata. Returns this exporter. |
| `setFacturX(...)` | method |  |
| `setProducer(producer)` | method | Sets the producer in the PDF's metadata. Returns this exporter. |
| `setProfile(profileName)` | method | Sets the ZUGFeRD/Factur-X profile by name; on other exporter types, it only logs a warning. Returns this exporter. |
| `setTransaction(invoice)` | method | Sets the invoice whose data is embedded into the PDF. Returns this exporter. |
| `setVersion(version)` | method | Same as setZUGFeRDVersion(version). Returns this exporter. |
| `setZUGFeRDVersion(version)` | method | Sets the ZUGFeRD version to write, as a number. Returns this exporter. |

<a id="types-mustang-mustangfileattachment"></a>

### MustangFileAttachment

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `description` | property, string, writable | A description of the attachment; writable. |
| `filename` | property, string, writable | The file name of the attachment; writable. |
| `getData()` | method | Returns the content of the file as binary data, or null if none is set. |
| `mimetype` | property, string, writable | The MIME type, e.g. "application/pdf"; writable. |
| `relation` | property, string, writable | The PDF/A-3 relationship of the attachment to the document; writable. |
| `setData(data)` | method | Sets the content of the file as binary data. Returns this attachment. |
| `setDescription(description)` | method | Sets a description of the attachment. Returns this attachment. |
| `setFilename(filename)` | method | Sets the file name of the attachment. Returns this attachment. |
| `setMimetype(mimetype)` | method | Sets the MIME type, e.g. "application/pdf". Returns this attachment. |
| `setRelation(relation)` | method | Sets the PDF/A-3 relationship of the attachment to the document. Returns this attachment. |

<a id="types-mustang-mustangincludednote"></a>

### MustangIncludedNote

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, writable | The text of the note; writable. |
| `setContent(text)` | method | Sets the text of the note. Returns this note. |
| `setSubjectCode(code)` | method | Sets the UNTDID 4451 subject code, e.g. "AAI" or "REG" (case-insensitive); without a code or with a blank one, removes it. Returns this note. |
| `subjectCode` | property, string, writable | The UNTDID 4451 subject code, e.g. "AAI" or "REG", or null; writable, case-insensitive. |

<a id="types-mustang-mustanginvoice"></a>

### MustangInvoice

| Member | Kind | Description |
| --- | --- | --- |
| `addAllowance(allowance)` | method | Adds a document-level allowance (discount). Returns this invoice. |
| `addCashDiscount(cashDiscount)` | method | Adds an early-payment discount. Returns this invoice. |
| `addCharge(charge)` | method | Adds a document-level charge (surcharge). Returns this invoice. |
| `addCustomsNote(text)` | method | Adds a note with the subject code for customs information. Returns this invoice. |
| `addDiscountBonusNote(text)` | method | Adds a note with the subject code for discounts and bonuses. Returns this invoice. |
| `addGeneralNote(text)` | method | Adds a note with the subject code for general information. Returns this invoice. |
| `addIncludedNote(note)` | method | Adds a note object with a subject code; see getNotes(). Returns this invoice. |
| `addIntroductionNote(text)` | method | Adds a note with the subject code for an introductory text. Returns this invoice. |
| `addInvoiceReferencedDocument(document)` | method | Adds a reference to a preceding invoice, e.g. the one being corrected. Returns this invoice. |
| `addItem(item)` | method | Adds an item (line) to the invoice. Returns this invoice. |
| `addLegalNote(text)` | method | Adds a note with the subject code for legal information. Returns this invoice. |
| `addNote(text)` | method | Adds a plain-text note without subject code; see getPlainNotes(). Returns this invoice. |
| `addPaymentTerms(...)` | method |  |
| `addRegulatoryNote(text)` | method | Adds a note with the subject code for regulatory information. Returns this invoice. |
| `addSellerNote(text)` | method | Adds a note with the subject code for remarks by the seller. Returns this invoice. |
| `addTaxNote(text)` | method | Adds a note with the subject code for tax information. Returns this invoice. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `businessProcessId` | property, string, writable |  |
| `buyerOrderReferencedDocument` | property, writable | The reference to the buyer's order, or null; writable. |
| `contractReferencedDocument` | property, writable | The reference to the contract, or null; writable. |
| `creditorReferenceId` | property, string, writable | The SEPA creditor identifier for direct debits; writable. |
| `currency` | property, string, writable | The invoice currency as an ISO 4217 code, e.g. "EUR"; writable. |
| `deliveryAddress` | property, writable | The party and address the goods are delivered to, or null; writable. |
| `deliveryDate` | property, date, writable | The date of delivery, or null; writable. |
| `deliveryNoteReferencedDocument` | property, writable | The reference to the delivery note, or null; writable. |
| `deliveryTypeCode` | property, string, writable |  |
| `despatchAdviceReferencedDocument` | property, writable | The reference to the despatch advice, or null; writable. |
| `detailedDeliveryPeriodFrom` | property, date, writable | The first day of the delivery (service) period, or null; writable. |
| `detailedDeliveryPeriodTo` | property, date, writable | The last day of the delivery (service) period, or null; writable. |
| `documentCode` | property, string, read-only | The UNTDID 1001 document type code, e.g. "380" for an invoice; read-only. |
| `documentName` | property, string, writable | The name (title) of the document; writable. |
| `dueDate` | property, date, writable | The date by which the invoice must be paid; writable. |
| `embedFileInXML(attachment)` | method | Embeds a file into the invoice XML as an additional referenced document. Returns this invoice. |
| `endCustomerDeliveryAddress` | property, writable |  |
| `exportXmlEn16931()` | method | Returns the invoice as ZUGFeRD/Factur-X XML in the EN 16931 profile, as a string. |
| `getAdditionalReferencedDocuments()` | method | Returns a list of the files embedded into the invoice XML. |
| `getAllowances()` | method | Returns a list of the document-level allowances (discounts). |
| `getCashDiscounts()` | method | Returns a list of the invoice's early-payment discounts. |
| `getCharges()` | method | Returns a list of the document-level charges (surcharges). |
| `getExtendedPaymentTerms(...)` | method |  |
| `getInvoiceReferencedDocuments()` | method | Returns a list of the references to preceding invoices. |
| `getItems()` | method | Returns a list of the invoice's items (lines). |
| `getNotes()` | method | Returns a list of the notes with subject codes as note objects; plain notes are in getPlainNotes(). |
| `getObjectIdentifierReferencedDocument(...)` | method |  |
| `getPlainNotes()` | method | Returns a list of the notes added with addNote(), as strings. |
| `getRelatedReferencedDocument(...)` | method |  |
| `getTenderReferencedDocument()` | method | Returns the reference to the tender or lot, or null. |
| `invoicee` | property, writable |  |
| `invoicer` | property, writable |  |
| `issueDate` | property, date, writable | The invoice date; writable. |
| `number` | property, string, writable | The invoice number; writable. |
| `ownCountry` | property, string, read-only | The seller's country; read-only. |
| `ownForeignOrganisationID` | property, string, writable |  |
| `ownLocation` | property, string, read-only | The seller's city; read-only. |
| `ownOrganisationFullPlaintextInfo` | property, string, writable |  |
| `ownOrganisationName` | property, string, writable | The seller's name; writable. |
| `ownStreet` | property, string, read-only | The seller's street; read-only. |
| `ownTaxID` | property, string, writable | The seller's tax number; writable. |
| `ownVATID` | property, string, writable | The seller's VAT ID; writable. |
| `ownZIP` | property, string, read-only | The seller's postal code; read-only. |
| `payee` | property, writable | The party that receives the payment, if it is not the seller, or null; writable. |
| `paymentReference` | property, string, writable | The reference the payer should give with the payment; writable. |
| `paymentTermDescription` | property, string, writable | The payment terms as free text; writable. |
| `paymentTerms` | property, writable | The payment terms, or null; writable. |
| `recipient` | property, writable | The recipient (buyer) of the invoice, or null; writable. |
| `referenceNumber` | property, string, writable | The buyer reference, e.g. the routing ID required by public buyers; writable. |
| `roundingAmount` | property, int, writable | The amount by which the total was rounded, or null; writable. |
| `sellerOrderReferencedDocument` | property, writable | The reference to the seller's order (order confirmation), or null; writable. |
| `sender` | property, writable | The sender (seller) of the invoice, or null; writable. |
| `setBusinessProcessId(...)` | method |  |
| `setBuyerOrderReferencedDocument(document)` | method | Sets the reference to the buyer's order. Returns this invoice. |
| `setContractReferencedDocument(document)` | method | Sets the reference to the contract. Returns this invoice. |
| `setCorrection(number)` | method | Makes this invoice a correction of the invoice with the given number. Returns this invoice. |
| `setCreditNote(...)` | method |  |
| `setCreditorReferenceId(id)` | method | Sets the SEPA creditor identifier for direct debits. Returns this invoice. |
| `setCurrency(currency)` | method | Sets the invoice currency as an ISO 4217 code, e.g. "EUR". Returns this invoice. |
| `setDeliveryAddress(tradeParty)` | method | Sets the party and address the goods are delivered to. Returns this invoice. |
| `setDeliveryDate(date)` | method | Sets the date of delivery. Returns this invoice. |
| `setDeliveryNoteReferencedDocument(document)` | method | Sets the reference to the delivery note. Returns this invoice. |
| `setDeliveryTypeCode(...)` | method |  |
| `setDespatchAdviceReferencedDocument(document)` | method | Sets the reference to the despatch advice. Returns this invoice. |
| `setDetailedDeliveryPeriod(fromDate, toDate)` | method | Sets the delivery (service) period. Returns this invoice. |
| `setDetailedDeliveryPeriodFrom(...)` | method |  |
| `setDetailedDeliveryPeriodTo(date)` | method | Sets the last day of the delivery (service) period. Returns this invoice. |
| `setDocumentName(name)` | method | Sets the name (title) of the document. Returns this invoice. |
| `setDueDate(date)` | method | Sets the date by which the invoice must be paid. Returns this invoice. |
| `setEndCustomerDeliveryAddress(...)` | method |  |
| `setInvoicee(...)` | method |  |
| `setInvoicer(...)` | method |  |
| `setIssueDate(date)` | method | Sets the invoice date. Returns this invoice. |
| `setNumber(number)` | method | Sets the invoice number. Returns this invoice. |
| `setObjectIdentifierReferencedDocument(...)` | method |  |
| `setPayee(tradeParty)` | method | Sets the party that receives the payment, if it is not the seller. Returns this invoice. |
| `setPaymentReference(reference)` | method | Sets the reference the payer should give with the payment. Returns this invoice. |
| `setPaymentTermDescription(text)` | method | Sets the payment terms as free text. Returns this invoice. |
| `setPaymentTerms(paymentTerms)` | method | Sets the payment terms. Returns this invoice. |
| `setRecipient(tradeParty)` | method | Sets the recipient (buyer) of the invoice. Returns this invoice. |
| `setReferenceNumber(reference)` | method | Sets the buyer reference, e.g. the routing ID required by public buyers. Returns this invoice. |
| `setRelatedReferencedDocument(...)` | method |  |
| `setRoundingAmount(amount)` | method | Sets the amount by which the total was rounded. Returns this invoice. |
| `setSellerOrderReferencedDocument(document)` | method | Sets the reference to the seller's order (order confirmation). Returns this invoice. |
| `setSender(tradeParty)` | method | Sets the sender (seller) of the invoice. Returns this invoice. |
| `setShipToCountry(country)` | method | Sets the country of the ship-to address. Returns this invoice. |
| `setShipToLocation(city)` | method | Sets the city of the ship-to address. Returns this invoice. |
| `setShipToOrganisationId(...)` | method |  |
| `setShipToOrganisationName(name)` | method | Sets the name of the organisation the goods are shipped to. Returns this invoice. |
| `setShipToStreet(street)` | method | Sets the street of the ship-to address. Returns this invoice. |
| `setShipToZip(zip)` | method | Sets the postal code of the ship-to address. Returns this invoice. |
| `setSpecifiedProcuringProjectId(id)` | method | Sets the ID of the project the invoice refers to. Returns this invoice. |
| `setSpecifiedProcuringProjectName(name)` | method | Sets the name of the project the invoice refers to. Returns this invoice. |
| `setTaxConversionRate(...)` | method |  |
| `setTaxConversionRateDateTime(...)` | method |  |
| `setTaxCurrency(currency)` | method | Sets the currency for the VAT amounts if it differs from the invoice currency. Returns this invoice. |
| `setTaxRepresentative(tradeParty)` | method | Sets the seller's tax representative. Returns this invoice. |
| `setTenderReferencedDocument(document)` | method | Sets the reference to the tender or lot. Returns this invoice. |
| `setTestIndicator()` | method | Marks the invoice as a test. Returns this invoice. |
| `setTotalPrepaidAmount(amount)` | method | Sets the amount that has already been paid. Returns this invoice. |
| `setVatDueDateTypeCode(...)` | method |  |
| `shipToCountry` | property, string, writable | The country of the ship-to address; writable. |
| `shipToLocation` | property, string, writable | The city of the ship-to address; writable. |
| `shipToOrganisationId` | property, string, writable |  |
| `shipToOrganisationName` | property, string, writable | The name of the organisation the goods are shipped to; writable. |
| `shipToStreet` | property, string, writable | The street of the ship-to address; writable. |
| `shipToZip` | property, string, writable | The postal code of the ship-to address; writable. |
| `specifiedProcuringProjectId` | property, string, writable | The ID of the project the invoice refers to; writable. |
| `specifiedProcuringProjectName` | property, string, writable | The name of the project the invoice refers to; writable. |
| `taxConversionRate` | property, int, writable |  |
| `taxConversionRateDateTime` | property, date, writable |  |
| `taxCurrency` | property, string, writable | The currency for the VAT amounts if it differs from the invoice currency, or null; writable. |
| `taxRepresentative` | property, writable | The seller's tax representative, or null; writable. |
| `testIndicator` | property, bool, read-only | True if the invoice is marked as a test; read-only. |
| `totalPrepaidAmount` | property, int, writable | The amount that has already been paid, or null; writable. |
| `valid` | property, bool, read-only |  |
| `vatDueDateTypeCode` | property, string, writable |  |

<a id="types-mustang-mustangitem"></a>

### MustangItem

| Member | Kind | Description |
| --- | --- | --- |
| `accountingReference` | property, string, writable | The buyer's accounting reference for the line; writable. |
| `addAdditionalReference(...)` | method |  |
| `addAllowance(allowance)` | method | Adds an allowance (discount) to the line. Returns this item. |
| `addCharge(charge)` | method | Adds a charge (surcharge) to the line. Returns this item. |
| `addIncludedNote(note)` | method | Adds a note, possibly with a subject code, to the line. Returns this item. |
| `addNote(text)` | method | Adds a free-text note to the line. Returns this item. |
| `addReferencedDocument(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisQuantity` | property, int, writable | The quantity the price refers to, e.g. 100 for a price per hundred; writable. |
| `buyerOrderReferencedDocument` | property, writable | The reference to the buyer's order for this line, or null; writable. |
| `contractReferencedDocument` | property, writable | The reference to the contract for this line, or null; writable. |
| `deliveryNoteReferencedDocument` | property, writable | The reference to the delivery note for this line, or null; writable. |
| `despatchAdviceReferencedDocument` | property, writable | The reference to the despatch advice for this line, or null; writable. |
| `detailedDeliveryPeriodFrom` | property, date, read-only | The first day of the line's delivery period, or null; read-only. |
| `detailedDeliveryPeriodTo` | property, date, read-only | The last day of the line's delivery period, or null; read-only. |
| `getAdditionalReferences(...)` | method |  |
| `getAllowances()` | method | Returns a list of the line's allowances (discounts). |
| `getCharges()` | method | Returns a list of the line's charges (surcharges). |
| `getNotes()` | method | Returns a list of the line's notes as note objects with content and subject code. |
| `getPlainNotes()` | method | Returns a list of the texts of the line's notes as strings. |
| `getReferencedDocuments(...)` | method |  |
| `grossPrice` | property, int, writable | The unit price before item allowances and charges; writable. |
| `id` | property, string, writable | The ID of the line; writable. |
| `lineSeller` | property, writable | The seller of this line, if it differs from the invoice's seller, or null; writable. |
| `lineStatusReasonCode` | property, string, writable |  |
| `lineTotalAmount` | property, int, writable | The net total amount of the line; writable. |
| `parentLineId` | property, string, writable | The ID of the line this line belongs to, or null; writable. |
| `price` | property, int, writable | The net unit price; writable. |
| `product` | property, writable | The product of the line, or null; writable. |
| `quantity` | property, int, writable | The invoiced quantity; writable. |
| `sellerOrderReferencedDocument` | property, writable | The reference to the seller's order (order confirmation) for this line, or null; writable. |
| `setAccountingReference(reference)` | method | Sets the buyer's accounting reference for the line. Returns this item. |
| `setBasisQuantity(quantity)` | method | Sets the quantity the price refers to, e.g. 100 for a price per hundred. Returns this item. |
| `setBuyerOrderReferencedDocument(document)` | method | Sets the reference to the buyer's order for this line. Returns this item. |
| `setContractReferencedDocument(document)` | method | Sets the reference to the contract for this line. Returns this item. |
| `setDeliveryNoteReferencedDocument(document)` | method | Sets the reference to the delivery note for this line. Returns this item. |
| `setDespatchAdviceReferencedDocument(document)` | method | Sets the reference to the despatch advice for this line. Returns this item. |
| `setDetailedDeliveryPeriod(fromDate, toDate)` | method | Sets the delivery period of the line. Returns this item. |
| `setGrossPrice(price)` | method | Sets the unit price before item allowances and charges. Returns this item. |
| `setId(id)` | method | Sets the ID of the line. Returns this item. |
| `setLineSeller(tradeParty)` | method | Sets a seller for this line that differs from the invoice's seller. Returns this item. |
| `setLineStatusReasonCode(...)` | method |  |
| `setLineTotalAmount(amount)` | method | Sets the net total amount of the line. Returns this item. |
| `setParentLineId(id)` | method | Sets the ID of the line this line belongs to, making it a sub-line. Returns this item. |
| `setPrice(price)` | method | Sets the net unit price. Returns this item. |
| `setProduct(product)` | method | Sets the product of the line. Returns this item. |
| `setQuantity(quantity)` | method | Sets the invoiced quantity. Returns this item. |
| `setSellerOrderReferencedDocument(document)` | method | Sets the reference to the seller's order (order confirmation) for this line. Returns this item. |
| `setTax(...)` | method |  |
| `tax` | property, int, writable |  |

<a id="types-mustang-mustanglegalorganisation"></a>

### MustangLegalOrganisation

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `schemedID` | property, writable | The registration ID of the organisation as a SchemedID, or null; writable. |
| `setSchemedID(schemedID)` | method | Sets the registration ID (a SchemedID) of the organisation; returns the organisation. |
| `setTradingBusinessName(name)` | method | Sets the trading business name; returns the organisation. |
| `tradingBusinessName` | property, string, writable | The trading business name of the organisation; writable. |

<a id="types-mustang-mustangpaymentterms"></a>

### MustangPaymentTerms

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `description` | property, string, writable | The free-text description of the payment terms; writable. |
| `dueDate` | property, date, writable | The due date of the payment, or null; writable. |
| `setDescription(description)` | method | Sets the free-text description of the payment terms; returns the payment terms. |
| `setDueDate(date)` | method | Sets the due date of the payment; returns the payment terms. |

<a id="types-mustang-mustangproduct"></a>

### MustangProduct

| Member | Kind | Description |
| --- | --- | --- |
| `addAllowance(allowance)` | method | Adds an Allowance (discount) to the product; returns the product. |
| `addCharge(charge)` | method | Adds a Charge (surcharge) to the product; returns the product. |
| `addGlobalId(schemedID)` | method | Adds a global ID (a SchemedID, e.g. a GTIN) to the product; returns the product. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `buyerAssignedId` | property, string, writable | The buyer's ID for the product; writable. |
| `countryOfOrigin` | property, string, writable | The country of origin of the product; writable. |
| `description` | property, string, writable | The description of the product; writable. |
| `getAllowances()` | method | Returns the allowances (discounts) of the product as a list, empty if there are none. |
| `getCharges()` | method | Returns the charges (surcharges) of the product as a list, empty if there are none. |
| `globalId` | property, string, read-only | The global ID of the product, or null; read-only, see addGlobalId(). |
| `globalIdScheme` | property, string, read-only | The scheme of the product's global ID, or null; read-only. |
| `intraCommunitySupply` | property, bool, read-only | True if the product is marked as intra-community supply; read-only, see setIntraCommunitySupply(). |
| `name` | property, string, writable | The name of the product; writable. |
| `reverseCharge` | property, bool, read-only | True if the product is marked as reverse charge; read-only, see setReverseCharge(). |
| `sellerAssignedId` | property, string, writable | The seller's ID for the product; writable. |
| `setBuyerAssignedId(id)` | method | Sets the buyer's ID for the product; returns the product. |
| `setCountryOfOrigin(country)` | method | Sets the country of origin of the product; returns the product. |
| `setDescription(description)` | method | Sets the description of the product; returns the product. |
| `setIntraCommunitySupply()` | method | Marks the product as intra-community supply (VAT exemption for deliveries inside the EU, outside the seller's country); returns the product. |
| `setName(name)` | method | Sets the name of the product; returns the product. |
| `setReverseCharge()` | method | Marks the product as reverse charge (VAT exemption for deliveries outside the EU); returns the product. |
| `setSellerAssignedId(id)` | method | Sets the seller's ID for the product; returns the product. |
| `setTaxCategoryCode(code)` | method | Sets the VAT category code of the product; returns the product. |
| `setTaxExemptionReason(reason)` | method | Sets the free-text reason for a VAT exemption; returns the product. |
| `setTaxExemptionReasonCode(code)` | method | Sets the code of the reason for a VAT exemption; returns the product. |
| `setUnit(unit)` | method | Sets the unit code of the product's quantity; returns the product. |
| `setVatPercent(percent)` | method | Sets the VAT rate of the product in percent; returns the product. |
| `taxCategoryCode` | property, string, writable | The VAT category code of the product; writable. |
| `taxExemptionReason` | property, string, writable | The free-text reason for a VAT exemption; writable. |
| `taxExemptionReasonCode` | property, string, writable | The code of the reason for a VAT exemption; writable. |
| `unit` | property, string, writable | The unit code of the product's quantity; writable. |
| `vatPercent` | property, int, writable | The VAT rate of the product in percent; writable. |

<a id="types-mustang-mustangreferenceddocument"></a>

### MustangReferencedDocument

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `attachmentBinaryObject` | property, writable | The attached document as a FileAttachment, or null; writable. |
| `formattedIssueDateTime` | property, date, writable | The issue date of the referenced document, or null; writable. |
| `issuerAssignedID` | property, string, writable | The ID (e.g. the order number) the issuer assigned to the referenced document; writable. |
| `lineID` | property, string, writable | The ID of the referenced line within the document; writable. |
| `name` | property, string, writable | The name of the referenced document; writable. |
| `referenceTypeCode` | property, string, writable | The reference type code of the referenced document; writable. |
| `setAttachmentBinaryObject(attachment)` | method | Attaches the referenced document as a FileAttachment; returns the reference. |
| `setFormattedIssueDateTime(date)` | method | Sets the issue date of the referenced document; returns the reference. |
| `setIssuerAssignedID(id)` | method | Sets the ID (e.g. the order number) the issuer assigned to the referenced document; returns the reference. |
| `setLineID(lineID)` | method | Sets the ID of the referenced line within the document; returns the reference. |
| `setName(name)` | method | Sets the name of the referenced document; returns the reference. |
| `setReferenceTypeCode(code)` | method | Sets the reference type code of the referenced document; returns the reference. |
| `setTypeCode(code)` | method | Sets the type code of the referenced document; returns the reference. |
| `setUriID(uri)` | method | Sets the URI of the referenced document; returns the reference. |
| `typeCode` | property, string, writable | The type code of the referenced document; writable. |
| `uriID` | property, string, writable | The URI of the referenced document; writable. |

<a id="types-mustang-mustangschemedid"></a>

### MustangSchemedID

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `id` | property, string, writable | The identifier value; writable. |
| `scheme` | property, string, writable | The identification scheme of the ID; writable. |
| `setId(id)` | method | Sets the identifier value; returns the SchemedID. |
| `setScheme(scheme)` | method | Sets the identification scheme of the ID; returns the SchemedID. |

<a id="types-mustang-mustangtradeparty"></a>

### MustangTradeParty

| Member | Kind | Description |
| --- | --- | --- |
| `addBankDetails(bankDetails)` | method | Adds BankDetails of the trade party; returns the trade party. |
| `addDebitDetails(directDebit)` | method | Adds DirectDebit details of the trade party; returns the trade party. |
| `addGlobalId(schemedID)` | method | Adds a global ID given as a SchemedID; returns the trade party. |
| `addTaxId(taxId)` | method | Adds a tax number of the trade party; returns the trade party. |
| `addUriUniversalCommunicationId(schemedID)` | method | Adds an electronic address (e.g. an email address with its scheme) given as a SchemedID; returns the trade party. |
| `addVatId(vatId)` | method | Adds a VAT ID of the trade party; returns the trade party. |
| `additionalAddress` | property, string, writable | An additional address line; writable. |
| `additionalAddressExtension` | property, string, writable | A further address line after additionalAddress; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `contact` | property, writable | The Contact person of the trade party, or null; writable. |
| `country` | property, string, writable | The country code of the address; writable. |
| `description` | property, string, writable | A free-text description of the trade party; writable. |
| `email` | property, string, writable | The email address of the trade party; writable. |
| `getBankDetails()` | method | Returns the bank details of the trade party as a list. |
| `getDebitDetails()` | method | Returns the direct debit details of the trade party as a list. |
| `globalId` | property, string, writable | The global ID (e.g. a GLN) of the trade party; writable. |
| `globalIdScheme` | property, string, writable | The scheme of the global ID; writable. |
| `id` | property, string, writable | The ID of the trade party; writable. |
| `legalOrganisation` | property, writable | The LegalOrganisation (legal registration) of the trade party, or null; writable. |
| `location` | property, string, writable | The city of the address; writable. |
| `name` | property, string, writable | The name of the trade party; writable. |
| `setAdditionalAddress(line)` | method | Sets an additional address line; returns the trade party. |
| `setAdditionalAddressExtension(line)` | method | Sets a further address line after additionalAddress; returns the trade party. |
| `setContact(contact)` | method | Sets the Contact person of the trade party; returns the trade party. |
| `setCountry(country)` | method | Sets the country code of the address; returns the trade party. |
| `setDescription(description)` | method | Sets a free-text description of the trade party; returns the trade party. |
| `setEmail(email)` | method | Sets the email address of the trade party; returns the trade party. |
| `setGlobalId(globalId)` | method | Sets the global ID (e.g. a GLN) of the trade party; returns the trade party. |
| `setGlobalIdScheme(scheme)` | method | Sets the scheme of the global ID; returns the trade party. |
| `setId(id)` | method | Sets the ID of the trade party; returns the trade party. |
| `setLegalOrganisation(legalOrganisation)` | method | Sets the LegalOrganisation (legal registration) of the trade party; returns the trade party. |
| `setLocation(city)` | method | Sets the city of the address; returns the trade party. |
| `setName(name)` | method | Sets the name of the trade party; returns the trade party. |
| `setStreet(street)` | method | Sets the street of the address; returns the trade party. |
| `setTaxId(taxId)` | method | Sets the tax number of the trade party; returns the trade party. |
| `setVatId(vatId)` | method | Sets the VAT ID of the trade party; returns the trade party. |
| `setZip(zip)` | method | Sets the postal code of the address; returns the trade party. |
| `street` | property, string, writable | The street of the address; writable. |
| `taxId` | property, string, writable | The tax number of the trade party; writable. |
| `uriUniversalCommunicationId` | property, string, read-only | The electronic address of the trade party, or null; read-only, see addUriUniversalCommunicationId(). |
| `uriUniversalCommunicationIdScheme` | property, string, read-only | The scheme of the electronic address, or null; read-only. |
| `vatId` | property, string, writable | The VAT ID of the trade party; writable. |
| `zip` | property, string, writable | The postal code of the address; writable. |

<a id="types-docusign"></a>

## docusign

<a id="types-docusign-docusignaccount"></a>

### DocusignAccount

| Member | Kind | Description |
| --- | --- | --- |
| `accountId` | property, string, read-only | The ID of the DocuSign account; read-only. |
| `accountName` | property, string, read-only | The name of the DocuSign account; read-only. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `baseUri` | property, string, read-only | The base URI of the API server that hosts the account; read-only. |
| `isDefault` | property, string, read-only |  |

<a id="types-docusign-docusignapiclient"></a>

### DocusignApiClient

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basePath` | property, string, writable | The base path of the DocuSign REST API that the client calls; writable. |
| `envelopes()` | method | Returns the envelopes API, which uses this client. |
| `getUserInfo(token)` | method | Returns the information about the user that the OAuth token belongs to, including their accounts. |
| `login(clientId, userId, scopes, privateKey, expiresIn)` | method | Like requestJWTUserToken, but also makes the client use the new token; returns the OAuth token, expiresIn is optional and defaults to 3600 seconds. |
| `oauthBasePath` | property, string, writable | The base path of the DocuSign authentication server; write-only, reading it returns an empty string. |
| `requestJWTUserToken(clientId, userId, scopes, privateKey, expiresIn)` | method | Requests an OAuth token for the user via JWT grant, with the scopes as a list and the private key as a string, and returns it; expiresIn is optional and defaults to 3600 seconds. |
| `setAccessToken(token)` | method | Makes the client authenticate its requests with the token, given as an OAuth token or as an access token string; other values are ignored. |

<a id="types-docusign-docusignbulkenvelopestatus"></a>

### DocusignBulkEnvelopeStatus

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `batchId` | property, string, writable | The ID of the bulk send batch, which can be used to fetch its detailed status; writable. |
| `batchSize` | property, string, writable | The number of items in the batch, as a string; writable. |
| `bulkEnvelopesBatchUri` | property, string, writable |  |
| `endPosition` | property, string, writable | The last position in the result set, as a string; writable. |
| `failed` | property, string, writable | The number of entries with the status failed, as a string; writable. |
| `nextUri` | property, string, writable | The URI of the next chunk of results, or null if there is none; writable. |
| `previousUri` | property, string, writable | The URI of the previous chunk of results; writable. |
| `queued` | property, string, writable | The number of entries with the status queued, as a string; writable. |
| `resultSetSize` | property, string, writable | The number of results in this response, as a string; writable. |
| `sent` | property, string, writable | The number of entries with the status sent, as a string; writable. |
| `startPosition` | property, string, writable | The first position of this response in the result set, as a string; writable. |
| `submittedDate` | property, string, writable | The date the batch was submitted, as a string; writable. |
| `totalSetSize` | property, string, writable | The total number of items available in the result set, as a string; writable. |

<a id="types-docusign-docusigncarboncopy"></a>

### DocusignCarbonCopy

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable | The access code that the recipient must enter to view the envelope; writable. |
| `addAccessCodeToEmail` | property, string, writable | "true" to include the access code in the email sent to the recipient; writable. |
| `agentCanEditEmail` | property, string, writable | "true" if an agent recipient may change the email address of this recipient; writable. |
| `agentCanEditName` | property, string, writable | "true" if an agent recipient may change the name of this recipient; writable. |
| `allowSystemOverrideForLockedRecipient` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoRespondedReason` | property, string, writable |  |
| `bulkSendV2Recipient` | property, string, writable |  |
| `clientUserId` | property, string, writable | The sender's own ID for the recipient; if it is set, the recipient is embedded instead of remote; writable. |
| `completedCount` | property, string, writable |  |
| `declinedDateTime` | property, string, writable | The date and time the recipient declined, as a string; writable. |
| `declinedReason` | property, string, writable | The reason the recipient gave for declining; writable. |
| `deliveredDateTime` | property, string, writable | The date and time the envelope was delivered to the recipient, as a string; writable. |
| `deliveryMethod` | property, string, writable |  |
| `designatorId` | property, string, writable |  |
| `designatorIdGuid` | property, string, writable |  |
| `documentTemplateId` | property, string, writable |  |
| `email` | property, string, writable | The email address of the recipient; writable. |
| `emailRecipientPostSigningURL` | property, string, writable |  |
| `embeddedRecipientStartURL` | property, string, writable | The URL that an embedded recipient is sent to from the link in the DocuSign email, or "SIGN_AT_DOCUSIGN" to sign directly at DocuSign; writable. |
| `faxNumber` | property, string, writable | The fax number of the recipient; writable. |
| `firstName` | property, string, writable | The first name of the recipient; writable. |
| `fullName` | property, string, writable | The full name of the recipient; writable. |
| `idCheckConfigurationName` | property, string, writable | The name of the authentication check that the recipient must pass, as configured in the account; writable. |
| `inheritEmailNotificationConfiguration` | property, string, writable |  |
| `lastName` | property, string, writable | The last name of the recipient; writable. |
| `linkedAccountConfigurationId` | property, string, writable |  |
| `lockedRecipientPhoneAuthEditable` | property, string, writable |  |
| `lockedRecipientSmsEditable` | property, string, writable |  |
| `name` | property, string, writable | The full legal name of the recipient; writable. |
| `note` | property, string, writable | A note for this recipient only, which is shown in the email and while signing; writable. |
| `recipientId` | property, string, writable | The ID of the recipient within the envelope, which tabs use to refer to it; writable. |
| `recipientIdGuid` | property, string, writable | The globally unique ID of the recipient; writable. |
| `recipientType` | property, string, writable | The type of the recipient; writable. |
| `requireIdLookup` | property, string, writable | "true" if the recipient must verify their identity with the configured ID check; writable. |
| `roleName` | property, string, writable | The role name of the recipient, which is required for template recipients; writable. |
| `routingOrder` | property, string, writable | The position of the recipient in the envelope's routing order, as a string, e.g. "1"; writable. |
| `sentDateTime` | property, string, writable | The date and time the envelope was sent to the recipient, as a string; writable. |
| `signedDateTime` | property, string, writable |  |
| `signingGroupId` | property, string, writable | The ID of the signing group that receives the copy; writable. |
| `signingGroupName` | property, string, writable | The display name of the signing group; writable. |
| `status` | property, string, writable | The status of the recipient, e.g. "sent" or "completed"; writable. |
| `statusCode` | property, string, writable |  |
| `suppressEmails` | property, string, writable | "true" to send no email notifications to the recipient; writable. |
| `templateLocked` | property, string, writable | "true" if the sender may not change the recipient taken from a template; writable. |
| `templateRequired` | property, string, writable | "true" if the sender may not remove the recipient taken from a template; writable. |
| `totalTabCount` | property, string, writable |  |
| `userId` | property, string, writable |  |
| `webFormRecipientViewId` | property, string, writable |  |

<a id="types-docusign-docusigncreateenvelopeoptions"></a>

### DocusignCreateEnvelopeOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-docusign-docusigndocument"></a>

### DocusignDocument

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `applyAnchorTabs` | property, string, writable |  |
| `assignTabsToRecipientId` | property, string, writable |  |
| `display` | property, string, writable |  |
| `docGenDocumentStatus` | property, string, writable |  |
| `documentBase64` | property, string, writable | The content of the document, encoded as base64; writable. |
| `documentId` | property, string, writable | The ID of the document within the envelope, which tabs use to refer to it; writable. |
| `documentTemplateId` | property, string, writable |  |
| `encryptedWithKeyManager` | property, string, writable | "true" if the sender has already encrypted the document for the DocuSign Key Manager; writable. |
| `fileExtension` | property, string, writable | The file extension of the document, e.g. "pdf" or "docx"; documents that are no PDFs are converted to PDF; writable. |
| `fileFormatHint` | property, string, writable |  |
| `includeInDownload` | property, string, writable | "true" if the document is included when the envelope's documents are downloaded; writable. |
| `isDocGenDocument` | property, string, writable |  |
| `name` | property, string, writable | The name of the document; writable. |
| `order` | property, string, writable | The position of the document within the envelope, as a string; writable. |
| `pages` | property, string, writable | The number of pages of the document, as a string; writable. |
| `password` | property, string, writable | The password that opens the document, if it is password-protected; writable. |
| `pdfFormFieldOption` | property, string, writable |  |
| `remoteUrl` | property, string, writable | The file ID of the document in the cloud storage service it is taken from; writable. |
| `signerMustAcknowledge` | property, string, writable |  |
| `templateLocked` | property, string, writable | "true" if the sender may not change the document taken from a template; writable. |
| `templateRequired` | property, string, writable | "true" if the sender may not remove the document taken from a template; writable. |
| `transformPdfFields` | property, string, writable | "true" to turn the PDF form fields of the document into DocuSign tabs; writable. |
| `uri` | property, string, writable | The URI of the document in the DocuSign API; writable. |

<a id="types-docusign-docusignenvelope"></a>

### DocusignEnvelope

| Member | Kind | Description |
| --- | --- | --- |
| `accessControlListBase64` | property, string, writable |  |
| `allowComments` | property, string, writable | "true" if recipients may add comments to the envelope; writable. |
| `allowMarkup` | property, string, writable | "true" if recipients may change the documents through markup; writable. |
| `allowReassign` | property, string, writable | "true" if recipients may reassign the envelope to someone else; writable. |
| `allowViewHistory` | property, string, writable | "true" if recipients may view the history of the envelope; writable. |
| `anySigner` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `asynchronous` | property, string, writable |  |
| `attachmentsUri` | property, string, writable | The API URI of the envelope's attachments; writable. |
| `authoritativeCopy` | property, string, writable |  |
| `authoritativeCopyDefault` | property, string, writable |  |
| `autoNavigation` | property, string, writable |  |
| `brandId` | property, string, writable | The ID of the brand used for the envelope; writable. |
| `brandLock` | property, string, writable |  |
| `burnDefaultTabData` | property, string, writable |  |
| `certificateUri` | property, string, writable | The API URI of the envelope's certificate of completion; writable. |
| `completedDateTime` | property, string, writable | The date and time the envelope was completed, as a string; writable. |
| `copyRecipientData` | property, string, writable |  |
| `createdDateTime` | property, string, writable | The date and time the envelope was created, as a string; writable. |
| `customFieldsUri` | property, string, writable | The API URI of the envelope's custom fields; writable. |
| `declinedDateTime` | property, string, writable | The date and time the envelope was declined, as a string; writable. |
| `deletedDateTime` | property, string, writable | The date and time the envelope was deleted, as a string; writable. |
| `deliveredDateTime` | property, string, writable | The date and time the envelope was delivered to the recipients, as a string; writable. |
| `disableResponsiveDocument` | property, string, writable | "true" to show the documents without responsive signing, i.e. not adapted to the screen size; writable. |
| `documentBase64` | property, string, writable |  |
| `documentsCombinedUri` | property, string, writable | The API URI for downloading all documents of the envelope combined; writable. |
| `documentsUri` | property, string, writable | The API URI of the envelope's documents; writable. |
| `emailBlurb` | property, string, writable | The message text of the email sent to the recipients; writable. |
| `emailSubject` | property, string, writable | The subject of the email sent to the recipients; writable. |
| `enableWetSign` | property, string, writable | "true" if signers may print the documents and sign them on paper; writable. |
| `enforceSignerVisibility` | property, string, writable | "true" if signers only see the documents that they have tabs on; writable. |
| `envelopeId` | property, string, writable | The ID of the envelope; writable. |
| `envelopeIdStamping` | property, string, writable | "true" if the envelope ID is stamped onto the documents; writable. |
| `envelopeLocation` | property, string, writable |  |
| `envelopeUri` | property, string, writable | The API URI of the envelope; writable. |
| `expireAfter` | property, string, writable | The number of days after sending after which the envelope expires, as a string; writable. |
| `expireDateTime` | property, string, writable | The date and time the envelope expires, as a string; writable. |
| `expireEnabled` | property, string, writable | "true" if the envelope expires; writable. |
| `externalEnvelopeId` | property, string, writable |  |
| `hasComments` | property, string, writable | "true" if the envelope has comments; writable. |
| `hasFormDataChanged` | property, string, writable | "true" if the form data of the envelope has changed; writable. |
| `hasWavFile` | property, string, writable |  |
| `holder` | property, string, writable |  |
| `initialSentDateTime` | property, string, writable | The date and time the envelope was first sent, as a string; writable. |
| `is21CFRPart11` | property, string, writable | "true" if the envelope follows the signing rules of 21 CFR Part 11; writable. |
| `isDynamicEnvelope` | property, string, writable |  |
| `isSignatureProviderEnvelope` | property, string, writable |  |
| `isTicketRelatedEnvelope` | property, string, writable |  |
| `lastModifiedDateTime` | property, string, writable | The date and time the envelope was last changed, as a string; writable. |
| `location` | property, string, writable |  |
| `messageLock` | property, string, writable | "true" if the email subject and message of the envelope may not be changed; writable. |
| `notificationUri` | property, string, writable | The API URI of the envelope's notification settings; writable. |
| `purgeCompletedDate` | property, string, writable | The date the documents of the envelope were purged, as a string; writable. |
| `purgeRequestDate` | property, string, writable | The date the purge of the envelope's documents was requested, as a string; writable. |
| `purgeState` | property, string, writable |  |
| `recipientsLock` | property, string, writable | "true" if the recipients of the envelope may not be changed; writable. |
| `recipientsUri` | property, string, writable | The API URI of the envelope's recipients; writable. |
| `sentDateTime` | property, string, writable | The date and time the envelope was sent, as a string; writable. |
| `signerCanSignOnMobile` | property, string, writable | "true" if signers may sign on a mobile device; writable. |
| `signingLocation` | property, string, writable | Where the envelope is signed, "Online" or "InPerson"; writable. |
| `status` | property, string, writable | The status of the envelope, e.g. "created", "sent", "delivered", "completed", "declined" or "voided"; writable. |
| `statusChangedDateTime` | property, string, writable | The date and time the status of the envelope last changed, as a string; writable. |
| `templatesUri` | property, string, writable | The API URI of the templates used by the envelope; writable. |
| `transactionId` | property, string, writable |  |
| `uSigState` | property, string, writable |  |
| `useDisclosure` | property, string, writable | "true" if recipients must accept the electronic record and signature disclosure; writable. |
| `voidedDateTime` | property, string, writable | The date and time the envelope was voided, as a string; writable. |
| `voidedReason` | property, string, writable | The reason the envelope was voided; writable. |

<a id="types-docusign-docusignenvelopedefinition"></a>

### DocusignEnvelopeDefinition

| Member | Kind | Description |
| --- | --- | --- |
| `accessControlListBase64` | property, string, writable |  |
| `accessibility` | property, string, writable |  |
| `addDocument(document)` | method | Adds the document to the envelope and returns the envelope definition. |
| `addTemplateRole(role)` | method | Adds the template role to the envelope and returns the envelope definition. |
| `allowComments` | property, string, writable | "true" if recipients may add comments to the envelope; writable. |
| `allowMarkup` | property, string, writable | "true" if recipients may change the documents through markup; writable. |
| `allowReassign` | property, string, writable | "true" if recipients may reassign the envelope to someone else; writable. |
| `allowRecipientRecursion` | property, string, writable |  |
| `allowViewHistory` | property, string, writable | "true" if recipients may view the history of the envelope; writable. |
| `anySigner` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `asynchronous` | property, string, writable |  |
| `attachmentsUri` | property, string, writable | The API URI of the envelope's attachments; writable. |
| `authoritativeCopy` | property, string, writable |  |
| `authoritativeCopyDefault` | property, string, writable |  |
| `autoNavigation` | property, string, writable |  |
| `brandId` | property, string, writable | The ID of the brand used for the envelope; writable. |
| `brandLock` | property, string, writable |  |
| `burnDefaultTabData` | property, string, writable |  |
| `certificateUri` | property, string, writable | The API URI of the envelope's certificate of completion; writable. |
| `completedDateTime` | property, string, writable | The date and time the envelope was completed, as a string; writable. |
| `copyRecipientData` | property, string, writable |  |
| `createdDateTime` | property, string, writable | The date and time the envelope was created, as a string; writable. |
| `customFieldsUri` | property, string, writable | The API URI of the envelope's custom fields; writable. |
| `declinedDateTime` | property, string, writable | The date and time the envelope was declined, as a string; writable. |
| `deletedDateTime` | property, string, writable | The date and time the envelope was deleted, as a string; writable. |
| `deliveredDateTime` | property, string, writable | The date and time the envelope was delivered to the recipients, as a string; writable. |
| `disableResponsiveDocument` | property, string, writable | "true" to show the documents without responsive signing, i.e. not adapted to the screen size; writable. |
| `documentBase64` | property, string, writable |  |
| `documentsCombinedUri` | property, string, writable | The API URI for downloading all documents of the envelope combined; writable. |
| `documentsUri` | property, string, writable | The API URI of the envelope's documents; writable. |
| `emailBlurb` | property, string, writable | The message text of the email sent to the recipients; writable. |
| `emailSubject` | property, string, writable | The subject of the email sent to the recipients; writable. |
| `enableWetSign` | property, string, writable | "true" if signers may print the documents and sign them on paper; writable. |
| `enforceSignerVisibility` | property, string, writable | "true" if signers only see the documents that they have tabs on; writable. |
| `envelopeId` | property, string, writable | The ID of the envelope; writable. |
| `envelopeIdStamping` | property, string, writable | "true" if the envelope ID is stamped onto the documents; writable. |
| `envelopeLocation` | property, string, writable |  |
| `envelopeUri` | property, string, writable | The API URI of the envelope; writable. |
| `expireAfter` | property, string, writable | The number of days after sending after which the envelope expires, as a string; writable. |
| `expireDateTime` | property, string, writable | The date and time the envelope expires, as a string; writable. |
| `expireEnabled` | property, string, writable | "true" if the envelope expires; writable. |
| `externalEnvelopeId` | property, string, writable |  |
| `hasComments` | property, string, writable | "true" if the envelope has comments; writable. |
| `hasFormDataChanged` | property, string, writable | "true" if the form data of the envelope has changed; writable. |
| `hasWavFile` | property, string, writable |  |
| `holder` | property, string, writable |  |
| `initialSentDateTime` | property, string, writable | The date and time the envelope was first sent, as a string; writable. |
| `is21CFRPart11` | property, string, writable | "true" if the envelope follows the signing rules of 21 CFR Part 11; writable. |
| `isDynamicEnvelope` | property, string, writable |  |
| `isSignatureProviderEnvelope` | property, string, writable |  |
| `lastModifiedDateTime` | property, string, writable | The date and time the envelope was last changed, as a string; writable. |
| `location` | property, string, writable |  |
| `messageLock` | property, string, writable | "true" if the email subject and message of the envelope may not be changed; writable. |
| `notificationUri` | property, string, writable | The API URI of the envelope's notification settings; writable. |
| `password` | property, string, writable |  |
| `purgeCompletedDate` | property, string, writable | The date the documents of the envelope were purged, as a string; writable. |
| `purgeRequestDate` | property, string, writable | The date the purge of the envelope's documents was requested, as a string; writable. |
| `purgeState` | property, string, writable |  |
| `recipients` | property, writable | The recipients of the envelope; writable, only a Recipients object can be assigned. |
| `recipientsLock` | property, string, writable | "true" if the recipients of the envelope may not be changed; writable. |
| `recipientsUri` | property, string, writable | The API URI of the envelope's recipients; writable. |
| `sentDateTime` | property, string, writable | The date and time the envelope was sent, as a string; writable. |
| `signerCanSignOnMobile` | property, string, writable | "true" if signers may sign on a mobile device; writable. |
| `signingLocation` | property, string, writable | Where the envelope is signed, "Online" or "InPerson"; writable. |
| `status` | property, string, writable | The status of the new envelope, "sent" to send it right away or "created" to save it as a draft; writable. |
| `statusChangedDateTime` | property, string, writable | The date and time the status of the envelope last changed, as a string; writable. |
| `templateId` | property, string, writable | The ID of the template that the envelope is created from; writable. |
| `templatesUri` | property, string, writable | The API URI of the templates used by the envelope; writable. |
| `transactionId` | property, string, writable |  |
| `uSigState` | property, string, writable |  |
| `useDisclosure` | property, string, writable | "true" if recipients must accept the electronic record and signature disclosure; writable. |
| `voidedDateTime` | property, string, writable | The date and time the envelope was voided, as a string; writable. |
| `voidedReason` | property, string, writable | The reason the envelope was voided; writable. |

<a id="types-docusign-docusignenvelopesummary"></a>

### DocusignEnvelopeSummary

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bulkEnvelopeStatus` | property, writable | The status of a bulk send, or null if there is none; writable. |
| `envelopeId` | property, string, writable | The ID of the created envelope; writable. |
| `errorDetails` | property, writable | The details of an error, or null if there is none; writable. |
| `recipientSigningUri` | property, string, writable |  |
| `recipientSigningUriError` | property, string, writable |  |
| `status` | property, string, writable | The status of the envelope, e.g. "sent" or "created"; writable. |
| `statusDateTime` | property, string, writable | The date and time the envelope reached its status, as a string; writable. |
| `uri` | property, string, writable | The API URI of the envelope; writable. |

<a id="types-docusign-docusignenvelopesapi"></a>

### DocusignEnvelopesApi

| Member | Kind | Description |
| --- | --- | --- |
| `CreateEnvelopeOptions(properties)` | method | Creates a new set of options for creating an envelope; an optional map argument is applied to its properties. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `createEnvelope(accountId, envelopeDefinition)` | method | Creates an envelope in the account from the envelope definition and returns the envelope summary. |
| `getDocumentWithHttpInfo(accountId, envelopeId, documentId, options)` | method | Downloads a document of the envelope and returns its content as binary, or null; options is an optional map with the get document options, e.g. certificate. |
| `getEnvelope(accountId, envelopeId, options)` | method | Fetches the envelope from the account and returns it; options is an optional map with the get envelope options, e.g. include. |

<a id="types-docusign-docusignerrordetails"></a>

### DocusignErrorDetails

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `errorCode` | property, string, writable | The code of the error; writable. |
| `message` | property, string, writable | The description of the error; writable. |

<a id="types-docusign-docusigngetdocumentoptions"></a>

### DocusignGetDocumentOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `certificate` | property, string, writable | "true" to include the certificate of completion in the combined PDF; writable. |
| `documentsByUserid` | property, string, writable |  |
| `encoding` | property, string, writable |  |
| `encrypt` | property, string, writable |  |
| `language` | property, string, writable | The language code for the certificate of completion; writable. |
| `recipientId` | property, string, writable |  |
| `sharedUserId` | property, string, writable |  |
| `showChanges` | property, string, writable | "true" to highlight changed fields in the returned PDF; writable. |
| `watermark` | property, string, writable | "true" to add the account's watermark to the PDF of an envelope that is not completed yet; writable. |

<a id="types-docusign-docusigngetenvelopeoptions"></a>

### DocusignGetEnvelopeOptions

| Member | Kind | Description |
| --- | --- | --- |
| `advancedUpdate` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `include` | property, string, writable | A comma-separated list of additional information to return with the envelope, e.g. "recipients,documents"; writable. |
| `includeAnchorTabLocations` | property, string, writable | "true" to include the locations of anchor tabs; writable. |

<a id="types-docusign-docusignoauthtoken"></a>

### DocusignOAuthToken

| Member | Kind | Description |
| --- | --- | --- |
| `accessToken` | property, string, writable | The access token for authenticating API requests; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `expiresIn` | property, int, writable | The number of seconds the access token is valid; writable, null is stored as 0. |
| `refreshToken` | property, string, writable | The token for getting a new access token, if any; writable. |
| `scope` | property, string, writable | The scopes granted to the token; writable. |
| `tokenType` | property, string, writable | The type of the token, usually "Bearer"; writable. |

<a id="types-docusign-docusignrecipients"></a>

### DocusignRecipients

| Member | Kind | Description |
| --- | --- | --- |
| `addCarbonCopy(carbonCopy)` | method | Adds the carbon copy recipient and returns the recipients. |
| `addSigner(signer)` | method | Adds the signer and returns the recipients. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-docusign-docusignsignhere"></a>

### DocusignSignHere

| Member | Kind | Description |
| --- | --- | --- |
| `agreementAttribute` | property, string, writable |  |
| `agreementAttributeLocked` | property, string, writable |  |
| `anchorAllowWhiteSpaceInCharacters` | property, string, writable |  |
| `anchorCaseSensitive` | property, string, writable | "true" if the anchor text is matched case-sensitively; writable. |
| `anchorHorizontalAlignment` | property, string, writable | The horizontal alignment of the tab relative to the anchor text; writable. |
| `anchorIgnoreIfNotPresent` | property, string, writable | "true" to ignore the tab if the anchor text is not found in the document; writable. |
| `anchorMatchWholeWord` | property, string, writable | "true" if the anchor text only matches whole words; writable. |
| `anchorString` | property, string, writable | The text in the document at which the tab is placed; writable. |
| `anchorTabProcessorVersion` | property, string, writable |  |
| `anchorUnits` | property, string, writable | The unit of the anchor offsets, e.g. "pixels", "inches", "mms" or "cms"; writable. |
| `anchorXOffset` | property, string, writable | The horizontal offset of the tab from the anchor text, in anchorUnits; writable. |
| `anchorYOffset` | property, string, writable | The vertical offset of the tab from the anchor text, in anchorUnits; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `caption` | property, string, writable |  |
| `conditionalParentLabel` | property, string, writable | The label of the tab whose value decides whether this tab is shown; writable. |
| `conditionalParentValue` | property, string, writable | The value of the conditional parent tab that makes this tab shown; writable. |
| `customTabId` | property, string, writable | The ID of the custom tab that this tab is based on; writable. |
| `documentId` | property, string, writable | The ID of the document that the tab is placed on; writable. |
| `formOrder` | property, string, writable |  |
| `formPageLabel` | property, string, writable |  |
| `formPageNumber` | property, string, writable |  |
| `handDrawRequired` | property, string, writable | "true" if the signer must draw the signature by hand; writable. |
| `height` | property, string, writable |  |
| `isSealSignTab` | property, string, writable |  |
| `mergeFieldXml` | property, string, writable |  |
| `name` | property, string, writable | The name of the tab; writable. |
| `optional` | property, string, writable | "true" if signing at this tab is optional; writable. |
| `pageNumber` | property, string, writable | The number of the page that the tab is placed on, as a string; writable. |
| `recipientId` | property, string, writable | The ID of the recipient who signs at the tab; writable. |
| `recipientIdGuid` | property, string, writable | The globally unique ID of the recipient who signs at the tab; writable. |
| `scaleValue` | property, string, writable | The scaling factor for the size of the signature, as a string, e.g. "0.5"; writable. |
| `source` | property, string, writable |  |
| `stampType` | property, string, writable | The kind of mark that is placed, e.g. "signature" or "stamp"; writable. |
| `status` | property, string, writable | The status of the tab; writable. |
| `tabFullyQualifiedPath` | property, string, writable |  |
| `tabId` | property, string, writable | The ID of the tab, which DocuSign assigns; writable. |
| `tabLabel` | property, string, writable | The label of the tab; writable. |
| `tabOrder` | property, string, writable | The position of the tab in the order in which the signer visits the tabs, as a string; writable. |
| `tabType` | property, string, writable | The type of the tab; writable. |
| `templateLocked` | property, string, writable | "true" if the sender may not change the tab taken from a template; writable. |
| `templateRequired` | property, string, writable | "true" if the sender may not remove the tab taken from a template; writable. |

<a id="types-docusign-docusignsigner"></a>

### DocusignSigner

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable | Access code the signer must enter to open the envelope; writable. |
| `addAccessCodeToEmail` | property, string, writable | "true" to include the access code in the notification email; writable. |
| `agentCanEditEmail` | property, string, writable | "true" if an agent recipient may change the signer's email address; writable. |
| `agentCanEditName` | property, string, writable | "true" if an agent recipient may change the signer's name; writable. |
| `allowSystemOverrideForLockedRecipient` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoNavigation` | property, string, writable |  |
| `autoRespondedReason` | property, string, writable |  |
| `bulkRecipientsUri` | property, string, writable |  |
| `bulkSendV2Recipient` | property, string, writable |  |
| `canSignOffline` | property, string, writable | "true" if the signer may sign offline; writable. |
| `clientUserId` | property, string, writable | Sender-defined ID that makes the signer an embedded recipient; writable. |
| `completedCount` | property, string, writable |  |
| `creationReason` | property, string, writable |  |
| `declinedDateTime` | property, string, writable | When the signer declined to sign, as a string; writable. |
| `declinedReason` | property, string, writable | The reason the signer gave for declining; writable. |
| `defaultRecipient` | property, string, writable |  |
| `deliveredDateTime` | property, string, writable | When the envelope was delivered to the signer, as a string; writable. |
| `deliveryMethod` | property, string, writable | How the envelope is delivered to the signer, e.g. email; writable. |
| `designatorId` | property, string, writable |  |
| `designatorIdGuid` | property, string, writable |  |
| `documentTemplateId` | property, string, writable |  |
| `email` | property, string, writable | The signer's email address; writable. |
| `emailRecipientPostSigningURL` | property, string, writable |  |
| `embeddedRecipientStartURL` | property, string, writable |  |
| `faxNumber` | property, string, writable | The signer's fax number; writable. |
| `firstName` | property, string, writable | The signer's first name; writable. |
| `fullName` | property, string, writable | The signer's full name; writable. |
| `idCheckConfigurationName` | property, string, writable |  |
| `inheritEmailNotificationConfiguration` | property, string, writable |  |
| `isBulkRecipient` | property, string, writable |  |
| `lastName` | property, string, writable | The signer's last name; writable. |
| `lockedRecipientPhoneAuthEditable` | property, string, writable |  |
| `lockedRecipientSmsEditable` | property, string, writable |  |
| `name` | property, string, writable | The signer's name; writable. |
| `notaryId` | property, string, writable |  |
| `notarySignerEmailSent` | property, string, writable |  |
| `note` | property, string, writable | A note shown only to this signer; writable. |
| `recipientId` | property, string, writable | ID of the recipient within the envelope; writable. |
| `recipientIdGuid` | property, string, writable | The recipient's ID as a GUID; writable. |
| `recipientSuppliesTabs` | property, string, writable |  |
| `recipientType` | property, string, writable | The type of the recipient, e.g. signer; writable. |
| `requireIdLookup` | property, string, writable |  |
| `requireSignOnPaper` | property, string, writable | "true" if the signer must sign on paper; writable. |
| `requireSignerCertificate` | property, string, writable |  |
| `requireUploadSignature` | property, string, writable |  |
| `roleName` | property, string, writable | Name of the template role the signer fills; writable. |
| `routingOrder` | property, string, writable | The signer's position in the signing order, as a string; writable. |
| `sentDateTime` | property, string, writable | When the envelope was sent to the signer, as a string; writable. |
| `signInEachLocation` | property, string, writable |  |
| `signedDateTime` | property, string, writable | When the signer signed, as a string; writable. |
| `signingGroupId` | property, string, writable | ID of the signing group that signs in place of a single person; writable. |
| `signingGroupName` | property, string, writable | Name of the signing group; writable. |
| `status` | property, string, writable | The signer's status, e.g. sent, delivered, completed or declined; writable. |
| `statusCode` | property, string, writable |  |
| `suppressEmails` | property, string, writable | "true" to send no emails to the signer; writable. |
| `tabs` | property, writable | The signer's tabs, i.e. the fields to sign or fill in; writable, must not be null. |
| `templateLocked` | property, string, writable |  |
| `templateRequired` | property, string, writable |  |
| `totalTabCount` | property, string, writable | The number of tabs assigned to the signer, as a string; writable. |
| `userId` | property, string, writable | The signer's DocuSign user ID; writable. |
| `webFormRecipientViewId` | property, string, writable |  |

<a id="types-docusign-docusigntabs"></a>

### DocusignTabs

| Member | Kind | Description |
| --- | --- | --- |
| `addSignHere(signHere)` | method | Adds a sign-here tab and returns these tabs. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-docusign-docusigntemplaterole"></a>

### DocusignTemplateRole

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable | Access code the recipient must enter to open the envelope; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `clientUserId` | property, string, writable | Sender-defined ID that makes the recipient an embedded recipient; writable. |
| `defaultRecipient` | property, string, writable |  |
| `deliveryMethod` | property, string, writable | How the envelope is delivered to the recipient, e.g. email; writable. |
| `email` | property, string, writable | The recipient's email address; writable. |
| `embeddedRecipientStartURL` | property, string, writable |  |
| `inPersonSignerName` | property, string, writable | Name of the person who signs in person; writable. |
| `name` | property, string, writable | The recipient's name; writable. |
| `roleName` | property, string, writable | Name of the template role this recipient fills; writable. |
| `routingOrder` | property, string, writable | The recipient's position in the signing order, as a string; writable. |
| `signingGroupId` | property, string, writable | ID of the signing group that fills the role in place of a single person; writable. |
| `tabs` | property, writable | The recipient's tabs, i.e. the fields to sign or fill in; writable, must not be null. |

<a id="types-docusign-docusignuserinfo"></a>

### DocusignUserInfo

| Member | Kind | Description |
| --- | --- | --- |
| `accounts` | property, list, read-only | List of the DocuSign accounts the user can access; read-only. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `email` | property, string, read-only | The user's email address; read-only. |
| `familyName` | property, string, read-only | The user's family name; read-only. |
| `givenName` | property, string, read-only | The user's given name; read-only. |
| `name` | property, string, read-only | The user's full name; read-only. |
| `sub` | property, string, read-only | The user's ID (the OAuth subject); read-only. |

<a id="types-msgraph"></a>

## msgraph

<a id="types-msgraph-emailaddress"></a>

### EmailAddress

| Member | Kind | Description |
| --- | --- | --- |
| `address` | property, string, writable | The email address; writable. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `name` | property, string, writable | The display name, or null; writable. |

<a id="types-msgraph-itembody"></a>

### ItemBody

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, writable | The content as HTML or plain text, depending on contentType; writable. |
| `contentType` | property, string, writable | The type of the content, "html" or "text"; writable. |
| `html` | property, bool, read-only | True if the content type is "html"; read-only. |

<a id="types-msgraph-mailfolder"></a>

### MailFolder

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `childFolderCount` | property, int, writable | The number of direct subfolders, or null if not loaded; writable. |
| `displayName` | property, string, writable | The folder's display name; writable. |
| `id` | property, string, writable | The Graph ID of the folder; writable. |
| `isHidden` | property, bool, writable | True if the folder is hidden, or null if not loaded; writable. |
| `parentFolderId` | property, string, writable | The Graph ID of the parent folder; writable. |
| `totalItemCount` | property, int, writable | The number of items in the folder, or null if not loaded; writable. |
| `unreadItemCount` | property, int, writable | The number of unread items in the folder, or null if not loaded; writable. |

<a id="types-msgraph-mailmessage"></a>

### MailMessage

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `body` | property, writable | The body of the message, or null if not selected; writable. |
| `categories` | property, list, writable | List of the category names of the message, or null if not selected; writable. |
| `id` | property, string, writable | The Graph ID of the message; writable. |
| `internetMessageId` | property, string, writable | The Message-ID header of the message, or null if not selected; writable. |
| `isRead` | property, bool, writable | True if the message has been read, or null if not selected; writable. |
| `parentFolderId` | property, string, writable | The Graph ID of the folder that contains the message, or null if not selected; writable. |
| `sender` | property, writable | The sender as a recipient with an emailAddress, or null if not selected; writable. |
| `subject` | property, string, writable | The subject of the message, or null if not selected; writable. |
| `uniqueBody` | property, writable | The part of the body that is unique to this message, without quoted earlier messages, or null if not selected; writable. |

<a id="types-msgraph-messagequery"></a>

### MessageQuery

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bodyType` | property, string, writable | "text" or "html" to get body and unique body in that format; null leaves the default, HTML; writable. |
| `filter` | property, string, writable | An OData filter ($filter), e.g. "isRead eq false", or null for none; writable. |
| `orderBy` | property, string, writable | The sort order ($orderby), default "receivedDateTime asc"; writable. |
| `select` | property, list, writable | List of the message properties to load ($select); null or empty means id, subject, sender and categories; writable. |
| `top` | property, int, writable | The page size ($top, default 10); all pages are read regardless; writable. |

<a id="types-msgraph-messageupdate"></a>

### MessageUpdate

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `categories` | property, list, writable | The new list of categories, replacing the old ones; an empty list removes all, null leaves them unchanged; writable. |
| `isRead` | property, bool, writable | Marks the message as read (true) or unread (false); null leaves it unchanged; writable. |
| `subject` | property, string, writable | The new subject; null leaves it unchanged; writable. |

<a id="types-msgraph-msgraphclient"></a>

### MsGraphClient

| Member | Kind | Description |
| --- | --- | --- |
| `allFolders(user)` | method | Returns a list of all mail folders of the user's mailbox, each folder directly followed by its subfolders. |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `createFolder(user, displayName, parentId)` | method | Creates a visible folder below the parent folder (without a parent, at the top level) and returns it. |
| `deleteFolder(user, folderId)` | method | Deletes a folder; Graph moves it to "Deleted Items". |
| `findFolder(user, displayName, parentId)` | method | Returns the first folder with that name (case-insensitive) below the parent folder, or null; without a parent, searches the top level. |
| `folder(user, folderId)` | method | Returns a mail folder of the user's mailbox, including its item counts; folderId may be a well-known name such as "inbox". |
| `folders(user, parentId)` | method | Returns a list of the mail folders directly below the parent folder; without a parent, the top-level folders. |
| `messages(user, folderId, query)` | method | Returns a list of all messages in the folder (default "inbox"), reading all pages; query is an optional map with select, top, orderBy, filter and bodyType. |
| `mime(user, messageId)` | method | Returns the complete message in MIME format (RFC 822) as a string. |
| `move(user, messageId, destinationId)` | method | Moves a message to another folder and returns its new ID, which Graph changes when moving. |
| `update(user, messageId, changes)` | method | Changes a message; changes is a map with subject, categories and/or isRead, and only the given values are sent. |

<a id="types-msgraph-msgraphoptions"></a>

### MsGraphOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `baseUrl` | property, string, writable | The Graph endpoint including the API version (default https://graph.microsoft.com/v1.0; blank restores it); writable. |
| `maxRetries` | property, int, writable | How often a throttled (429) or unavailable (503, 504) request is retried (default 4); writable. |
| `proxyHost` | property, string, writable | Host name of the HTTP proxy; null or blank means no proxy; writable. |
| `proxyPort` | property, int, writable | Port of the HTTP proxy; writable. |
| `timeoutSeconds` | property, int, writable | Timeout for requests in seconds (default 60); writable. |
| `tokenCache` | property, bool, writable | If true, tokens are also stored on disk in the operating system's secure store, so they survive restarts (default false); writable. |

<a id="types-msgraph-recipient"></a>

### Recipient

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `emailAddress` | property, writable | The recipient's email address, with address and name; writable. |

<a id="types-mcp"></a>

## mcp

<a id="types-mcp-mcpconnection"></a>

### McpConnection

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `call(name, arguments)` | method | Calls a tool with a map of arguments (none if omitted or null) and returns its result; a tool error is a script error if the option throwOnError is set. |
| `capabilities` | property, read-only | The server's capabilities as a map, as received in JSON, or null; read-only. |
| `close()` | method | Closes the connection; further calls fail, closing again does nothing. Happens automatically when the script ends. |
| `instructions` | property, string, read-only | The server's instructions on how to use it, or null; read-only. |
| `protocolVersion` | property, string, read-only | The MCP protocol version agreed on with the server; read-only. |
| `serverName` | property, string, read-only | The server's name as reported when connecting, or null; read-only. |
| `serverVersion` | property, string, read-only | The server's version as reported when connecting, or null; read-only. |
| `tools()` | method | Returns a list of all tools of the server, each a map with name, description, inputSchema and so on. |

<a id="types-mcp-mcpoptions"></a>

### McpOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `clientName` | property, string, writable | The name this client reports to the server (default "Janitor"; blank restores it); writable. |
| `clientVersion` | property, string, writable | The version this client reports to the server (blank restores the default); writable. |
| `throwOnError` | property, bool, writable | If true, a tool result flagged as an error becomes a script error instead of a result with isError = true; writable. |
| `timeoutSeconds` | property, int, writable | Time limit in seconds for connecting and for each request (default 60; 0 or less means 60); writable. |
| `transport` | property, string, writable | "streamable" (the default) or "sse" for older servers; writable. |

<a id="types-mcp-mcptoolresult"></a>

### McpToolResult

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, read-only | List of all parts of the content as maps, e.g. {"type": "text", "text": "..."}; read-only. |
| `isError` | property, bool, read-only | True if the server flagged the result as an error, e.g. because the tool failed; read-only. |
| `structured` | property, read-only | The structured content of the result, or null if the tool returned none; read-only. |
| `text` | property, string, read-only | All text parts of the content, joined by newlines; empty if there are none; read-only. |
