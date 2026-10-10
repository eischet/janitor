// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

/**
 * A key-value pair that is attached to a log message.
 * @param key the key
 * @param value the value
 */
public record LoggingKeyValue(String key, Object value) { }
