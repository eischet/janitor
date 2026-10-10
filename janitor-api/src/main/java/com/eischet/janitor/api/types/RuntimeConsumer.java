// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

/**
 * Like the functional interface Consumer, but with a throws clause, so it can be used in interpreted code.
 * @param <T> any type of consumable object.
 */
@FunctionalInterface
public interface RuntimeConsumer<T> {
    /**
     * Accepts the given object.
     * @param object the object to accept.
     * @throws Exception on errors
     */
    void accept(T object) throws Exception;
}
