package com.eischet.janitor.orm.meta;

import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;

public interface JoinWrangler<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity,
        U extends Uplink
        >
        extends Wrangler<J, U> {
}
