// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.HasModifiedDate;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.Versioned;
import com.eischet.janitor.orm.example.ExampleDaoCollection;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The API model and the OpenAPI description that are derived from the meta-data of the entities. */
public class OrmApiModelTestCase extends JanitorTest {

    // --- fixtures: a category with a key, a tag without key (and without path), and an item that refers to both ---

    public static class Category implements OrmEntity {
        static final ForeignKeyNull<Category> NULL = new ForeignKeyNull<>(Category.class);
        static final EntityDispatchTable<Category, TestCollection> DISPATCH = new EntityDispatchTable<>(Category.class, up -> new Category(), NULL, TestCollection::getCategoryDao);

        static {
            DISPATCH.dbTable("test_category", "category_id", "category_key", "category_name", "seq_category");
            DISPATCH.addLongColumn("id", "category_id", Category::getId, Category::setId);
            DISPATCH.addStringColumn("key", "category_key", Category::getKey, Category::setKey, 20);
            DISPATCH.addStringColumn("name", "category_name", Category::getName, Category::setName, 50);
            RestOrm.expose(DISPATCH, "categories").setMetaData(RestMetaData.MAX_PAGE_SIZE, 2);
        }

        private long id;
        private String key;
        private String name;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    public static class Tag implements OrmEntity {
        static final ForeignKeyNull<Tag> NULL = new ForeignKeyNull<>(Tag.class);
        static final EntityDispatchTable<Tag, TestCollection> DISPATCH = new EntityDispatchTable<>(Tag.class, up -> new Tag(), NULL, TestCollection::getTagDao);

        static {
            DISPATCH.dbTable("test_tag", "tag_id", null, "tag_name", "seq_tag"); // no key column, and not exposed (no path)
            DISPATCH.addLongColumn("id", "tag_id", Tag::getId, Tag::setId);
            DISPATCH.addStringColumn("name", "tag_name", Tag::getName, Tag::setName, 50);
        }

        private long id;
        private String name;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return null; }
        @Override public void setKey(final String key) { }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    public static class Item implements OrmEntity, HasModifiedDate, Versioned {
        static final ForeignKeyNull<Item> NULL = new ForeignKeyNull<>(Item.class);
        static final EntityDispatchTable<Item, TestCollection> DISPATCH = new EntityDispatchTable<>(Item.class, up -> new Item(up), NULL, TestCollection::getItemDao);

        static {
            DISPATCH.dbTable("test_item", "item_id", "item_key", "item_name", "seq_item")
                    .versionColumn("version_no")
                    .modifiedColumn("modified_at");
            DISPATCH.addLongColumn("id", "item_id", Item::getId, Item::setId);
            DISPATCH.addStringColumn("key", "item_key", Item::getKey, Item::setKey, 30);
            DISPATCH.addStringColumn("name", "item_name", Item::getName, Item::setName, 80)
                    .setMetaData(Janitor.MetaData.HELP, "The display name.");
            RestOrm.example(DISPATCH.addStringColumn("code", "item_code", Item::getCode, Item::setCode, 5), "\"AB-1\"");
            RestOrm.hidden(DISPATCH.addStringColumn("secret", "item_secret", Item::getSecret, Item::setSecret, 40));
            RestOrm.notFilterable(DISPATCH.addIntegerColumn("rank", "item_rank", Item::getRank, Item::setRank));
            DISPATCH.addDateTimeColumn("modifiedDate", "modified_at", Item::getModifiedDate, Item::setModifiedDate);
            DISPATCH.addLongColumn("version", "version_no", Item::getVersion, Item::setVersion);
            DISPATCH.addReference("category", "category_id", Category.DISPATCH, Item::getCategory, Item::setCategory, item -> item.source);
            DISPATCH.addReference("tag", "tag_id", Tag.DISPATCH, Item::getTag, Item::setTag, item -> item.source);
            RestOrm.expose(DISPATCH, "items");
        }

        private final TestCollection source;
        private long id;
        private String key;
        private String name;
        private String code;
        private String secret;
        private int rank;
        private LocalDateTime modifiedDate;
        private long version;
        private ForeignKey<Category> category = Category.NULL;
        private ForeignKey<Tag> tag = Tag.NULL;

        Item(final TestCollection source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        @Override public @Nullable LocalDateTime getModifiedDate() { return modifiedDate; }
        @Override public void setModifiedDate(final @Nullable LocalDateTime updateDate) { this.modifiedDate = updateDate; }
        @Override public long getVersion() { return version; }
        @Override public void setVersion(final long version) { this.version = version; }
        String getCode() { return code; }
        void setCode(final String code) { this.code = code; }
        String getSecret() { return secret; }
        void setSecret(final String secret) { this.secret = secret; }
        int getRank() { return rank; }
        void setRank(final int rank) { this.rank = rank; }
        ForeignKey<Category> getCategory() { return category; }
        void setCategory(final ForeignKey<Category> category) { this.category = category; }
        ForeignKey<Tag> getTag() { return tag; }
        void setTag(final ForeignKey<Tag> tag) { this.tag = tag; }
    }

    public static class CategoryDao extends GenericDao<Category, TestCollection> {
        CategoryDao(final TestCollection c) { super(new DispatchTable<CategoryDao>(false), c, Category.class, Category.DISPATCH, Category::new); }
        @Override public @NotNull Class<Category> getEntityClass() { return Category.class; }
        @Override public @NotNull String getEntityClassName() { return "Category"; }
    }

    public static class TagDao extends GenericDao<Tag, TestCollection> {
        TagDao(final TestCollection c) { super(new DispatchTable<TagDao>(false), c, Tag.class, Tag.DISPATCH, Tag::new); }
        @Override public @NotNull Class<Tag> getEntityClass() { return Tag.class; }
        @Override public @NotNull String getEntityClassName() { return "Tag"; }
    }

    public static class ItemDao extends GenericDao<Item, TestCollection> {
        ItemDao(final TestCollection c) { super(new DispatchTable<ItemDao>(false), c, Item.class, Item.DISPATCH, () -> new Item(c)); }
        @Override public @NotNull Class<Item> getEntityClass() { return Item.class; }
        @Override public @NotNull String getEntityClassName() { return "Item"; }
    }

    public static class TestCollection extends OrmDaoCollection<TestCollection> implements Uplink {
        static final DispatchTable<TestCollection> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final CategoryDao categoryDao;
        private final TagDao tagDao;
        private final ItemDao itemDao;

        TestCollection(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.categoryDao = new CategoryDao(this);
            this.tagDao = new TagDao(this);
            this.itemDao = new ItemDao(this);
        }

        CategoryDao getCategoryDao() { return categoryDao; }
        TagDao getTagDao() { return tagDao; }
        ItemDao getItemDao() { return itemDao; }
        @Override public DataManager getDataManager() { return dataManager; }
        @Override public @NotNull Clock getClock() { return Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC); }
    }

    private OrmApiModel itemModel() {
        return OrmApiModel.of(new TestCollection(TestDb.newManager()));
    }

    // --- the model ---

    @Test
    public void onlyEntitiesWithAPathAreExposed() {
        final OrmApiModel model = itemModel();
        assertEquals(List.of("categories", "items"), model.getEntities().stream().map(ApiEntity::path).toList());
        assertNull(model.findByPath("tags"));
    }

    @Test
    public void hiddenPropertiesAndPropertiesWithoutColumnAreNotExposed() {
        final ApiEntity item = itemModel().findByPath("items");
        assertNull(item.field("secret"));
        assertNotNull(item.field("code"));
    }

    @Test
    public void idVersionAndModifiedDateAreReadOnlyAndNamed() {
        final ApiEntity item = itemModel().findByPath("items");
        assertEquals("id", item.idField());
        assertEquals("key", item.keyField());
        assertEquals("version", item.versionField());
        assertEquals("modifiedDate", item.modifiedField());
        assertTrue(item.field("id").readOnly());
        assertTrue(item.field("version").readOnly());
        assertTrue(item.field("modifiedDate").readOnly());
        assertFalse(item.field("name").readOnly());
    }

    @Test
    public void primitivesAreRequiredAndTextsAreNullable() {
        final ApiEntity item = itemModel().findByPath("items");
        assertTrue(item.field("id").required());
        assertFalse(item.field("id").nullable());
        assertTrue(item.field("rank").required());
        assertTrue(item.field("name").nullable());
        assertFalse(item.field("name").required());
        assertEquals(30, item.field("key").maxLength());
    }

    @Test
    public void referencesUseTheKeyOfTheTargetIfItHasOneAndTheIdOtherwise() {
        final ApiEntity item = itemModel().findByPath("items");
        final ApiField category = item.field("category");
        assertEquals("Category", category.referencedEntity());
        assertTrue(category.referenceByKey());
        assertEquals("string", category.jsonType());
        assertNull(category.jsonFormat());
        final ApiField tag = item.field("tag");
        assertEquals("Tag", tag.referencedEntity());
        assertFalse(tag.referenceByKey());
        assertEquals("integer", tag.jsonType());
        assertEquals("int64", tag.jsonFormat());
        assertTrue(category.nullable() && tag.nullable());
    }

    @Test
    public void filteringAndSortingFollowTheDefaultsAndCanBeSwitchedOff() {
        final ApiEntity item = itemModel().findByPath("items");
        assertTrue(item.field("name").filterable());
        assertFalse(item.field("rank").filterable());
        assertTrue(item.field("rank").sortable());
        final ApiEntity article = OrmApiModel.of(new ExampleDaoCollection(TestDb.newManager())).findByPath("articles");
        assertFalse(article.field("description").filterable(), "long texts are not filterable by default");
    }

    @Test
    public void tablesWithoutKeyAreNotAddressableByKey() {
        assertTrue(itemModel().findByPath("items").hasKey());
        assertFalse(new ApiEntity("X", "x", Tag.DISPATCH, List.of(), "id", null, null, null, 1, null, RestOperations.READ_ONLY.set()).hasKey());
    }

    // --- the schema ---

    @Test
    @SuppressWarnings("unchecked")
    public void theEntitySchemaDescribesTypesFormatsAndConstraints() {
        final ApiEntity item = itemModel().findByPath("items");
        final Map<String, Object> schema = OrmSchemaBuilder.entitySchema(item);
        final Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertEquals(List.of("string", "null"), ((Map<String, Object>) properties.get("name")).get("type"));
        assertEquals("The display name.", ((Map<String, Object>) properties.get("name")).get("description"));
        assertEquals(80, ((Map<String, Object>) properties.get("name")).get("maxLength"));
        assertEquals("int64", ((Map<String, Object>) properties.get("id")).get("format"));
        assertEquals(Boolean.TRUE, ((Map<String, Object>) properties.get("id")).get("readOnly"));
        assertEquals("Category", ((Map<String, Object>) properties.get("category")).get("x-references"));
        assertEquals("AB-1", ((Map<String, Object>) properties.get("code")).get("example"));
        assertEquals("date-time", ((Map<String, Object>) properties.get("modifiedDate")).get("format"));
        final List<Object> required = (List<Object>) schema.get("required");
        assertTrue(required.contains("id") && required.contains("rank") && required.contains("version"));
        assertFalse(required.contains("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void decimalsCarryPrecisionAndScale() {
        final ApiField price = new ApiField("price", "price", ColumnTypeHint.DECIMAL, false, true, false, true, true, null, 10, 2, null, false, null, null);
        final Map<String, Object> schema = OrmSchemaBuilder.fieldSchema(price);
        assertEquals("number", schema.get("type"));
        assertEquals(10, schema.get("x-precision"));
        assertEquals(2, schema.get("x-scale"));
    }

    // --- the OpenAPI description ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> describe() {
        return OpenApiEmitter.of(itemModel()).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void theDescriptionContainsThePathsOfExposedEntitiesOnly() {
        final Map<String, Object> paths = (Map<String, Object>) describe().get("paths");
        assertTrue(paths.keySet().containsAll(List.of("/items", "/items/search", "/items/{id}", "/items/by-key/{key}", "/categories", "/categories/{id}")));
        assertTrue(paths.keySet().stream().noneMatch(path -> path.contains("tags")), paths.keySet().toString());
        final Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) describe().get("components")).get("schemas");
        assertTrue(schemas.keySet().containsAll(List.of("Item", "ItemPage", "Category", "FilterExpression", "SearchRequest", "Problem")));
        assertFalse(schemas.containsKey("Tag"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void versionedEntitiesAdvertiseETagAndLastModified() {
        final Map<String, Object> paths = (Map<String, Object>) describe().get("paths");
        final Map<String, Object> ok = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) paths.get("/items/{id}")).get("get")).get("responses")).get("200"));
        final Map<String, Object> headers = (Map<String, Object>) ok.get("headers");
        assertTrue(headers.containsKey("ETag") && headers.containsKey("Last-Modified"));
        final Map<String, Object> plain = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) paths.get("/categories/{id}")).get("get")).get("responses")).get("200"));
        assertNull(plain.get("headers"));
    }

    @Test
    public void everyReferenceInTheDescriptionResolves() {
        final Map<String, Object> document = describe();
        final List<String> refs = new ArrayList<>();
        collectRefs(document, refs);
        assertFalse(refs.isEmpty());
        for (final String ref : refs) {
            assertTrue(ref.startsWith("#/"), ref);
            Object node = document;
            for (final String part : ref.substring(2).split("/")) {
                assertInstanceOf(Map.class, node, ref);
                node = ((Map<?, ?>) node).get(part);
                assertNotNull(node, "dangling reference " + ref);
            }
        }
    }

    private static void collectRefs(final Object node, final List<String> refs) {
        if (node instanceof Map<?, ?> map) {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                if ("$ref".equals(entry.getKey())) {
                    refs.add((String) entry.getValue());
                } else {
                    collectRefs(entry.getValue(), refs);
                }
            }
        } else if (node instanceof List<?> list) {
            list.forEach(element -> collectRefs(element, refs));
        }
    }

    @Test
    public void theDescriptionIsWrittenAsJson() throws Exception {
        final String json = OpenApiEmitter.of(itemModel()).toJson().replaceAll("\\s", "");
        assertTrue(json.contains("\"openapi\":\"3.1.0\""), json);
        assertTrue(json.contains("\"/items/{id}\""), json);
        assertTrue(json.contains("\"x-references\":\"Category\""), json);
    }

    @Test
    public void theServerUrlIsOptional() {
        final Map<String, Object> without = OpenApiEmitter.of(itemModel()).build();
        assertNull(without.get("servers"));
        final Map<String, Object> with = new OpenApiEmitter(itemModel(), OpenApiOptions.defaults().withServerUrl("/api")).build();
        assertNotNull(with.get("servers"));
    }

    @Test
    public void entitiesAreReadOnlyUnlessTheyOptIn() {
        assertEquals(RestOperations.READ_ONLY.set(), itemModel().findByPath("items").operations());
        assertFalse(itemModel().findByPath("items").allows(RestOperation.CREATE));
        final var scratch = new EntityDispatchTable<>(Category.class, up -> new Category(), Category.NULL, TestCollection::getCategoryDao);
        RestOrm.allow(scratch, RestOperation.CREATE, RestOperation.DELETE);
        assertEquals(java.util.Set.of(RestOperation.LIST, RestOperation.SEARCH, RestOperation.GET, RestOperation.CREATE, RestOperation.DELETE), scratch.getMetaData(RestMetaData.OPERATIONS).set());
        RestOrm.operations(scratch, RestOperation.GET);
        assertEquals(java.util.Set.of(RestOperation.GET), scratch.getMetaData(RestMetaData.OPERATIONS).set());
        assertTrue(RestOperation.LIST.isRead() && !RestOperation.UPDATE.isRead());
    }

    @Test
    public void exposeRejectsMalformedPaths() {
        assertThrows(IllegalArgumentException.class, () -> RestOrm.expose(Category.DISPATCH, "/categories"));
        assertThrows(IllegalArgumentException.class, () -> RestOrm.expose(Category.DISPATCH, "a/{id}"));
        assertThrows(IllegalArgumentException.class, () -> RestOrm.expose(Category.DISPATCH, " "));
    }

    @Test
    public void exampleArticleIsExposedAsArticles() {
        final OrmApiModel model = OrmApiModel.of(new ExampleDaoCollection(TestDb.newManager()));
        final ApiEntity article = model.findByPath("articles");
        assertEquals("Article", article.className());
        assertEquals(List.of("id", "key", "shortCode", "name", "description", "stock", "active"), article.fields().stream().map(ApiField::name).toList());
    }
}
