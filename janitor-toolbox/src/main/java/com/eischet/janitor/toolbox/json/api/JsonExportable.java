// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;


import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/** Implemented by objects that can write themselves as JSON, as a list, an object or a single value. */
public interface JsonExportable {

    /**
     * @return true if this is written as a JSON array
     */
    boolean isList();
    /**
     * @return true if this is written as a JSON object
     */
    boolean isObject();
    /**
     * @return true if this is written as a single JSON value
     */
    boolean isValue();
    /**
     * @return true if this has its default value or is empty, so that it may be left out
     */
    boolean isDefaultOrEmpty();

    /**
     * Writes this as JSON.
     * @param producer the target
     * @throws JsonException if this cannot be written
     */
    void writeJson(JsonOutputStream producer) throws JsonException;

    /**
     * Converts this to JSON text.
     * @param environment creates the JSON
     * @return the JSON text
     * @throws JsonException if this cannot be written
     */
    default @Language("JSON") String exportToJson(final @NotNull JsonOutputSupport environment) throws JsonException {
        return environment.writeJson(this::writeJson);
    }

    interface JsonOutputMapped {
        /**
         * Writes this as a JSON value.
         * @param producer the target
         * @throws JsonException if this cannot be written
         */
        void writeAsJsonValue(JsonOutputStream producer) throws JsonException;
        /**
         * @return true if this should be left out of the JSON output
         */
        default boolean isOmittedInJson() {
            return false;
        }
    }

}
