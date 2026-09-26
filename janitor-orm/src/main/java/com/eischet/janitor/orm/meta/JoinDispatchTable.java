package com.eischet.janitor.orm.meta;

import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * {@link OrmDispatchTable} for join objects; the table is its own {@link JoinWrangler}.
 */
public class JoinDispatchTable<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity,
        U extends Uplink
        > extends OrmDispatchTable<J, U> implements JoinWrangler<J, L, R, U> {

    public JoinDispatchTable(final @NotNull Class<J> wrangledClass, final @NotNull Function<U, J> constructor) {
        super(wrangledClass, constructor);
    }

    protected JoinDispatchTable(final @NotNull JoinDispatchTable<J, L, R, U> parent, final boolean includeApplyMethod) {
        super(parent, includeApplyMethod);
    }

    @Override
    public JoinDispatchTable<J, L, R, U> extend(final boolean includeApplyMethod) {
        return new JoinDispatchTable<>(this, includeApplyMethod);
    }

    @Override
    public JoinDispatchTable<J, L, R, U> extend() {
        return extend(true);
    }

}
