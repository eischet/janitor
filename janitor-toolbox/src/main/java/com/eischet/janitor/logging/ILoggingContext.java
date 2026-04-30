package com.eischet.janitor.logging;


import org.jetbrains.annotations.Nullable;

/**
 * This interface provides three optional pieces of context information for logging purposes:
 * <ul>
 *     <li><strong>App:</strong> Application/module name.</li>
 *     <li><strong>User:</strong> Username who's working with the app.</li>
 *     <li><strong>Entity:</strong> An acting entity in whose context the action happens.</li>
 * </ul>
 * <p>
 *     The {@code com.eischet.janitor.logging.formatter.ColoredConsoleFormatter}
 *     and {@code com.eischet.janitor.logging.formatter.PlainConsoleFormatter}
 *     in module {@code }janitor-logging} are responsible for rendering these values
 *     into the log.
 * </p>
 * <p>
 *     To get an instance of {@link ILoggingContext}, you can use the {@link LoggingContext} class.
 * </p>
 */
public interface ILoggingContext {
    @Nullable String getApp();
    @Nullable String getUser();
    @Nullable String getEntity();
}
