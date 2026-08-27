package com.eischet.janitor.mods.os;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.modules.os.OperatingSystemModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for the os.exec() deadlock: neither the child process' stdout nor stderr was ever
 * read, so a child that writes more than the OS pipe buffer (~64 KB) to either stream blocks on the
 * write, and Process.waitFor() then blocks forever. exec() now drains both streams in the background
 * while waiting -- see OperatingSystemModule.waitFor()/drainInBackground(). The @Timeout on each test
 * is what actually catches a regression: without the fix, these tests would hang instead of failing.
 * <p>
 * The child process here is a small single-file-source-launched Java program (rather than a shell
 * command) so this stays portable across operating systems and avoids any shell-quoting concerns.
 */
public class OperatingSystemModuleExecTestCase extends JanitorTest {

    private static final int LINES = 20_000;
    private static final String PAYLOAD = "a".repeat(100);

    private static String javaExecutable() {
        final boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + (windows ? "java.exe" : "java");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void execDoesNotDeadlockOnLargeStdout(@TempDir final Path tempDir) throws JanitorCompilerException, JanitorRuntimeException, Exception {
        final Path source = tempDir.resolve("BigStdout.java");
        Files.writeString(source, """
                public class BigStdout {
                    public static void main(String[] args) {
                        for (int i = 0; i < %d; i++) {
                            System.out.println("%s");
                        }
                    }
                }
                """.formatted(LINES, PAYLOAD));

        assertEquals(0, execJava(source));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void execDoesNotDeadlockOnLargeStderr(@TempDir final Path tempDir) throws JanitorCompilerException, JanitorRuntimeException, Exception {
        final Path source = tempDir.resolve("BigStderr.java");
        Files.writeString(source, """
                public class BigStderr {
                    public static void main(String[] args) {
                        for (int i = 0; i < %d; i++) {
                            System.err.println("%s");
                        }
                    }
                }
                """.formatted(LINES, PAYLOAD));

        assertEquals(0, execJava(source));
    }

    private int execJava(final Path source) throws JanitorCompilerException, JanitorRuntimeException {
        @Language("Janitor") final String script = """
                import os;
                return os.exec([javaBin, javaFile]);
                """;
        final JanitorObject result = evaluate(script,
                env -> env.addModule(OperatingSystemModule.REGISTRATION),
                globals -> {
                    globals.bind("javaBin", javaExecutable());
                    globals.bind("javaFile", source.toString());
                });
        return ((JInt) result).getAsInt();
    }

}
