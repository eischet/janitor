# Hosting Janitor

This part is for developers of applications that embed Janitor (hosts). It explains how a host gives scripts modules
and global variables, and how it documents them on top of this reference. The Java API itself is described in its
Javadoc; `janitor-demo` in the Janitor repository is a complete, small host.

<a id="embedding"></a>

## Embedding in a nutshell

A host has an **environment** (a subclass of `JanitorDefaultEnvironment`) that holds the modules and the shared built-in
scope, and a **runtime** (a subclass of `BaseRuntime`) that compiles and runs scripts and receives their output.

```java
JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingLocale(Locale.GERMANY)) {
    @Override
    public void warn(String message) {
        log.warn(message);
    }
};
BaseRuntime runtime = new BaseRuntime(env) {
    @Override
    public JanitorObject print(JanitorScriptProcess process, JCallArgs args) {
        log.info(args.getList().stream().map(JanitorObject::janitorToString).collect(Collectors.joining(" ")));
        return JNull.NULL;
    }

    @Override
    public void warn(String warning) {
        log.warn(warning);
    }
};
RunnableScript script = runtime.compile("discount-rule", source);  // throws JanitorCompilerException
JanitorObject result = script.run(process -> process.bind("order", orderObject));
```

The `JanitorFormatting` passed to the environment decides how dates and numbers are formatted where a script does not
give a pattern, e.g. `date.format()`.

<a id="registering-modules"></a>

## Registering modules

Modules for `import name;` are registered with the environment:

```java
env.addModule(CollectionsModule.REGISTRATION);
env.addModule(PdfModule.REGISTRATION);
env.addModule(new JanitorModuleRegistration("billing", BillingModule::new));
```

- `JanitorModulesCommon.registerCommonModules(env, includingNonSandboxed)` (artifact `janitor-modules-common`)
  registers the usual standard modules; `files` and `os` only with `includingNonSandboxed = true`. See the table in
  [modules.md](modules.md#standard-modules).
- `env.autoDiscoverModules()` registers all modules that `DiscoverableModules` services on the class path provide
  (`java.util.ServiceLoader`).
- Register a module only for scripts that may use everything it offers: Janitor does not restrict what a registered
  module can do. `files`, `os`, `httpclient`, `msgraph`, `mcp`, `docusign` and `brrr` reach outside the process.

<a id="writing-modules"></a>

## Writing a module

A module is a `JanitorModule`, usually a `JanitorComposed` with a dispatch table. Give every method and property a HELP
text that starts with its call form, as the built-in types do: then `help(...)` works in scripts, and the host can
generate tables like those in [builtins.md](builtins.md) from it.

```java
public class BillingModule extends JanitorComposed<BillingModule> implements JanitorModule {
    private static final DispatchTable<BillingModule> DISPATCH = new DispatchTable<>(BillingModule::new, false);

    static {
        DISPATCH.addMethod("invoiceTotal", (self, process, args) -> {
            String invoiceNumber = args.getRequiredStringValue(0);
            return process.getBuiltins().floatingPoint(lookUpTotal(invoiceNumber));
        }).setMetaData(Janitor.MetaData.HELP, "invoiceTotal(invoiceNumber): Returns the total of the invoice, as a float.");
    }

    public BillingModule() {
        super(DISPATCH);
    }
}
```

<a id="module-resolvers"></a>

## Resolving modules by name

`import "NAME" as alias;` asks the environment's module resolvers, which the host adds with
`env.addModuleResolver(resolver)`. A resolver returns a module for a name, or `null` if it does not know the name; the
resolver added last is asked first. Use this form for modules whose names are not Java identifiers, that are looked up in
a database, or that are themselves Janitor scripts:

```java
env.addModuleResolver((process, name) -> {
    String source = scriptRepository.findModuleSource(name);   // e.g. scripts that users maintain in the host
    return source == null ? null : ScriptModule.moduleFromScript(process, name, source, null);
});
```

A module made from a script exposes the variables and functions defined at the top level of that script.

<a id="binding-globals"></a>

## Binding global variables

There are two places for globals:

- **Per run**: `script.run(process -> process.bind("name", value))`. The run methods (`run`, `runAndKeepGlobals`,
  `runInScope`), `filterScript` and the `ScriptModule` classes take a callback that receives the script process after it
  has been created and before the script starts. Use it for the data a script works on, such as the current record.
  `bind` accepts Janitor objects and plain strings, numbers and booleans, `bindF` binds a function, and both return the
  process, so calls can be chained: `process.bind("order", order).bind("user", user)`. The callback can also configure
  the process, e.g. its [instruction limit](#limits).
- **Shared by all scripts**: `env.setupBuiltinScope(scope -> scope.bind("name", value))`. Use this only for values that
  never change, such as constants or stateless functions. The built-in scope is shared between all scripts of the
  environment, so mutable values would leak from one run into the next.

Host objects become usable in scripts when they are `JanitorObject`s, typically `JanitorComposed` subclasses with a
dispatch table, or wrapped with `JanitorWrapper`. Give their members HELP texts, too.

<a id="limits"></a>

## Limiting scripts

A script can be limited to a number of instructions, so that an endless loop ends with a
`JanitorInstructionLimitExceededException` instead of blocking the host:

- `JanitorDefaultEnvironment.setMaxInstructionCount(n)` sets a default for every script process of the environment,
  including filter scripts, templates and callbacks; 0, the default, means no limit.
- A run can override it in its setup callback: `script.run(process -> process.setMaxInstructionCount(n))`.

```java
env.setMaxInstructionCount(100_000);                                  // all scripts
script.run(process -> process.bind("order", order).setMaxInstructionCount(1_000_000)); // this run
```

A script cannot escape the limit: `catch` sees the error, but every further instruction fails again, so the script
ends with this error anyway. A function that the host calls later as a callback runs in a new process, which gets the
environment's default, not the limit of the run that created the function.

<a id="extension-points"></a>

## Extension points for host documentation

This reference describes Janitor as every host gets it. A host describes **what it adds** in documents of its own, and
refers to the following sections. Their anchors are stable, so host documents can link to them, e.g.
`hosting.md#host-provided-modules`. The reference itself never names a particular host.

<a id="host-provided"></a>

### Provided by the host

A host document should cover these points, in this order, each under a heading of its own. Leave out what does not apply.

<a id="host-provided-context"></a>

#### Script contexts

Where scripts are used in the host (rules, filters, buttons, scheduled jobs, ...), and for each context:

- what the result of the script means (see [the result of a script](index.md#script-result)), e.g. "a filter keeps a
  record if the result is truthy";
- which globals are bound;
- which modules are registered;
- whether there is an instruction limit;
- where `print` output goes.

<a id="host-provided-globals"></a>

#### Globals

Every global variable and function that the host binds, with its type and meaning. Built-in globals (`print`,
`assert`, `help`, `dir`) need not be repeated; describe only what the host changes, e.g. where `print` writes.

<a id="host-provided-modules"></a>

#### Modules

Which [standard modules](modules.md#standard-modules) the host registers, in which contexts, and the host's own modules:
the name and import form (`import name;` or `import "NAME" as alias;`), and for each function its parameters, result,
errors and an example.

<a id="host-provided-types"></a>

#### Types

The host's object types (business objects, records, ...) with their methods and properties, as in
[builtins.md](builtins.md). If the host gives its members HELP texts, it can generate these tables, like Janitor does.

<a id="host-provided-conventions"></a>

#### Conventions and examples

Naming conventions, typical scripts, and pitfalls that are specific to the host.

<a id="host-document-template"></a>

### A template for host documents

```markdown
# Scripting in Example App

Scripts in Example App are written in Janitor. This document describes only what Example App adds; for the language
itself, see the Janitor language reference (janitor-docs, version 0.9.70), especially pitfalls.md.

## Script contexts
<!-- extends: hosting.md#host-provided-context -->

## Globals
<!-- extends: hosting.md#host-provided-globals -->

## Modules
<!-- extends: hosting.md#host-provided-modules -->

## Types
<!-- extends: hosting.md#host-provided-types -->

## Conventions and examples
<!-- extends: hosting.md#host-provided-conventions -->
```

When a host passes documentation to an AI model, it should give the model this reference first, then its own document,
and say which context a script is for.

<a id="shipping-the-reference"></a>

## Shipping the reference

The Markdown files of this reference are published in the artifact `com.eischet.janitor:janitor-docs`, with the same
version as Janitor, under `META-INF/janitor/docs/` on the class path. The artifact has no dependencies.

```xml
<dependency>
    <groupId>com.eischet.janitor</groupId>
    <artifactId>janitor-docs</artifactId>
    <version>${janitor.version}</version>
</dependency>
```

`com.eischet.janitor.docs.JanitorDocs` reads them:

| Method | Returns |
| --- | --- |
| `JanitorDocs.version()` | the Janitor version that the reference describes |
| `JanitorDocs.files()` | the file names, in reading order |
| `JanitorDocs.read(name)` | the content of one file, as an `Optional<String>` |
| `JanitorDocs.readAll()` | all files in one text, e.g. for the context of an AI model |

Without the class, read `META-INF/janitor/docs/janitor-docs.properties`: `version` is the version, and `files` lists
the files in reading order, separated by commas. Links between the files are relative (`modules.md#import`), so they
keep working when a host serves the files from one directory.
