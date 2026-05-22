package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.LinkReferenceDefinition;

public class CMLinkReferenceDefinition extends CMNode {
    private static final WrapperDispatchTable<LinkReferenceDefinition> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("label",
            node -> node.janitorGetHostValue().getLabel(),
            (self, value) -> self.janitorGetHostValue().setLabel(value)
        );
        dispatch.addStringProperty("destination",
            node -> node.janitorGetHostValue().getDestination(),
            (self, value) -> self.janitorGetHostValue().setDestination(value)
        );
        dispatch.addStringProperty("title",
            node -> node.janitorGetHostValue().getTitle(),
            (self, value) -> self.janitorGetHostValue().setTitle(value)
        );
    }

    public CMLinkReferenceDefinition(final LinkReferenceDefinition node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMLinkReferenceDefinition() {
        this(new LinkReferenceDefinition());
    }
}
