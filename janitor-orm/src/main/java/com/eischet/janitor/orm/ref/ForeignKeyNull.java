package com.eischet.janitor.orm.ref;


import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A null-pointer to a foreign key.
 * You are encouraged to create one instance of this class per entity class, e.g.: {@code public static final ForeignKeyNull<MisoBranch> NULL = new ForeignKeyNull<>();}.
 * This can be more readable than creating dummy instances where needed, and will conserve memory.
 * @param <T> some entity class.
 */
public final class ForeignKeyNull<T extends OrmEntity> implements ForeignKey<T> {

    /**
     * Returns a pointer to this object.
     * This comes in handy when you need a factory method for NULL objects, e.g.: {@code MisoBranch.NULL::pointer} instead of {@code () -> new ForeignKeyNull<>()}.
     * @return a pointer to this object
     */
    public ForeignKeyNull<T> pointer() {
        return this;
    }

    @Override
    public @NotNull JanitorObject janitorUnpack() {
        return Janitor.NULL;
    }

    @Override
    public @NotNull Optional<T> resolve(@NotNull final DatabaseConnection conn) throws DatabaseError {
        return Optional.empty();
    }

    @Override
    public void preResolve(@NotNull final DatabaseConnection conn) throws DatabaseError {
    }

    @Override
    public @Nullable Class<T> getReferencedEntityClass() {
        return null;
    }

    @Override
    public @NotNull String getReferencedEntityClassName() {
        return "null";
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean janitorIsTrue() {
        return false;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public @NotNull Optional<T> resolve() {
        return Optional.empty();
    }


}
