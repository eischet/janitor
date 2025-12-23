package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Base interface for all ORM entities, which are represented as tables in the database.
 * As a lowest common denominator, we assume that entities should have the following fields/columns:
 * <ul>
 *     <li>a unique "id", represented a a long, e.g. person_id</li>
 *     <li>a unique "key", a short name of the object that can be used to look it up, e.g. a login</li>
 *     <li>a non-unique "name", as a more descriptive label that the object carries, e.g. a person's full name</li>
 *     <li>a flag that tells us if an entity has been soft-deleted</li>
 * </ul>
 */
public interface OrmEntity extends OrmObject {
    /**
     * Returns the unique ID of the entity.
     * @return the unique ID of the entity
     */
    long getId();

    /**
     * Sets the unique ID of the entity.
     * This should be called by the ORM framework only because it's assumed that database sequences are used to generated these keys.
     * @param id the unique ID of the entity
     */
    void setId(long id);

    /**
     * Gets the lookup key of the entity.
     * @return the lookup key of the entity
     */
    @Nullable String getKey();

    /**
     * Sets the lookup key of the entity.
     * @param key the lookup key of the entity
     */
    void setKey(String key);

    /**
     * Gets the name of the entity.
     * @return the name of the entity
     */
    @Nullable String getName();

    /**
     * Sets the name of the entity.
     * @param name the name of the entity
     */
    void setName(String name);

    /**
     * Returns true if the entity has been soft-deleted.
     * @return true if the entity has been soft-deleted
     */
    boolean isSoftDeleted();

    /**
     * Sets the soft-deleted flag of the entity.
     * @param softDeleted the soft-deleted flag of the entity
     */
    void setSoftDeleted(boolean softDeleted);

    /**
     * Returns the key, or "" if the key is null.
     * This can useful in situations where a non-null value is needed, like sorting and some UI frameworks.
     * @return the key, or "" if the key is null
     */
    default @NotNull String coalesceKey() {
        return coalesceKey("");
    }

    /**
     * Returns the key, or the default value if the key is null.
     * This can useful in situations where a non-null value is needed, like sorting and some UI frameworks.
     * @param defaultValue the default value to return if the key is null
     * @return the key, or the default value if the key is null
     */
    default @NotNull String coalesceKey(@NotNull String defaultValue) {
        final var key = getKey();
        return key == null ? defaultValue : key;
    }

    /**
     * Returns the name, or "" if the name is null.
     * This can useful in situations where a non-null value is needed, like sorting and some UI frameworks.
     * @return the name, or "" if the name is null
     */
    default @NotNull String coalesceName() {
        return coalesceName("");
    }

    /**
     * Returns the name, or the default value if the name is null.
     * This can useful in situations where a non-null value is needed, like sorting and some UI frameworks.
     * @param defaultValue the default value to return if the name is null
     * @return the name, or the default value if the name is null
     */
    default @NotNull String coalesceName(String defaultValue) {
        final var name = getName();
        return name == null ? defaultValue : name;
    }

    /**
     * A comparator that compares entities by their ID.
     */
    Comparator<? super @NotNull OrmEntity> COMPARE_BY_ID = Comparator.comparingLong(OrmEntity::getId);

    /**
     * A comparator that compares entities by their key.
     */
    Comparator<? super @NotNull OrmEntity> COMPARE_BY_KEY = Comparator.comparing(OrmEntity::coalesceKey);

    /**
     * A comparator that compares entities by their name.
     */
    Comparator<? super @NotNull OrmEntity> COMPARE_BY_NAME = Comparator.comparing(OrmEntity::coalesceName);

    /**
     * Called by the ORM layer before updating this entity in the database.
     * If you need to do anything special before that happens, override this method.
     * You can even abort the update by throwing a DatabaseError.
     * @throws DatabaseError when you see fit to do so
     */
    default void beforeUpdate() throws DatabaseError {
    }

    /**
     * Called by the ORM layer before inserting this entity in the database.
     * If you need to do anything special before that happens, override this method.
     * You can even abort the insertion by throwing a DatabaseError.
     * @throws DatabaseError when you see fit to do so
     */
    default void beforeInsert() throws DatabaseError {
    }

    /**
     * Called by the ORM layer before deleting this entity from the database.
     * If you need to do anything special before that happens, override this method.
     * You can even abort the deletion by throwing a DatabaseError.
     * @throws DatabaseError when you see fit to do so
     */
    default void beforeDelete() throws DatabaseError {
    }

}
