// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.standalone.repl;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileModuleResolverTestCase {

    private static void write(final Path file, final String text) throws Exception {
        Files.createDirectories(file.getParent());
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    private static VariableScope runFile(final Path script, final List<Path> searchPath) {
        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment(searchPath);
        Janitor.setUserProvider(() -> env);
        final VariableScope scope = JanitorStandaloneRepl.runScriptFile(env, script, false, true);
        assertNotNull(scope, "the script must run without errors");
        return scope;
    }

    private static String get(final VariableScope scope, final String name) {
        return scope.getVariable(name).janitorToString();
    }

    private static JanitorRuntimeException runFails(final Path script, final List<Path> searchPath) throws Exception {
        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment(searchPath);
        Janitor.setUserProvider(() -> env);
        final JanitorRuntime runtime = JanitorStandaloneRepl.createStdoutRuntime(env);
        final String source = Files.readString(script, StandardCharsets.UTF_8);
        return assertThrows(JanitorRuntimeException.class,
                () -> runtime.compile(script.getFileName().toString(), source, script.toString()).run(runtime));
    }

    @Test
    void dotSlashLoadsAScriptNextToTheImportingScript(@TempDir Path dir) throws Exception {
        write(dir.resolve("greet.janitor"), "function hello(n) { return \"hello \" + n; }");
        write(dir.resolve("main.janitor"), "import \"./greet\" as g;\nresult = g.hello(\"world\");");
        assertEquals("hello world", get(runFile(dir.resolve("main.janitor"), List.of()), "result"));
    }

    @Test
    void relativeImportsInAnImportedModuleAreRelativeToThatModule(@TempDir Path dir) throws Exception {
        write(dir.resolve("lib/a.janitor"), "import \"./b\" as b;\nvalue = \"a+\" + b.value;");
        write(dir.resolve("lib/b.janitor"), "value = \"b\";");
        write(dir.resolve("app/main.janitor"), "import \"../lib/a\" as a;\nresult = a.value;");
        assertEquals("a+b", get(runFile(dir.resolve("app/main.janitor"), List.of()), "result"));
    }

    @Test
    void namesWithoutDotAreLookedUpInTheSearchPathInOrder(@TempDir Path dir) throws Exception {
        final Path first = dir.resolve("first");
        final Path second = dir.resolve("second");
        write(second.resolve("util/strings.janitor"), "value = \"second\";");
        write(second.resolve("only.janitor"), "value = \"only\";");
        write(first.resolve("util/strings.janitor"), "value = \"first\";");
        write(dir.resolve("main.janitor"), "import \"util/strings\" as s;\nimport \"only\" as o;\nresult = s.value + \",\" + o.value;");
        assertEquals("first,only", get(runFile(dir.resolve("main.janitor"), List.of(first, second)), "result"));
    }

    @Test
    void theScriptDirectoryIsNotPartOfTheSearchPath(@TempDir Path dir) throws Exception {
        write(dir.resolve("local.janitor"), "value = 1;");
        write(dir.resolve("main.janitor"), "import \"local\" as l;");
        final JanitorRuntimeException e = runFails(dir.resolve("main.janitor"), List.of(dir.resolve("modules")));
        assertTrue(e.getMessage().contains("Module not found: 'local'"), e.getMessage());
    }

    @Test
    void anImportedModuleIsSharedUntilItsFileChanges(@TempDir Path dir) throws Exception {
        final Path counter = dir.resolve("counter.janitor");
        write(counter, "count = 0;\nfunction next() { count = count + 1; return count; }");
        write(dir.resolve("main.janitor"), """
                import "./counter" as c1;
                import "./counter" as c2;
                c1.next();
                result = c2.next();""");
        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment(List.of());
        Janitor.setUserProvider(() -> env);
        final VariableScope scope = JanitorStandaloneRepl.runScriptFile(env, dir.resolve("main.janitor"), false, true);
        assertNotNull(scope);
        assertEquals("2", get(scope, "result"), "both imports must see the same module");

        write(counter, "count = 10;\nfunction next() { count = count + 1; return count; }");
        Files.setLastModifiedTime(counter, FileTime.fromMillis(Files.getLastModifiedTime(counter).toMillis() + 5000));
        final VariableScope again = JanitorStandaloneRepl.runScriptFile(env, dir.resolve("main.janitor"), false, true);
        assertNotNull(again);
        assertEquals("12", get(again, "result"), "a changed file must be loaded again");
    }

    @Test
    void aCycleOfImportsIsAnError(@TempDir Path dir) throws Exception {
        write(dir.resolve("a.janitor"), "import \"./b\" as b;");
        write(dir.resolve("b.janitor"), "import \"./a\" as a;");
        write(dir.resolve("main.janitor"), "import \"./a\" as a;");
        final JanitorRuntimeException e = runFails(dir.resolve("main.janitor"), List.of());
        assertTrue(e.getMessage().contains("imports itself"), e.getMessage());
    }

    @Test
    void aMissingRelativeModuleNamesTheFileItLookedFor(@TempDir Path dir) throws Exception {
        write(dir.resolve("main.janitor"), "import \"./nope\" as n;");
        final JanitorRuntimeException e = runFails(dir.resolve("main.janitor"), List.of());
        assertTrue(e.getMessage().contains("nope.janitor"), e.getMessage());
    }

    @Test
    void theSearchPathComesFromTheOptionsThenTheVariableOrTheDefault(@TempDir Path dir) {
        final Path home = dir.resolve("home");
        final Path extra = dir.resolve("extra");
        assertEquals(List.of(extra, home.resolve(".janitor").resolve("modules")),
                FileModuleResolver.defaultSearchPath(List.of(extra), null, home));
        final String variable = dir.resolve("x") + File.pathSeparator + dir.resolve("y");
        assertEquals(List.of(extra, dir.resolve("x"), dir.resolve("y"), home.resolve(".janitor").resolve("modules")),
                FileModuleResolver.defaultSearchPath(List.of(extra), variable, home),
                "the user's modules are searched last, also when the variable is set");
    }
}
