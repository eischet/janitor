// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Filters on fields of referenced entities, like "product.shortCode" or "product.vendor.name", which become sub-queries,
 * both as filter expressions and as lambdas: an item refers to a product, which refers to a vendor.
 */
public class PathFilterTestCase extends JanitorTest {

    public static class Vendor implements OrmEntity {
        static final ForeignKeyNull<Vendor> NULL = new ForeignKeyNull<>(Vendor.class);
        static final EntityDispatchTable<Vendor, Shop> DISPATCH = new EntityDispatchTable<>(Vendor.class, up -> new Vendor(), NULL, Shop::getVendorDao);

        static {
            DISPATCH.dbTable("p_vendor", "vendor_id", "vendor_key", "vendor_name", "seq_p_vendor");
            DISPATCH.addLongColumn("id", "vendor_id", Vendor::getId, Vendor::setId);
            DISPATCH.addStringColumn("key", "vendor_key", Vendor::getKey, Vendor::setKey, 20);
            DISPATCH.addStringColumn("name", "vendor_name", Vendor::getName, Vendor::setName, 50);
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

    public static class Product implements OrmEntity {
        static final ForeignKeyNull<Product> NULL = new ForeignKeyNull<>(Product.class);
        static final EntityDispatchTable<Product, Shop> DISPATCH = new EntityDispatchTable<>(Product.class, Product::new, NULL, Shop::getProductDao);

        static {
            DISPATCH.dbTable("p_product", "product_id", "product_key", "product_name", "seq_p_product");
            DISPATCH.addLongColumn("id", "product_id", Product::getId, Product::setId);
            DISPATCH.addStringColumn("key", "product_key", Product::getKey, Product::setKey, 20);
            DISPATCH.addStringColumn("name", "product_name", Product::getName, Product::setName, 50);
            DISPATCH.addStringColumn("shortCode", "short_code", Product::getShortCode, Product::setShortCode, 10);
            DISPATCH.addReference("vendor", "vendor_id", Vendor.DISPATCH, Product::getVendor, Product::setVendor, product -> product.source);
        }

        private final Shop source;
        private long id;
        private String key;
        private String name;
        private String shortCode;
        private ForeignKey<Vendor> vendor = Vendor.NULL;

        Product(final Shop source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        String getShortCode() { return shortCode; }
        void setShortCode(final String shortCode) { this.shortCode = shortCode; }
        ForeignKey<Vendor> getVendor() { return vendor; }
        void setVendor(final ForeignKey<Vendor> vendor) { this.vendor = vendor; }
    }

    public static class Item implements OrmEntity {
        static final ForeignKeyNull<Item> NULL = new ForeignKeyNull<>(Item.class);
        static final EntityDispatchTable<Item, Shop> DISPATCH = new EntityDispatchTable<>(Item.class, Item::new, NULL, Shop::getItemDao);

        static {
            DISPATCH.dbTable("p_item", "item_id", "item_key", "item_name", "seq_p_item");
            DISPATCH.addLongColumn("id", "item_id", Item::getId, Item::setId);
            DISPATCH.addStringColumn("key", "item_key", Item::getKey, Item::setKey, 20);
            DISPATCH.addStringColumn("name", "item_name", Item::getName, Item::setName, 50);
            DISPATCH.addReference("product", "product_id", Product.DISPATCH, Item::getProduct, Item::setProduct, item -> item.source);
        }

        private final Shop source;
        private long id;
        private String key;
        private String name;
        private ForeignKey<Product> product = Product.NULL;

        Item(final Shop source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        ForeignKey<Product> getProduct() { return product; }
        void setProduct(final ForeignKey<Product> product) { this.product = product; }
    }

    public static class VendorDao extends GenericDao<Vendor, Shop> {
        VendorDao(final Shop c) { super(new DispatchTable<VendorDao>(false), c, Vendor.class, Vendor.DISPATCH, Vendor::new); }
        @Override public @NotNull Class<Vendor> getEntityClass() { return Vendor.class; }
        @Override public @NotNull String getEntityClassName() { return "Vendor"; }
    }

    public static class ProductDao extends GenericDao<Product, Shop> {
        ProductDao(final Shop c) { super(new DispatchTable<ProductDao>(false), c, Product.class, Product.DISPATCH, () -> new Product(c)); }
        @Override public @NotNull Class<Product> getEntityClass() { return Product.class; }
        @Override public @NotNull String getEntityClassName() { return "Product"; }
    }

    public static class ItemDao extends GenericDao<Item, Shop> {
        ItemDao(final Shop c) { super(new DispatchTable<ItemDao>(false), c, Item.class, Item.DISPATCH, () -> new Item(c)); }
        @Override public @NotNull Class<Item> getEntityClass() { return Item.class; }
        @Override public @NotNull String getEntityClassName() { return "Item"; }

        String sqlFor(final FilterExpression filter) {
            return createFindByFilterQuery(FilterQuery.from(filter)).getSql();
        }
    }

    public static class Shop extends OrmDaoCollection<Shop> implements Uplink {
        static final DispatchTable<Shop> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final VendorDao vendorDao;
        private final ProductDao productDao;
        private final ItemDao itemDao;

        Shop(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.vendorDao = new VendorDao(this);
            this.productDao = new ProductDao(this);
            this.itemDao = new ItemDao(this);
        }

        VendorDao getVendorDao() { return vendorDao; }
        ProductDao getProductDao() { return productDao; }
        ItemDao getItemDao() { return itemDao; }
        @Override public DataManager getDataManager() { return dataManager; }
    }

    private Shop shop;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create table p_vendor (vendor_id bigint primary key, vendor_key varchar(20), vendor_name varchar(50))"));
            conn.update(UpdateStatement.of("create table p_product (product_id bigint primary key, product_key varchar(20), product_name varchar(50), "
                    + "short_code varchar(10), vendor_id bigint references p_vendor(vendor_id))"));
            conn.update(UpdateStatement.of("create table p_item (item_id bigint primary key, item_key varchar(20), item_name varchar(50), "
                    + "product_id bigint references p_product(product_id))"));
            conn.update(UpdateStatement.of("insert into p_vendor values (1, 'V1', 'Acme'), (2, 'V2', 'Globex')"));
            conn.update(UpdateStatement.of("insert into p_product values (1, 'P1', 'Bazooka', 'BAZ', 1), (2, 'P2', 'Foobar', 'FOO', 2), (3, 'P3', 'Orphan', null, null)"));
            conn.update(UpdateStatement.of("insert into p_item values (1, 'I1', 'first', 1), (2, 'I2', 'second', 2), (3, 'I3', '', 3), (4, 'I4', null, null)"));
        });
        shop = new Shop(manager);
    }

    private List<String> keys(final FilterExpression filter) throws DatabaseError {
        return shop.getDataManager().callTransaction(conn -> shop.getItemDao().findByFilter(conn, FilterQuery.from(filter)))
                .stream().map(Item::getKey).sorted().toList();
    }

    private List<String> select(final String script) throws Exception {
        final JanitorObject result = evaluate(script, g -> {
            g.bind("items", shop.getItemDao());
            g.bind("products", shop.getProductDao());
        });
        return ((JList) result).stream().map(item -> ((Item) item).getKey()).sorted().toList();
    }

    @Test
    void pathsBecomeSubQueries() throws Exception {
        final FilterExpression filter = FilterExpression.from("product.shortCode", FilterOperator.EQ, "BAZ");
        assertEquals(List.of("I1"), keys(filter));
        assertTrue(shop.getItemDao().sqlFor(filter).contains("in (select"), shop.getItemDao().sqlFor(filter));
        assertEquals(List.of("I2"), keys(FilterExpression.from("product.vendor.name", FilterOperator.STARTSWITH, "glob")));
        assertEquals(Integer.valueOf(1), shop.getDataManager().<Integer>callTransaction(conn -> shop.getItemDao().countByFilter(conn, filter)));
    }

    @Test
    void entitiesWithoutReferenceNeverMatchAPath() throws Exception {
        // I3's product has no short code; I4 has no product at all, and is not found, following the rules of SQL
        assertEquals(List.of("I3"), keys(new FilterExpression("product.shortCode", FilterOperator.ISNULL)));
        assertEquals(List.of("I4"), keys(new FilterExpression("product", FilterOperator.ISNULL)));
    }

    @Test
    void invalidPaths() {
        assertFalse(shop.getItemDao().isFilterField("name.length"));
        assertFalse(shop.getItemDao().isFilterField("product.nope"));
        assertTrue(shop.getItemDao().isFilterField("product.vendor.name"));
        assertThrows(MalformedExpression.class, () -> keys(FilterExpression.from("name.length", FilterOperator.EQ, 1)));
        assertThrows(MalformedExpression.class, () -> keys(FilterExpression.from("product.nope", FilterOperator.EQ, "x")));
    }

    @Test
    void lambdasWithPaths() throws Exception {
        assertEquals(List.of("I1"), select("return items.select(i -> i.product.shortCode == \"BAZ\")"));
        assertEquals(List.of("I2"), select("return items.select(i -> i.product.vendor.name.startsWithIgnoreCase(\"glob\"))"));
        assertEquals(List.of("I1", "I2"), select("return items.select(i -> i.product.vendor != null)"));
    }

    @Test
    void referencesAreComparedByTheirId() throws Exception {
        assertEquals(List.of("I2"), select("p = products.findByKey(\"P2\"); return items.select(i -> i.product == p)"));
        assertEquals(List.of("I1"), select("return items.select(i -> i.product == 1)"));
        assertEquals(List.of("I4"), select("return items.select(i -> i.product == null)"));
    }

    @Test
    void emptiness() throws Exception {
        assertEquals(List.of("I3", "I4"), select("return items.select(i -> i.name == null or i.name == \"\")"));
        assertEquals(List.of("I1", "I2"), select("return items.select(i -> i.name != null and i.name != \"\")"));
    }

    @Test
    void rejectsPathsThatAreNoFields() {
        final JanitorArgumentException e = assertThrows(JanitorArgumentException.class,
                () -> select("return items.select(i -> i.product.nope == 1)"));
        assertTrue(e.getMessage().contains("'product.nope' is not a field"), e.getMessage());
    }

}
