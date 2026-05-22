package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.BlockQuote;

public class CMBlockQuote extends CMNode {
    private static final WrapperDispatchTable<BlockQuote> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMBlockQuote(final BlockQuote node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMBlockQuote() {
        this(new BlockQuote());
    }

}
