package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.ThematicBreak;

public class CMThematicBreak extends CMNode {
    private static final WrapperDispatchTable<ThematicBreak> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        );
    }

    public CMThematicBreak(final ThematicBreak node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMThematicBreak() {
        this(new ThematicBreak());
    }
}
