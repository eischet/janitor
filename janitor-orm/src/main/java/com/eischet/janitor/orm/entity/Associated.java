package com.eischet.janitor.orm.entity;

/**
 * Marker interface for collections of entities that are associated with another entity.
 * @param <T> the type of the associated entities
 */
public interface Associated<T extends OrmObject> {
}
