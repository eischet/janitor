package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Text;

public class CMText extends CMNode {

    private static final WrapperDispatchTable<Text> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            text -> text.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
            );
    }

    public CMText(final Text text) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), text);
    }

    public CMText() {
        this(new Text());
    }
}
