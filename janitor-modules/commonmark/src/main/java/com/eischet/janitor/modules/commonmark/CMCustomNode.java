package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.CustomNode;

public class CMCustomNode extends CMNode {
    private static final WrapperDispatchTable<CustomNode> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMCustomNode(final CustomNode node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }
}
