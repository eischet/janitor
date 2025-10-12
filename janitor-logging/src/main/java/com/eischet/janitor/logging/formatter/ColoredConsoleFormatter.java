package com.eischet.janitor.logging.formatter;


import com.eischet.janitor.logging.jul.ILoggingContext;
import org.jline.jansi.Ansi;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;

/**
 * ANSI Formatter for JUL.
 */
public class ColoredConsoleFormatter extends BasicFormatter {

    @Override
    protected String formatLogRecord(final ToolLogCategory cat, final String thread, final String ts, final String loggerName, final String message, final List<Object> arguments, final List<KeyValuePair> keyValuePairs, final List<Marker> markers, final Map<String, String> contextMap, final ILoggingContext loggingContext) {
        return Ansi.ansi().reset().fg(Ansi.Color.WHITE).a(ts).a(" ").apply(ansi -> applyCategory(ansi, cat)).a(" ").a(cat.getCompactRepresentation()).a(" ").reset().a(" ").fg(Ansi.Color.WHITE).a(thread).apply(ansi -> {
                    if (loggingContext != null && loggingContext.getApp() != null) {
                        ansi.a(" <").fg(Ansi.Color.BLUE).a(loggingContext.getApp()).apply(nestedAnsi -> {
                            if (loggingContext.getUser() != null) {
                                nestedAnsi.fg(Ansi.Color.WHITE).a(", ").fg(Ansi.Color.BLUE).a(loggingContext.getUser());
                            }
                        }).fg(Ansi.Color.WHITE).a(">");
                    }
                }).a(" ").fg(Ansi.Color.YELLOW).a(loggerName).fg(Ansi.Color.WHITE)

                .apply(ansi -> {

                    if (contextMap != null && !contextMap.isEmpty()) {
                        ansi.a(" <").a(contextMap).a("> ");
                    }

                })

                .apply(ansi -> {
                    if (markers != null && !markers.isEmpty()) {
                        ansi.a(" ");
                        ansi.a(markers.toString());
                        for (final Marker marker : markers) {

                            ansi.fgBright(Ansi.Color.CYAN).a(marker.getName()).fg(Ansi.Color.WHITE).a(" ");
                        }
                    }
                }).a(" ").a(message).apply(ansi -> {
                    if (arguments != null && !arguments.isEmpty()) {
                        ansi.a(" (");
                        ansi.fgBright(Ansi.Color.BLUE).a(arguments.get(0));
                        for (int i = 1; i < arguments.size(); i++) {
                            ansi.fg(Ansi.Color.WHITE).a(", ").fgBright(Ansi.Color.BLUE).a(arguments.get(0));
                        }
                        ansi.fg(Ansi.Color.WHITE).a(")");
                    }
                    if (keyValuePairs != null) {
                        for (final KeyValuePair keyValuePair : keyValuePairs) {
                            ansi.a(" ").fgBright(Ansi.Color.MAGENTA).a(keyValuePair.key).fg(Ansi.Color.WHITE).a("=").fg(Ansi.Color.BLUE).a(keyValuePair.value);
                        }

                    }
                }).reset().a("\n").toString();
    }

    private static void applyCategory(final Ansi ansi, final ToolLogCategory cat) {
        if (cat != null) {
            switch (cat) {
                case INFO -> {
                    ansi.bg(Ansi.Color.GREEN).fgBright(Ansi.Color.WHITE);
                }
                case DEBUG -> {
                    ansi.bg(Ansi.Color.GREEN).fg(Ansi.Color.YELLOW);
                }
                case ERROR -> {
                    ansi.bgBright(Ansi.Color.YELLOW).fgBright(Ansi.Color.RED);
                }
                case WARNING, INVALID -> {
                    ansi.bgBright(Ansi.Color.YELLOW).fgBright(Ansi.Color.WHITE);
                }
            }
        }
    }

}
