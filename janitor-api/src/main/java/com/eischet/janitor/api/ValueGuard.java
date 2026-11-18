// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * Decides what a script gets to see: the interpreter passes every value through the guard that the script obtains from a
 * variable, a member, a method call, a function call or an index, before the script can use it. Literals and the
 * results of operators do not pass it, because the script made them itself.
 * <p>
 * A host sets a guard with {@link JanitorScriptProcess#setValueGuard(ValueGuard)} for scripts that may only see part of
 * the data they can reach, e.g. to hide records the current user must not see when a script navigates from one record to
 * another. The guard is called very often, so it should return values it does not care about right away and remember
 * what it has already decided.
 * </p>
 */
@FunctionalInterface
public interface ValueGuard {

    /**
     * Decides what the script gets to see.
     *
     * @param process the running script
     * @param value   the value the script is about to obtain; this can be a wrapper such as a property or a map entry,
     *                {@link JanitorObject#janitorUnpack()} returns what is inside
     * @return the same value, a replacement, or {@link Janitor#NULL} to hide it
     * @throws JanitorRuntimeException to refuse the access with an error
     */
    @NotNull JanitorObject guard(@NotNull JanitorScriptProcess process, @NotNull JanitorObject value) throws JanitorRuntimeException;

}
