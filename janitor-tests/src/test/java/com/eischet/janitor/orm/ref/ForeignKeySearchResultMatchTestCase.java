// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.ref;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ForeignKeySearchResult (e.g. the value of a combo box) used to match no other kind of foreign key, not even the entity it points to,
 * because ForeignKey.matches() and isPointingTo() only compared ForeignKeyInteger/ForeignKeyString/ForeignKeyIdentity. A search result
 * is compared by its ID now, like a ForeignKeyInteger; an empty search result matches nothing, like ForeignKeyNull.
 */
public class ForeignKeySearchResultMatchTestCase extends JanitorTest {

    /**
     * An entity that is its own foreign key, like real entities.
     */
    static class Person implements OrmEntity, ForeignKeyIdentity<Person> {
        private long id;
        private String key;
        private String name;
        private boolean softDeleted;

        Person(final long id, final String key) {
            this.id = id;
            this.key = key;
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

        @Override
        public @NotNull Person getIdentity() {
            return this;
        }

        @Override
        public void writeJson(final JsonOutputStream producer) throws JsonException {
            producer.value(id);
        }
    }

    /**
     * A Dao that only knows its entity class, which is all that matching needs; every other call fails.
     */
    @SuppressWarnings("unchecked")
    private static final Dao<Person> DAO = (Dao<Person>) Proxy.newProxyInstance(
            Dao.class.getClassLoader(),
            new Class<?>[]{Dao.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getEntityClass" -> Person.class;
                case "getEntityClassName" -> Person.class.getSimpleName();
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "PersonDao";
                default -> throw new UnsupportedOperationException(method.getName());
            });

    private static ForeignKeySearchResult<Person> searchResult(final long id) {
        return new ForeignKeySearchResult<>(DAO, id, "P" + id, "Person " + id, false);
    }

    @Test
    public void searchResultMatchesTheEntityItPointsTo() {
        final Person person = new Person(7, "P7");
        assertTrue(ForeignKey.matches(searchResult(7), person));
        assertTrue(ForeignKey.matches(person, searchResult(7)));
        assertTrue(ForeignKey.matchesWithUnknownType(searchResult(7), person));
        assertTrue(ForeignKey.isPointingTo(searchResult(7), person));
        assertTrue(ForeignKey.isPointingToWithUnknownType(searchResult(7), person));
        assertEquals(searchResult(7), person);
    }

    @Test
    public void searchResultDoesNotMatchAnotherEntity() {
        final Person person = new Person(8, "P8");
        assertFalse(ForeignKey.matches(searchResult(7), person));
        assertFalse(ForeignKey.matchesWithUnknownType(searchResult(7), person));
        assertFalse(ForeignKey.isPointingTo(searchResult(7), person));
    }

    @Test
    public void searchResultsAreComparedById() {
        assertTrue(ForeignKey.matches(searchResult(7), searchResult(7)));
        assertFalse(ForeignKey.matches(searchResult(7), searchResult(8)));
        assertEquals(searchResult(7), searchResult(7));
    }

    @Test
    public void searchResultMatchesForeignKeyIntegerWithTheSameId() {
        final ForeignKeyInteger<Person> fk = new ForeignKeyInteger<>(7, DAO);
        assertTrue(ForeignKey.matches(searchResult(7), fk));
        assertTrue(ForeignKey.matches(fk, searchResult(7)));
        assertFalse(ForeignKey.matches(searchResult(8), fk));
        assertEquals(searchResult(7), fk);
        assertEquals(fk, searchResult(7));
        assertEquals(fk.hashCode(), searchResult(7).hashCode(), "equal foreign keys must have equal hash codes");
    }

    @Test
    public void emptySearchResultMatchesNothing() {
        final ForeignKeySearchResult<Person> empty = searchResult(0);
        assertFalse(ForeignKey.matches(empty, searchResult(0)));
        assertFalse(ForeignKey.matches(empty, new Person(0, null)));
        assertFalse(ForeignKey.matches(empty, new ForeignKeyInteger<>(0, DAO)));
    }

}
