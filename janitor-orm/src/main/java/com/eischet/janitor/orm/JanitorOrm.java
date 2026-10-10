// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm;

import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.orm.sql.ColumnCase;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/** Namespace for the ORM support: it holds the meta-data keys that describe how entities map to database tables and columns. */
public final class JanitorOrm {


    /** The meta-data keys used by the ORM, which are attached to dispatch tables and their properties. */
    public static class MetaData {
        /**
         * A hint for the column type that should be used
         */
        public static MetaDataKey<ColumnTypeHint> COLUMN_TYPE = new MetaDataKey<>("column_type", ColumnTypeHint.class);

        /**
         * Declares that a text column only ever holds upper-case (or lower-case) values, which lets filters
         * compare case-insensitively without folding the column; see {@link ColumnCase}. Absent means mixed case.
         */
        public static MetaDataKey<ColumnCase> COLUMN_CASE = new MetaDataKey<>("column_case", ColumnCase.class);

        public static MetaDataKey<String> ID_SEQUENCE = new MetaDataKey<>("id_sequence", String.class);

        public static MetaDataKey<Integer> MAX_LENGTH = new MetaDataKey<>("max_length", Integer.class);

        /**
         * Marks a column as lazily loaded: {@code GenericDao} excludes it from the default SELECT column
         * list (findById/findByKey/findAll/findByFilter/findByAssociation), so it's fetched from the
         * database only when the corresponding {@link com.eischet.janitor.orm.entity.LazyLoadedString}
         * field is actually read, not up front with the rest of the row. Set via
         * {@link com.eischet.janitor.orm.entity.OrmObject#addLazyTextProperty}. INSERT/UPDATE are
         * unaffected — they still write the column's current value like any other property.
         */
        public static MetaDataKey<Boolean> LAZY_LOAD = new MetaDataKey<>("lazy_load", Boolean.class);

        // public static MetaDataKey<String> OUTWARD_FOREIGN_KEYS = new MetaDataKey<>("outward_foreign_keys", String.class);

        /**
         * The column name for an object property, in case you're working with a database.
         */
        public static MetaDataKey<String> COLUMN_NAME = new MetaDataKey<>("column_name", String.class);

        /**
         * The database table name for an object.
         */
        public static MetaDataKey<String> TABLE_NAME = new MetaDataKey<>("table_name", String.class);

        /**
         * The column name of an ID field for an object, e.g. "person_id".
         */
        public static MetaDataKey<String> ID_FIELD = new MetaDataKey<>("id_field", String.class);

        /**
         * The column name of a KEY field for an object, e.g. "person_key".
         */
        public static MetaDataKey<String> KEY_FIELD = new MetaDataKey<>("key_field", String.class);

        /**
         * The column name of a Name field for an object, e.g. "person_name".
         */
        public static MetaDataKey<String> NAME_FIELD = new MetaDataKey<>("name_field", String.class);

        /**
         * Names those fields that are part of the primary key of the join table.
         */
        public static MetaDataKey<StringList> JOIN_TABLE_PK = new MetaDataKey<>("join_table_pk", StringList.class);

        /**
         * The version range of the object.
         *
         * This can be used to adjust to schema differences automatically, most useful when you do not have control over a schema, e.g.
         * when accessing third party schemas.
         */
        public static MetaDataKey<VersionRange> VERSION_RANGE = new MetaDataKey<>("version_range", VersionRange.class);

        // Work around the situation that we cannot pass List<String>.class nor List.class to new MetaDataKey.... LOL
        /** A list of strings that can be used as a value of a {@link MetaDataKey}, which cannot be declared for a generic list type. */
        public static class StringList extends ArrayList<String> {
            private StringList(@NotNull final Collection<? extends String> c) {
                super(c);
            }
            /**
             * @param elements the elements
             * @return a list with the given elements
             */
            public static StringList of(@NotNull final String... elements) {
                return new StringList(List.of(elements));
            }
            /**
             * @param c the elements
             * @return a list with the given elements
             */
            public static StringList of(@NotNull final Collection<? extends String> c) {
                return new StringList(c);
            }
            /**
             * @return an empty list
             */
            public static StringList of() {
                return new StringList(new LinkedList<>());
            }
        }


    }

    /** Reserved for a fluent configuration API; it has no functionality yet. */
    public static final class Builder {


    }


    private JanitorOrm() {
    }

}
