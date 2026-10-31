// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.StrongEmphasis;

/** Wrapper for the CommonMark strong emphasis node. */
public class CMStrongEmphasis extends CMNode {
    private static final WrapperDispatchTable<StrongEmphasis> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("openingDelimiter",
            node -> node.janitorGetHostValue().getOpeningDelimiter())
                .setMetaData(Janitor.MetaData.HELP, "openingDelimiter: The delimiter that opens the strong emphasis, \"**\" or \"__\"; read-only.");
        dispatch.addStringProperty("closingDelimiter",
            node -> node.janitorGetHostValue().getClosingDelimiter())
                .setMetaData(Janitor.MetaData.HELP, "closingDelimiter: The delimiter that closes the strong emphasis, \"**\" or \"__\"; read-only.");
    }

    public CMStrongEmphasis(final StrongEmphasis node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMStrongEmphasis() {
        this(new StrongEmphasis());
    }
}
