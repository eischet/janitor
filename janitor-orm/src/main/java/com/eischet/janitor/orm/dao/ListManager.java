package com.eischet.janitor.orm.dao;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.orm.entity.OrmEntity;

/**
 * Manages lists of entities that belong to another entity.
 * @param <T> the type of the owning entity
 * @param <E> the type of the list elements
 */
public interface ListManager<T extends OrmEntity, E extends JanitorObject> {
}
