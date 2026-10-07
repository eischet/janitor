package com.eischet.janitor.orm.example;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKeyNull;

/**
 * Example entity: a catalog article, stored in the table {@code demo_article}.
 * <p>
 * Shows the pieces that a typical entity consists of: a {@link EntityDispatchTable} that maps properties to
 * columns, plain getters and setters, and a no-argument-free constructor that receives the owning
 * {@link ExampleDaoCollection}. See {@link ArticleDao} for the DAO and {@link ExampleDaoCollection} for the
 * registry that ties them together.
 * </p>
 * <p>
 * Of interest for filtering: {@code key} is a mixed-case column, while {@code shortCode} is declared as
 * all-upper-case via {@link com.eischet.janitor.orm.meta.OrmPropertyHandle#allUpperCase()}, so that
 * case-insensitive filters on it don't need to fold the column in the database.
 * </p>
 */
public class Article implements OrmEntity {

    public static final ForeignKeyNull<Article> NULL = new ForeignKeyNull<>(Article.class);

    public static final EntityDispatchTable<Article, ExampleDaoCollection> DISPATCH =
            new EntityDispatchTable<>(Article.class, Article::new, NULL, ExampleDaoCollection::getArticleDao);

    static {
        DISPATCH.dbTable("demo_article", "article_id", "article_key", "article_name", "seq_demo_article_id");
        DISPATCH.addLongColumn("id", "article_id", Article::getId, Article::setId);
        DISPATCH.addStringColumn("key", "article_key", Article::getKey, Article::setKey, 50);
        DISPATCH.addStringColumn("shortCode", "short_code", Article::getShortCode, Article::setShortCode, 10).allUpperCase();
        DISPATCH.addStringColumn("name", "article_name", Article::getName, Article::setName, 100);
        DISPATCH.addTextColumn("description", "description", Article::getDescription, Article::setDescription);
        DISPATCH.addIntegerColumn("stock", "stock", Article::getStock, Article::setStock);
        DISPATCH.addBooleanColumn("active", "active", Article::isActive, Article::setActive);
    }

    private final ExampleDaoCollection source;
    private long id;
    private String key;
    private String shortCode;
    private String name;
    private String description;
    private int stock;
    private boolean active;
    private boolean softDeleted;

    public Article(final ExampleDaoCollection source) {
        this.source = source;
    }

    public ExampleDaoCollection getSource() {
        return source;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(final String key) {
        this.key = key;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(final String shortCode) {
        this.shortCode = shortCode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(final int stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public boolean isSoftDeleted() {
        return softDeleted;
    }

    @Override
    public void setSoftDeleted(final boolean softDeleted) {
        this.softDeleted = softDeleted;
    }

    @Override
    public String toString() {
        return "Article{" + id + ", " + key + ", " + shortCode + "}";
    }
}
