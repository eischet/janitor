package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmObject;

import java.util.List;

public interface DaoLogging {

    void lazyLoadedForeignKey(String entityClass, Object identifier, OrmObject result);

    void lazyLoadedAssociation(String entityClass, String keyColumn, long parentId, List<? extends OrmEntity> results);
}
