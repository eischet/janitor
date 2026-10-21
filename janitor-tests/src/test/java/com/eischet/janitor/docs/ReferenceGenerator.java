// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docs;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.docusign.DocusignModule;
import com.eischet.janitor.env.JElement;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.generator.GeneratorModule;
import com.eischet.janitor.modules.brrr.BrrrModule;
import com.eischet.janitor.modules.commonmark.CommonMarkModule;
import com.eischet.janitor.modules.common.JanitorModulesCommon;
import com.eischet.janitor.modules.files.FilesModule;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.modules.janitor.JanitorInternalsModule;
import com.eischet.janitor.modules.mcp.McpModule;
import com.eischet.janitor.modules.msgraph.MsGraphModule;
import com.eischet.janitor.modules.mustang.MustangModule;
import com.eischet.janitor.modules.os.OperatingSystemModule;
import com.eischet.janitor.modules.pdf.PdfModule;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates the parts of the language reference in {@code docs/reference} that describe built-in types and modules, by
 * reading the dispatch tables that the interpreter actually uses. See {@code docs/reference/README.md} for how to run it.
 * <p>
 * Markdown files mark generated sections like this; everything between the markers is replaced:
 * <pre>
 * &lt;!-- generated:begin builtins/string --&gt;
 * ...
 * &lt;!-- generated:end --&gt;
 * </pre>
 * A file that starts with {@link #GENERATED_FILE_MARKER} is generated as a whole.
 */
public final class ReferenceGenerator {

    /** First line of a file that is generated as a whole. */
    public static final String GENERATED_FILE_MARKER = "<!-- generated file: do not edit, see README.md -->";

    /** The file that is generated as a whole. */
    public static final String MODULE_TYPES_FILE = "module-types.md";

    private static final Pattern SECTION = Pattern.compile("(<!-- generated:begin (\\S+) -->)(.*?)(<!-- generated:end -->)", Pattern.DOTALL);

    /** Members that every built-in value has; documented once, in their own section. */
    private static final String COMMON = "common";

    /**
     * A standard module of the project.
     * @param registration the registration, which provides the name used in {@code import name;}
     * @param artifact the Maven coordinates (groupId:artifactId) of the artifact that contains the module
     * @param javaRegistration how Java code refers to the registration
     */
    public record ModuleInfo(JanitorModuleRegistration registration, String artifact, String javaRegistration) {
        public String name() {
            return registration.getQualifiedName();
        }
    }

    /** All standard modules, in the order in which the reference lists them. */
    public static final List<ModuleInfo> MODULES = List.of(
            new ModuleInfo(CollectionsModule.REGISTRATION, "com.eischet.janitor:janitor-lang", "CollectionsModule.REGISTRATION"),
            new ModuleInfo(JanitorInternalsModule.REGISTRATION, "com.eischet.janitor.modules:janitor", "JanitorInternalsModule.REGISTRATION"),
            new ModuleInfo(GeneratorModule.REGISTRATION, "com.eischet.janitor:janitor-generator", "GeneratorModule.REGISTRATION"),
            new ModuleInfo(HttpClientModule.REGISTRATION, "com.eischet.janitor.modules:httpclient", "HttpClientModule.REGISTRATION"),
            new ModuleInfo(BrrrModule.REGISTRATION, "com.eischet.janitor.modules:brrr", "BrrrModule.REGISTRATION"),
            new ModuleInfo(FilesModule.REGISTRATION, "com.eischet.janitor.modules:files", "FilesModule.REGISTRATION"),
            new ModuleInfo(OperatingSystemModule.REGISTRATION, "com.eischet.janitor.modules:os", "OperatingSystemModule.REGISTRATION"),
            new ModuleInfo(CommonMarkModule.REGISTRATION, "com.eischet.janitor.modules:commonmark", "CommonMarkModule.REGISTRATION"),
            new ModuleInfo(PdfModule.REGISTRATION, "com.eischet.janitor.modules:pdf", "PdfModule.REGISTRATION"),
            new ModuleInfo(MustangModule.REGISTRATION, "com.eischet.janitor.modules:mustang", "MustangModule.REGISTRATION"),
            new ModuleInfo(DocusignModule.REGISTRATION, "com.eischet.janitor.modules:docusign", "DocusignModule.REGISTRATION"),
            new ModuleInfo(MsGraphModule.REGISTRATION, "com.eischet.janitor.modules:msgraph", "MsGraphModule.REGISTRATION"),
            new ModuleInfo(McpModule.REGISTRATION, "com.eischet.janitor.modules:mcp", "McpModule.REGISTRATION")
    );

    /**
     * Registers all standard modules with an environment.
     * @param env the environment
     */
    public static void registerAllModules(final JanitorEnvironment env) {
        for (final ModuleInfo module : MODULES) {
            if (!module.name().equals(CollectionsModule.REGISTRATION.getQualifiedName())) { // the test runtime always has it
                env.addModule(module.registration());
            }
        }
    }

    /**
     * A member (method or property) of a type, as the reference shows it.
     * @param name the name used in scripts
     * @param signature the call form, e.g. {@code substring(from, to)}, or the name for properties
     * @param kind "method" or "property", with details
     * @param description the description, or an empty string
     */
    public record Member(String name, String signature, String kind, String description) {
    }

    private final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(ReferenceGenerator::registerAllModules);
    private final List<String> problems = new ArrayList<>();
    private final Set<String> commonNames;

    public ReferenceGenerator() {
        commonNames = runtime.getEnvironment().getBuiltinTypes().internals().getBaseDispatcher().streamAttributeNames().collect(Collectors.toSet());
    }

    /**
     * @return the problems found while generating, e.g. built-in members without a HELP text; empty if everything is fine
     */
    public List<String> getProblems() {
        return List.copyOf(problems);
    }

    /**
     * Generates all sections.
     * @return the content of each section, by id
     * @throws Exception if a script used to obtain sample values fails
     */
    public Map<String, String> generateSections() throws Exception {
        final Map<String, String> sections = new LinkedHashMap<>();
        sections.put("builtins/globals", globals());
        sections.put("builtins/" + COMMON, table(members(runtime.getEnvironment().getBuiltinTypes().internals().getBaseDispatcher(), Set.of(), true, COMMON)));
        builtinType(sections, "string", "\"\"");
        builtinType(sections, "int", "1");
        builtinType(sections, "float", "1.5");
        builtinType(sections, "list", "[]");
        builtinType(sections, "map", "return {};"); // a lone "{}" would be an empty block
        builtinType(sections, "set", "import collections; collections.set()");
        builtinType(sections, "binary", "\"\".toBinaryUtf8()");
        builtinType(sections, "regex", "re/x/");
        builtinType(sections, "matcher", "re/x/.matcher(\"\")");
        builtinType(sections, "duration", "@1d");
        builtinType(sections, "date", "@2024-01-01");
        builtinType(sections, "datetime", "@2024-01-01-00:00");
        sections.put("builtins/xml-element", table(members(new JElement().getDispatcher(), commonNames, true, "xml-element")));
        sections.put("modules/index", moduleIndex());
        for (final ModuleInfo module : MODULES) {
            sections.put("modules/" + module.name(), moduleMembers(module));
        }
        return sections;
    }

    private void builtinType(final Map<String, String> sections, final String className, final String sampleSource) throws Exception {
        final JanitorObject sample = evaluate(sampleSource);
        if (!className.equals(sample.janitorClassName()) && !"matcher".equals(className)) {
            problems.add("sample for " + className + " has class " + sample.janitorClassName());
        }
        sections.put("builtins/" + className, table(members(dispatcherOf(sample), commonNames, true, className)));
    }

    private String globals() {
        final List<String> names = new ArrayList<>(runtime.getEnvironment().getBuiltinScope().dir());
        Collections.sort(names);
        return names.stream().map(name -> "`" + name + "`").collect(Collectors.joining(", ")) + "\n";
    }

    /**
     * @return the names of the functions in the built-in scope, which the reference must describe
     */
    public List<String> builtinGlobals() {
        return runtime.getEnvironment().getBuiltinScope().dir().stream().sorted().toList();
    }

    private JanitorObject evaluate(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return runtime.compile("reference-generator", source).run();
    }

    private static @Nullable Dispatcher<?> dispatcherOf(final JanitorObject object) {
        if (object instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher();
        }
        if (object instanceof JanitorWrapper<?> wrapper) {
            return wrapper.getDispatcher();
        }
        return null;
    }

    private List<Member> members(final @Nullable Dispatcher<?> dispatcher, final Set<String> excluded, final boolean helpRequired, final String owner) {
        if (dispatcher == null) {
            return List.of();
        }
        final TreeSet<String> names = dispatcher.streamAttributeNames()
                .filter(name -> !name.startsWith("__")) // operator implementations like __in__ and __get__
                .filter(name -> !excluded.contains(name))
                .collect(Collectors.toCollection(TreeSet::new));
        final List<Member> members = new ArrayList<>();
        for (final String name : names) {
            final boolean method = dispatcher.getMetaData(name, Janitor.MetaData.TYPE_HINT) == Janitor.MetaData.TypeHint.METHOD;
            final String help = dispatcher.getMetaData(name, Janitor.MetaData.HELP);
            String kind = method ? "method" : "property";
            if (!method) {
                final Janitor.MetaData.TypeHint hint = dispatcher.getMetaData(name, Janitor.MetaData.TYPE_HINT);
                if (hint != null) {
                    kind += ", " + typeName(hint);
                }
                if (dispatcher instanceof GenericDispatchTable<?> table && table.getDirectAttributes().stream().anyMatch(a -> a.name().equals(name))) {
                    kind += table.isReadOnlyProperty(name) ? ", read-only" : ", writable";
                }
            }
            members.add(member(owner, name, method, kind, help, helpRequired));
        }
        return members;
    }

    private Member member(final String owner, final String name, final boolean method, final String kind, final @Nullable String help, final boolean helpRequired) {
        if (help == null || help.isBlank()) {
            if (helpRequired) {
                problems.add(owner + "." + name + ": no HELP text");
            }
            return new Member(name, method ? name + "(...)" : name, kind, "");
        }
        final String text = help.strip().replaceAll("\\s+", " ");
        if (method && text.startsWith(name + "(")) {
            final int end = text.indexOf("): ");
            if (end > 0) {
                return new Member(name, text.substring(0, end + 1), kind, text.substring(end + 3));
            }
        }
        if (!method && text.startsWith(name + ": ")) {
            return new Member(name, name, kind, text.substring(name.length() + 2));
        }
        if (helpRequired) {
            problems.add(owner + "." + name + ": the HELP text must start with \"" + name + (method ? "(arguments): " : ": ") + "\", but is: " + text);
        }
        return new Member(name, method ? name + "(...)" : name, kind, text);
    }

    private static String typeName(final Janitor.MetaData.TypeHint hint) {
        return switch (hint) {
            case NUMBER -> "number";
            case INTEGER -> "int";
            case FLOAT -> "float";
            case STRING -> "string";
            case BOOLEAN -> "bool";
            case METHOD -> "method";
            case DATE -> "date";
            case DATETIME -> "datetime";
            case LIST -> "list";
        };
    }

    private static String table(final List<Member> members) {
        if (members.isEmpty()) {
            return "No members.\n";
        }
        final StringBuilder out = new StringBuilder();
        out.append("| Member | Kind | Description |\n");
        out.append("| --- | --- | --- |\n");
        for (final Member member : members) {
            out.append("| `").append(member.signature().replace("|", "\\|")).append("` | ")
                    .append(member.kind()).append(" | ")
                    .append(member.description().replace("|", "\\|")).append(" |\n");
        }
        return out.toString();
    }

    private String moduleIndex() {
        final Set<String> sandboxed = registeredBy(env -> JanitorModulesCommon.registerCommonModules(env, false));
        final Set<String> nonSandboxed = registeredBy(env -> JanitorModulesCommon.registerCommonModules(env, true));
        final StringBuilder out = new StringBuilder();
        out.append("| Module | Maven artifact | Java registration | Registered by `JanitorModulesCommon.registerCommonModules(env, includingNonSandboxed)` |\n");
        out.append("| --- | --- | --- | --- |\n");
        for (final ModuleInfo module : MODULES) {
            final String common;
            if (sandboxed.contains(module.name())) {
                common = "always";
            } else if (nonSandboxed.contains(module.name())) {
                common = "only with `includingNonSandboxed = true`";
            } else {
                common = "no; register it explicitly";
            }
            out.append("| [`").append(module.name()).append("`](modules.md#module-").append(module.name()).append(") | `")
                    .append(module.artifact()).append("` | `").append(module.javaRegistration()).append("` | ").append(common).append(" |\n");
        }
        return out.toString();
    }

    private static Set<String> registeredBy(final Consumer<JanitorEnvironment> registration) {
        final Set<String> names = new HashSet<>();
        final JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
            }

            @Override
            public @NotNull JanitorDefaultEnvironment addModule(final @NotNull JanitorModuleRegistration moduleRegistration) {
                names.add(moduleRegistration.getQualifiedName());
                return this;
            }
        };
        registration.accept(env);
        return names;
    }

    private JanitorObject module(final ModuleInfo module) throws Exception {
        return evaluate("import " + module.name() + "; return " + module.name() + ";");
    }

    private String moduleMembers(final ModuleInfo module) throws Exception {
        final Dispatcher<?> dispatcher = dispatcherOf(module(module));
        if (dispatcher == null) {
            return "This module does not describe its members in a dispatch table, so they cannot be listed here; see the description above.\n";
        }
        return table(members(dispatcher, Set.of(), false, module.name()));
    }

    /**
     * Generates {@link #MODULE_TYPES_FILE}: the members of the object types that the standard modules create.
     * @return the whole file
     * @throws Exception if the module classes cannot be inspected
     */
    public String generateModuleTypesFile() throws Exception {
        final StringBuilder out = new StringBuilder();
        out.append(GENERATED_FILE_MARKER).append("\n\n");
        out.append("# Object types of the standard modules\n\n");
        out.append("This file lists the members of the objects that the [standard modules](modules.md) create, as their dispatch tables\n");
        out.append("define them. It is generated from the code, so it is complete but terse: a description is shown where the code has\n");
        out.append("one. Each module is only available if the host registers it. Members that every object has, like `apply(map)`, are\n");
        out.append("listed, too.\n");
        for (final ModuleInfo module : MODULES) {
            final Class<?> moduleClass = module(module).getClass();
            final List<Class<?>> types = typesWithDispatchTables(moduleClass);
            out.append("\n<a id=\"types-").append(module.name()).append("\"></a>\n\n## ").append(module.name()).append("\n\n");
            if (types.isEmpty()) {
                out.append("This module defines no object types of its own.\n");
                continue;
            }
            for (int i = 0; i < types.size(); i++) {
                final Class<?> type = types.get(i);
                out.append(i == 0 ? "" : "\n")
                        .append("<a id=\"types-").append(module.name()).append("-").append(typeTitle(type).toLowerCase(Locale.ROOT).replace('.', '-')).append("\"></a>\n\n")
                        .append("### ").append(typeTitle(type)).append("\n\n");
                final Dispatcher<?> instanceDispatcher = instanceDispatcher(type);
                if (instanceDispatcher != null) {
                    out.append(table(members(instanceDispatcher, Set.of(), false, typeTitle(type))));
                } else {
                    for (final Dispatcher<?> dispatcher : staticDispatchers(type)) {
                        out.append(table(members(dispatcher, Set.of(), false, typeTitle(type))));
                    }
                }
            }
        }
        return out.toString();
    }

    private static String typeTitle(final Class<?> type) {
        // nested classes keep their outer class, e.g. HttpException.Wrapper
        return type.getName().substring(type.getPackageName().length() + 1).replace('$', '.');
    }

    private static List<Class<?>> typesWithDispatchTables(final Class<?> moduleClass) throws IOException, URISyntaxException, ClassNotFoundException {
        final String packageName = moduleClass.getPackageName();
        final Path location = Path.of(moduleClass.getProtectionDomain().getCodeSource().getLocation().toURI());
        final List<String> classNames = new ArrayList<>();
        final String packagePath = packageName.replace('.', '/') + "/";
        if (Files.isDirectory(location)) {
            final Path dir = location.resolve(packagePath);
            try (Stream<Path> files = Files.list(dir)) {
                files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".class")).forEach(n -> classNames.add(packageName + "." + n.substring(0, n.length() - 6)));
            }
        } else {
            try (JarFile jar = new JarFile(location.toFile())) {
                jar.stream().map(java.util.zip.ZipEntry::getName)
                        .filter(n -> n.startsWith(packagePath) && n.endsWith(".class") && n.indexOf('/', packagePath.length()) < 0)
                        .forEach(n -> classNames.add(n.substring(0, n.length() - 6).replace('/', '.')));
            }
        }
        Collections.sort(classNames);
        final List<Class<?>> result = new ArrayList<>();
        for (final String className : classNames) {
            final Class<?> type = Class.forName(className, true, moduleClass.getClassLoader());
            if (type != moduleClass && !staticDispatchers(type).isEmpty()) {
                result.add(type);
            }
        }
        result.sort(Comparator.comparing(ReferenceGenerator::typeTitle));
        return result;
    }

    /**
     * Some types combine their dispatch table with that of a base type only per instance (Dispatcher.inherit), so an
     * instance shows all members. Types without a public no-argument constructor fall back to their static tables.
     */
    private static @Nullable Dispatcher<?> instanceDispatcher(final Class<?> type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            return null;
        }
        try {
            final Object instance = type.getConstructor().newInstance();
            return instance instanceof JanitorObject object ? dispatcherOf(object) : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static List<Dispatcher<?>> staticDispatchers(final Class<?> type) {
        final List<Dispatcher<?>> result = new ArrayList<>();
        for (final Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Dispatcher.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    final Object value = field.get(null);
                    if (value instanceof Dispatcher<?> dispatcher) {
                        result.add(dispatcher);
                    }
                } catch (ReflectiveOperationException | RuntimeException e) {
                    throw new IllegalStateException("cannot read " + type.getName() + "." + field.getName(), e);
                }
            }
        }
        return result;
    }

    /**
     * Replaces the generated sections of a markdown file.
     * @param markdown the file's content
     * @param sections the generated sections, by id
     * @param used receives the ids of the sections that the file contains
     * @return the new content
     */
    public static String applySections(final String markdown, final Map<String, String> sections, final Set<String> used) {
        final Matcher matcher = SECTION.matcher(markdown);
        final StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            final String id = matcher.group(2);
            final String content = sections.get(id);
            if (content == null) {
                throw new IllegalArgumentException("unknown generated section: " + id);
            }
            used.add(id);
            matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group(1) + "\n" + content + matcher.group(4)));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
