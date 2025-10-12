package com.eischet.janitor.logging.formatter;

import com.eischet.janitor.logging.jul.ILoggingContext;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;

public class PlainConsoleFormatter extends BasicFormatter {

    @Override
    protected String formatLogRecord(final ToolLogCategory cat, final String thread, final String ts, final String loggerName, final String message, final List<Object> arguments, final List<KeyValuePair> keyValuePairs, final List<Marker> markers, final Map<String, String> contextMap, final ILoggingContext loggingContext) {
        final StringBuilder result = new StringBuilder();

        result.append(ts).append(' ').append(loggerName).append(' ').append(cat).append(' ').append(thread);

        if (loggingContext != null) {
            if (loggingContext.getApp() != null) {
                result.append(" <").append(loggingContext.getApp());
                if (loggingContext.getUser() != null) {
                    result.append(", ").append(loggingContext.getUser());
                }
                result.append('>');
            }
        }

        result.append(' ').append(message);
        if (arguments != null && !arguments.isEmpty()) {
            result.append(" (").append(arguments.get(0));
            for (int i = 1; i < arguments.size(); i++) {
                result.append(", ").append(arguments.get(i));
            }
            result.append(')');
        }
        if (keyValuePairs != null) {
            keyValuePairs.forEach(keyValuePair -> result.append(' ').append(keyValuePair.key).append('=').append(keyValuePair.value));
        }
        // ggf. später noch Markers

        result.append('\n');
        return result.toString();
        /* alt:
        if (keyValuePairs != null) {
            return "%s %s %s %s - %s %s\n".formatted(ts, loggerName, thread, cat, message, keyValuePairs);
        }
        return "%s %s %s %s - %s\n".formatted(ts, loggerName, thread, cat, message);
         */
    }

}
