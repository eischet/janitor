// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

/**
 * A functional interface for getting a property value of unknown type.
 * TODO: this should probably be replaced with Supplier&lt;Object&gt; or even be removed entirely.
 */
@FunctionalInterface
public interface FlatProperty {
    /**
     * @return the value of the property
     */
    Object getValue();
}
