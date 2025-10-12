package com.eischet.janitor.logging.jul;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class JulLogRecord extends LogRecord {
    private @Nullable List<Marker> markers;
    private @Nullable List<KeyValuePair> keyValuePairs;
    private @Nullable List<Object> arguments;
    private Map<String, String> contextMap;

    private ILoggingContext loggingContext;

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

    public @Nullable List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(final @Nullable List<Marker> markers) {
        this.markers = markers;
    }

    public @Nullable List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(final @Nullable List<KeyValuePair> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    public @Nullable List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(final @Nullable List<Object> arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getContextMap() {
        return contextMap;
    }

    public void setContextMap(final Map<String, String> contextMap) {
        this.contextMap = contextMap;
    }

    public void setLoggingContext(final ILoggingContext loggingContext) {
        this.loggingContext = loggingContext;
    }

    public ILoggingContext getLoggingContext() {
        return loggingContext;
    }
}
