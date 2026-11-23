// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.ext.gfm.tables.TableHead;

/**
 * Wrapper for the GFM table head node: the head of a table; its child is the TableRow with the header cells.
 */
public class CMTableHead extends CMNode {
    public static final WrapperDispatchTable<TableHead> dispatch = new WrapperDispatchTable<>();

    public CMTableHead(final TableHead node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMTableHead() {
        this(new TableHead());
    }
}
