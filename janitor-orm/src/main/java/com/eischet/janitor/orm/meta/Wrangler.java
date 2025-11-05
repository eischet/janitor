package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmObject;
import org.jetbrains.annotations.NotNull;

public interface Wrangler<T extends OrmObject, U extends Uplink> {
    @NotNull Class<T> getWrangledClass();
    @NotNull String getSimpleClassName();
    @NotNull DispatchTable<T> getDispatchTable();
    @NotNull T createNewInstance(final @NotNull U uplink);
}
