// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * The standard {@link JoinWrangler}, which creates join records with a constructor function.
 * @param <J> the type of the join records
 * @param <L> the type of the entities on the left side
 * @param <R> the type of the entities on the right side
 * @param <U> the type of the uplink
 */
public class SimpleJoinWrangler<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity,
        U extends Uplink
    > implements JoinWrangler<J, L, R, U> {

    protected final DispatchTable<J> dispatchTable;
    protected final Class<J> wrangledClass;
    protected final Function<U, J> constructor;

    public SimpleJoinWrangler(final DispatchTable<J> dispatchTable, final Class<J> wrangledClass, final Function<U, J> constructor) {
        this.dispatchTable = dispatchTable;
        this.wrangledClass = wrangledClass;
        this.constructor = constructor;
    }

    @Override
    public @NotNull Class<J> getWrangledClass() {
        return wrangledClass;
    }

    @Override
    public @NotNull String getSimpleClassName() {
        return wrangledClass.getSimpleName();
    }

    @Override
    public @NotNull DispatchTable<J> getDispatchTable() {
        return dispatchTable;
    }

    @Override
    public @NotNull J createNewInstance(@NotNull final U uplink) {
        return constructor.apply(uplink);
    }

}
