// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

import com.eischet.janitor.logging.formatter.ColoredConsoleFormatter;

import java.util.logging.*;

/** Configures java.util.logging for unit tests: colored console output, with warnings and errors only. */
public class JanitorUnitTestLogging {

    /*
        Theoretically, it should be possible to use this class as a JUL configurer:

            System.setProperty("java.util.logging.config.class", JanitorUnitTestLogging.class.getName());

        I haven't got it to work, though, and as a workaround I'll simply call setup().

     */

    public JanitorUnitTestLogging() {
        setup();
    }

    /** Configures the root logger with a colored console handler. */
    public static void setup() {
        // intentionally silent: System.out.println("CONFIGURING UNIT TEST LOGGING");
        final ErrorManager errorManager = new JanitorLoggingErrorManager();
        final ColoredConsoleFormatter formatter = new ColoredConsoleFormatter();
        final JanitorLoggingConsoleHandler consoleHandler = new JanitorLoggingConsoleHandler(System.out, formatter, errorManager);
        final Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.WARNING);
        for (final Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
                rootLogger.fine("remove handler: " + handler);
            } else {
                rootLogger.fine("keep handler: " + handler);
            }
        }
    }


}
