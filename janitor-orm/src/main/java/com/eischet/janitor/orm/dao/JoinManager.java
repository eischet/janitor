// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;

import java.util.List;

/**
 * Finds the join records that connect entities.
 * @param <J> the type of the join records
 * @param <L> the type of the entities on the left side
 * @param <R> the type of the entities on the right side
 */
public interface JoinManager<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity
        > {

    List<J> getRightJoins(L left);
    List<J> getLeftJoins(R right);

}
