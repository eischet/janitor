// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.ext.gfm.tables.TableBody;

/**
 * Wrapper for the GFM table body node: the body of a table; its children are the TableRow nodes below the head.
 */
public class CMTableBody extends CMNode {
    private static final WrapperDispatchTable<TableBody> dispatch = new WrapperDispatchTable<>();

    public CMTableBody(final TableBody node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMTableBody() {
        this(new TableBody());
    }
}
