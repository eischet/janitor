package com.eischet.janitor.orm.entity;

import com.eischet.janitor.api.metadata.MetaDataBuilder;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.interop.*;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.sql.ColumnTypeHint;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Root interface for all ORM objects, including entities and joiners.
 * Extends JanitorObject, because we're relying on the scripting language to work with actual object instances.
 */
public interface OrmObject extends JanitorObject {

    // TODO: I'd have thought that IntelliJ should report warnings when nullable/non-nullable methods are mixed, but it doesn't'

    static <X extends JanitorObject> MetaDataBuilder<X> addStringProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter, final int maxLength) {
        return dispatchTable.addStringProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NVARCHAR)
                .setMetaData(JanitorOrm.MetaData.MAX_LENGTH, maxLength);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveLongGetter<X> getter, final PrimitiveLongSetter<X> setter) {
        return dispatchTable.addLongProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addNullableLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, Long> getter, final NullableSetter<X, Long> setter) {
        return dispatchTable.addNullableLongProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addIntegerProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveIntGetter<X> getter, final PrimitiveIntSetter<X> setter) {
        return dispatchTable.addIntegerProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter) {
        return dispatchTable.addStringProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NCLOB);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addDateProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, LocalDate> getter, final NullableSetter<X, LocalDate> setter) {
        return dispatchTable.addDateProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATE);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addDateTimeProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, LocalDateTime> getter, final NullableSetter<X, LocalDateTime> setter) {
        return dispatchTable.addDateTimeProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATETIME);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addBooleanProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveBooleanGetter<X> getter, final PrimitiveBooleanSetter<X> setter) {
        return dispatchTable.addBooleanProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BIT);
    }

    static <X extends JanitorObject> MetaDataBuilder<X> addNullableBooleanProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, Boolean> getter, final NullableSetter<X, Boolean> setter) {
        return dispatchTable.addNullableBooleanProperty(name, getter, setter)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BIT);
    }

}
