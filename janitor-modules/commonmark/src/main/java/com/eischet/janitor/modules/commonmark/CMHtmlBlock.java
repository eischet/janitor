// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.HtmlBlock;

/** Wrapper for the CommonMark html block node. */
public class CMHtmlBlock extends CMNode {
    private static final WrapperDispatchTable<HtmlBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "literal: The raw HTML of the block; writable.");
    }

    public CMHtmlBlock(final HtmlBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHtmlBlock() {
        this(new HtmlBlock());
    }
}
