package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.SoftLineBreak;

public class CMSoftLineBreak extends CMNode {
    private static final WrapperDispatchTable<SoftLineBreak> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMSoftLineBreak(final SoftLineBreak node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMSoftLineBreak() {
        this(new SoftLineBreak());
    }
}
