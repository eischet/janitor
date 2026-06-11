package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmObject;

public interface EntityChangeListener<T extends OrmObject> {

    enum Type {
        INSERT,
        UPDATE,
        DELETE
    }

    void onChange(Type type, T entity);

}
