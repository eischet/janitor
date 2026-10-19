// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.metrics;

import com.eischet.janitor.orm.dao.GenericDao;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the {@link DaoMetrics} of a DAO; installed by the host with {@link OrmMetrics#install}. The host decides how to name and label
 * the metrics, e.g. from the DAO's entity class and its DAO collection.
 */
@FunctionalInterface
public interface DaoMetricsProvider {

    /**
     * The metrics of one DAO; called once per DAO, when its cache is first used.
     */
    @NotNull DaoMetrics forDao(@NotNull GenericDao<?, ?> dao);
}
