package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.StrongEmphasis;

public class CMStrongEmphasis extends CMNode {
    private static final WrapperDispatchTable<StrongEmphasis> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("openingDelimiter",
            node -> node.janitorGetHostValue().getOpeningDelimiter());
        dispatch.addStringProperty("closingDelimiter",
            node -> node.janitorGetHostValue().getClosingDelimiter());
    }

    public CMStrongEmphasis(final StrongEmphasis node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMStrongEmphasis() {
        this(new StrongEmphasis());
    }
}
