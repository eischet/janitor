// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Emphasis;

/** Wrapper for the CommonMark emphasis node. */
public class CMEmphasis extends CMNode {
    private static final WrapperDispatchTable<Emphasis> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("openingDelimiter",
            node -> node.janitorGetHostValue().getOpeningDelimiter())
                .setMetaData(Janitor.MetaData.HELP, "openingDelimiter: The delimiter that opens the emphasis, \"*\" or \"_\"; read-only.");
        dispatch.addStringProperty("closingDelimiter",
            node -> node.janitorGetHostValue().getClosingDelimiter())
                .setMetaData(Janitor.MetaData.HELP, "closingDelimiter: The delimiter that closes the emphasis, \"*\" or \"_\"; read-only.");
    }

    public CMEmphasis(final Emphasis node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMEmphasis() {
        this(new Emphasis());
    }
}
