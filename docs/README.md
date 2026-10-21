# Maintaining the language reference

`docs/reference/` contains the Janitor language reference, for people and AI models. This file explains how it is kept
in line with the code. It is not part of the published reference.

## Files

| File | Written by |
| --- | --- |
| `index.md`, `language.md`, `pitfalls.md`, `hosting.md`, `open-questions.md` | hand |
| `builtins.md`, `modules.md` | hand, with generated sections |
| `module-types.md` | the generator, completely |

Headings that others may link to have an explicit anchor in the line before them, `<a id="..."></a>`. Hosts link to
these anchors, so do not rename them; add new ones instead. The extension points for host documents are in
`hosting.md#extension-points`. The reference must not mention a particular host application.

## Generated sections

Sections between `<!-- generated:begin ID -->` and `<!-- generated:end -->` are generated from the interpreter's dispatch
tables by `ReferenceGenerator` (in `janitor-tests`, package `com.eischet.janitor.docs`):

- `builtins/...`: the members of the built-in types, with the HELP texts of their registrations in `janitor-lang`
  (`JStringClass.applyDefaults` and so on). Every member of a built-in type must have a HELP text that starts with its
  call form, `"name(arguments): description"` for methods and `"name: description"` for properties.
- `modules/...`: the table of standard modules and the members of each module object.
- `module-types.md`: the members of the object types in each module's package.

To regenerate everything after changing built-in methods or modules, run:

```bash
mvn -pl janitor-tests -am test -Dtest=ReferenceDocsTest -Djanitor.docs.regenerate=true -Dsurefire.failIfNoSpecifiedTests=false
```

Without `-Djanitor.docs.regenerate=true`, `ReferenceDocsTest` fails if a generated section is out of date, if a built-in
member has no proper HELP text, if a module of `janitor-modules/pom.xml` or a global function is not described, if a
link or anchor is broken, or if `janitor-docs.properties` does not list every file. A new standard module has to be
added to `ReferenceGenerator.MODULES` and described in `modules.md` with an anchor `<a id="module-NAME"></a>`.

## Examples

`ReferenceExamplesTest` runs every code block of the reference whose info string starts with `janitor`, with all
standard modules registered and the test runtime, which separates `print` arguments with a space and formats dates in
German.

| Info string | Check |
| --- | --- |
| `janitor` | compiles and runs without error |
| `janitor error` | fails, when compiling or when running |
| `janitor compile-only` | compiles; not run, e.g. because it needs a network, files or host bindings |

A `janitor` block can be followed by an `output` block (exactly what the script prints) and a `result` block (the
script's result as a string), a `janitor error` block by an `error` block (text that the error message must contain).
Avoid examples whose output depends on the machine: the order of map entries, the current date, and the default locale
of the Java VM (`"%.2f".format(x)`, month names).

Code blocks in other languages (`java`, `xml`, `markdown`, `bash`) are not checked.

## Publishing

The artifact `janitor-docs` packages `docs/reference/*.md` under `META-INF/janitor/docs/`, together with
`janitor-docs.properties` (version and reading order, in `janitor-docs/src/main/resources-filtered`). When you add a
file to the reference, add it to the `files` list there.
