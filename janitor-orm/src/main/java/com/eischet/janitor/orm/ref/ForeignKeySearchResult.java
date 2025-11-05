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

public final class ForeignKeySearchResult<T extends OrmEntity, R> implements ForeignKey<T> {

    private final long id;
    private final Dao<T> dao;
    private final R result;
    private T resolved;

    public ForeignKeySearchResult(final long id, final Dao<T> dao, final R result) {
        this.id = id;
        this.dao = dao;
        this.result = result;
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

    public R getResult() {
        return result;
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
    public @NotNull JanitorObject janitorUnpack() {
        if (isEmpty()) {
            return Janitor.NULL;
        } else {
            if (resolved == null) {
                resolved = dao.lazyLoadById(id);
            }
            if (resolved == null) {
                return Janitor.NULL;
            }
            return resolved;
        }
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
    public String toString() {
        return "ForeignKeySearchResult{" +
               "id=" + id +
               ", dao=" + dao +
               ", resolved=" + (resolved != null) +
               ", result=" + result +
               '}';
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(id);
    }

    @Override
    public boolean isNull() {
        return id <= 0;
    }

}
