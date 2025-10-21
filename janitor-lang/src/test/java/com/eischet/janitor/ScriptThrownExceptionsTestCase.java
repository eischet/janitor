package com.eischet.janitor;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptThrownException;
import com.eischet.janitor.api.types.functions.JCallable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScriptThrownExceptionsTestCase extends JanitorTest {

    /**
     * Throwing strings as exceptions is supported.
     */
    @Test
    void throwStringAsException() {
        final JanitorScriptThrownException thrown = assertThrows(JanitorScriptThrownException.class, () -> evaluate("throw 'foo'"));
        assertEquals("""
                Traceback (most recent call last):
                  Module 'unnamed'
                ScriptThrownException: foo""", thrown.getMessage());
    }

    /**
     * Throwing arbitrary objects as exceptions is supported, even though not really encouraged.
     *
     */
    @Test
    void throwNumberAsException() {
        final JanitorScriptThrownException thrown = assertThrows(JanitorScriptThrownException.class, () -> evaluate("print('foo'); throw 17; print('bar')"));
        assertEquals("""
                Traceback (most recent call last):
                  Module 'unnamed', line 1, column 0
                    print('foo'); throw 17; print('bar')
                ScriptThrownException: 17""", thrown.getMessage());
    }

    protected static class FumbleException extends JanitorRuntimeException {
        public FumbleException(final @NotNull JanitorScriptProcess process) {
            super(process, FumbleException.class);
        }
    }

    @Test
    void throwSoCalledRealExceptions() {
        final JCallable.Wrapper wrapper = new JCallable.Wrapper((process, arguments) -> new FumbleException(process), "Fumble");
        final FumbleException fumble = assertThrows(FumbleException.class, () -> evaluate(
                "throw Fumble()",
                g -> g.bind("Fumble", wrapper)));
        assertEquals("""
                Traceback (most recent call last):
                  Module 'unnamed', line 1, column 6
                    throw Fumble()
                FumbleException""", fumble.getMessage());
    }

}
