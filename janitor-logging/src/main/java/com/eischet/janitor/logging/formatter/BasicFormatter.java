// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging.formatter;

import com.eischet.janitor.logging.ILoggingContext;
import com.eischet.janitor.logging.jul.JulLogRecord;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple formatter that looks like the Janitor authors like their logging.
 *
 * <p>The default JUL format uses a 2-line layout, which is harder to scan in log files than a compact single-line format.
 * This formatter produces one line per record.</p>
 *
 */
public abstract class BasicFormatter extends Formatter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ThreadMXBean threadManagementBean;

    private static final List<ExceptionFormattingHandler> exceptionFormattingHandlers = new ArrayList<>();

    /**
     * Registers a handler that can format specific kinds of exceptions in log output.
     * @param handler the handler
     */
    public static void registerExceptionFormattingHandler(final ExceptionFormattingHandler handler) {
        if (handler != null) {
            exceptionFormattingHandlers.add(handler);
        }
    }

    public BasicFormatter() {
        threadManagementBean = ManagementFactory.getThreadMXBean();
    }

    private static String calcDate(long millisecs) {
        if (millisecs <= 0) {
            return "No Date?";
        }
        final Date resultdate = new Date(millisecs);
        return DATE_FORMAT.format(resultdate);
    }

    private String formatThreadName(final String threadName) {
        if (threadName == null || threadName.isEmpty()) {
            return threadName;
        }
        if (threadName.startsWith("cron4j::")) {
            return "CRON";
        }
        return threadName;
    }

    @Override
    public String format(final LogRecord record) {
        final ToolLogCategory cat = ToolLogCategory.forLevel(record.getLevel());
        String thread = "";
        if (threadManagementBean != null) {
            final ThreadInfo ti = threadManagementBean.getThreadInfo(record.getLongThreadID());
            if (ti != null) {
                thread = formatThreadName(ti.getThreadName());
            }
        }
        if (thread == null || thread.isEmpty()) {
            thread = "#" + record.getLongThreadID();
        }

        final String ts = calcDate(record.getMillis());
        String message = record.getMessage();

        Object[] params = record.getParameters();
        if (params != null) {
            message += " " + Arrays.toString(params);
        }
        final Throwable thrown = record.getThrown();
        boolean omitStackTrace = false;

        if (thrown != null) {
            for (final ExceptionFormattingHandler handler : exceptionFormattingHandlers) {
                @Nullable final String result = handler.formatLogException(thrown);
                if (result != null) {
                    message += " " + result;
                    omitStackTrace = true; // we have a custom formatting, so no need for the stack trace
                    break; // only one handler is allowed to format the exception
                }
            }
            if (!omitStackTrace) {
                message += " STACK TRACE: <" + formatStackTrace(thrown) + ">";
            }
        }


        List<Object> arguments = null;
        List<KeyValuePair> keyValuePairs = null;
        List<Marker> markers = null;
        Map<String, String> contextMap = null;
        ILoggingContext loggingContext = null;
        if (record instanceof JulLogRecord extendedRecord) {
            keyValuePairs = extendedRecord.getKeyValuePairs();
            markers = extendedRecord.getMarkers();
            arguments = extendedRecord.getArguments();
            contextMap = extendedRecord.getContextMap();
            loggingContext = extendedRecord.getLoggingContext();
        }
        return formatLogRecord(cat, thread, ts, record.getLoggerName(), message, arguments, keyValuePairs, markers, contextMap, loggingContext);
    }

    /**
     * Formats an exception like {@link Throwable#printStackTrace()}, but prints each message only once.
     *
     * <p>Wrapping exceptions usually repeat the messages of their causes: {@code new RuntimeException(e)} takes over
     * {@code e.toString()} as its message, and a Janitor script error lists the messages of all of its causes. With
     * printStackTrace, the same text then appears once per cause. Here, a message that is already part of the output is left out,
     * only the class name and the stack frames of that exception are printed; a wrapper whose message is just
     * {@code cause.toString()} gets the message of its cause without the class name in front of it.
     * Stack frames, suppressed exceptions and the "... n more" lines are the same as with printStackTrace.</p>
     *
     * @param thrown the exception
     * @return the formatted stack trace, ending with a line break
     */
    public static String formatStackTrace(final Throwable thrown) {
        final StringBuilder out = new StringBuilder();
        appendThrowable(out, thrown, new StackTraceElement[0], "", "", Collections.newSetFromMap(new IdentityHashMap<>()));
        return out.append('\n').toString();
    }

    private static void appendThrowable(final StringBuilder out, final Throwable t, final StackTraceElement[] enclosingTrace,
                                        final String caption, final String prefix, final Set<Throwable> seen) {
        if (!out.isEmpty()) {
            out.append('\n').append(prefix).append(caption);
        }
        if (!seen.add(t)) {
            out.append("[CIRCULAR REFERENCE: ").append(t.getClass().getName()).append(']');
            return;
        }
        final Throwable cause = t.getCause();
        String message = t.getMessage();
        if (cause != null && message != null && message.equals(cause.toString())) {
            message = cause.getMessage();
        }
        out.append(t.getClass().getName());
        if (message != null && !message.isBlank() && out.indexOf(message.strip()) < 0) {
            out.append(": ").append(message);
        }

        final StackTraceElement[] trace = t.getStackTrace();
        int m = trace.length - 1;
        int n = enclosingTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
            m--;
            n--;
        }
        for (int i = 0; i <= m; i++) {
            out.append('\n').append(prefix).append("\tat ").append(trace[i]);
        }
        final int inCommon = trace.length - 1 - m;
        if (inCommon > 0) {
            out.append('\n').append(prefix).append("\t... ").append(inCommon).append(" more");
        }

        for (final Throwable suppressed : t.getSuppressed()) {
            appendThrowable(out, suppressed, trace, "Suppressed: ", prefix + "\t", seen);
        }
        if (cause != null) {
            appendThrowable(out, cause, trace, "Caused by: ", prefix, seen);
        }
    }

    /**
     * Formats a log record, after its parts have been collected.
     * @param cat the category of the record
     * @param thread the name of the thread
     * @param ts the timestamp, as a string
     * @param loggerName the name of the logger
     * @param message the message
     * @param arguments the arguments of the message
     * @param keyValuePairs the key-value pairs attached to the record
     * @param markers the markers attached to the record
     * @param contextMap the diagnostic context
     * @param loggingContext the logging context, may be null
     * @return the formatted text
     */
    protected abstract String formatLogRecord(final ToolLogCategory cat, final String thread, final String ts, final String loggerName, final String message, final List<Object> arguments, final List<KeyValuePair> keyValuePairs, final List<Marker> markers, final Map<String, String> contextMap, final ILoggingContext loggingContext);

}
