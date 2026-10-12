// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small helpers for building JSON documents (the API description) as trees of {@link Map}, {@link List}, {@link String}, {@link Number},
 * {@link Boolean} and {@code null}, and for writing such trees.
 */
final class JsonTree {

    private JsonTree() {
    }

    /** @return a new, empty object that keeps the order in which its members were added */
    static @NotNull Map<String, Object> object() {
        return new LinkedHashMap<>();
    }

    /** Shortcut for an object with the given alternating keys and values; null values are left out. */
    static @NotNull Map<String, Object> object(final Object... keysAndValues) {
        final Map<String, Object> result = object();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            if (keysAndValues[i + 1] != null) {
                result.put((String) keysAndValues[i], keysAndValues[i + 1]);
            }
        }
        return result;
    }

    /** @return the JSON text of a tree */
    static @NotNull String toJson(final @Nullable Object tree) throws JsonException {
        return Janitor.current().writeJson(stream -> write(stream, tree));
    }

    /** Writes a tree to a JSON stream. */
    static void write(final @NotNull JsonOutputStream stream, final @Nullable Object tree) throws JsonException {
        switch (tree) {
            case null -> stream.nullValue();
            case Map<?, ?> map -> {
                stream.beginObject();
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    stream.key((String) entry.getKey());
                    write(stream, entry.getValue());
                }
                stream.endObject();
            }
            case List<?> list -> {
                stream.beginArray();
                for (final Object element : list) {
                    write(stream, element);
                }
                stream.endArray();
            }
            case String string -> stream.value(string);
            case Boolean bool -> stream.value(bool.booleanValue());
            case Integer integer -> stream.value(integer.longValue());
            case Long longValue -> stream.value(longValue.longValue());
            case Number number -> stream.value(number);
            default -> throw new JsonException("cannot write " + tree.getClass().getSimpleName() + " as JSON");
        }
    }
}
