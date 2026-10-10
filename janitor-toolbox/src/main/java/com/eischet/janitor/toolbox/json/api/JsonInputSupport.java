// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/** Creates JSON readers. */
public interface JsonInputSupport {
    /**
     * @param json the JSON text
     * @return a reader for the text
     */
    JsonInputStream createInputStream(final String json);
}
