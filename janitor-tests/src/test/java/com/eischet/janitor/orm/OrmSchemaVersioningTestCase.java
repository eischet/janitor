package com.eischet.janitor.orm;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.LazyLoadedString;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.versioning.Version;
import com.eischet.janitor.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrmSchemaVersioningTestCase extends JanitorTest {

    static class TargetEntity implements OrmEntity {
        static final ForeignKeyNull<TargetEntity> NULL = new ForeignKeyNull<>();
        static final EntityDispatchTable<TargetEntity, VersionedTestCollection> DISPATCH =
                new EntityDispatchTable<>(TargetEntity.class, TargetEntity::new, NULL, up -> null);

        static {
            DISPATCH.dbTable("target_entity", "target_id", "target_key", "target_n", "seq_target_id");
            DISPATCH.addLongColumn("id", "target_id", TargetEntity::getId, TargetEntity::setId);
        }

        private long id;
        private String key;
        private String name;
        private boolean softDeleted;

        TargetEntity() {}
        TargetEntity(VersionedTestCollection col) {}

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return softDeleted; }
        @Override public void setSoftDeleted(final boolean softDeleted) { this.softDeleted = softDeleted; }
    }

    static class VersionedEntity implements OrmEntity {
        static final ForeignKeyNull<VersionedEntity> NULL = new ForeignKeyNull<>();
        static final EntityDispatchTable<VersionedEntity, VersionedTestCollection> DISPATCH =
                new EntityDispatchTable<>(VersionedEntity.class, VersionedEntity::new, NULL, up -> null);

        static {
            DISPATCH.dbTable("versioned_entity", "entity_id", "entity_key", "entity_n", "seq_entity_id");
            DISPATCH.addLongColumn("id", "entity_id", VersionedEntity::getId, VersionedEntity::setId);
            DISPATCH.addStringColumn("baseCol", "base_col", VersionedEntity::getBaseCol, VersionedEntity::setBaseCol, 50);
            DISPATCH.addStringColumn("v2Col", "v2_col", VersionedEntity::getV2Col, VersionedEntity::setV2Col, 50)
                    .setMetaData(JanitorOrm.MetaData.VERSION_RANGE, VersionRange.startingWith(Version.ofWithRuntimeError("2.0.0")));
            DISPATCH.addLazyTextColumn("v2LazyCol", "v2_lazy_col", it -> it.v2LazyCol)
                    .setMetaData(JanitorOrm.MetaData.VERSION_RANGE, VersionRange.startingWith(Version.ofWithRuntimeError("2.0.0")));
            DISPATCH.addReference("target", "target_id", TargetEntity.DISPATCH, VersionedEntity::getTarget, VersionedEntity::setTarget, it -> it.source)
                    .setMetaData(JanitorOrm.MetaData.VERSION_RANGE, VersionRange.startingWith(Version.ofWithRuntimeError("3.0.0")));
        }

        private final VersionedTestCollection source;
        private long id;
        private String key;
        private String name;
        private boolean softDeleted;
        private String baseCol;
        private String v2Col;
        final LazyLoadedString v2LazyCol;
        private ForeignKey<TargetEntity> target = TargetEntity.NULL;

        VersionedEntity(final VersionedTestCollection source) {
            this.source = source;
            this.v2LazyCol = new LazyLoadedString(() -> source.getEntityDao(), this::getId, "v2_lazy_col");
        }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return softDeleted; }
        @Override public void setSoftDeleted(final boolean softDeleted) { this.softDeleted = softDeleted; }

        public String getBaseCol() { return baseCol; }
        public void setBaseCol(final String baseCol) { this.baseCol = baseCol; }
        public String getV2Col() { return v2Col; }
        public void setV2Col(final String v2Col) { this.v2Col = v2Col; }
        public ForeignKey<TargetEntity> getTarget() { return target; }
        public void setTarget(final ForeignKey<TargetEntity> target) { this.target = target; }
    }

    static class VersionedJoin implements OrmJoined {
        static final OrmDispatchTable<VersionedJoin, VersionedTestCollection> DISPATCH = new OrmDispatchTable<>(VersionedJoin.class, VersionedJoin::new);

        static {
            DISPATCH.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "versioned_join");
            DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of("parent_id", "child_id"));
            DISPATCH.addLongColumn("parentId", "parent_id", VersionedJoin::getParentId, VersionedJoin::setParentId);
            DISPATCH.addLongColumn("childId", "child_id", VersionedJoin::getChildId, VersionedJoin::setChildId);
            DISPATCH.addStringColumn("extraV2", "extra_v2", VersionedJoin::getExtraV2, VersionedJoin::setExtraV2, 50)
                    .setMetaData(JanitorOrm.MetaData.VERSION_RANGE, VersionRange.startingWith(Version.ofWithRuntimeError("2.0.0")));
        }

        VersionedJoin() {}
        VersionedJoin(VersionedTestCollection col) {}

        private long parentId;
        private long childId;
        private String extraV2;

        public long getParentId() { return parentId; }
        public void setParentId(final long parentId) { this.parentId = parentId; }
        public long getChildId() { return childId; }
        public void setChildId(final long childId) { this.childId = childId; }
        public String getExtraV2() { return extraV2; }
        public void setExtraV2(final String extraV2) { this.extraV2 = extraV2; }
    }

    static class VersionedEntityDao extends GenericDao<VersionedEntity, VersionedTestCollection> {
        VersionedEntityDao(final VersionedTestCollection collection) {
            super(new DispatchTable<VersionedEntityDao>(false), collection, VersionedEntity.class, VersionedEntity.DISPATCH, () -> new VersionedEntity(collection));
        }
        @Override public @NotNull Class<VersionedEntity> getEntityClass() { return VersionedEntity.class; }
        @Override public @NotNull String getEntityClassName() { return "VersionedEntity"; }
        public List<String> getPublicColumns() { return columns; }
        public List<String> getPublicSelectColumns() { return selectColumns; }
    }

    static class VersionedJoinDao extends JoinDao<VersionedJoin> {
        VersionedJoinDao(final VersionedTestCollection collection) {
            super(VersionedJoin.class, new DispatchTable<VersionedJoinDao>(false), collection, VersionedJoin.DISPATCH, VersionedJoin::new);
        }
        @Override
        public VersionedJoin createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException {
            return new VersionedJoin();
        }
        public List<String> getPublicColumns() { return columns; }
    }

    static class VersionedTestCollection extends OrmDaoCollection<VersionedTestCollection> implements Uplink {
        static final DispatchTable<VersionedTestCollection> DISPATCH = new DispatchTable<>();
        private final VersionedEntityDao entityDao;
        private final VersionedJoinDao joinDao;

        VersionedTestCollection(final Version version) {
            super(DISPATCH, version);
            this.entityDao = new VersionedEntityDao(this);
            this.joinDao = new VersionedJoinDao(this);
        }

        VersionedEntityDao getEntityDao() { return entityDao; }
        VersionedJoinDao getJoinDao() { return joinDao; }

        @Override public DataManager getDataManager() { return null; }
    }

    @Test
    void testVersion1Filtering() throws DatabaseError {
        final VersionedTestCollection colV1 = new VersionedTestCollection(Version.ofWithRuntimeError("1.0.0"));
        final VersionedEntityDao entityDao = colV1.getEntityDao();
        final VersionedJoinDao joinDao = colV1.getJoinDao();

        // In v1, v2_col, v2_lazy_col and target_id should be excluded
        assertEquals(List.of("entity_id", "base_col"), entityDao.getPublicColumns());
        assertEquals(List.of("entity_id", "base_col"), entityDao.getPublicSelectColumns());

        // In v1, extra_v2 should be excluded from JoinDao columns
        assertEquals(List.of("parent_id", "child_id"), joinDao.getPublicColumns());

        // fetchLazyColumn on excluded column returns null without trying to access DataManager
        assertNull(entityDao.fetchLazyColumn(100L, "v2_lazy_col"));
    }

    @Test
    void testVersion2Filtering() {
        final VersionedTestCollection colV2 = new VersionedTestCollection(Version.ofWithRuntimeError("2.0.0"));
        final VersionedEntityDao entityDao = colV2.getEntityDao();
        final VersionedJoinDao joinDao = colV2.getJoinDao();

        // In v2, v2_col and v2_lazy_col are included, but target_id (v3) is excluded
        // selectColumns excludes lazy column
        assertTrue(entityDao.getPublicColumns().contains("v2_col"));
        assertTrue(entityDao.getPublicColumns().contains("v2_lazy_col"));
        assertFalse(entityDao.getPublicColumns().contains("target_id"));

        assertTrue(entityDao.getPublicSelectColumns().contains("v2_col"));
        assertFalse(entityDao.getPublicSelectColumns().contains("v2_lazy_col"));
        assertFalse(entityDao.getPublicSelectColumns().contains("target_id"));

        // JoinDao includes extra_v2
        assertEquals(List.of("parent_id", "child_id", "extra_v2"), joinDao.getPublicColumns());
    }

    @Test
    void testVersion3Filtering() {
        final VersionedTestCollection colV3 = new VersionedTestCollection(Version.ofWithRuntimeError("3.0.0"));
        final VersionedEntityDao entityDao = colV3.getEntityDao();

        // In v3, target_id is included
        assertTrue(entityDao.getPublicColumns().contains("target_id"));
        assertTrue(entityDao.getPublicSelectColumns().contains("target_id"));
    }
}
