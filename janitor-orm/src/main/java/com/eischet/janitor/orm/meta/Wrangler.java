// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmObject;
import org.jetbrains.annotations.NotNull;

/**
 * Knows everything that is needed to work with a type of ORM object generically: its class, its dispatch table and how to create instances.
 * @param <T> the type of the objects
 * @param <U> the type of the uplink
 */
public interface Wrangler<T extends OrmObject, U extends Uplink> {
    @NotNull Class<T> getWrangledClass();
    @NotNull String getSimpleClassName();
    @NotNull DispatchTable<T> getDispatchTable();
    @NotNull T createNewInstance(final @NotNull U uplink);
}
