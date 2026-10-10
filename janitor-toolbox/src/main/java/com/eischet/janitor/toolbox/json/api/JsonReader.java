// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/**
 * Interface for classes that support reading their own object properties from a JSON stream.
 *
 * This works with the dispatch tables to support reading object properties.
 */
public interface JsonReader {
    /**
     * Reads this object from a JSON stream.
     * @param stream the source
     * @throws JsonException if the JSON is invalid
     */
    void readJson(final JsonInputStream stream) throws JsonException;
}
