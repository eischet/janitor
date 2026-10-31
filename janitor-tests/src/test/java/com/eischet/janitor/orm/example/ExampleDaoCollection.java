// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.DataManager;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.DaoProperties;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;

import java.util.List;

/**
 * Example registry of DAOs; each DAO registers itself here when it is constructed. It also provides the
 * {@link DataManager} that all of its DAOs use.
 */
public class ExampleDaoCollection extends OrmDaoCollection<ExampleDaoCollection> implements Uplink {

    public static final DispatchTable<ExampleDaoCollection> DISPATCH = new DispatchTable<>();

    /** Scripts reach the DAOs by the names of their entities, e.g. {@code example.Article}. */
    public static final DaoProperties<ExampleDaoCollection> DAOS = new DaoProperties<>(DISPATCH, "");

    static {
        OrmDaoCollection.addRegistryProperties(DISPATCH);
        DAOS.add("Article", ExampleDaoCollection::getArticleDao, GenericDao.dispatcherFor(ArticleDao.DISPATCH));
    }

    private final DataManager dataManager;
    private final ArticleDao articleDao;
    private final List<String> undeclaredDaos;

    public ExampleDaoCollection(final DataManager dataManager) {
        this(dataManager, dataManager.getDialect());
    }

    /** For tests that need another dialect than the one of the data manager, e.g. one that cannot limit rows in SQL. */
    public ExampleDaoCollection(final DataManager dataManager, final com.eischet.dbxs.dialects.DatabaseDialect dialect) {
        super(DISPATCH, dialect, null, null);
        this.dataManager = dataManager;
        this.articleDao = new ArticleDao(this);
        this.undeclaredDaos = DAOS.undeclared(this);
    }

    /** The registered DAOs that {@link #DAOS} does not declare; empty, unless a DAO was forgotten there. */
    public List<String> getUndeclaredDaos() {
        return undeclaredDaos;
    }

    public ArticleDao getArticleDao() {
        return articleDao;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }
}
