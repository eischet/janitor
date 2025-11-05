package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;

import java.util.List;

public interface JoinManager<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity
        > {

    List<J> getRightJoins(L left);
    List<J> getLeftJoins(R right);

}
