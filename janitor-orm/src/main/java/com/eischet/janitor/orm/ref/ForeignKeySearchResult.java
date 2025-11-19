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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public non-sealed class ForeignKeySearchResult<T extends OrmEntity> implements ForeignKey<T>, OrmEntity {

    protected final long id;
    protected final String key;
    protected final String name;
    protected final boolean softDeleted;

    private final Dao<T> dao;

    public ForeignKeySearchResult(final Dao<T> dao, final long id, final String key, final String name, final boolean softDeleted) {
        this.id = id;
        this.dao = dao;
        this.key = key;
        this.name = name;
        this.softDeleted = softDeleted;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        // ignore
    }

    public boolean isEmpty() {
        return getId() <= 0;
    }

    @Override
    public boolean janitorIsTrue() {
        return !isEmpty();
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
        return Optional.ofNullable(dao.lazyLoadById(id));
    }

    @Override
    public @NotNull JanitorObject janitorUnpack() {
        if (isEmpty()) {
            return Janitor.NULL;
        } else {
            return Janitor.nullableObject(dao.lazyLoadById(id));
        }
    }


    @Override
    public void preResolve(@NotNull DatabaseConnection conn) throws DatabaseError {
    }

    @Override
    public @NotNull Optional<T> resolve(final @NotNull DatabaseConnection conn) throws DatabaseError {
        return Optional.ofNullable(dao.lazyLoadById(id));
    }

    @Override
    public String toString() {
        return "ForeignKeySearchResult{" +
               "id=" + id +
               ", dao=" + dao +
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

    @Override
    public @Nullable String getKey() {
        return key;
    }

    @Override
    public void setKey(final String key) {
        // ignore
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        // ignore
    }

    @Override
    public boolean isSoftDeleted() {
        return softDeleted;
    }

    @Override
    public void setSoftDeleted(final boolean softDeleted) {
        // ignore
    }

    public Dao<T> getDao() {
        return dao;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ForeignKeySearchResult<?> that)) return false;
        return id == that.id && Objects.equals(dao, that.dao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dao);
    }
}
