// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.ext.gfm.tables.TableCell;

import java.util.Locale;

/**
 * Wrapper for the GFM table cell node; its children are the inline contents of the cell.
 */
public class CMTableCell extends CMNode {
    private static final WrapperDispatchTable<TableCell> dispatch = new WrapperDispatchTable<>();

    static {
        dispatch.addBooleanProperty("header",
            node -> node.janitorGetHostValue().isHeader(),
            (self, value) -> self.janitorGetHostValue().setHeader(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "header: True if the cell is in the head of the table; writable.");
        dispatch.addStringProperty("alignment",
            node -> alignmentName(node.janitorGetHostValue().getAlignment()),
            (self, value) -> self.janitorGetHostValue().setAlignment(alignmentOf(value))
        )
                .setMetaData(Janitor.MetaData.HELP, "alignment: The alignment of the cell's column, \"left\", \"center\" or \"right\", or null if the table does not specify one; writable.");
        dispatch.addIntegerProperty("width",
            node -> node.janitorGetHostValue().getWidth(),
            (self, value) -> self.janitorGetHostValue().setWidth(value)
        )
                .setMetaData(Janitor.MetaData.HELP, "width: The number of dashes in the delimiter row below the head for this column, 0 if unknown; writable.");
    }

    public CMTableCell(final TableCell node) {
        super(Dispatcher.inherit(CMNode.dispatch, dispatch), node);
    }

    public CMTableCell() {
        this(new TableCell());
    }

    private static String alignmentName(final TableCell.Alignment alignment) {
        return alignment == null ? null : alignment.name().toLowerCase(Locale.ROOT);
    }

    private static TableCell.Alignment alignmentOf(final String name) {
        return name == null ? null : TableCell.Alignment.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
