// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/** A {@link JsonExportable} that is exported as a JSON array. */
public interface JsonExportableList extends JsonExportable {
    @Override
    default boolean isList() {
        return true;
    }

    @Override
    default boolean isObject() {
        return false;
    }

    @Override
    default boolean isValue() {
        return false;
    }

}
