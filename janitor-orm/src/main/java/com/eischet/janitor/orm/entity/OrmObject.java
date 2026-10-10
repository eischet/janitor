package com.eischet.janitor.orm.entity;

import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.interop.*;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.sql.ColumnTypeHint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Root interface for all ORM objects, including entities and joiners.
 * Extends JanitorObject, because we're relying on the scripting language to work with actual object instances.
 */
public interface OrmObject extends JanitorObject {

    // TODO: I'd have thought that IntelliJ should report warnings when nullable/non-nullable methods are mixed, but it doesn't'

    /**
     * A string in a regular (database character set) VARCHAR column.
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addStringProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter, final int maxLength) {
        return OrmPropertyHandle.of(dispatchTable.addStringProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.VARCHAR)
                .setMetaData(JanitorOrm.MetaData.MAX_LENGTH, maxLength);
    }

    /**
     * A string in a national character set NVARCHAR column.
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addNationalStringProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter, final int maxLength) {
        return OrmPropertyHandle.of(dispatchTable.addStringProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NVARCHAR)
                .setMetaData(JanitorOrm.MetaData.MAX_LENGTH, maxLength);
    }

    /**
     * Adds a long property that is stored in an integer column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Long> addLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveLongGetter<X> getter, final PrimitiveLongSetter<X> setter) {
        return OrmPropertyHandle.of(dispatchTable.addLongProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    /**
     * Adds a nullable long property that is stored in an integer column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Long> addNullableLongProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, Long> getter, final NullableSetter<X, Long> setter) {
        return OrmPropertyHandle.of(dispatchTable.addNullableLongProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    /**
     * Adds an integer property that is stored in an integer column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Integer> addIntegerProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveIntGetter<X> getter, final PrimitiveIntSetter<X> setter) {
        return OrmPropertyHandle.of(dispatchTable.addIntegerProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    /**
     * A long text in a regular (database character set) CLOB column.
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter) {
        return OrmPropertyHandle.of(dispatchTable.addStringProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.CLOB);
    }

    /**
     * A long text in a national character set NCLOB column (e.g. Assyst on Oracle).
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addNationalTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, String> getter, final NullableSetter<X, String> setter) {
        return OrmPropertyHandle.of(dispatchTable.addStringProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.NCLOB);
    }

    /**
     * Like {@link #addTextProperty}, but for a field backed by a {@link LazyLoadedString} instead of a
     * plain {@code String} — the value isn't fetched with the rest of the row; {@code GenericDao} leaves
     * this column out of its default SELECT entirely (see {@link JanitorOrm.MetaData#LAZY_LOAD}), and it's
     * loaded only when something actually reads it, via {@code LazyLoadedString.getValue()}.
     * <p>
     * The script-facing property registered here is still a plain string, exactly like
     * {@link #addTextProperty} — {@code accessor} just tells this how to reach the entity's
     * {@link LazyLoadedString} field to read from / write to. A typical entity looks like:
     * <pre>{@code
     * protected final LazyLoadedString configJson = new LazyLoadedString(
     *         () -> getSource().getFooDao(), this::getId, "config_json");
     *
     * public String getConfigJson() { return configJson.getValue(); }
     * public void setConfigJson(final String value) { configJson.setValue(value); }
     * // ... addLazyTextProperty(DISPATCH, "configJson", "config_json", Foo::getConfigJson, Foo::setConfigJson)
     * }</pre>
     * — i.e. the entity's own getter/setter keep the ordinary {@code String} signature, unchanged for
     * both Java-side and script-side callers; only the registration call and the field itself differ from
     * {@link #addTextProperty}.
     *
     * @param accessor reaches into the entity to get its {@link LazyLoadedString} field
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addLazyTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, LazyLoadedString> accessor) {
        return addLazyTextProperty(dispatchTable, name, column, accessor, ColumnTypeHint.CLOB);
    }

    /**
     * Like {@link #addLazyTextProperty}, but for a national character set NCLOB column.
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, String> addLazyNationalTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, LazyLoadedString> accessor) {
        return addLazyTextProperty(dispatchTable, name, column, accessor, ColumnTypeHint.NCLOB);
    }

    /**
     * Adds a lazily loaded text property that is stored in a column of the given type.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param accessor gets the lazily loaded string of an object
     * @param columnTypeHint the type of the column, i.e. CLOB or NCLOB
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    private static <X extends JanitorObject> OrmPropertyHandle<X, String> addLazyTextProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final Function<X, LazyLoadedString> accessor, final ColumnTypeHint columnTypeHint) {
        return OrmPropertyHandle.of(dispatchTable.addStringProperty(name, x -> accessor.apply(x).getValue(), (x, v) -> accessor.apply(x).setValue(v)))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, columnTypeHint)
                .setMetaData(JanitorOrm.MetaData.LAZY_LOAD, Boolean.TRUE);
    }

    /**
     * Adds a date property that is stored in a DATE column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, LocalDate> addDateProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, LocalDate> getter, final NullableSetter<X, LocalDate> setter) {
        return OrmPropertyHandle.of(dispatchTable.addDateProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATE);
    }

    /**
     * Adds a datetime property that is stored in a datetime column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, LocalDateTime> addDateTimeProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, LocalDateTime> getter, final NullableSetter<X, LocalDateTime> setter) {
        return OrmPropertyHandle.of(dispatchTable.addDateTimeProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DATETIME);
    }

    /**
     * Adds a boolean property that is stored in a bit column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Boolean> addBooleanProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveBooleanGetter<X> getter, final PrimitiveBooleanSetter<X> setter) {
        return OrmPropertyHandle.of(dispatchTable.addBooleanProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BIT);
    }

    /**
     * Like {@link #addBooleanProperty}, but for schemas that store the flag as a {@code "y"}/{@code "n"}
     * character column instead of a native boolean/numeric one. See {@link ColumnTypeHint#BOOL_CHAR}.
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Boolean> addBoolCharProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final PrimitiveBooleanGetter<X> getter, final PrimitiveBooleanSetter<X> setter) {
        return OrmPropertyHandle.of(dispatchTable.addBooleanProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BOOL_CHAR);
    }

    /**
     * Adds a nullable boolean property that is stored in a bit column.
     * @param dispatchTable the dispatch table to add the property to
     * @param name the name of the property
     * @param column the name of the database column
     * @param getter reads the property
     * @param setter writes the property
     * @param <X> the type of the object
     * @return a handle for adding meta-data to the property
     */
    static <X extends JanitorObject> OrmPropertyHandle<X, Boolean> addNullableBooleanProperty(final DispatchTable<X> dispatchTable, final String name, final String column, final NullableGetter<X, Boolean> getter, final NullableSetter<X, Boolean> setter) {
        return OrmPropertyHandle.of(dispatchTable.addNullableBooleanProperty(name, getter, setter))
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, column)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.BIT);
    }

}
