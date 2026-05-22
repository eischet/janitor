package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Heading;

public class CMHeading extends CMNode {
    private static final WrapperDispatchTable<Heading> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addIntegerProperty("level",
            node -> node.janitorGetHostValue().getLevel(),
            (self, value) -> self.janitorGetHostValue().setLevel(value)
        );
    }

    public CMHeading(final Heading node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHeading() {
        this(new Heading());
    }
}
