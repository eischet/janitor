// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.GenericDao;
import org.jetbrains.annotations.NotNull;

/**
 * Example DAO for {@link Article}. A GenericDao needs very little: the dispatch table of the entity does the mapping.
 */
public class ArticleDao extends GenericDao<Article, ExampleDaoCollection> {

    /** The script methods of this DAO on top of those of every DAO; none so far. */
    public static final DispatchTable<ArticleDao> DISPATCH = new DispatchTable<>(false);

    public ArticleDao(final ExampleDaoCollection collection) {
        super(DISPATCH, collection, Article.class, Article.DISPATCH, () -> new Article(collection));
    }

    @Override
    public @NotNull Class<Article> getEntityClass() {
        return Article.class;
    }

    @Override
    public @NotNull String getEntityClassName() {
        return "Article";
    }

    /**
     * The SQL that {@link #findByFilter} would run, for tests that want to look at the generated statement.
     */
    public String sqlFor(final FilterQuery query) {
        return createFindByFilterQuery(query).getSql();
    }
}
