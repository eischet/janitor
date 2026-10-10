// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.ListItem;

/** Wrapper for the CommonMark list item node. */
public class CMListItem extends CMNode {
    private static final WrapperDispatchTable<ListItem> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addNullableIntegerProperty("markerIndent",
            node -> node.janitorGetHostValue().getMarkerIndent(),
            (self, value) -> self.janitorGetHostValue().setMarkerIndent(value)
        );
        dispatch.addNullableIntegerProperty("contentIndent",
            node -> node.janitorGetHostValue().getContentIndent(),
            (self, value) -> self.janitorGetHostValue().setContentIndent(value)
        );
    }

    public CMListItem(final ListItem node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMListItem() {
        this(new ListItem());
    }
}
