package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmObject;

/**
 * Notified when a DAO inserts, updates or deletes an entity.
 * @param <T> the type of the entities
 */
public interface EntityChangeListener<T extends OrmObject> {

    enum Type {
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * Called after an entity was inserted, updated or deleted.
     * @param type the kind of change
     * @param entity the entity
     */
    void onChange(Type type, T entity);

}
