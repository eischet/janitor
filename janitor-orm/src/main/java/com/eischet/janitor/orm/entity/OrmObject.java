package com.eischet.janitor.orm.entity;

import com.eischet.janitor.api.metadata.MetaDataBuilder;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Root interface for all ORM objects, including entities and joiners.
 * Extends JanitorObject, because we're relying on the scripting language to work with actual object instances.
 */
public interface OrmObject extends JanitorObject {

    // TODO: I'd have thought that IntelliJ should report warnings when nullable/non-nullable methods are mixed, but it doesn't'

    static <X extends JanitorObject> MetaDataBuilder<X> addStringProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, String> getter, final BiConsumer<X, String> setter, final int maxLength) {
        return dispatchTable.addStringProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NVARCHAR)
                .setMetaData(JanitorOrm.MetaData.MAX_LENGTH, maxLength);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, @NotNull Long> getter, final BiConsumer<X, @NotNull Long> setter) {
        return dispatchTable.addLongProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addNullableLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, @Nullable Long> getter, final BiConsumer<X, @Nullable Long> setter) {
        return dispatchTable.addNullableLongProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addIntegerProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, Integer> getter, final BiConsumer<X, Integer> setter) {
        return dispatchTable.addIntegerProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, String> getter, final BiConsumer<X, String> setter) {
        return dispatchTable.addStringProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NCLOB);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addDateProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, LocalDate> getter, final BiConsumer<X, LocalDate> setter) {
        return dispatchTable.addDateProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATE);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addDateTimeProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, LocalDateTime> getter, final BiConsumer<X, LocalDateTime> setter) {
        return dispatchTable.addDateTimeProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATETIME);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addBooleanProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, Boolean> getter, final BiConsumer<X, Boolean> setter) {
        return dispatchTable.addBooleanProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BIT);
    }

}
