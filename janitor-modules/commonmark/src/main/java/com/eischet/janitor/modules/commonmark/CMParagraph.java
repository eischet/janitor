package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Paragraph;

public class CMParagraph extends CMNode {
    private static final WrapperDispatchTable<Paragraph> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMParagraph(final Paragraph node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMParagraph() {
        this(new Paragraph());
    }
}
