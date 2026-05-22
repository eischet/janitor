package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.HardLineBreak;

public class CMHardLineBreak extends CMNode {
    private static final WrapperDispatchTable<HardLineBreak> dispatch = new WrapperDispatchTable<>();

    public CMHardLineBreak(final HardLineBreak node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHardLineBreak() {
        this(new HardLineBreak());
    }
}
