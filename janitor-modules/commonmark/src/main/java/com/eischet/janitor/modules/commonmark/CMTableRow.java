// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.ext.gfm.tables.TableRow;

/**
 * Wrapper for the GFM table row node: a row of a table; its children are TableCell nodes.
 */
public class CMTableRow extends CMNode {
    public static final WrapperDispatchTable<TableRow> dispatch = new WrapperDispatchTable<>();

    public CMTableRow(final TableRow node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMTableRow() {
        this(new TableRow());
    }
}
