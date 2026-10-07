package com.eischet.janitor.orm.example;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.GenericDao;
import org.jetbrains.annotations.NotNull;

/**
 * Example DAO for {@link Article}. A GenericDao needs very little: the dispatch table of the entity does the mapping.
 */
public class ArticleDao extends GenericDao<Article, ExampleDaoCollection> {

    public ArticleDao(final ExampleDaoCollection collection) {
        super(new DispatchTable<ArticleDao>(false), collection, Article.class, Article.DISPATCH, () -> new Article(collection));
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
