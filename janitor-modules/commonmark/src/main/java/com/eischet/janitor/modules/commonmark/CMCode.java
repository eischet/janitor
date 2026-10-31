// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Code;

/** Wrapper for the CommonMark code node. */
public class CMCode extends CMNode {
    private static final WrapperDispatchTable<Code> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("literal",
            node -> node.janitorGetHostValue().getLiteral(),
            (self, value) -> self.janitorGetHostValue().setLiteral(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "literal: The text of the inline code span; writable.");
    }

    public CMCode(final Code node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMCode() {
        this(new Code());
    }
}
