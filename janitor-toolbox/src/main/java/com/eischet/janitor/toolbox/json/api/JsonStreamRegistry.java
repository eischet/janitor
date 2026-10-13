// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/** Discovers the {@link JsonStreamFactory} implementations once and chooses the default. */
final class JsonStreamRegistry {

    private JsonStreamRegistry() {
    }

    /** Lazily initialized, so that the class path is only scanned when JSON is actually used. */
    private static final class Holder {
        static final List<JsonStreamFactory> AVAILABLE = ServiceLoader.load(JsonStreamFactory.class).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingInt(JsonStreamFactory::priority).reversed().thenComparing(JsonStreamFactory::name))
                .toList();
        static final JsonStreamFactory DEFAULT = chooseDefault();
    }

    static List<JsonStreamFactory> available() {
        return Holder.AVAILABLE;
    }

    static @NotNull JsonStreamFactory defaultFactory() {
        return Holder.DEFAULT;
    }

    static @NotNull JsonStreamFactory named(final @NotNull String name) {
        for (final JsonStreamFactory factory : Holder.AVAILABLE) {
            if (factory.name().equals(name)) {
                return factory;
            }
        }
        throw new IllegalArgumentException("unknown JSON implementation '" + name + "', available: " + names());
    }

    private static JsonStreamFactory chooseDefault() {
        if (Holder.AVAILABLE.isEmpty()) {
            throw new IllegalStateException("no JsonStreamFactory implementation found on the class path");
        }
        final String selected = System.getProperty(JsonStreamFactory.SYSTEM_PROPERTY);
        if (selected == null || selected.isBlank()) {
            return Holder.AVAILABLE.getFirst();
        }
        try {
            return named(selected.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("system property " + JsonStreamFactory.SYSTEM_PROPERTY + ": " + e.getMessage(), e);
        }
    }

    private static String names() {
        return Holder.AVAILABLE.stream().map(JsonStreamFactory::name).collect(Collectors.joining(", "));
    }

}
