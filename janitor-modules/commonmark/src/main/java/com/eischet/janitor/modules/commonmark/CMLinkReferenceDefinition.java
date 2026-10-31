// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.LinkReferenceDefinition;

/** Wrapper for the CommonMark link reference definition node. */
public class CMLinkReferenceDefinition extends CMNode {
    private static final WrapperDispatchTable<LinkReferenceDefinition> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("label",
            node -> node.janitorGetHostValue().getLabel(),
            (self, value) -> self.janitorGetHostValue().setLabel(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "label: The label that links use to refer to this definition; writable.");
        dispatch.addStringProperty("destination",
            node -> node.janitorGetHostValue().getDestination(),
            (self, value) -> self.janitorGetHostValue().setDestination(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "destination: The URL that the definition points to; writable.");
        dispatch.addStringProperty("title",
            node -> node.janitorGetHostValue().getTitle(),
            (self, value) -> self.janitorGetHostValue().setTitle(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "title: The title of the definition; writable.");
    }

    public CMLinkReferenceDefinition(final LinkReferenceDefinition node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMLinkReferenceDefinition() {
        this(new LinkReferenceDefinition());
    }
}
