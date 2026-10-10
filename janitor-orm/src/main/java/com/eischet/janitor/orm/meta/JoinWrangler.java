// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;

/**
 * A {@link Wrangler} for join records.
 * @param <J> the type of the join records
 * @param <L> the type of the entities on the left side
 * @param <R> the type of the entities on the right side
 * @param <U> the type of the uplink
 */
public interface JoinWrangler<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity,
        U extends Uplink
        >
        extends Wrangler<J, U> {
}
