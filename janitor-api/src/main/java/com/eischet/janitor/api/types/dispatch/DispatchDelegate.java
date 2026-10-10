// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Helper interface for looking up attributes from an optional parent dispatcher.
 *
 * @param <T> parent's type
 */
public interface DispatchDelegate<T> {
    /**
     * Looks up an attribute in the parent dispatcher.
     * @param instance the object whose attribute is looked up
     * @param process the running script process
     * @param name the name of the attribute
     * @return the attribute, or null if there is none
     * @throws JanitorRuntimeException if the lookup fails
     */
    JanitorObject delegate(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException;
}
