package com.eischet.janitor.logging.formatter;

import com.eischet.janitor.logging.jul.ILoggingContext;
import com.eischet.janitor.logging.jul.JulLogRecord;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * <p>My main gripe with the original format is that it uses a strikingly ugly and utterly unreadable 2-line format ... for my tastes.
 * People are free to disagree, of course, even when they're wrong. ;-)</p>
 *
 */
public abstract class BasicFormatter extends Formatter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ThreadMXBean threadManagementBean;

    private static final List<ExceptionFormattingHandler> exceptionFormattingHandlers = new ArrayList<>();

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

        if (!exceptionFormattingHandlers.isEmpty()) {
            for (final ExceptionFormattingHandler handler : exceptionFormattingHandlers) {
                @Nullable final String result = handler.formatLogException(thrown);
                if (result != null) {
                    message += " " + result;
                    omitStackTrace = true; // we have a custom formatting, so no need for the stack trace
                    break; // only one handler is allowed to format the exception
                }
            }
        }
        if (thrown != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                thrown.printStackTrace(pw);
                pw.close();
                sw.close();
                message += " STACK TRACE: <" + sw + ">";

            } catch (IOException e) {
                System.err.printf("error formatting exception %s\n", thrown);
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

    protected abstract String formatLogRecord(final ToolLogCategory cat, final String thread, final String ts, final String loggerName, final String message, final List<Object> arguments, final List<KeyValuePair> keyValuePairs, final List<Marker> markers, final Map<String, String> contextMap, final ILoggingContext loggingContext);

}
