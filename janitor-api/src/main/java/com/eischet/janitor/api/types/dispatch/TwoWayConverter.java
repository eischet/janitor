// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

/**
 * Converts between Janitor values and Java values, in both directions.
 * @param <T> the Java type
 */
public interface TwoWayConverter<T> extends ConverterFromJanitor<T>, ConverterToJanitor<T> {
}
