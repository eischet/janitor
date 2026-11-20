// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.ext.gfm.tables.TableBlock;

/**
 * Wrapper for the GFM table block node: the whole table; its children are the TableHead and the TableBody.
 */
public class CMTableBlock extends CMNode {
    private static final WrapperDispatchTable<TableBlock> dispatch = new WrapperDispatchTable<>();

    public CMTableBlock(final TableBlock node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMTableBlock() {
        this(new TableBlock());
    }
}
