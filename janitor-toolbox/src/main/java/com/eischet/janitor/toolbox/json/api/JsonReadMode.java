// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/** How strictly a {@link JsonInputStream} follows the JSON specification. */
public enum JsonReadMode {

    /** Accept only JSON as specified by RFC 8259. */
    STRICT,

    /**
     * Additionally accept what people tend to write by hand:
     * <ul>
     *     <li>comments, both <code>// ...</code> and <code>/* ... *&#47;</code></li>
     *     <li>strings and names in single quotes</li>
     *     <li>names without quotes, if they are valid Java identifiers</li>
     *     <li>trailing commas in arrays and objects</li>
     *     <li>{@code NaN}, {@code Infinity} and {@code -Infinity}</li>
     *     <li>a leading byte order mark</li>
     * </ul>
     * Nothing beyond this list should be relied upon. The Gson implementation deviates for now: it rejects trailing
     * commas in objects, reads a trailing comma in an array as an extra null, and accepts a few more oddities.
     */
    LENIENT

}
