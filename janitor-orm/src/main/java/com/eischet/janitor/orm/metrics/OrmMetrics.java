// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.metrics;

import com.eischet.janitor.orm.dao.GenericDao;
import org.jetbrains.annotations.NotNull;

/**
 * Where janitor-orm reports cache metrics: a process-wide {@link DaoMetricsProvider}, {@link DaoMetrics#NOOP} for every DAO until the host
 * installs one. Install it at startup, before DAOs are used; a DAO asks once and keeps what it got.
 */
public final class OrmMetrics {

    private static volatile DaoMetricsProvider provider = dao -> DaoMetrics.NOOP;

    private OrmMetrics() {
    }

    /**
     * Sets the provider for all DAOs that have not asked yet.
     */
    public static void install(final @NotNull DaoMetricsProvider provider) {
        OrmMetrics.provider = provider;
    }

    /**
     * The metrics of a DAO, from the installed provider; {@link DaoMetrics#NOOP} if the provider fails or returns nothing.
     */
    public static @NotNull DaoMetrics forDao(final @NotNull GenericDao<?, ?> dao) {
        try {
            final DaoMetrics metrics = provider.forDao(dao);
            return metrics == null ? DaoMetrics.NOOP : metrics;
        } catch (RuntimeException e) {
            return DaoMetrics.NOOP;
        }
    }
}
