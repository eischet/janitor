package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.HtmlInline;

public class CMHtmlInline extends CMNode {
    private static final WrapperDispatchTable<HtmlInline> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        );
    }

    public CMHtmlInline(final HtmlInline node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHtmlInline() {
        this(new HtmlInline());
    }
}
