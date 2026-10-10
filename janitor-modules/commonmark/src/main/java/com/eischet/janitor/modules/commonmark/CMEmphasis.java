// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Emphasis;

/** Wrapper for the CommonMark emphasis node. */
public class CMEmphasis extends CMNode {
    private static final WrapperDispatchTable<Emphasis> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("openingDelimiter",
            node -> node.janitorGetHostValue().getOpeningDelimiter());
        dispatch.addStringProperty("closingDelimiter",
            node -> node.janitorGetHostValue().getClosingDelimiter());
    }

    public CMEmphasis(final Emphasis node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMEmphasis() {
        this(new Emphasis());
    }
}
