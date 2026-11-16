// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types;

/**
 * Implemented by objects that want to know when a script binds them to a name and when that binding ends, e.g. to count
 * references or to release a resource as soon as the script no longer holds it.
 * <p>
 * Scopes call these methods only for objects that implement this interface, and leaving a scope that never held such an
 * object costs nothing. Resources that only need to be released when the script process ends are better served by
 * {@link JanitorCleanupRequired}.
 * </p>
 * <p>Intended Users: scripting runtime.</p>
 */
public interface JanitorScopeAware {

    /**
     * Called when the object is bound to a name in a scope: once per binding, so an object bound under two names is
     * entered twice.
     */
    default void janitorEnterScope() {
        // ignored by default
    }

    /**
     * Called when a binding of the object ends: when its scope is left, or when the name is bound to another value.
     * <p>
     * This is a chance to release resources for objects that were <b>created by the script</b>.
     * Global objects passed into a script should not react to this event to avoid becoming unusable in case they are reused!
     * </p>
     */
    default void janitorLeaveScope() {
        // ignored by default
    }

}
