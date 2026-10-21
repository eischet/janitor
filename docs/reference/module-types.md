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
| `name` | property, string, read-only |  |

<a id="types-generator-generatedfield"></a>

### GeneratedField

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `name` | property, string, read-only |  |

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
| `body` | property, string, read-only |  |
| `code` | property, int, read-only |  |
| `headers` | property, read-only |  |
| `method` | property, string, read-only |  |
| `url` | property, string, read-only |  |

<a id="types-httpclient-janitorhttpclient"></a>

### JanitorHttpClient

| Member | Kind | Description |
| --- | --- | --- |
| `addHeader(...)` | method |  |
| `allowCookies(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basic(...)` | method |  |
| `close(...)` | method |  |
| `convertJsonToPureAscii(...)` | method |  |
| `delete(...)` | method |  |
| `getBinary(...)` | method |  |
| `getJson(...)` | method |  |
| `getString(...)` | method |  |
| `ignoreSecurityIssues(...)` | method |  |
| `patchJson(...)` | method |  |
| `postForm(...)` | method |  |
| `postJson(...)` | method |  |
| `prettyJson` | property, bool, writable |  |
| `proxy(...)` | method |  |
| `putJson(...)` | method |  |
| `setBearerToken(...)` | method |  |
| `setPrettyJson(...)` | method |  |
| `setRequestTimeout(...)` | method |  |
| `setTimeout(...)` | method |  |
| `verbose(...)` | method |  |

<a id="types-brrr"></a>

## brrr

<a id="types-brrr-brrrmessage"></a>

### BrrrMessage

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `expiration_date` | property, datetime, writable |  |
| `filter_criteria` | property, string, writable |  |
| `image_url` | property, string, writable |  |
| `interruption_level` | property, string, writable |  |
| `message` | property, string, writable |  |
| `open_url` | property, string, writable |  |
| `send(...)` | method |  |
| `sound` | property, string, writable |  |
| `subtitle` | property, string, writable |  |
| `thread_id` | property, string, writable |  |
| `title` | property, string, writable |  |

<a id="types-files"></a>

## files

<a id="types-files-zipfile"></a>

### ZipFile

| Member | Kind | Description |
| --- | --- | --- |
| `addBinary(...)` | method |  |
| `addFile(...)` | method |  |
| `addText(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close(...)` | method |  |
| `setComment(...)` | method |  |
| `setLevel(...)` | method |  |
| `setMethod(...)` | method |  |

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
| `marker` | property, string |  |
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
| `literal` | property, string |  |
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
| `closingDelimiter` | property, string |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `openingDelimiter` | property, string |  |
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
| `closingFenceLength` | property, int |  |
| `fenceCharacter` | property, string |  |
| `fenceIndent` | property, int |  |
| `fenceLength` | property, int |  |
| `firstChild` | property |  |
| `info` | property, string |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `literal` | property, string |  |
| `next` | property |  |
| `openingFenceLength` | property, int |  |
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
| `level` | property, int |  |
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
| `literal` | property, string |  |
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
| `literal` | property, string |  |
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
| `destination` | property, string |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string |  |
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
| `literal` | property, string |  |
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
| `destination` | property, string |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string |  |
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
| `destination` | property, string |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `label` | property, string |  |
| `lastChild` | property |  |
| `next` | property |  |
| `parent` | property |  |
| `prependChild` | property |  |
| `previous` | property |  |
| `title` | property, string |  |
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
| `tight` | property, bool, writable |  |

<a id="types-commonmark-cmlistitem"></a>

### CMListItem

| Member | Kind | Description |
| --- | --- | --- |
| `appendChild` | property |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `contentIndent` | property, int |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `markerIndent` | property, int |  |
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
| `appendChild(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `firstChild` | property, read-only |  |
| `insertAfter(...)` | method |  |
| `insertBefore(...)` | method |  |
| `lastChild` | property, read-only |  |
| `next` | property, read-only |  |
| `parent` | property, read-only |  |
| `prependChild(...)` | method |  |
| `previous` | property, read-only |  |
| `toHtml(...)` | method | Converts the node to html representation |
| `toMarkdown(...)` | method | Converts the node to markdown representation |
| `toText(...)` | method | Converts the node to plain text representation |
| `type` | property, string, read-only |  |
| `unlink(...)` | method |  |
| `visit(...)` | method |  |

<a id="types-commonmark-cmorderedlist"></a>

### CMOrderedList

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `markerDelimiter` | property, string |  |
| `markerStartNumber` | property, int |  |
| `startNumber` | property, int |  |
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
| `closingDelimiter` | property, string |  |
| `firstChild` | property |  |
| `insertAfter` | property |  |
| `insertBefore` | property |  |
| `lastChild` | property |  |
| `next` | property |  |
| `openingDelimiter` | property, string |  |
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
| `literal` | property, string |  |
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
| `literal` | property, string |  |
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
| `add(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only |  |
| `font` | property, writable |  |
| `leading` | property, float, writable |  |
| `name` | property, string, writable |  |
| `reference` | property, string, writable |  |
| `size` | property, int, read-only |  |

<a id="types-pdf-jpdfannotation"></a>

### JPDFAnnotation

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, read-only |  |
| `llx` | property, float, read-only |  |
| `lly` | property, float, read-only |  |
| `setDimensions(...)` | method |  |
| `title` | property, string, read-only |  |
| `urx` | property, float, read-only |  |
| `ury` | property, float, read-only |  |

<a id="types-pdf-jpdfbasefont"></a>

### JPDFBaseFont

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `getFont(...)` | method |  |
| `getWidthPoint(...)` | method |  |
| `postscriptFontName` | property, string, read-only |  |

<a id="types-pdf-jpdfchapter"></a>

### JPDFChapter

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `addSection(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bookmarkOpen` | property, bool, writable |  |
| `indentation` | property, float, writable |  |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `numberDepth` | property, int, writable |  |
| `numberStyle` | property, int, writable |  |
| `title` | property, writable |  |
| `triggerNewPage` | property, bool, writable |  |

<a id="types-pdf-jpdfchunk"></a>

### JPDFChunk

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `characterSpacing` | property, float, writable |  |
| `content` | property, string, read-only |  |
| `empty` | property, bool, read-only |  |
| `font` | property, writable |  |
| `horizontalScaling` | property, float, writable |  |
| `setAnchor(...)` | method |  |
| `setBackground(...)` | method |  |
| `setGenericTag(...)` | method |  |
| `setLocalDestination(...)` | method |  |
| `setLocalGoto(...)` | method |  |
| `setNewPage(...)` | method |  |
| `setUnderline(...)` | method |  |
| `textRise` | property, float, writable |  |

<a id="types-pdf-jpdfcolor"></a>

### JPDFColor

| Member | Kind | Description |
| --- | --- | --- |
| `alpha` | property, int, read-only |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `blue` | property, int, read-only |  |
| `green` | property, int, read-only |  |
| `red` | property, int, read-only |  |
| `rgb` | property, string, read-only |  |

<a id="types-pdf-jpdfdocument"></a>

### JPDFDocument

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `addAuthor(...)` | method |  |
| `addCreator(...)` | method |  |
| `addHeader(...)` | method |  |
| `addKeywords(...)` | method |  |
| `addSubject(...)` | method |  |
| `addTitle(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close(...)` | method |  |
| `isOpen` | property, bool, read-only |  |
| `marginBottom` | property, float, read-only |  |
| `marginLeft` | property, float, read-only |  |
| `marginRight` | property, float, read-only |  |
| `marginTop` | property, float, read-only |  |
| `newPage(...)` | method |  |
| `open(...)` | method |  |
| `pageNumber` | property, int, read-only |  |
| `pageSize` | property, writable |  |
| `resetFooter(...)` | method |  |
| `resetHeader(...)` | method |  |
| `setFooter(...)` | method |  |
| `setHeader(...)` | method |  |
| `setMarginMirroring(...)` | method |  |
| `setMargins(...)` | method |  |

<a id="types-pdf-jpdffont"></a>

### JPDFFont

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bold` | property, bool, read-only |  |
| `calculatedSize` | property, float, read-only |  |
| `color` | property, writable |  |
| `family` | property, int, writable |  |
| `familyname` | property, string, read-only |  |
| `italic` | property, bool, read-only |  |
| `size` | property, float, writable |  |
| `strikethru` | property, bool, read-only |  |
| `style` | property, int, writable |  |
| `underlined` | property, bool, read-only |  |

<a id="types-pdf-jpdfheaderfooter"></a>

### JPDFHeaderFooter

| Member | Kind | Description |
| --- | --- | --- |
| `addPadding(...)` | method |  |
| `after` | property, read-only |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `before` | property, read-only |  |
| `numbered` | property, bool, read-only |  |
| `setAlignment(...)` | method |  |
| `setPadding(...)` | method |  |
| `setPageNumber(...)` | method |  |

<a id="types-pdf-jpdfimage"></a>

### JPDFImage

| Member | Kind | Description |
| --- | --- | --- |
| `absoluteX` | property, float, read-only |  |
| `absoluteY` | property, float, read-only |  |
| `alignment` | property, int, writable |  |
| `alt` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `plainHeight` | property, float, read-only |  |
| `plainWidth` | property, float, read-only |  |
| `scaleAbsolute(...)` | method |  |
| `scalePercent(...)` | method |  |
| `scaleToFit(...)` | method |  |
| `scaledHeight` | property, float, read-only |  |
| `scaledWidth` | property, float, read-only |  |
| `setAbsolutePosition(...)` | method |  |
| `setRotationDegrees(...)` | method |  |
| `spacingAfter` | property, float, writable |  |
| `spacingBefore` | property, float, writable |  |
| `widthPercentage` | property, float, writable |  |

<a id="types-pdf-jpdflist"></a>

### JPDFList

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `alignindent` | property, bool, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoindent` | property, bool, writable |  |
| `empty` | property, bool, read-only |  |
| `first` | property, int, writable |  |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `lettered` | property, bool, writable |  |
| `lowercase` | property, bool, writable |  |
| `numbered` | property, bool, writable |  |
| `setListSymbol(...)` | method |  |
| `setPostSymbol(...)` | method |  |
| `setPreSymbol(...)` | method |  |
| `size` | property, int, read-only |  |
| `symbolIndent` | property, float, writable |  |

<a id="types-pdf-jpdflistitem"></a>

### JPDFListItem

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `alignment` | property, int, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only |  |
| `font` | property, writable |  |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `leading` | property, float, writable |  |
| `listSymbol` | property, writable |  |
| `size` | property, int, read-only |  |

<a id="types-pdf-jpdfmeta"></a>

### JPDFMeta

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, read-only |  |
| `name` | property, string, read-only |  |

<a id="types-pdf-jpdfpcell"></a>

### JPDFPCell

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `backgroundColor` | property, writable |  |
| `border` | property, int, writable |  |
| `borderWidth` | property, float, writable |  |
| `colspan` | property, int, writable |  |
| `fixedHeight` | property, float, writable |  |
| `horizontalAlignment` | property, int, writable |  |
| `minimumHeight` | property, float, writable |  |
| `noWrap` | property, bool, writable |  |
| `paddingBottom` | property, float, writable |  |
| `paddingLeft` | property, float, writable |  |
| `paddingRight` | property, float, writable |  |
| `paddingTop` | property, float, writable |  |
| `rowspan` | property, int, writable |  |
| `setImage(...)` | method |  |
| `setPadding(...)` | method |  |
| `setPhrase(...)` | method |  |
| `useAscender` | property, bool, writable |  |
| `useBorderPadding` | property, bool, writable |  |
| `useDescender` | property, bool, writable |  |
| `verticalAlignment` | property, int, writable |  |

<a id="types-pdf-jpdfptable"></a>

### JPDFPTable

| Member | Kind | Description |
| --- | --- | --- |
| `addCell(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `completeRow(...)` | method |  |
| `deleteBodyRows(...)` | method |  |
| `deleteLastRow(...)` | method |  |
| `deleteRow(...)` | method |  |
| `headerRows` | property, int, writable |  |
| `horizontalAlignment` | property, int, writable |  |
| `lockedWidth` | property, bool, writable |  |
| `numberOfColumns` | property, int, read-only |  |
| `setSpacingAfter(...)` | method |  |
| `setSpacingBefore(...)` | method |  |
| `setTotalWidth(...)` | method |  |
| `setWidths(...)` | method |  |
| `size` | property, int, read-only |  |
| `skipFirstHeader` | property, bool, writable |  |
| `skipLastFooter` | property, bool, writable |  |
| `splitRows` | property, bool, writable |  |
| `totalWidth` | property, float, read-only |  |
| `widthPercentage` | property, float, writable |  |

<a id="types-pdf-jpdfparagraph"></a>

### JPDFParagraph

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `alignment` | property, int, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only |  |
| `extraParagraphSpace` | property, float, writable |  |
| `firstLineIndent` | property, float, writable |  |
| `font` | property, writable |  |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `keepTogether` | property, bool, writable |  |
| `leading` | property, float, writable |  |
| `multipliedLeading` | property, float, writable |  |
| `runDirection` | property, int, writable |  |
| `size` | property, int, read-only |  |
| `spacingAfter` | property, float, writable |  |
| `spacingBefore` | property, float, writable |  |

<a id="types-pdf-jpdfphrase"></a>

### JPDFPhrase

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `empty` | property, bool, read-only |  |
| `font` | property, writable |  |
| `leading` | property, float, writable |  |
| `size` | property, int, read-only |  |

<a id="types-pdf-jpdfreader"></a>

### JPDFReader

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `close(...)` | method |  |
| `encrypted` | property, bool, read-only |  |
| `numberOfPages` | property, int, read-only |  |
| `pdfVersion` | property, string, read-only |  |
| `rebuilt` | property, bool, read-only |  |

<a id="types-pdf-jpdfrectangle"></a>

### JPDFRectangle

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `backgroundColor` | property, writable |  |
| `border` | property, int, writable |  |
| `borderColor` | property, writable |  |
| `borderWidth` | property, float, writable |  |
| `bottom` | property, float, writable |  |
| `height` | property, float, read-only |  |
| `left` | property, float, writable |  |
| `right` | property, float, writable |  |
| `rotate(...)` | method |  |
| `rotation` | property, int, writable |  |
| `top` | property, float, writable |  |
| `width` | property, float, read-only |  |

<a id="types-pdf-jpdfsection"></a>

### JPDFSection

| Member | Kind | Description |
| --- | --- | --- |
| `add(...)` | method |  |
| `addSection(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bookmarkOpen` | property, bool, writable |  |
| `indentation` | property, float, writable |  |
| `indentationLeft` | property, float, writable |  |
| `indentationRight` | property, float, writable |  |
| `numberDepth` | property, int, writable |  |
| `numberStyle` | property, int, writable |  |
| `title` | property, writable |  |
| `triggerNewPage` | property, bool, writable |  |

<a id="types-pdf-jpdfwriter"></a>

### JPDFWriter

| Member | Kind | Description |
| --- | --- | --- |
| `addJavaScript(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bytes` | property, read-only |  |
| `close(...)` | method |  |

<a id="types-mustang"></a>

## mustang

<a id="types-mustang-mustangallowance"></a>

### MustangAllowance

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable |  |
| `basisQuantity` | property, int, writable |  |
| `isCharge` | property, bool, read-only |  |
| `percent` | property, int, writable |  |
| `reason` | property, string, writable |  |
| `reasonCode` | property, string, writable |  |
| `sequenceNumeric` | property, int, writable |  |
| `setBasisAmount(...)` | method |  |
| `setBasisQuantity(...)` | method |  |
| `setPercent(...)` | method |  |
| `setReason(...)` | method |  |
| `setReasonCode(...)` | method |  |
| `setSequenceNumeric(...)` | method |  |
| `setTaxAllowanceChargeBasisAmount(...)` | method |  |
| `setTaxBasisAmount(...)` | method |  |
| `setTaxCalculatedAmount(...)` | method |  |
| `setTaxCategoryCode(...)` | method |  |
| `setTaxDueDateTypeCode(...)` | method |  |
| `setTaxExemptionReason(...)` | method |  |
| `setTaxExemptionReasonCode(...)` | method |  |
| `setTaxLineTotalBasisAmount(...)` | method |  |
| `setTaxPointDate(...)` | method |  |
| `setTaxRateApplicablePercent(...)` | method |  |
| `setTotalAmount(...)` | method |  |
| `taxAllowanceChargeBasisAmount` | property, int, writable |  |
| `taxBasisAmount` | property, int, writable |  |
| `taxCalculatedAmount` | property, int, writable |  |
| `taxCategoryCode` | property, string, writable |  |
| `taxDueDateTypeCode` | property, string, writable |  |
| `taxExemptionReason` | property, string, writable |  |
| `taxExemptionReasonCode` | property, string, writable |  |
| `taxLineTotalBasisAmount` | property, int, writable |  |
| `taxPointDate` | property, date, writable |  |
| `taxRateApplicablePercent` | property, int, writable |  |
| `totalAmount` | property, int, writable |  |

<a id="types-mustang-mustangbankdetails"></a>

### MustangBankDetails

| Member | Kind | Description |
| --- | --- | --- |
| `accountName` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bic` | property, string, writable |  |
| `iban` | property, string, writable |  |
| `paymentMeansCode` | property, string, writable |  |
| `paymentMeansInformation` | property, string, writable |  |
| `setAccountName(...)` | method |  |
| `setBic(...)` | method |  |
| `setIban(...)` | method |  |
| `setPaymentMeansCode(...)` | method |  |
| `setPaymentMeansInformation(...)` | method |  |

<a id="types-mustang-mustangcashdiscount"></a>

### MustangCashDiscount

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable |  |
| `days` | property, int, writable |  |
| `percent` | property, int, writable |  |
| `setBasisAmount(...)` | method |  |
| `setDays(...)` | method |  |
| `setPercent(...)` | method |  |

<a id="types-mustang-mustangcharge"></a>

### MustangCharge

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisAmount` | property, int, writable |  |
| `basisQuantity` | property, int, writable |  |
| `isCharge` | property, bool, read-only |  |
| `percent` | property, int, writable |  |
| `reason` | property, string, writable |  |
| `reasonCode` | property, string, writable |  |
| `sequenceNumeric` | property, int, writable |  |
| `setBasisAmount(...)` | method |  |
| `setBasisQuantity(...)` | method |  |
| `setPercent(...)` | method |  |
| `setReason(...)` | method |  |
| `setReasonCode(...)` | method |  |
| `setSequenceNumeric(...)` | method |  |
| `setTaxAllowanceChargeBasisAmount(...)` | method |  |
| `setTaxBasisAmount(...)` | method |  |
| `setTaxCalculatedAmount(...)` | method |  |
| `setTaxCategoryCode(...)` | method |  |
| `setTaxDueDateTypeCode(...)` | method |  |
| `setTaxExemptionReason(...)` | method |  |
| `setTaxExemptionReasonCode(...)` | method |  |
| `setTaxLineTotalBasisAmount(...)` | method |  |
| `setTaxPointDate(...)` | method |  |
| `setTaxRateApplicablePercent(...)` | method |  |
| `setTotalAmount(...)` | method |  |
| `taxAllowanceChargeBasisAmount` | property, int, writable |  |
| `taxBasisAmount` | property, int, writable |  |
| `taxCalculatedAmount` | property, int, writable |  |
| `taxCategoryCode` | property, string, writable |  |
| `taxDueDateTypeCode` | property, string, writable |  |
| `taxExemptionReason` | property, string, writable |  |
| `taxExemptionReasonCode` | property, string, writable |  |
| `taxLineTotalBasisAmount` | property, int, writable |  |
| `taxPointDate` | property, date, writable |  |
| `taxRateApplicablePercent` | property, int, writable |  |
| `totalAmount` | property, int, writable |  |

<a id="types-mustang-mustangcontact"></a>

### MustangContact

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `country` | property, string, writable |  |
| `email` | property, string, writable |  |
| `fax` | property, string, writable |  |
| `location` | property, string, writable |  |
| `name` | property, string, writable |  |
| `phone` | property, string, writable |  |
| `setCountry(...)` | method |  |
| `setEmail(...)` | method |  |
| `setFax(...)` | method |  |
| `setLocation(...)` | method |  |
| `setName(...)` | method |  |
| `setPhone(...)` | method |  |
| `setStreet(...)` | method |  |
| `setZip(...)` | method |  |
| `street` | property, string, writable |  |
| `zip` | property, string, writable |  |

<a id="types-mustang-mustangdirectdebit"></a>

### MustangDirectDebit

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `iban` | property, string, writable |  |
| `mandate` | property, string, writable |  |
| `paymentMeansCode` | property, string, writable |  |
| `paymentMeansInformation` | property, string, writable |  |
| `setIban(...)` | method |  |
| `setMandate(...)` | method |  |
| `setPaymentMeansCode(...)` | method |  |
| `setPaymentMeansInformation(...)` | method |  |

<a id="types-mustang-mustangexporter"></a>

### MustangExporter

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `attachFile(...)` | method |  |
| `close(...)` | method |  |
| `disableAutoClose(...)` | method |  |
| `disableFacturX(...)` | method |  |
| `export(...)` | method |  |
| `ignorePDFAErrors(...)` | method |  |
| `load(...)` | method |  |
| `setCreator(...)` | method |  |
| `setFacturX(...)` | method |  |
| `setProducer(...)` | method |  |
| `setProfile(...)` | method |  |
| `setTransaction(...)` | method |  |
| `setVersion(...)` | method |  |
| `setZUGFeRDVersion(...)` | method |  |

<a id="types-mustang-mustangfileattachment"></a>

### MustangFileAttachment

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `description` | property, string, writable |  |
| `filename` | property, string, writable |  |
| `getData(...)` | method |  |
| `mimetype` | property, string, writable |  |
| `relation` | property, string, writable |  |
| `setData(...)` | method |  |
| `setDescription(...)` | method |  |
| `setFilename(...)` | method |  |
| `setMimetype(...)` | method |  |
| `setRelation(...)` | method |  |

<a id="types-mustang-mustangincludednote"></a>

### MustangIncludedNote

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, writable |  |
| `setContent(...)` | method |  |
| `setSubjectCode(...)` | method |  |
| `subjectCode` | property, string, writable |  |

<a id="types-mustang-mustanginvoice"></a>

### MustangInvoice

| Member | Kind | Description |
| --- | --- | --- |
| `addAllowance(...)` | method |  |
| `addCashDiscount(...)` | method |  |
| `addCharge(...)` | method |  |
| `addCustomsNote(...)` | method |  |
| `addDiscountBonusNote(...)` | method |  |
| `addGeneralNote(...)` | method |  |
| `addIncludedNote(...)` | method |  |
| `addIntroductionNote(...)` | method |  |
| `addInvoiceReferencedDocument(...)` | method |  |
| `addItem(...)` | method |  |
| `addLegalNote(...)` | method |  |
| `addNote(...)` | method |  |
| `addPaymentTerms(...)` | method |  |
| `addRegulatoryNote(...)` | method |  |
| `addSellerNote(...)` | method |  |
| `addTaxNote(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `businessProcessId` | property, string, writable |  |
| `buyerOrderReferencedDocument` | property, writable |  |
| `contractReferencedDocument` | property, writable |  |
| `creditorReferenceId` | property, string, writable |  |
| `currency` | property, string, writable |  |
| `deliveryAddress` | property, writable |  |
| `deliveryDate` | property, date, writable |  |
| `deliveryNoteReferencedDocument` | property, writable |  |
| `deliveryTypeCode` | property, string, writable |  |
| `despatchAdviceReferencedDocument` | property, writable |  |
| `detailedDeliveryPeriodFrom` | property, date, writable |  |
| `detailedDeliveryPeriodTo` | property, date, writable |  |
| `documentCode` | property, string, read-only |  |
| `documentName` | property, string, writable |  |
| `dueDate` | property, date, writable |  |
| `embedFileInXML(...)` | method |  |
| `endCustomerDeliveryAddress` | property, writable |  |
| `exportXmlEn16931(...)` | method |  |
| `getAdditionalReferencedDocuments(...)` | method |  |
| `getAllowances(...)` | method |  |
| `getCashDiscounts(...)` | method |  |
| `getCharges(...)` | method |  |
| `getExtendedPaymentTerms(...)` | method |  |
| `getInvoiceReferencedDocuments(...)` | method |  |
| `getItems(...)` | method |  |
| `getNotes(...)` | method |  |
| `getObjectIdentifierReferencedDocument(...)` | method |  |
| `getPlainNotes(...)` | method |  |
| `getRelatedReferencedDocument(...)` | method |  |
| `getTenderReferencedDocument(...)` | method |  |
| `invoicee` | property, writable |  |
| `invoicer` | property, writable |  |
| `issueDate` | property, date, writable |  |
| `number` | property, string, writable |  |
| `ownCountry` | property, string, read-only |  |
| `ownForeignOrganisationID` | property, string, writable |  |
| `ownLocation` | property, string, read-only |  |
| `ownOrganisationFullPlaintextInfo` | property, string, writable |  |
| `ownOrganisationName` | property, string, writable |  |
| `ownStreet` | property, string, read-only |  |
| `ownTaxID` | property, string, writable |  |
| `ownVATID` | property, string, writable |  |
| `ownZIP` | property, string, read-only |  |
| `payee` | property, writable |  |
| `paymentReference` | property, string, writable |  |
| `paymentTermDescription` | property, string, writable |  |
| `paymentTerms` | property, writable |  |
| `recipient` | property, writable |  |
| `referenceNumber` | property, string, writable |  |
| `roundingAmount` | property, int, writable |  |
| `sellerOrderReferencedDocument` | property, writable |  |
| `sender` | property, writable |  |
| `setBusinessProcessId(...)` | method |  |
| `setBuyerOrderReferencedDocument(...)` | method |  |
| `setContractReferencedDocument(...)` | method |  |
| `setCorrection(...)` | method |  |
| `setCreditNote(...)` | method |  |
| `setCreditorReferenceId(...)` | method |  |
| `setCurrency(...)` | method |  |
| `setDeliveryAddress(...)` | method |  |
| `setDeliveryDate(...)` | method |  |
| `setDeliveryNoteReferencedDocument(...)` | method |  |
| `setDeliveryTypeCode(...)` | method |  |
| `setDespatchAdviceReferencedDocument(...)` | method |  |
| `setDetailedDeliveryPeriod(...)` | method |  |
| `setDetailedDeliveryPeriodFrom(...)` | method |  |
| `setDetailedDeliveryPeriodTo(...)` | method |  |
| `setDocumentName(...)` | method |  |
| `setDueDate(...)` | method |  |
| `setEndCustomerDeliveryAddress(...)` | method |  |
| `setInvoicee(...)` | method |  |
| `setInvoicer(...)` | method |  |
| `setIssueDate(...)` | method |  |
| `setNumber(...)` | method |  |
| `setObjectIdentifierReferencedDocument(...)` | method |  |
| `setPayee(...)` | method |  |
| `setPaymentReference(...)` | method |  |
| `setPaymentTermDescription(...)` | method |  |
| `setPaymentTerms(...)` | method |  |
| `setRecipient(...)` | method |  |
| `setReferenceNumber(...)` | method |  |
| `setRelatedReferencedDocument(...)` | method |  |
| `setRoundingAmount(...)` | method |  |
| `setSellerOrderReferencedDocument(...)` | method |  |
| `setSender(...)` | method |  |
| `setShipToCountry(...)` | method |  |
| `setShipToLocation(...)` | method |  |
| `setShipToOrganisationId(...)` | method |  |
| `setShipToOrganisationName(...)` | method |  |
| `setShipToStreet(...)` | method |  |
| `setShipToZip(...)` | method |  |
| `setSpecifiedProcuringProjectId(...)` | method |  |
| `setSpecifiedProcuringProjectName(...)` | method |  |
| `setTaxConversionRate(...)` | method |  |
| `setTaxConversionRateDateTime(...)` | method |  |
| `setTaxCurrency(...)` | method |  |
| `setTaxRepresentative(...)` | method |  |
| `setTenderReferencedDocument(...)` | method |  |
| `setTestIndicator(...)` | method |  |
| `setTotalPrepaidAmount(...)` | method |  |
| `setVatDueDateTypeCode(...)` | method |  |
| `shipToCountry` | property, string, writable |  |
| `shipToLocation` | property, string, writable |  |
| `shipToOrganisationId` | property, string, writable |  |
| `shipToOrganisationName` | property, string, writable |  |
| `shipToStreet` | property, string, writable |  |
| `shipToZip` | property, string, writable |  |
| `specifiedProcuringProjectId` | property, string, writable |  |
| `specifiedProcuringProjectName` | property, string, writable |  |
| `taxConversionRate` | property, int, writable |  |
| `taxConversionRateDateTime` | property, date, writable |  |
| `taxCurrency` | property, string, writable |  |
| `taxRepresentative` | property, writable |  |
| `testIndicator` | property, bool, read-only |  |
| `totalPrepaidAmount` | property, int, writable |  |
| `valid` | property, bool, read-only |  |
| `vatDueDateTypeCode` | property, string, writable |  |

<a id="types-mustang-mustangitem"></a>

### MustangItem

| Member | Kind | Description |
| --- | --- | --- |
| `accountingReference` | property, string, writable |  |
| `addAdditionalReference(...)` | method |  |
| `addAllowance(...)` | method |  |
| `addCharge(...)` | method |  |
| `addIncludedNote(...)` | method |  |
| `addNote(...)` | method |  |
| `addReferencedDocument(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basisQuantity` | property, int, writable |  |
| `buyerOrderReferencedDocument` | property, writable |  |
| `contractReferencedDocument` | property, writable |  |
| `deliveryNoteReferencedDocument` | property, writable |  |
| `despatchAdviceReferencedDocument` | property, writable |  |
| `detailedDeliveryPeriodFrom` | property, date, read-only |  |
| `detailedDeliveryPeriodTo` | property, date, read-only |  |
| `getAdditionalReferences(...)` | method |  |
| `getAllowances(...)` | method |  |
| `getCharges(...)` | method |  |
| `getNotes(...)` | method |  |
| `getPlainNotes(...)` | method |  |
| `getReferencedDocuments(...)` | method |  |
| `grossPrice` | property, int, writable |  |
| `id` | property, string, writable |  |
| `lineSeller` | property, writable |  |
| `lineStatusReasonCode` | property, string, writable |  |
| `lineTotalAmount` | property, int, writable |  |
| `parentLineId` | property, string, writable |  |
| `price` | property, int, writable |  |
| `product` | property, writable |  |
| `quantity` | property, int, writable |  |
| `sellerOrderReferencedDocument` | property, writable |  |
| `setAccountingReference(...)` | method |  |
| `setBasisQuantity(...)` | method |  |
| `setBuyerOrderReferencedDocument(...)` | method |  |
| `setContractReferencedDocument(...)` | method |  |
| `setDeliveryNoteReferencedDocument(...)` | method |  |
| `setDespatchAdviceReferencedDocument(...)` | method |  |
| `setDetailedDeliveryPeriod(...)` | method |  |
| `setGrossPrice(...)` | method |  |
| `setId(...)` | method |  |
| `setLineSeller(...)` | method |  |
| `setLineStatusReasonCode(...)` | method |  |
| `setLineTotalAmount(...)` | method |  |
| `setParentLineId(...)` | method |  |
| `setPrice(...)` | method |  |
| `setProduct(...)` | method |  |
| `setQuantity(...)` | method |  |
| `setSellerOrderReferencedDocument(...)` | method |  |
| `setTax(...)` | method |  |
| `tax` | property, int, writable |  |

<a id="types-mustang-mustanglegalorganisation"></a>

### MustangLegalOrganisation

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `schemedID` | property, writable |  |
| `setSchemedID(...)` | method |  |
| `setTradingBusinessName(...)` | method |  |
| `tradingBusinessName` | property, string, writable |  |

<a id="types-mustang-mustangpaymentterms"></a>

### MustangPaymentTerms

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `description` | property, string, writable |  |
| `dueDate` | property, date, writable |  |
| `setDescription(...)` | method |  |
| `setDueDate(...)` | method |  |

<a id="types-mustang-mustangproduct"></a>

### MustangProduct

| Member | Kind | Description |
| --- | --- | --- |
| `addAllowance(...)` | method |  |
| `addCharge(...)` | method |  |
| `addGlobalId(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `buyerAssignedId` | property, string, writable |  |
| `countryOfOrigin` | property, string, writable |  |
| `description` | property, string, writable |  |
| `getAllowances(...)` | method |  |
| `getCharges(...)` | method |  |
| `globalId` | property, string, read-only |  |
| `globalIdScheme` | property, string, read-only |  |
| `intraCommunitySupply` | property, bool, read-only |  |
| `name` | property, string, writable |  |
| `reverseCharge` | property, bool, read-only |  |
| `sellerAssignedId` | property, string, writable |  |
| `setBuyerAssignedId(...)` | method |  |
| `setCountryOfOrigin(...)` | method |  |
| `setDescription(...)` | method |  |
| `setIntraCommunitySupply(...)` | method |  |
| `setName(...)` | method |  |
| `setReverseCharge(...)` | method |  |
| `setSellerAssignedId(...)` | method |  |
| `setTaxCategoryCode(...)` | method |  |
| `setTaxExemptionReason(...)` | method |  |
| `setTaxExemptionReasonCode(...)` | method |  |
| `setUnit(...)` | method |  |
| `setVatPercent(...)` | method |  |
| `taxCategoryCode` | property, string, writable |  |
| `taxExemptionReason` | property, string, writable |  |
| `taxExemptionReasonCode` | property, string, writable |  |
| `unit` | property, string, writable |  |
| `vatPercent` | property, int, writable |  |

<a id="types-mustang-mustangreferenceddocument"></a>

### MustangReferencedDocument

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `attachmentBinaryObject` | property, writable |  |
| `formattedIssueDateTime` | property, date, writable |  |
| `issuerAssignedID` | property, string, writable |  |
| `lineID` | property, string, writable |  |
| `name` | property, string, writable |  |
| `referenceTypeCode` | property, string, writable |  |
| `setAttachmentBinaryObject(...)` | method |  |
| `setFormattedIssueDateTime(...)` | method |  |
| `setIssuerAssignedID(...)` | method |  |
| `setLineID(...)` | method |  |
| `setName(...)` | method |  |
| `setReferenceTypeCode(...)` | method |  |
| `setTypeCode(...)` | method |  |
| `setUriID(...)` | method |  |
| `typeCode` | property, string, writable |  |
| `uriID` | property, string, writable |  |

<a id="types-mustang-mustangschemedid"></a>

### MustangSchemedID

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `id` | property, string, writable |  |
| `scheme` | property, string, writable |  |
| `setId(...)` | method |  |
| `setScheme(...)` | method |  |

<a id="types-mustang-mustangtradeparty"></a>

### MustangTradeParty

| Member | Kind | Description |
| --- | --- | --- |
| `addBankDetails(...)` | method |  |
| `addDebitDetails(...)` | method |  |
| `addGlobalId(...)` | method |  |
| `addTaxId(...)` | method |  |
| `addUriUniversalCommunicationId(...)` | method |  |
| `addVatId(...)` | method |  |
| `additionalAddress` | property, string, writable |  |
| `additionalAddressExtension` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `contact` | property, writable |  |
| `country` | property, string, writable |  |
| `description` | property, string, writable |  |
| `email` | property, string, writable |  |
| `getBankDetails(...)` | method |  |
| `getDebitDetails(...)` | method |  |
| `globalId` | property, string, writable |  |
| `globalIdScheme` | property, string, writable |  |
| `id` | property, string, writable |  |
| `legalOrganisation` | property, writable |  |
| `location` | property, string, writable |  |
| `name` | property, string, writable |  |
| `setAdditionalAddress(...)` | method |  |
| `setAdditionalAddressExtension(...)` | method |  |
| `setContact(...)` | method |  |
| `setCountry(...)` | method |  |
| `setDescription(...)` | method |  |
| `setEmail(...)` | method |  |
| `setGlobalId(...)` | method |  |
| `setGlobalIdScheme(...)` | method |  |
| `setId(...)` | method |  |
| `setLegalOrganisation(...)` | method |  |
| `setLocation(...)` | method |  |
| `setName(...)` | method |  |
| `setStreet(...)` | method |  |
| `setTaxId(...)` | method |  |
| `setVatId(...)` | method |  |
| `setZip(...)` | method |  |
| `street` | property, string, writable |  |
| `taxId` | property, string, writable |  |
| `uriUniversalCommunicationId` | property, string, read-only |  |
| `uriUniversalCommunicationIdScheme` | property, string, read-only |  |
| `vatId` | property, string, writable |  |
| `zip` | property, string, writable |  |

<a id="types-docusign"></a>

## docusign

<a id="types-docusign-docusignaccount"></a>

### DocusignAccount

| Member | Kind | Description |
| --- | --- | --- |
| `accountId` | property, string, read-only |  |
| `accountName` | property, string, read-only |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `baseUri` | property, string, read-only |  |
| `isDefault` | property, string, read-only |  |

<a id="types-docusign-docusignapiclient"></a>

### DocusignApiClient

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `basePath` | property, string, writable |  |
| `envelopes(...)` | method |  |
| `getUserInfo(...)` | method |  |
| `login(...)` | method |  |
| `oauthBasePath` | property, string, writable |  |
| `requestJWTUserToken(...)` | method |  |
| `setAccessToken(...)` | method |  |

<a id="types-docusign-docusignbulkenvelopestatus"></a>

### DocusignBulkEnvelopeStatus

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `batchId` | property, string, writable |  |
| `batchSize` | property, string, writable |  |
| `bulkEnvelopesBatchUri` | property, string, writable |  |
| `endPosition` | property, string, writable |  |
| `failed` | property, string, writable |  |
| `nextUri` | property, string, writable |  |
| `previousUri` | property, string, writable |  |
| `queued` | property, string, writable |  |
| `resultSetSize` | property, string, writable |  |
| `sent` | property, string, writable |  |
| `startPosition` | property, string, writable |  |
| `submittedDate` | property, string, writable |  |
| `totalSetSize` | property, string, writable |  |

<a id="types-docusign-docusigncarboncopy"></a>

### DocusignCarbonCopy

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable |  |
| `addAccessCodeToEmail` | property, string, writable |  |
| `agentCanEditEmail` | property, string, writable |  |
| `agentCanEditName` | property, string, writable |  |
| `allowSystemOverrideForLockedRecipient` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoRespondedReason` | property, string, writable |  |
| `bulkSendV2Recipient` | property, string, writable |  |
| `clientUserId` | property, string, writable |  |
| `completedCount` | property, string, writable |  |
| `declinedDateTime` | property, string, writable |  |
| `declinedReason` | property, string, writable |  |
| `deliveredDateTime` | property, string, writable |  |
| `deliveryMethod` | property, string, writable |  |
| `designatorId` | property, string, writable |  |
| `designatorIdGuid` | property, string, writable |  |
| `documentTemplateId` | property, string, writable |  |
| `email` | property, string, writable |  |
| `emailRecipientPostSigningURL` | property, string, writable |  |
| `embeddedRecipientStartURL` | property, string, writable |  |
| `faxNumber` | property, string, writable |  |
| `firstName` | property, string, writable |  |
| `fullName` | property, string, writable |  |
| `idCheckConfigurationName` | property, string, writable |  |
| `inheritEmailNotificationConfiguration` | property, string, writable |  |
| `lastName` | property, string, writable |  |
| `linkedAccountConfigurationId` | property, string, writable |  |
| `lockedRecipientPhoneAuthEditable` | property, string, writable |  |
| `lockedRecipientSmsEditable` | property, string, writable |  |
| `name` | property, string, writable |  |
| `note` | property, string, writable |  |
| `recipientId` | property, string, writable |  |
| `recipientIdGuid` | property, string, writable |  |
| `recipientType` | property, string, writable |  |
| `requireIdLookup` | property, string, writable |  |
| `roleName` | property, string, writable |  |
| `routingOrder` | property, string, writable |  |
| `sentDateTime` | property, string, writable |  |
| `signedDateTime` | property, string, writable |  |
| `signingGroupId` | property, string, writable |  |
| `signingGroupName` | property, string, writable |  |
| `status` | property, string, writable |  |
| `statusCode` | property, string, writable |  |
| `suppressEmails` | property, string, writable |  |
| `templateLocked` | property, string, writable |  |
| `templateRequired` | property, string, writable |  |
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
| `documentBase64` | property, string, writable |  |
| `documentId` | property, string, writable |  |
| `documentTemplateId` | property, string, writable |  |
| `encryptedWithKeyManager` | property, string, writable |  |
| `fileExtension` | property, string, writable |  |
| `fileFormatHint` | property, string, writable |  |
| `includeInDownload` | property, string, writable |  |
| `isDocGenDocument` | property, string, writable |  |
| `name` | property, string, writable |  |
| `order` | property, string, writable |  |
| `pages` | property, string, writable |  |
| `password` | property, string, writable |  |
| `pdfFormFieldOption` | property, string, writable |  |
| `remoteUrl` | property, string, writable |  |
| `signerMustAcknowledge` | property, string, writable |  |
| `templateLocked` | property, string, writable |  |
| `templateRequired` | property, string, writable |  |
| `transformPdfFields` | property, string, writable |  |
| `uri` | property, string, writable |  |

<a id="types-docusign-docusignenvelope"></a>

### DocusignEnvelope

| Member | Kind | Description |
| --- | --- | --- |
| `accessControlListBase64` | property, string, writable |  |
| `allowComments` | property, string, writable |  |
| `allowMarkup` | property, string, writable |  |
| `allowReassign` | property, string, writable |  |
| `allowViewHistory` | property, string, writable |  |
| `anySigner` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `asynchronous` | property, string, writable |  |
| `attachmentsUri` | property, string, writable |  |
| `authoritativeCopy` | property, string, writable |  |
| `authoritativeCopyDefault` | property, string, writable |  |
| `autoNavigation` | property, string, writable |  |
| `brandId` | property, string, writable |  |
| `brandLock` | property, string, writable |  |
| `burnDefaultTabData` | property, string, writable |  |
| `certificateUri` | property, string, writable |  |
| `completedDateTime` | property, string, writable |  |
| `copyRecipientData` | property, string, writable |  |
| `createdDateTime` | property, string, writable |  |
| `customFieldsUri` | property, string, writable |  |
| `declinedDateTime` | property, string, writable |  |
| `deletedDateTime` | property, string, writable |  |
| `deliveredDateTime` | property, string, writable |  |
| `disableResponsiveDocument` | property, string, writable |  |
| `documentBase64` | property, string, writable |  |
| `documentsCombinedUri` | property, string, writable |  |
| `documentsUri` | property, string, writable |  |
| `emailBlurb` | property, string, writable |  |
| `emailSubject` | property, string, writable |  |
| `enableWetSign` | property, string, writable |  |
| `enforceSignerVisibility` | property, string, writable |  |
| `envelopeId` | property, string, writable |  |
| `envelopeIdStamping` | property, string, writable |  |
| `envelopeLocation` | property, string, writable |  |
| `envelopeUri` | property, string, writable |  |
| `expireAfter` | property, string, writable |  |
| `expireDateTime` | property, string, writable |  |
| `expireEnabled` | property, string, writable |  |
| `externalEnvelopeId` | property, string, writable |  |
| `hasComments` | property, string, writable |  |
| `hasFormDataChanged` | property, string, writable |  |
| `hasWavFile` | property, string, writable |  |
| `holder` | property, string, writable |  |
| `initialSentDateTime` | property, string, writable |  |
| `is21CFRPart11` | property, string, writable |  |
| `isDynamicEnvelope` | property, string, writable |  |
| `isSignatureProviderEnvelope` | property, string, writable |  |
| `isTicketRelatedEnvelope` | property, string, writable |  |
| `lastModifiedDateTime` | property, string, writable |  |
| `location` | property, string, writable |  |
| `messageLock` | property, string, writable |  |
| `notificationUri` | property, string, writable |  |
| `purgeCompletedDate` | property, string, writable |  |
| `purgeRequestDate` | property, string, writable |  |
| `purgeState` | property, string, writable |  |
| `recipientsLock` | property, string, writable |  |
| `recipientsUri` | property, string, writable |  |
| `sentDateTime` | property, string, writable |  |
| `signerCanSignOnMobile` | property, string, writable |  |
| `signingLocation` | property, string, writable |  |
| `status` | property, string, writable |  |
| `statusChangedDateTime` | property, string, writable |  |
| `templatesUri` | property, string, writable |  |
| `transactionId` | property, string, writable |  |
| `uSigState` | property, string, writable |  |
| `useDisclosure` | property, string, writable |  |
| `voidedDateTime` | property, string, writable |  |
| `voidedReason` | property, string, writable |  |

<a id="types-docusign-docusignenvelopedefinition"></a>

### DocusignEnvelopeDefinition

| Member | Kind | Description |
| --- | --- | --- |
| `accessControlListBase64` | property, string, writable |  |
| `accessibility` | property, string, writable |  |
| `addDocument(...)` | method |  |
| `addTemplateRole(...)` | method |  |
| `allowComments` | property, string, writable |  |
| `allowMarkup` | property, string, writable |  |
| `allowReassign` | property, string, writable |  |
| `allowRecipientRecursion` | property, string, writable |  |
| `allowViewHistory` | property, string, writable |  |
| `anySigner` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `asynchronous` | property, string, writable |  |
| `attachmentsUri` | property, string, writable |  |
| `authoritativeCopy` | property, string, writable |  |
| `authoritativeCopyDefault` | property, string, writable |  |
| `autoNavigation` | property, string, writable |  |
| `brandId` | property, string, writable |  |
| `brandLock` | property, string, writable |  |
| `burnDefaultTabData` | property, string, writable |  |
| `certificateUri` | property, string, writable |  |
| `completedDateTime` | property, string, writable |  |
| `copyRecipientData` | property, string, writable |  |
| `createdDateTime` | property, string, writable |  |
| `customFieldsUri` | property, string, writable |  |
| `declinedDateTime` | property, string, writable |  |
| `deletedDateTime` | property, string, writable |  |
| `deliveredDateTime` | property, string, writable |  |
| `disableResponsiveDocument` | property, string, writable |  |
| `documentBase64` | property, string, writable |  |
| `documentsCombinedUri` | property, string, writable |  |
| `documentsUri` | property, string, writable |  |
| `emailBlurb` | property, string, writable |  |
| `emailSubject` | property, string, writable |  |
| `enableWetSign` | property, string, writable |  |
| `enforceSignerVisibility` | property, string, writable |  |
| `envelopeId` | property, string, writable |  |
| `envelopeIdStamping` | property, string, writable |  |
| `envelopeLocation` | property, string, writable |  |
| `envelopeUri` | property, string, writable |  |
| `expireAfter` | property, string, writable |  |
| `expireDateTime` | property, string, writable |  |
| `expireEnabled` | property, string, writable |  |
| `externalEnvelopeId` | property, string, writable |  |
| `hasComments` | property, string, writable |  |
| `hasFormDataChanged` | property, string, writable |  |
| `hasWavFile` | property, string, writable |  |
| `holder` | property, string, writable |  |
| `initialSentDateTime` | property, string, writable |  |
| `is21CFRPart11` | property, string, writable |  |
| `isDynamicEnvelope` | property, string, writable |  |
| `isSignatureProviderEnvelope` | property, string, writable |  |
| `lastModifiedDateTime` | property, string, writable |  |
| `location` | property, string, writable |  |
| `messageLock` | property, string, writable |  |
| `notificationUri` | property, string, writable |  |
| `password` | property, string, writable |  |
| `purgeCompletedDate` | property, string, writable |  |
| `purgeRequestDate` | property, string, writable |  |
| `purgeState` | property, string, writable |  |
| `recipients` | property, writable |  |
| `recipientsLock` | property, string, writable |  |
| `recipientsUri` | property, string, writable |  |
| `sentDateTime` | property, string, writable |  |
| `signerCanSignOnMobile` | property, string, writable |  |
| `signingLocation` | property, string, writable |  |
| `status` | property, string, writable |  |
| `statusChangedDateTime` | property, string, writable |  |
| `templateId` | property, string, writable |  |
| `templatesUri` | property, string, writable |  |
| `transactionId` | property, string, writable |  |
| `uSigState` | property, string, writable |  |
| `useDisclosure` | property, string, writable |  |
| `voidedDateTime` | property, string, writable |  |
| `voidedReason` | property, string, writable |  |

<a id="types-docusign-docusignenvelopesummary"></a>

### DocusignEnvelopeSummary

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bulkEnvelopeStatus` | property, writable |  |
| `envelopeId` | property, string, writable |  |
| `errorDetails` | property, writable |  |
| `recipientSigningUri` | property, string, writable |  |
| `recipientSigningUriError` | property, string, writable |  |
| `status` | property, string, writable |  |
| `statusDateTime` | property, string, writable |  |
| `uri` | property, string, writable |  |

<a id="types-docusign-docusignenvelopesapi"></a>

### DocusignEnvelopesApi

| Member | Kind | Description |
| --- | --- | --- |
| `CreateEnvelopeOptions(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `createEnvelope(...)` | method |  |
| `getDocumentWithHttpInfo(...)` | method |  |
| `getEnvelope(...)` | method |  |

<a id="types-docusign-docusignerrordetails"></a>

### DocusignErrorDetails

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `errorCode` | property, string, writable |  |
| `message` | property, string, writable |  |

<a id="types-docusign-docusigngetdocumentoptions"></a>

### DocusignGetDocumentOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `certificate` | property, string, writable |  |
| `documentsByUserid` | property, string, writable |  |
| `encoding` | property, string, writable |  |
| `encrypt` | property, string, writable |  |
| `language` | property, string, writable |  |
| `recipientId` | property, string, writable |  |
| `sharedUserId` | property, string, writable |  |
| `showChanges` | property, string, writable |  |
| `watermark` | property, string, writable |  |

<a id="types-docusign-docusigngetenvelopeoptions"></a>

### DocusignGetEnvelopeOptions

| Member | Kind | Description |
| --- | --- | --- |
| `advancedUpdate` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `include` | property, string, writable |  |
| `includeAnchorTabLocations` | property, string, writable |  |

<a id="types-docusign-docusignoauthtoken"></a>

### DocusignOAuthToken

| Member | Kind | Description |
| --- | --- | --- |
| `accessToken` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `expiresIn` | property, int, writable |  |
| `refreshToken` | property, string, writable |  |
| `scope` | property, string, writable |  |
| `tokenType` | property, string, writable |  |

<a id="types-docusign-docusignrecipients"></a>

### DocusignRecipients

| Member | Kind | Description |
| --- | --- | --- |
| `addCarbonCopy(...)` | method |  |
| `addSigner(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-docusign-docusignsignhere"></a>

### DocusignSignHere

| Member | Kind | Description |
| --- | --- | --- |
| `agreementAttribute` | property, string, writable |  |
| `agreementAttributeLocked` | property, string, writable |  |
| `anchorAllowWhiteSpaceInCharacters` | property, string, writable |  |
| `anchorCaseSensitive` | property, string, writable |  |
| `anchorHorizontalAlignment` | property, string, writable |  |
| `anchorIgnoreIfNotPresent` | property, string, writable |  |
| `anchorMatchWholeWord` | property, string, writable |  |
| `anchorString` | property, string, writable |  |
| `anchorTabProcessorVersion` | property, string, writable |  |
| `anchorUnits` | property, string, writable |  |
| `anchorXOffset` | property, string, writable |  |
| `anchorYOffset` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `caption` | property, string, writable |  |
| `conditionalParentLabel` | property, string, writable |  |
| `conditionalParentValue` | property, string, writable |  |
| `customTabId` | property, string, writable |  |
| `documentId` | property, string, writable |  |
| `formOrder` | property, string, writable |  |
| `formPageLabel` | property, string, writable |  |
| `formPageNumber` | property, string, writable |  |
| `handDrawRequired` | property, string, writable |  |
| `height` | property, string, writable |  |
| `isSealSignTab` | property, string, writable |  |
| `mergeFieldXml` | property, string, writable |  |
| `name` | property, string, writable |  |
| `optional` | property, string, writable |  |
| `pageNumber` | property, string, writable |  |
| `recipientId` | property, string, writable |  |
| `recipientIdGuid` | property, string, writable |  |
| `scaleValue` | property, string, writable |  |
| `source` | property, string, writable |  |
| `stampType` | property, string, writable |  |
| `status` | property, string, writable |  |
| `tabFullyQualifiedPath` | property, string, writable |  |
| `tabId` | property, string, writable |  |
| `tabLabel` | property, string, writable |  |
| `tabOrder` | property, string, writable |  |
| `tabType` | property, string, writable |  |
| `templateLocked` | property, string, writable |  |
| `templateRequired` | property, string, writable |  |

<a id="types-docusign-docusignsigner"></a>

### DocusignSigner

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable |  |
| `addAccessCodeToEmail` | property, string, writable |  |
| `agentCanEditEmail` | property, string, writable |  |
| `agentCanEditName` | property, string, writable |  |
| `allowSystemOverrideForLockedRecipient` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `autoNavigation` | property, string, writable |  |
| `autoRespondedReason` | property, string, writable |  |
| `bulkRecipientsUri` | property, string, writable |  |
| `bulkSendV2Recipient` | property, string, writable |  |
| `canSignOffline` | property, string, writable |  |
| `clientUserId` | property, string, writable |  |
| `completedCount` | property, string, writable |  |
| `creationReason` | property, string, writable |  |
| `declinedDateTime` | property, string, writable |  |
| `declinedReason` | property, string, writable |  |
| `defaultRecipient` | property, string, writable |  |
| `deliveredDateTime` | property, string, writable |  |
| `deliveryMethod` | property, string, writable |  |
| `designatorId` | property, string, writable |  |
| `designatorIdGuid` | property, string, writable |  |
| `documentTemplateId` | property, string, writable |  |
| `email` | property, string, writable |  |
| `emailRecipientPostSigningURL` | property, string, writable |  |
| `embeddedRecipientStartURL` | property, string, writable |  |
| `faxNumber` | property, string, writable |  |
| `firstName` | property, string, writable |  |
| `fullName` | property, string, writable |  |
| `idCheckConfigurationName` | property, string, writable |  |
| `inheritEmailNotificationConfiguration` | property, string, writable |  |
| `isBulkRecipient` | property, string, writable |  |
| `lastName` | property, string, writable |  |
| `lockedRecipientPhoneAuthEditable` | property, string, writable |  |
| `lockedRecipientSmsEditable` | property, string, writable |  |
| `name` | property, string, writable |  |
| `notaryId` | property, string, writable |  |
| `notarySignerEmailSent` | property, string, writable |  |
| `note` | property, string, writable |  |
| `recipientId` | property, string, writable |  |
| `recipientIdGuid` | property, string, writable |  |
| `recipientSuppliesTabs` | property, string, writable |  |
| `recipientType` | property, string, writable |  |
| `requireIdLookup` | property, string, writable |  |
| `requireSignOnPaper` | property, string, writable |  |
| `requireSignerCertificate` | property, string, writable |  |
| `requireUploadSignature` | property, string, writable |  |
| `roleName` | property, string, writable |  |
| `routingOrder` | property, string, writable |  |
| `sentDateTime` | property, string, writable |  |
| `signInEachLocation` | property, string, writable |  |
| `signedDateTime` | property, string, writable |  |
| `signingGroupId` | property, string, writable |  |
| `signingGroupName` | property, string, writable |  |
| `status` | property, string, writable |  |
| `statusCode` | property, string, writable |  |
| `suppressEmails` | property, string, writable |  |
| `tabs` | property, writable |  |
| `templateLocked` | property, string, writable |  |
| `templateRequired` | property, string, writable |  |
| `totalTabCount` | property, string, writable |  |
| `userId` | property, string, writable |  |
| `webFormRecipientViewId` | property, string, writable |  |

<a id="types-docusign-docusigntabs"></a>

### DocusignTabs

| Member | Kind | Description |
| --- | --- | --- |
| `addSignHere(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |

<a id="types-docusign-docusigntemplaterole"></a>

### DocusignTemplateRole

| Member | Kind | Description |
| --- | --- | --- |
| `accessCode` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `clientUserId` | property, string, writable |  |
| `defaultRecipient` | property, string, writable |  |
| `deliveryMethod` | property, string, writable |  |
| `email` | property, string, writable |  |
| `embeddedRecipientStartURL` | property, string, writable |  |
| `inPersonSignerName` | property, string, writable |  |
| `name` | property, string, writable |  |
| `roleName` | property, string, writable |  |
| `routingOrder` | property, string, writable |  |
| `signingGroupId` | property, string, writable |  |
| `tabs` | property, writable |  |

<a id="types-docusign-docusignuserinfo"></a>

### DocusignUserInfo

| Member | Kind | Description |
| --- | --- | --- |
| `accounts` | property, list, read-only |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `email` | property, string, read-only |  |
| `familyName` | property, string, read-only |  |
| `givenName` | property, string, read-only |  |
| `name` | property, string, read-only |  |
| `sub` | property, string, read-only |  |

<a id="types-msgraph"></a>

## msgraph

<a id="types-msgraph-emailaddress"></a>

### EmailAddress

| Member | Kind | Description |
| --- | --- | --- |
| `address` | property, string, writable |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `name` | property, string, writable |  |

<a id="types-msgraph-itembody"></a>

### ItemBody

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, string, writable |  |
| `contentType` | property, string, writable |  |
| `html` | property, bool, read-only |  |

<a id="types-msgraph-mailfolder"></a>

### MailFolder

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `childFolderCount` | property, int, writable |  |
| `displayName` | property, string, writable |  |
| `id` | property, string, writable |  |
| `isHidden` | property, bool, writable |  |
| `parentFolderId` | property, string, writable |  |
| `totalItemCount` | property, int, writable |  |
| `unreadItemCount` | property, int, writable |  |

<a id="types-msgraph-mailmessage"></a>

### MailMessage

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `body` | property, writable |  |
| `categories` | property, list, writable |  |
| `id` | property, string, writable |  |
| `internetMessageId` | property, string, writable |  |
| `isRead` | property, bool, writable |  |
| `parentFolderId` | property, string, writable |  |
| `sender` | property, writable |  |
| `subject` | property, string, writable |  |
| `uniqueBody` | property, writable |  |

<a id="types-msgraph-messagequery"></a>

### MessageQuery

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `bodyType` | property, string, writable |  |
| `filter` | property, string, writable |  |
| `orderBy` | property, string, writable |  |
| `select` | property, list, writable |  |
| `top` | property, int, writable |  |

<a id="types-msgraph-messageupdate"></a>

### MessageUpdate

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `categories` | property, list, writable |  |
| `isRead` | property, bool, writable |  |
| `subject` | property, string, writable |  |

<a id="types-msgraph-msgraphclient"></a>

### MsGraphClient

| Member | Kind | Description |
| --- | --- | --- |
| `allFolders(...)` | method |  |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `createFolder(...)` | method |  |
| `deleteFolder(...)` | method |  |
| `findFolder(...)` | method |  |
| `folder(...)` | method |  |
| `folders(...)` | method |  |
| `messages(...)` | method |  |
| `mime(...)` | method |  |
| `move(...)` | method |  |
| `update(...)` | method |  |

<a id="types-msgraph-msgraphoptions"></a>

### MsGraphOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `baseUrl` | property, string, writable |  |
| `maxRetries` | property, int, writable |  |
| `proxyHost` | property, string, writable |  |
| `proxyPort` | property, int, writable |  |
| `timeoutSeconds` | property, int, writable |  |
| `tokenCache` | property, bool, writable |  |

<a id="types-msgraph-recipient"></a>

### Recipient

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `emailAddress` | property, writable |  |

<a id="types-mcp"></a>

## mcp

<a id="types-mcp-mcpconnection"></a>

### McpConnection

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `call(...)` | method |  |
| `capabilities` | property, read-only |  |
| `close(...)` | method |  |
| `instructions` | property, string, read-only |  |
| `protocolVersion` | property, string, read-only |  |
| `serverName` | property, string, read-only |  |
| `serverVersion` | property, string, read-only |  |
| `tools(...)` | method |  |

<a id="types-mcp-mcpoptions"></a>

### McpOptions

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `clientName` | property, string, writable |  |
| `clientVersion` | property, string, writable |  |
| `throwOnError` | property, bool, writable |  |
| `timeoutSeconds` | property, int, writable |  |
| `transport` | property, string, writable |  |

<a id="types-mcp-mcptoolresult"></a>

### McpToolResult

| Member | Kind | Description |
| --- | --- | --- |
| `apply(map)` | method | Assigns each entry of the map to the property of the same name, and returns the object itself. |
| `content` | property, read-only |  |
| `isError` | property, bool, read-only |  |
| `structured` | property, read-only |  |
| `text` | property, string, read-only |  |
