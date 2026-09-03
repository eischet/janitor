package com.eischet.janitor.standalone.repl;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.janitor.JanitorInternalsModule;
import com.eischet.janitor.repl.ConsoleReplIO;
import com.eischet.janitor.repl.JanitorRepl;
import com.eischet.janitor.version.Revision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end smoke test for "janitor script.janitor args...": exercises the full main() path (only the
 * success case -- error paths call System.exit(), which would kill this test's JVM, so those are covered
 * by ReplCommandLineTestCase at the parsing level instead).
 */
class JanitorStandaloneReplTestCase {

    @Test
    void runningAScriptFileExecutesItAndExposesTrailingArgsViaJanitorArgs(@TempDir Path tempDir) throws Exception {
        final Path script = tempDir.resolve("hello.janitor");
        Files.writeString(script, """
                import janitor;
                print("host=" + janitor.host);
                print("args=" + janitor.args);
                """, StandardCharsets.UTF_8);

        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            // "-v" here comes AFTER the script file, so it must land in janitor.args, not flip on verbose mode.
            JanitorStandaloneRepl.main(new String[]{script.toString(), "-v", "first", "second"});
        } finally {
            System.setOut(originalOut);
        }

        final String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("host=standalone"), "expected janitor.host to be 'standalone', got: " + output);
        assertTrue(output.contains("[-v, first, second]"), "expected janitor.args to carry everything after the script file, got: " + output);
        assertEquals(JanitorInternalsModule.HOST_STANDALONE, JanitorInternalsModule.host);
    }

    /**
     * Exercises the -i mechanism directly at the runScriptFile() level (package-private for exactly this
     * purpose), rather than through the full main() -> interactive-REPL path: driving the actual jline
     * console would block on terminal input, which isn't available in a test JVM. This still covers the
     * part that's new here -- that a successfully run script hands back a Scope with its top-level
     * variables in it, ready to be merged into a REPL (JanitorRepl.replEatScope() itself, and the merge
     * call site in seedRepl(), are both single-line uses of already-established framework mechanisms).
     */
    @Test
    void aLeadingShebangLineInAScriptFileIsIgnored(@TempDir Path tempDir) throws Exception {
        final Path script = tempDir.resolve("hashbang.janitor");
        Files.writeString(script, """
                #!/usr/bin/env janitor
                print("it ran");
                """, StandardCharsets.UTF_8);

        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            JanitorStandaloneRepl.main(new String[]{script.toString()});
        } finally {
            System.setOut(originalOut);
        }
        assertTrue(captured.toString(StandardCharsets.UTF_8).contains("it ran"));
    }

    @Test
    void interactiveModeReturnsTheScriptsTopLevelScopeOnSuccess(@TempDir Path tempDir) throws Exception {
        final Path script = tempDir.resolve("defines.janitor");
        Files.writeString(script, "x = 42;\ny = \"hello\";\n", StandardCharsets.UTF_8);

        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment();
        Janitor.setUserProvider(() -> env);

        final Scope scope = JanitorStandaloneRepl.runScriptFile(env, script, false, true);

        assertNotNull(scope, "with -i, a successfully run script must hand back its scope");
        final JanitorObject x = scope.lookupLocally(null, "x");
        final JanitorObject y = scope.lookupLocally(null, "y");
        assertNotNull(x, "the script's top-level variable 'x' must be present in the returned scope");
        assertEquals("42", x.janitorToString());
        assertNotNull(y, "the script's top-level variable 'y' must be present in the returned scope");
        assertEquals("hello", y.janitorToString());
    }

    @Test
    void interactiveModeReturnsNullScopeOnFailureWithoutExitingTheProcess(@TempDir Path tempDir) throws Exception {
        final Path script = tempDir.resolve("broken.janitor");
        Files.writeString(script, "this is not { valid janitor (((", StandardCharsets.UTF_8);

        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment();
        Janitor.setUserProvider(() -> env);

        // must return (not System.exit()) so the caller can still start an interactive shell, matching
        // "python -i" dropping you into the shell even after the script itself failed.
        final Scope scope = JanitorStandaloneRepl.runScriptFile(env, script, false, true);
        assertNull(scope);
    }

    @Test
    void versionFlagPrintsTheRevisionAndReturnsWithoutTouchingAnythingElse() {
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            JanitorStandaloneRepl.main(new String[]{"-V"});
        } finally {
            System.setOut(originalOut);
        }
        assertEquals(Revision.REVISION, captured.toString(StandardCharsets.UTF_8).strip());
    }

    /**
     * seedRepl() (package-private for this purpose) is what -i and -q actually rely on to suppress the
     * REPL's banner -- driving this directly rather than through main() avoids blocking on the
     * interactive loop, which needs real terminal input that isn't available in a test JVM.
     */
    @Test
    void seedReplSuppressesTheBannerWhenAskedAndLeavesItAloneOtherwise() {
        final JanitorEnvironment env = JanitorStandaloneRepl.createEnvironment();
        Janitor.setUserProvider(() -> env);
        final JanitorRuntime runtime = JanitorStandaloneRepl.createStdoutRuntime(env);

        final JanitorRepl quiet = new JanitorRepl(runtime, new ConsoleReplIO());
        JanitorStandaloneRepl.seedRepl(quiet, null, true);
        assertNull(quiet.getLogo(), "-q/-i must suppress the welcome banner");

        final JanitorRepl normal = new JanitorRepl(runtime, new ConsoleReplIO());
        JanitorStandaloneRepl.seedRepl(normal, null, false);
        assertNotNull(normal.getLogo(), "a plain interactive shell without -q must still show its banner");
    }

}
