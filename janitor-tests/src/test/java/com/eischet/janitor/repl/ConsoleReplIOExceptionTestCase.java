package com.eischet.janitor.repl;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.modules.mustang.MustangModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * {@link ConsoleReplIO#exception(Exception)} always prints "Error: " + the exception's message,
 * which for a {@link JanitorRuntimeException} is already a fully formatted script-level traceback
 * (module/line/source line, down to the error). The raw Java stack trace underneath used to be
 * printed unconditionally, which was pure interpreter-internal noise for e.g. a NameException.
 * It should only appear for {@link JanitorNativeException}, where the cause is a real Java
 * exception from host code and the stack trace is the only way to debug it.
 */
class ConsoleReplIOExceptionTestCase extends JanitorTest {

    private String captureStderr(final Exception e) {
        final PrintStream originalErr = System.err;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
        try {
            new ConsoleReplIO().exception(e);
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString();
    }

    @Test
    void scriptLevelExceptionsDoNotPrintAJavaStackTrace() throws JanitorCompilerException {
        try {
            evaluate("someUndefinedVariable;");
            fail("expected a JanitorNameException");
        } catch (JanitorRuntimeException e) {
            assertFalse(e instanceof JanitorNativeException, "test setup: expected a non-native runtime exception");
            assertEquals("", captureStderr(e));
        }
    }

    @Test
    void nativeExceptionsStillPrintTheJavaStackTrace() throws JanitorCompilerException {
        final @Language("Janitor") String script = """
            import mustang;
            mustang.Exporter().load("/this/path/does/not/exist.pdf");
            """;
        try {
            evaluate(script, env -> env.addModule(MustangModule.REGISTRATION), null);
            fail("expected a JanitorNativeException");
        } catch (JanitorRuntimeException e) {
            assertInstanceOf(JanitorNativeException.class, e);
            final String stderr = captureStderr(e);
            assertTrue(stderr.contains("\tat "), "expected a Java stack trace in stderr, but got: " + stderr);
        }
    }

}
