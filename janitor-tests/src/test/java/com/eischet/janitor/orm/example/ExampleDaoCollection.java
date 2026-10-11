// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.DataManager;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;

/**
 * Example registry of DAOs; each DAO registers itself here when it is constructed. It also provides the
 * {@link DataManager} that all of its DAOs use.
 */
public class ExampleDaoCollection extends OrmDaoCollection<ExampleDaoCollection> implements Uplink {

    public static final DispatchTable<ExampleDaoCollection> DISPATCH = new DispatchTable<>();

    static {
        OrmDaoCollection.addRegistryProperties(DISPATCH);
    }

    private final DataManager dataManager;
    private final ArticleDao articleDao;

    public ExampleDaoCollection(final DataManager dataManager) {
        this(dataManager, dataManager.getDialect());
    }

    /** For tests that need another dialect than the one of the data manager, e.g. one that cannot limit rows in SQL. */
    public ExampleDaoCollection(final DataManager dataManager, final com.eischet.dbxs.dialects.DatabaseDialect dialect) {
        super(DISPATCH, dialect, null, null);
        this.dataManager = dataManager;
        this.articleDao = new ArticleDao(this);
    }

    public ArticleDao getArticleDao() {
        return articleDao;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }
}
