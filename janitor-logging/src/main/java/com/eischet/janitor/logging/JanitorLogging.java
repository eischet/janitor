package com.eischet.janitor.logging;


import com.eischet.janitor.logging.formatter.ColoredConsoleFormatter;
import com.eischet.janitor.logging.formatter.PlainConsoleFormatter;
import com.eischet.janitor.logging.formatter.ToolLogCategory;
import com.eischet.janitor.logging.jul.LoggerPins;
import com.eischet.jul.rolling.RenamingBackupPolicy;
import com.eischet.jul.rolling.RollingFileHandler;
import com.eischet.jul.rolling.TimeRollingPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.jansi.Ansi;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.*;

/**
 * This helper class initializes Java Util Logging and SLF4J based just on a file name used to the main log file.
 * <p>The idea is to have reasonable defaults built in from the start instead of messing around with the minutiae of logging configuration all day long.</p>
 * <p>Rolling file sizes or number of days can be configured via environment variables or system properties; the latter takes precedence. The names
 * used for these properties can be overriden by setting the {@link #NOLOGFILE}, {@link #NOCONSOLELOG}, {@link #CONSOLELOGCOLOR}, {@link #LOGDAYS},
 * {@link #LOGMINUTES} and {@link #LOGSIZE} properties. The defaults of these settings are the same as the member names, e.g. LOGSIZE.</p>
 * <p>The {@link #WARNINGS_ONLY} and {@link #ERRORS_ONLY} lists can be used to limit logging to specific packages or classes.</p>
 */
public class JanitorLogging {

    /**
     * Logger names (packages/classes) for which only Warnings and above should be logged.
     * Add any packages you want <b>before</b> calling {@link #configure(String)}.
     * Example: "org.eclipse.jetty.ee10.annotations.AnnotationParser".
     */
    public static final Set<String> WARNINGS_ONLY = new HashSet<>();

    /**
     * Logger names (packages/classes) for which only Errors and above should be logged.
     * Add any packages you want <b>before</b> calling {@link #configure(String)}.
     * Example: "org.eclipse.jetty.ee10.annotations.AnnotationParser".
     */
    public static final Set<String> ERRORS_ONLY = new HashSet<>();

    /**
     * Configurable name for an env property that turns of logging to file when set to "true".
     */
    public static String NOLOGFILE = "NOLOGFILE";

    /**
     * Configurable name for an env property that turns off console logging when set to "true".
     */
    public static String NOCONSOLELOG = "NOCONSOLELOG";

    /**
     * Configurable name for an env property that turns on colored console logging when set to "true".
     */
    public static String CONSOLELOGCOLOR = "CONSOLELOGCOLOR";

    /**
     * Configurable name for an env property that sets the maximum number of days to keep log files.
     */
    public static String LOGDAYS = "LOGDAYS";

    /**
     * Configurable name for an env property that sets the maximum number of minutes to keep log files.
     */
    public static String LOGMINUTES = "LOGMINUTES";

    /**
     * Configurable name for an env property that sets the maximum number of log files.
     */
    public static String LOGFILES = "LOGFILES";

    /**
     * Configurable name for an env property that sets the maximum log file size in bytes.
     */
    public static String LOGSIZE = "LOGSIZE";

    /**
     * Path where the log files are stored
     */
    public static @Nullable File LOG_FOLDER;

    public static @NotNull org.slf4j.Logger getLogger(final @NotNull Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Remove all existing handlers from the root logger.
     */
    public static void removeAnyHandlers() {
        // remove all existing handlers
        final Logger rootLogger = Logger.getLogger("");
        for (final Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
    }

    /*
     * Get a setting from Java's system properties (preferred) or environment variables.
     */
    public static String env(final String key) {
        final String systemProperty = System.getProperty(key);
        final String envProperty = System.getenv(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return envProperty;
    }

    /**
     * Configure logging.
     * @param logName root name of the log files we'll write
     */
    public static void configure(final String logName) {
        configure(logName, null);
    }

    /**
     * Configure logging.
     * @param logName root name of the log files we'll write
     */
    public static void configure(final String logName, final @Nullable Supplier<Filter> filterSupplier) {
        final boolean noLogFile = "true".equalsIgnoreCase(env(NOLOGFILE));
        final boolean noConsoleLog = "true".equalsIgnoreCase(env(NOCONSOLELOG));
        final boolean color = "true".equalsIgnoreCase(env(CONSOLELOGCOLOR));
        final String logDaysString = env(LOGDAYS);
        int days = 0;
        if (logDaysString != null && !logDaysString.isBlank()) {
            try {
                days = Integer.parseInt(logDaysString, 10);
            } catch (NumberFormatException e) {
                System.err.println("error parsing " + LOGDAYS + ": " + e.getMessage() + "; logging will use default file sizes!");
                e.printStackTrace(System.err);
            }
        }
        final String logMinutesString = env(LOGMINUTES);
        int minutes = 0;
        if (logMinutesString != null && !logMinutesString.isBlank()) {
            try {
                minutes = Integer.parseInt(logMinutesString, 10);
            } catch (NumberFormatException e) {
                System.err.println("error parsing " + LOGMINUTES + ": " + e.getMessage() + "; logging will use default file sizes!");
                e.printStackTrace(System.err);
            }
        }

        int logFiles = 4;
        int logSize = 1024 * 1024 * 10;

        final String logFilesString = env(LOGFILES);
        if (logFilesString != null && !logFilesString.isBlank()) {
            try {
                logFiles = Integer.parseInt(logFilesString, 10);
            } catch (NumberFormatException e) {
                System.err.println("error parsing " + LOGFILES + ": " + e.getMessage() + "; logging will use default file sizes!");
                e.printStackTrace(System.err);
            }
        }

        final String logSizeString = env(LOGSIZE);
        if (logSizeString != null && !logSizeString.isBlank()) {
            try {
                logSize = Integer.parseInt(logSizeString, 10);
            } catch (NumberFormatException e) {
                System.err.println("error parsing " + LOGSIZE + ": " + e.getMessage() + "; logging will use default file sizes!");
                    e.printStackTrace(System.err);
            }
        }

        System.out.printf("configuring logging: file --> %s, console --> %s, color --> %s, days --> %s, size=%s, files=%s, name=%s\n", !noLogFile, !noConsoleLog, color, days, logSize, logFiles, logName);

        removeAnyHandlers();

        final Logger rootLogger = Logger.getLogger("");

        // unless explicitly disabled, add our own file handler
        if (!noLogFile) {
            final String saneLogName = sanitizeFilename(logName);
            if (!Objects.equals(saneLogName, logName)) {
                System.out.printf("rewrote log file name: %s --> %s\n", logName, saneLogName);
            }

            boolean addStream = false;

            final File logFolder = new File("logs");

            if (!logFolder.exists()) {
                System.out.println("Log directory not found, will try to create it at " + logFolder.getAbsolutePath());
                if (!logFolder.mkdirs()) {
                    System.err.println("Failed to create log directory, the app will probably not start up.");
                }
            } else {
                System.out.println("Logging to: " + logFolder.getAbsolutePath());
            }

            JanitorLogging.LOG_FOLDER = logFolder;

            if (minutes > 0) {
                final RollingFileHandler rolf = new RollingFileHandler(logFolder, saneLogName + ".log", 0, new TimeRollingPolicy(TimeRollingPolicy.TimeBoundary.MINUTELY), new RenamingBackupPolicy(saneLogName + "_%s.log", minutes));
                rolf.setFormatter(new PlainConsoleFormatter());
                if (filterSupplier != null) {
                    rolf.setFilter(filterSupplier.get());
                }
                rolf.setLevel(Level.INFO);
                rootLogger.addHandler(rolf);
                addStream = true;
            }

            if (!addStream && days > 0) {
                final RollingFileHandler rolf = new RollingFileHandler(logFolder, saneLogName + ".log", 0, new TimeRollingPolicy(TimeRollingPolicy.TimeBoundary.DAILY), new RenamingBackupPolicy(saneLogName + "_%s.log", days));
                rolf.setFormatter(new PlainConsoleFormatter());
                if (filterSupplier != null) {
                    rolf.setFilter(filterSupplier.get());
                }
                rolf.setLevel(Level.INFO);
                rootLogger.addHandler(rolf);
                addStream = true;
            }

            if (!addStream) {
                final FileHandler fileHandler;
                try {
                    fileHandler = new FileHandler("logs/" + saneLogName + "-%g.log", logSize, logFiles, true);
                    // fileHandler.setLevel(Level.INFO);
                    fileHandler.setFormatter(new PlainConsoleFormatter());
                    rootLogger.addHandler(fileHandler);
                    if (filterSupplier != null) {
                        fileHandler.setFilter(filterSupplier.get());
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // unless explicitly disabled, add our console logging handler
        if (!noConsoleLog) {

            final ErrorManager errorManager = new JanitorLoggingErrorManager();

            if (color) {
                // scheint die Farben AUSzuschalten: AnsiConsole.systemInstall();
                final ColoredConsoleFormatter formatter = new ColoredConsoleFormatter();
                final JanitorLoggingConsoleHandler consoleHandler = new JanitorLoggingConsoleHandler(System.out, formatter, errorManager);
                // consoleHandler.setFilter(cda.getAntiFileFiler());
                rootLogger.addHandler(consoleHandler);
            } else {
                final PlainConsoleFormatter formatter = new PlainConsoleFormatter();
                final JanitorLoggingConsoleHandler consoleHandler = new JanitorLoggingConsoleHandler(System.out, formatter, errorManager);
                // consoleHandler.setFilter(cda.getAntiFileFiler());
                rootLogger.addHandler(consoleHandler);
            }
        }

        // always add the database appender
        // rootLogger.addHandler(cda);

        for (final String packageName : WARNINGS_ONLY) {
            setLoggerLevel(packageName, Level.WARNING);
        }
        for (final String packageName : ERRORS_ONLY) {
            setLoggerLevel(packageName, Level.SEVERE);
        }

    }

    /**
     * Sanitize a filename for use in a log file name.
     * @param fn user suggested log file name
     * @return final log file name
     */
    private static String sanitizeFilename(String fn) {
        fn = fn.toLowerCase(Locale.ROOT);
        fn = fn.replaceAll("[\\\\/:*?\"<>|.%$§()= ]", "");
        if (fn.isBlank()) {
            fn = "anonymous";
        }
        return fn;
    }

    /**
     * Set the logging level for a specific logger.
     * @param name name of the logger, e.g. MyClass.class.getName()
     * @param level the level to set
     */
    public static void setLoggerLevel(final String name, final Level level) {
        Logger atm = Logger.getLogger(name);
        atm.setLevel(Level.WARNING);
        atm.setUseParentHandlers(false);
        for (var h : atm.getHandlers()) {
            h.setLevel(level);
            atm.removeHandler(h);
        }
        LoggerPins.pin(name);
        // we MUST keep a reference to the logger, or it will be garbage collected at the next possible moment and come back strong with 50 belly dancers, *can't stop* the thing then ;-)
    }

    public static Ansi.Color foregroundFor(final ToolLogCategory category) {
        if (category != null) {
            return switch (category) {
                case INFO -> Ansi.Color.BLUE;
                case DEBUG -> Ansi.Color.MAGENTA;
                case INVALID -> Ansi.Color.CYAN;
                case ERROR, WARNING -> Ansi.Color.RED;
            };
        }
        return Ansi.Color.DEFAULT;
    }

}
