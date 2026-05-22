package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.FencedCodeBlock;

public class CMFencedCodeBlock extends CMNode {
    private static final WrapperDispatchTable<FencedCodeBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("fenceCharacter",
            node -> node.janitorGetHostValue().getFenceCharacter(),
            (self, value) -> self.janitorGetHostValue().setFenceCharacter(value)
        );
        dispatch.addNullableIntegerProperty("openingFenceLength",
            node -> node.janitorGetHostValue().getOpeningFenceLength(),
            (self, value) -> self.janitorGetHostValue().setOpeningFenceLength(value)
        );
        dispatch.addNullableIntegerProperty("closingFenceLength",
            node -> node.janitorGetHostValue().getClosingFenceLength(),
            (self, value) -> self.janitorGetHostValue().setClosingFenceLength(value)
        );
        dispatch.addIntegerProperty("fenceIndent",
            node -> node.janitorGetHostValue().getFenceIndent(),
            (self, value) -> self.janitorGetHostValue().setFenceIndent(value)
        );
        dispatch.addStringProperty("info",
            node -> node.janitorGetHostValue().getInfo(),
            (self, value) -> self.janitorGetHostValue().setInfo(value)
        );
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        );
        // TODO: property fenceChar
        dispatch.addIntegerProperty("fenceLength",
            node -> node.janitorGetHostValue().getFenceLength(),
            (self, value) -> self.janitorGetHostValue().setFenceLength(value)
        );
    }

    public CMFencedCodeBlock(final FencedCodeBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMFencedCodeBlock() {
        this(new FencedCodeBlock());
    }
}
