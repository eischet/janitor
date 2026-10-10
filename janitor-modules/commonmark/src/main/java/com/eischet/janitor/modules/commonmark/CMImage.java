// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.Image;

/** Wrapper for the CommonMark image node. */
public class CMImage extends CMNode {
    private static final WrapperDispatchTable<Image> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("destination",
            node -> node.janitorGetHostValue().getDestination(),
            (self, value) -> self.janitorGetHostValue().setDestination(value)
        );
        dispatch.addStringProperty("title",
            node -> node.janitorGetHostValue().getTitle(),
            (self, value) -> self.janitorGetHostValue().setTitle(value)
        );
    }

    public CMImage(final Image node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMImage() {
        this(new Image());
    }
}
