package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.function.Function;

public class RegularDispatchTable<T extends JanitorObject> extends GenericDispatchTable<T> {

    public RegularDispatchTable() {
    }

    public <P extends JanitorObject> RegularDispatchTable(final Dispatcher<P> parent, final Function<T, P> caster) {
        super(parent, caster);
    }

}
