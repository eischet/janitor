package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ForeignKeyString<T extends OrmEntity> implements ForeignKey<T> {

    private final @NotNull String key;
    private final @NotNull Dao<T> dao;
    private T resolved;

    public ForeignKeyString(final @NotNull String key, final @NotNull Dao<T> dao) {
        this.key = key;
        this.dao = dao;
    }

    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @NotNull String getReferencedEntityClassName() {
        return dao.getEntityClassName();
    }

    @Override
    public @NotNull Class<T> getReferencedEntityClass() {
        return dao.getEntityClass();
    }

    @Override
    public long getId() {
        return resolve().map(OrmEntity::getId).orElse(0L);
    }

    @Override
    public @NotNull Optional<T> resolve() {
        if (key.isBlank()) {
            return Optional.empty();
        }
        if (resolved == null) {
            resolved = dao.lazyLoadByKey(key);
        }
        return Optional.ofNullable(resolved);
    }


    @Override
    public @NotNull Optional<T> resolve(final @NotNull DatabaseConnection conn) throws DatabaseError {
        if (resolved == null) {
            resolved = dao.findByKey(conn, key);
        }
        return Optional.ofNullable(resolved);
    }

    @Override
    public void preResolve(@NotNull final DatabaseConnection conn) throws DatabaseError {
        resolved = dao.findByKey(conn, key);
    }

    @Override
    public @NotNull JanitorObject janitorUnpack() {
        if (resolved == null) {
            resolved = dao.lazyLoadByKey(key);
        }
        return Janitor.nullableObject(resolved);
    }


    @Override
    public String toString() {
        return "ForeignKeyString{" +
               "key=" + key +
               ", dao=" + dao +
               ", resolved=" + (resolved != null) +
               '}';
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(key);
    }

    public static <X extends OrmEntity> ForeignKeyString<X> createWithForce(String key, final Dao<X> dao) {
        return new ForeignKeyString<>(key, dao);
    }

    @Override
    public boolean isNull() {
        return key.isEmpty();
    }

}
