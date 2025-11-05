package com.eischet.janitor.orm;

import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.orm.meta.WranglerSource;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class JanitorOrm {


    public static class MetaData {
        /**
         * A hint for the column type that should be used
         */
        public static MetaDataKey<ColumnTypeHint> COLUMN_TYPE = new MetaDataKey<>("column_type", ColumnTypeHint.class);

        public static MetaDataKey<String> ID_SEQUENCE = new MetaDataKey<>("id_sequence", String.class);

        public static MetaDataKey<Integer> MAX_LENGTH = new MetaDataKey<>("max_length", Integer.class);

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

        // Work around the situation that we cannot pass List<String>.class nor List.class to new MetaDataKey.... LOL
        public static class StringList extends ArrayList<String> {
            private StringList(@NotNull final Collection<? extends String> c) {
                super(c);
            }
            public static StringList of(@NotNull final String... elements) {
                return new StringList(List.of(elements));
            }
            public static StringList of(@NotNull final Collection<? extends String> c) {
                return new StringList(c);
            }
            public static StringList of() {
                return new StringList(new LinkedList<>());
            }
        }


        public static MetaDataKey<WranglerSource> WRANGLER = new MetaDataKey<>("wrangler", WranglerSource.class);

    }

    public static final class Builder {


    }


    private JanitorOrm() {
    }

}
