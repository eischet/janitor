package com.eischet.janitor.api.errors.glue;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for exceptions thrown by "glue code", i.e. Java code that works with or for Janitor, but does not do so in the context of a running script process,
 * where "proper" runtime exceptions cannot be thrown.
 * Runtime exceptions carry a script stack trace with them, while others don't. That's why they are so important that we have this glue stuff.
 * Without this additional layer of complexity, code that does not have access to
 */
public class JanitorGlueException extends JanitorException {

    /**
     * In cases where the glue exception needs to be translated to a proper runtime exception, this interface will be used.
     */
    @FunctionalInterface
    public interface RuntimeExceptionConverter {
        JanitorRuntimeException createRuntimeException(final JanitorScriptProcess process, final JanitorGlueException glueException);
    }

    private final RuntimeExceptionConverter runtimeExceptionConverter;

    public JanitorGlueException(final @NotNull RuntimeExceptionConverter runtimeExceptionConverter, final String message) {
        super(message);
        this.runtimeExceptionConverter = runtimeExceptionConverter;
    }

    public JanitorGlueException(final @NotNull RuntimeExceptionConverter runtimeExceptionConverter, final String message, final Throwable cause) {
        super(message, cause);
        this.runtimeExceptionConverter = runtimeExceptionConverter;
    }

    public JanitorGlueException(final @NotNull RuntimeExceptionConverter runtimeExceptionConverter, final Throwable cause) {
        super(cause);
        this.runtimeExceptionConverter = runtimeExceptionConverter;
    }

    public JanitorGlueException(final @NotNull RuntimeExceptionConverter runtimeExceptionConverter, final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.runtimeExceptionConverter = runtimeExceptionConverter;
    }

    public JanitorRuntimeException toRuntimeException(final JanitorScriptProcess process) {
        return runtimeExceptionConverter.createRuntimeException(process, this);
    }

}
