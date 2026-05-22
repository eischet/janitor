package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Block;


public class CMBlock extends CMNode {

    private static final WrapperDispatchTable<Block> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMBlock(final Block node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }
}
