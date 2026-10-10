// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.CustomBlock;

/** Wrapper for custom CommonMark blocks, which come from extensions. */
public class CMCustomBlock extends CMNode {
    private static final WrapperDispatchTable<CustomBlock> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
    }

    public CMCustomBlock(final CustomBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }
}
