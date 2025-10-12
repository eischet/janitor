package com.eischet.janitor.logging.jul;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class LoggerPins {
    private static final Map<String, Logger> PINS = new ConcurrentHashMap<>();

    public static Logger pin(String name) {
        return PINS.computeIfAbsent(name, Logger::getLogger);
    }
}