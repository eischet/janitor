package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.HtmlBlock;

public class CMHtmlBlock extends CMNode {
    private static final WrapperDispatchTable<HtmlBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        );
    }

    public CMHtmlBlock(final HtmlBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHtmlBlock() {
        this(new HtmlBlock());
    }
}
