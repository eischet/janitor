// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Keeps strong references to java.util.logging loggers, because the logging system holds loggers only weakly,
 * and a logger that gets garbage collected would lose its configuration.
 */
public final class LoggerPins {
    private static final Map<String, Logger> PINS = new ConcurrentHashMap<>();

    /**
     * Gets a logger, and keeps a reference to it.
     * @param name the name of the logger
     * @return the logger
     */
    public static Logger pin(String name) {
        return PINS.computeIfAbsent(name, Logger::getLogger);
    }
}