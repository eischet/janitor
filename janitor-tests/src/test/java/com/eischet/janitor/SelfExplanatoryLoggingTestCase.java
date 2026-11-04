// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.logging.JanitorWrappingLogger;
import com.eischet.janitor.logging.SelfExplanatory;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import static org.junit.jupiter.api.Assertions.*;

class SelfExplanatoryLoggingTestCase {

    private final RecordingLogger recorder = new RecordingLogger();
    private final JanitorWrappingLogger log = new JanitorWrappingLogger(recorder, null);

    private static final class RecordingLogger extends LegacyAbstractLogger {
        Level level;
        String message;
        Throwable throwable;

        @Override
        protected String getFullyQualifiedCallerName() {
            return null;
        }

        @Override
        protected void handleNormalizedLoggingCall(final Level level, final Marker marker, final String messagePattern, final Object[] arguments, final Throwable throwable) {
            this.level = level;
            this.message = MessageFormatter.basicArrayFormat(messagePattern, arguments);
            this.throwable = throwable;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }
    }

    private static final class Wrapper extends Exception implements SelfExplanatory {
        Wrapper(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private static JanitorRuntimeException scriptError() {
        return assertThrows(JanitorNameException.class, () -> OutputCatchingTestRuntime.fresh().compile("broken", "print(nope);").run());
    }

    private static JanitorRuntimeException failingNativeCall(final JNativeMethod method) {
        return assertThrows(JanitorRuntimeException.class, () -> OutputCatchingTestRuntime.fresh().compile("broken", "fail();").run(g -> g.bind("fail", method)));
    }

    @Test
    void aScriptErrorIsLoggedWithoutStackTrace() {
        final JanitorRuntimeException e = scriptError();
        log.error("error running {}", "tool", e);
        assertNull(recorder.throwable);
        assertEquals(Level.ERROR, recorder.level);
        assertEquals("error running tool:\n" + e.getMessage().strip(), recorder.message);
    }

    @Test
    void everyWayToPassTheExceptionIsCondensed() {
        final JanitorRuntimeException e = scriptError();
        final String expected = "failed:\n" + e.getMessage().strip();

        log.warn("failed", e);
        assertEquals(expected, recorder.message);
        assertNull(recorder.throwable);
        assertEquals(Level.WARN, recorder.level);

        log.info("failed", (Object) e);
        assertEquals(expected, recorder.message);
        assertNull(recorder.throwable);
        assertEquals(Level.INFO, recorder.level);

        log.debug("{} {}", "really", "failed", e);
        assertEquals("really failed:\n" + e.getMessage().strip(), recorder.message);
        assertNull(recorder.throwable);
        assertEquals(Level.DEBUG, recorder.level);

        log.error((Marker) null, "failed", e);
        assertEquals(expected, recorder.message);
        assertNull(recorder.throwable);
    }

    @Test
    void aCompilerErrorIsLoggedWithoutStackTrace() {
        final JanitorCompilerException e = assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("broken", "if ("));
        log.error("cannot compile", e);
        assertNull(recorder.throwable);
        assertTrue(recorder.message.startsWith("cannot compile:\n"), recorder.message);
    }

    @Test
    void bracesInTheMessageAreNotTakenForPlaceholders() {
        log.error("failed", new Wrapper("a map: {} and {}", null));
        assertEquals("failed:\na map: {} and {}", recorder.message);
    }

    @Test
    void aWrapperAddsItsOwnMessageAndNothingIsRepeated() {
        final JanitorRuntimeException e = scriptError();
        log.error("tool", new Wrapper("error running script", e));
        assertNull(recorder.throwable);
        assertEquals("tool:\nerror running script\n" + e.getMessage().strip(), recorder.message);

        log.error(e.getMessage(), e);
        assertEquals(e.getMessage(), recorder.message);
    }

    @Test
    void anExceptionWithoutMessageIsNamed() {
        log.error("failed", new Wrapper(null, null));
        assertEquals("failed:\n" + Wrapper.class.getName(), recorder.message);
    }

    @Test
    void aJavaCauseKeepsTheStackTrace() {
        final JanitorRuntimeException e = failingNativeCall(new JNativeMethod((process, args) -> {
            throw new JanitorNativeException(process, "the call failed", new IllegalStateException("java fault"));
        }));
        log.error("failed", e);
        assertSame(e, recorder.throwable);
        assertEquals("failed", recorder.message);
    }

    @Test
    void aJavaExceptionAroundAScriptErrorKeepsTheStackTrace() {
        final RuntimeException e = new RuntimeException("wrapped", scriptError());
        log.error("failed", e);
        assertSame(e, recorder.throwable);
    }

    @Test
    void anInternalErrorOfTheInterpreterKeepsTheStackTrace() {
        final JanitorRuntimeException e = failingNativeCall(new JNativeMethod((process, args) -> {
            throw new JanitorInternalException(process, "interpreter fault", null);
        }));
        assertInstanceOf(JanitorInternalException.class, e);
        log.error("failed", e);
        assertSame(e, recorder.throwable);
    }

    @Test
    void aSuppressedExceptionKeepsTheStackTrace() {
        final JanitorRuntimeException e = scriptError();
        e.addSuppressed(new IllegalStateException("cannot close"));
        log.error("failed", e);
        assertSame(e, recorder.throwable);
    }

    @Test
    void messagesWithoutExceptionStayFormats() {
        log.error("value {}", 1);
        assertEquals("value 1", recorder.message);
        assertNull(recorder.throwable);
    }
}
