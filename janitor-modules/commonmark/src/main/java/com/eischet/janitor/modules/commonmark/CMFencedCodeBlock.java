// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.FencedCodeBlock;

/** Wrapper for the CommonMark fenced code block node. */
public class CMFencedCodeBlock extends CMNode {
    private static final WrapperDispatchTable<FencedCodeBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("fenceCharacter",
            node -> node.janitorGetHostValue().getFenceCharacter(),
            (self, value) -> self.janitorGetHostValue().setFenceCharacter(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "fenceCharacter: The character of the fence, \"`\" or \"~\"; writable.");
        dispatch.addNullableIntegerProperty("openingFenceLength",
            node -> node.janitorGetHostValue().getOpeningFenceLength(),
            (self, value) -> self.janitorGetHostValue().setOpeningFenceLength(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "openingFenceLength: The number of fence characters in the opening fence; writable, may be null.");
        dispatch.addNullableIntegerProperty("closingFenceLength",
            node -> node.janitorGetHostValue().getClosingFenceLength(),
            (self, value) -> self.janitorGetHostValue().setClosingFenceLength(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "closingFenceLength: The number of fence characters in the closing fence; writable, may be null.");
        dispatch.addIntegerProperty("fenceIndent",
            node -> node.janitorGetHostValue().getFenceIndent(),
            (self, value) -> self.janitorGetHostValue().setFenceIndent(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "fenceIndent: The indentation of the opening fence, in spaces; writable.");
        dispatch.addStringProperty("info",
            node -> node.janitorGetHostValue().getInfo(),
            (self, value) -> self.janitorGetHostValue().setInfo(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "info: The info string after the opening fence, usually the language of the code; writable.");
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "literal: The code inside the block; writable.");
        // TODO: property fenceChar
        dispatch.addIntegerProperty("fenceLength",
            node -> node.janitorGetHostValue().getFenceLength(),
            (self, value) -> self.janitorGetHostValue().setFenceLength(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "fenceLength: The number of fence characters in the opening fence; writable.");
    }

    public CMFencedCodeBlock(final FencedCodeBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMFencedCodeBlock() {
        this(new FencedCodeBlock());
    }
}
