// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Heading;

/** Wrapper for the CommonMark heading node. */
public class CMHeading extends CMNode {
    private static final WrapperDispatchTable<Heading> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addIntegerProperty("level",
            node -> node.janitorGetHostValue().getLevel(),
            (self, value) -> self.janitorGetHostValue().setLevel(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "level: The level of the heading, from 1 to 6; writable.");
    }

    public CMHeading(final Heading node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMHeading() {
        this(new Heading());
    }
}
