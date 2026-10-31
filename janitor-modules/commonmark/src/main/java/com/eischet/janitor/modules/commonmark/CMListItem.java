// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
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
        )
                .setMetaData(Janitor.MetaData.HELP, "markerIndent: The indentation of the list marker, in columns; writable, may be null.");
        dispatch.addNullableIntegerProperty("contentIndent",
            node -> node.janitorGetHostValue().getContentIndent(),
            (self, value) -> self.janitorGetHostValue().setContentIndent(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "contentIndent: The indentation of the item's content, in columns; writable, may be null.");
    }

    public CMListItem(final ListItem node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMListItem() {
        this(new ListItem());
    }
}
