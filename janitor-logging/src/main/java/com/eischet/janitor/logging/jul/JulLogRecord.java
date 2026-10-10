package com.eischet.janitor.logging.jul;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/** A log record for java.util.logging that carries the extra information that SLF4J offers: markers, key-value pairs, arguments and context. */
public class JulLogRecord extends LogRecord {
    private @Nullable List<Marker> markers;
    private @Nullable List<KeyValuePair> keyValuePairs;
    private @Nullable List<Object> arguments;
    private Map<String, String> contextMap;

    private com.eischet.janitor.logging.ILoggingContext loggingContext;

    public JulLogRecord(final Level level, final String msg) {
        super(level, msg);
    }

    @Override
    public void setInstant(final Instant instant) {
        super.setInstant(instant);
    }

    @Override
    public Instant getInstant() {
        return super.getInstant();
    }

    @Override
    public void setMillis(final long millis) {
        super.setMillis(millis);
    }

    @Override
    public long getMillis() {
        return super.getMillis();
    }

    /**
     * @return the markers
     */
    public @Nullable List<Marker> getMarkers() {
        return markers;
    }

    /**
     * Sets the markers.
     * @param markers the markers
     */
    public void setMarkers(final @Nullable List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * @return the key-value pairs
     */
    public @Nullable List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    /**
     * Sets the key-value pairs.
     * @param keyValuePairs the key-value pairs
     */
    public void setKeyValuePairs(final @Nullable List<KeyValuePair> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    /**
     * @return the arguments of the message
     */
    public @Nullable List<Object> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments of the message.
     * @param arguments the arguments
     */
    public void setArguments(final @Nullable List<Object> arguments) {
        this.arguments = arguments;
    }

    /**
     * @return the diagnostic context
     */
    public Map<String, String> getContextMap() {
        return contextMap;
    }

    /**
     * Sets the diagnostic context.
     * @param contextMap the diagnostic context
     */
    public void setContextMap(final Map<String, String> contextMap) {
        this.contextMap = contextMap;
    }

    /**
     * Sets the logging context.
     * @param loggingContext the logging context
     */
    public void setLoggingContext(final com.eischet.janitor.logging.ILoggingContext loggingContext) {
        this.loggingContext = loggingContext;
    }

    /**
     * @return the logging context
     */
    public com.eischet.janitor.logging.ILoggingContext getLoggingContext() {
        return loggingContext;
    }
}
