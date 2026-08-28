package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.StatementConfigurator;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.DaoLogging;
import com.eischet.janitor.orm.dao.EntityChangeListener;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for ForeignKeyInteger/ForeignKeyString.hashCode(): they used to fold the lazily
 * populated, mutable "resolved" field into the hash code, even though equals() (via
 * ForeignKey.matchesWithUnknownType()) never looks at "resolved" at all -- only at the referenced
 * entity class plus the id (ForeignKeyInteger) or key (ForeignKeyString). Two equal() instances,
 * one already resolved and one not, therefore used to have different hash codes, breaking the
 * equals/hashCode contract.
 */
public class ForeignKeyHashCodeTestCase extends JanitorTest {

    static class TestEntity implements OrmEntity {
        private long id;
        private String key;
        private String name;
        private boolean softDeleted;

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(final long id) {
            this.id = id;
        }

        @Override
        public @Nullable String getKey() {
            return key;
        }

        @Override
        public void setKey(final String key) {
            this.key = key;
        }

        @Override
        public @Nullable String getName() {
            return name;
        }

        @Override
        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean isSoftDeleted() {
            return softDeleted;
        }

        @Override
        public void setSoftDeleted(final boolean softDeleted) {
            this.softDeleted = softDeleted;
        }
    }

    /**
     * Minimal Dao stand-in. Only getEntityClass()/lazyLoadById()/lazyLoadByKey() are actually
     * exercised by these tests (via ForeignKey.resolve()/resolveOrNull()); everything else throws,
     * since a real Dao needs a real database connection to do anything meaningful.
     */
    static class TestDao implements Dao<TestEntity> {
        @Override
        public @NotNull Class<TestEntity> getEntityClass() {
            return TestEntity.class;
        }

        @Override
        public @NotNull String getEntityClassName() {
            return TestEntity.class.getSimpleName();
        }

        @Override
        public @Nullable TestEntity findByKey(@NotNull final DatabaseConnection conn, @Nullable final String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable TestEntity findById(@NotNull final DatabaseConnection conn, final long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<TestEntity> findAll(@NotNull final DatabaseConnection conn, @Nullable final Integer limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<TestEntity> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<TestEntity> findByFilter(@NotNull final DatabaseConnection conn, @NotNull final FilterQuery filterQuery) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int countByFilter(@NotNull final DatabaseConnection conn, @Nullable final FilterExpression filterExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(@NotNull final DatabaseConnection conn, @NotNull final TestEntity record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(@NotNull final DatabaseConnection conn, @NotNull final TestEntity record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(@NotNull final DatabaseConnection conn, @NotNull final TestEntity record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<TestEntity> findByAssociation(@NotNull final DatabaseConnection conn, final String foreignKeyColumn, final long foreignKeyValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<TestEntity> lazyLoadByAssociation(final String foreignKeyColumn, final OrmEntity parentEntity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable TestEntity lazyLoadById(final long id) {
            // Must return a genuinely non-null, freshly-allocated entity here -- if this returned
            // null (or the same cached instance every time), the "resolved" field would never
            // actually change identity/hashCode, and the regression this test guards against
            // wouldn't be observable.
            final TestEntity entity = new TestEntity();
            entity.setId(id);
            return entity;
        }

        @Override
        public @Nullable TestEntity lazyLoadByKey(final String key) {
            final TestEntity entity = new TestEntity();
            entity.setKey(key);
            return entity;
        }

        @Override
        public void setLogging(final DaoLogging logging) {
        }

        @Override
        public ListenerRegistration addChangeListener(final EntityChangeListener<TestEntity> listener) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void resolvingAForeignKeyIntegerDoesNotChangeItsHashCode() {
        final TestDao dao = new TestDao();
        final ForeignKeyInteger<TestEntity> fk = new ForeignKeyInteger<>(42, dao);
        final int hashBefore = fk.hashCode();
        fk.resolveOrNull();
        final int hashAfter = fk.hashCode();
        assertEquals(hashBefore, hashAfter, "hashCode() must not change once the FK has been resolved");
    }

    @Test
    public void twoForeignKeyIntegersWithTheSameIdHaveTheSameHashCodeRegardlessOfResolvedState() {
        final TestDao dao = new TestDao();
        final ForeignKeyInteger<TestEntity> unresolved = new ForeignKeyInteger<>(42, dao);
        final ForeignKeyInteger<TestEntity> resolved = new ForeignKeyInteger<>(42, dao);
        resolved.resolveOrNull();
        assertEquals(unresolved, resolved);
        assertEquals(unresolved.hashCode(), resolved.hashCode(), "equal ForeignKeyInteger instances must have equal hash codes, per the equals/hashCode contract");
    }

    @Test
    public void resolvingAForeignKeyStringDoesNotChangeItsHashCode() {
        final TestDao dao = new TestDao();
        final ForeignKeyString<TestEntity> fk = new ForeignKeyString<>("some-key", dao);
        final int hashBefore = fk.hashCode();
        fk.resolveOrNull();
        final int hashAfter = fk.hashCode();
        assertEquals(hashBefore, hashAfter, "hashCode() must not change once the FK has been resolved");
    }

    @Test
    public void twoForeignKeyStringsWithTheSameKeyHaveTheSameHashCodeRegardlessOfResolvedState() {
        final TestDao dao = new TestDao();
        final ForeignKeyString<TestEntity> unresolved = new ForeignKeyString<>("some-key", dao);
        final ForeignKeyString<TestEntity> resolved = new ForeignKeyString<>("some-key", dao);
        resolved.resolveOrNull();
        assertEquals(unresolved, resolved);
        assertEquals(unresolved.hashCode(), resolved.hashCode(), "equal ForeignKeyString instances must have equal hash codes, per the equals/hashCode contract");
    }

}
