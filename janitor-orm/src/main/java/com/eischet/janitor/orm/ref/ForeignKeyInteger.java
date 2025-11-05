package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ForeignKeyInteger<T extends OrmEntity> implements ForeignKey<T> {

    private final long id;
    private final Dao<T> dao;
    private T resolved;

    public ForeignKeyInteger(final long id, final Dao<T> dao) {
        this.id = id;
        this.dao = dao;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean isEmpty() {
        return getId() <= 0;
    }

    @Override
    public boolean janitorIsTrue() {
        return !isEmpty();
    }

    @Override
    public @NotNull Optional<T> resolve() {
        if (isEmpty()) {
            return Optional.empty();
        }
        if (resolved == null) {
            resolved = dao.lazyLoadById(id);
        }
        return Optional.ofNullable(resolved);
    }

    @Override
    public void preResolve(@NotNull DatabaseConnection conn) throws DatabaseError {
        if (resolved == null) {
            resolved = dao.findById(conn, id);
        }
    }

    @Override
    public @NotNull Optional<T> resolve(final @NotNull DatabaseConnection conn) throws DatabaseError {
        preResolve(conn);
        return Optional.ofNullable(resolved);
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
    public String toString() {
        return "ForeignKeyInteger{" +
               "id=" + id +
               ", dao=" + dao +
               ", resolved=" + (resolved != null) +
               '}';
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(id);
    }

    public static <X extends OrmEntity> ForeignKeyInteger<X> createWithForce(long id, final Dao<X> dao) {
        return new ForeignKeyInteger<>(id, dao);
    }

    @Override
    public boolean isNull() {
        return id <= 0;
    }

}
