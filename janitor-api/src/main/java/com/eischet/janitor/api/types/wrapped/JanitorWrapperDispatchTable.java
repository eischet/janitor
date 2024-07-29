package com.eischet.janitor.api.types.wrapped;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;

import java.util.function.Function;

public class JanitorWrapperDispatchTable<T> extends GenericDispatchTable<JanitorWrapper<T>> {

    public JanitorWrapperDispatchTable() {
    }

    public <P extends JanitorObject> JanitorWrapperDispatchTable(final Dispatcher<P> parent, final Function<JanitorWrapper<T>, P> caster) {
        super(parent, caster);
    }
}
