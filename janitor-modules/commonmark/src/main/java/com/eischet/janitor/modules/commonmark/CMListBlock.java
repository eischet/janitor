package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.ListBlock;

public abstract class CMListBlock extends CMNode {
    protected static final WrapperDispatchTable<ListBlock> dispatch = new WrapperDispatchTable<>();

    static {
        dispatch.addBooleanProperty("tight",
            node -> node.janitorGetHostValue().isTight(),
            (self, value) -> self.janitorGetHostValue().setTight(value)
        );
    }

    public CMListBlock(final ListBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMListBlock(final Dispatcher dispatcher, final ListBlock node) {
        super(dispatcher, node);
    }
}
