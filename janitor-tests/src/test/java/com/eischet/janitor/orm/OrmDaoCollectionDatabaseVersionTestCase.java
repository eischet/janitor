// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.dialects.DatabaseDialectGeneric;
import com.eischet.dbxs.dialects.DatabaseDialectH2;
import com.eischet.dbxs.dialects.DatabaseDialectMicrosoft;
import com.eischet.dbxs.dialects.DatabaseDialectOracle;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The collection decides once, from dialect and database version, whether rows can be limited in SQL; no version means "modern database". */
public class OrmDaoCollectionDatabaseVersionTestCase {

    static class Collection extends OrmDaoCollection<Collection> {
        static final DispatchTable<Collection> DISPATCH = new DispatchTable<>();

        Collection(final DatabaseDialect dialect, final @Nullable DatabaseVersion version) {
            super(DISPATCH, dialect, null, version);
        }

        @Override
        public DataManager getDataManager() {
            return null;
        }
    }

    private static DatabaseVersion version(final int major) {
        final DatabaseVersion v = new DatabaseVersion();
        v.setMajorVersion(major);
        return v;
    }

    @Test
    void noVersionMeansModern() {
        assertTrue(new Collection(new DatabaseDialectH2(), null).canLimitAndOffset());
        assertTrue(new Collection(new DatabaseDialectOracle(), null).canLimitAndOffset());
        assertTrue(new Collection(new DatabaseDialectMicrosoft(), null).canLimitAndOffset());
        assertNull(new Collection(new DatabaseDialectOracle(), null).getDatabaseVersion());
    }

    @Test
    void noVersionDoesNotMakeUpCapabilitiesThatTheDialectLacks() {
        assertFalse(new Collection(new DatabaseDialectGeneric(), null).canLimitAndOffset());
    }

    @Test
    void legacyVersionsAreRespected() {
        assertFalse(new Collection(new DatabaseDialectOracle(), version(11)).canLimitAndOffset());
        assertTrue(new Collection(new DatabaseDialectOracle(), version(12)).canLimitAndOffset());
        assertFalse(new Collection(new DatabaseDialectMicrosoft(), version(10)).canLimitAndOffset());
        assertTrue(new Collection(new DatabaseDialectMicrosoft(), version(11)).canLimitAndOffset());
    }

    @Test
    void dialectIsExposed() {
        final DatabaseDialect dialect = new DatabaseDialectH2();
        assertSame(dialect, new Collection(dialect, null).getDialect());
    }
}
