package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.IndentedCodeBlock;

public class CMIndentedCodeBlock extends CMNode {
    private static final WrapperDispatchTable<IndentedCodeBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        );
    }

    public CMIndentedCodeBlock(final IndentedCodeBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMIndentedCodeBlock() {
        this(new IndentedCodeBlock());
    }
}
