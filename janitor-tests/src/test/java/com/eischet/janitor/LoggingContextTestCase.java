// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor;

import com.eischet.janitor.logging.ILoggingContext;
import com.eischet.janitor.logging.JanitorWrappingLogger;
import com.eischet.janitor.logging.LoggingContext;
import com.eischet.janitor.logging.LoggingScope;
import com.eischet.janitor.logging.formatter.ColoredConsoleFormatter;
import com.eischet.janitor.logging.formatter.PlainConsoleFormatter;
import com.eischet.janitor.logging.jul.JulLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A log line says who and what it was written for, and by which thread: the logging context of the thread (app, user, entity)
 * plus the tag of the logger, and the name of the thread even if it is gone by the time the line is written.
 */
class LoggingContextTestCase {

    @BeforeEach
    @AfterEach
    void clear() {
        LoggingContext.clear();
    }

    /**
     * Captures the context of the thread at the time of each call, like the JUL adapter does.
     */
    private static final class ContextCapturingLogger extends LegacyAbstractLogger {
        ILoggingContext context;

        @Override
        protected String getFullyQualifiedCallerName() {
            return null;
        }

        @Override
        protected void handleNormalizedLoggingCall(final Level level, final Marker marker, final String messagePattern, final Object[] arguments, final Throwable throwable) {
            context = LoggingContext.getSnapshot(false);
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

    private static String plain(final ILoggingContext context) {
        final JulLogRecord record = new JulLogRecord(java.util.logging.Level.INFO, "message");
        record.setLoggingContext(context);
        return new PlainConsoleFormatter().format(record);
    }

    private static String colored(final ILoggingContext context) {
        final JulLogRecord record = new JulLogRecord(java.util.logging.Level.INFO, "message");
        record.setLoggingContext(context);
        return new ColoredConsoleFormatter().format(record).replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * A logger with a tag (e.g. a debug flag) shows it next to what the thread works on, instead of hiding it.
     */
    @Test
    void loggerTagDoesNotHideTheEntity() {
        final ContextCapturingLogger capture = new ContextCapturingLogger();
        final JanitorWrappingLogger log = new JanitorWrappingLogger(capture, "DEBUG_ORDERS");
        LoggingContext.setApp("shop");
        LoggingContext.setUser("alice");
        LoggingContext.setEntity("ORDER-7");
        log.warn("cannot ship");
        assertTrue(plain(capture.context).contains(" <shop, alice, ORDER-7 | DEBUG_ORDERS> message"), plain(capture.context));
        assertEquals("ORDER-7", LoggingContext.getSnapshot(false).getEntity());
        assertNull(LoggingContext.getSnapshot(false).getTag());
    }

    /**
     * The colored console shows a context even when no app is set.
     */
    @Test
    void coloredConsoleShowsContextWithoutApp() {
        final ContextCapturingLogger capture = new ContextCapturingLogger();
        new JanitorWrappingLogger(capture, "DEBUG_ORDERS").info("starting");
        assertTrue(colored(capture.context).contains(" <DEBUG_ORDERS>"), colored(capture.context));
        LoggingContext.setUser("alice");
        new JanitorWrappingLogger(capture, null).info("starting");
        assertTrue(colored(capture.context).contains(" <alice>"), colored(capture.context));
    }

    /**
     * A record names the thread that wrote it, also a virtual thread that has ended before the record is formatted.
     */
    @Test
    void recordNamesItsThread() throws Exception {
        final AtomicReference<JulLogRecord> record = new AtomicReference<>();
        final Thread thread = Thread.ofVirtual().name("worker-1").start(() ->
            record.set(new JulLogRecord(java.util.logging.Level.INFO, "message")));
        thread.join();
        final String line = new PlainConsoleFormatter().format(record.get());
        assertTrue(line.contains(" worker-1 ") || line.contains(" worker-1\t") || line.matches("(?s).* worker-1( .*|$)"), line);
    }

    /**
     * A scope sets the context for a stretch of work and gives back the enclosing one afterwards, even if the work cleared it.
     */
    @Test
    void scopeRestoresTheEnclosingContext() {
        try (LoggingScope request = LoggingScope.open("shop", null, null)) {
            request.setUser("alice");
            try (LoggingScope ignored = LoggingScope.openKeepingApp("jobs", null, "ORDER-7")) {
                assertEquals("shop", LoggingContext.getSnapshot(false).getApp());
                assertEquals("ORDER-7", LoggingContext.getSnapshot(false).getEntity());
                LoggingContext.clear();
            }
            assertEquals("alice", LoggingContext.getSnapshot(false).getUser());
            assertNull(LoggingContext.getSnapshot(false).getEntity());
            try (LoggingScope ignored = LoggingScope.replacing(null, null, "ORDER-8")) {
                assertNull(LoggingContext.getSnapshot(false).getApp());
            }
            assertEquals("shop", LoggingContext.getSnapshot(false).getApp());
        }
        assertNull(LoggingContext.getSnapshot(false).getApp());
        assertNull(LoggingContext.getSnapshot(false).getUser());
    }
}
