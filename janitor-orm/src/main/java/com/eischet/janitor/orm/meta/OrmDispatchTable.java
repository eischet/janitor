package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.MetaDataBuilder;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.interop.*;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmObject;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * A {@link DispatchTable} that knows about its ORM class: the Java class, how to create new instances,
 * and the column-backed property helpers that used to be static methods on {@link OrmObject}.
 * This replaces the separate "wrangler" object; the table <i>is</i> the wrangler.
 *
 * @param <T> any type of ORM object
 * @param <U> type of uplink, that is an object that contains your DAOs
 */
public class OrmDispatchTable<T extends OrmObject, U extends Uplink> extends DispatchTable<T> implements Wrangler<T, U> {

    protected final @NotNull Class<T> wrangledClass;
    protected final @NotNull Function<U, T> constructor;

    public OrmDispatchTable(final @NotNull Class<T> wrangledClass, final @NotNull Function<U, T> constructor) {
        super(null, true);
        this.wrangledClass = wrangledClass;
        this.constructor = constructor;
        setMetaData(Janitor.MetaData.CLASS, wrangledClass.getSimpleName());
    }

    /**
     * Create a child table for {@link #extend(boolean)}; class and constructor are taken from the parent.
     */
    protected OrmDispatchTable(final @NotNull OrmDispatchTable<T, U> parent, final boolean includeApplyMethod) {
        super(parent, it -> it, includeApplyMethod);
        this.wrangledClass = parent.wrangledClass;
        this.constructor = parent.constructor;
    }

    @Override
    public OrmDispatchTable<T, U> extend(final boolean includeApplyMethod) {
        return new OrmDispatchTable<>(this, includeApplyMethod);
    }

    @Override
    public OrmDispatchTable<T, U> extend() {
        return extend(true);
    }

    @Override
    public @NotNull Class<T> getWrangledClass() {
        return wrangledClass;
    }

    @Override
    public @NotNull String getSimpleClassName() {
        return wrangledClass.getSimpleName();
    }

    @Override
    public @NotNull DispatchTable<T> getDispatchTable() {
        return this;
    }

    @Override
    public @NotNull T createNewInstance(final @NotNull U uplink) {
        return constructor.apply(uplink);
    }

    public @NotNull Function<U, T> getUplinkConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + wrangledClass.getSimpleName() + "}";
    }

    // column-backed properties, see OrmObject for the implementation

    public MetaDataBuilder<T> addStringColumn(final String name, final String column, final NullableGetter<T, String> getter, final NullableSetter<T, String> setter, final int maxLength) {
        return OrmObject.addStringProperty(this, name, column, getter, setter, maxLength);
    }

    public MetaDataBuilder<T> addTextColumn(final String name, final String column, final NullableGetter<T, String> getter, final NullableSetter<T, String> setter) {
        return OrmObject.addTextProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addLongColumn(final String name, final String column, final PrimitiveLongGetter<T> getter, final PrimitiveLongSetter<T> setter) {
        return OrmObject.addLongProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addNullableLongColumn(final String name, final String column, final NullableGetter<T, Long> getter, final NullableSetter<T, Long> setter) {
        return OrmObject.addNullableLongProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addIntegerColumn(final String name, final String column, final PrimitiveIntGetter<T> getter, final PrimitiveIntSetter<T> setter) {
        return OrmObject.addIntegerProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addDateColumn(final String name, final String column, final NullableGetter<T, LocalDate> getter, final NullableSetter<T, LocalDate> setter) {
        return OrmObject.addDateProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addDateTimeColumn(final String name, final String column, final NullableGetter<T, LocalDateTime> getter, final NullableSetter<T, LocalDateTime> setter) {
        return OrmObject.addDateTimeProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addBooleanColumn(final String name, final String column, final PrimitiveBooleanGetter<T> getter, final PrimitiveBooleanSetter<T> setter) {
        return OrmObject.addBooleanProperty(this, name, column, getter, setter);
    }

    public MetaDataBuilder<T> addNullableBooleanColumn(final String name, final String column, final NullableGetter<T, Boolean> getter, final NullableSetter<T, Boolean> setter) {
        return OrmObject.addNullableBooleanProperty(this, name, column, getter, setter);
    }

}
