package com.eischet.janitor.orm.entity;

/**
 * Base interface for all ORM entities, which are represented as tables in the database.
 * We assume, as a lowest common denominator, that all entities have a primary key of type long and a unique key of type string.
 */
public interface OrmEntity extends OrmObject {
    long getId();
    void setId(long id);
    String getKey();
    void setKey(String key);
}
