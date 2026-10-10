// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/** A JSON writer that always writes {@code null}. */
public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
