package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Document;

public class CMDocument extends CMNode {
    private static final WrapperDispatchTable<Document> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMDocument(final Document node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMDocument() {
        this(new Document());
    }
}
