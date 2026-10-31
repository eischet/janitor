// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.List;

/**
 * Wraps org.openpdf.text.List: an ordered or unordered list of ListItems.
 */
public class JPDFList extends JanitorWrapper<List> {

    public static final WrapperDispatchTable<List> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addBuilderMethod("add", (self, process, args) -> {
            final JPDFListItem item = args.getRequired(0, JPDFListItem.class);
            self.janitorGetHostValue().add(item.getListItem());
        }).setMetaData(Janitor.MetaData.HELP, "add(item): Adds a ListItem to the list; returns the list.");
        DISPATCH_TABLE.addIntegerProperty("size", self -> self.janitorGetHostValue().size())
                .setMetaData(Janitor.MetaData.HELP, "size: The number of items in the list; read-only.");
        DISPATCH_TABLE.addBooleanProperty("empty", self -> self.janitorGetHostValue().isEmpty())
                .setMetaData(Janitor.MetaData.HELP, "empty: True if the list has no items; read-only.");
        DISPATCH_TABLE.addBooleanProperty("numbered", self -> self.janitorGetHostValue().isNumbered(), (self, value) -> self.janitorGetHostValue().setNumbered(value))
                .setMetaData(Janitor.MetaData.HELP, "numbered: True for an ordered list with numbers (or letters), false for a bulleted list; writable.");
        DISPATCH_TABLE.addBooleanProperty("lettered", self -> self.janitorGetHostValue().isLettered(), (self, value) -> self.janitorGetHostValue().setLettered(value))
                .setMetaData(Janitor.MetaData.HELP, "lettered: True if an ordered list uses letters instead of numbers; writable.");
        DISPATCH_TABLE.addBooleanProperty("lowercase", self -> self.janitorGetHostValue().isLowercase(), (self, value) -> self.janitorGetHostValue().setLowercase(value))
                .setMetaData(Janitor.MetaData.HELP, "lowercase: True if a lettered list uses lowercase letters; writable.");
        DISPATCH_TABLE.addBooleanProperty("autoindent", self -> self.janitorGetHostValue().isAutoindent(), (self, value) -> self.janitorGetHostValue().setAutoindent(value))
                .setMetaData(Janitor.MetaData.HELP, "autoindent: True if the symbol indentation is calculated from the widest list symbol; writable.");
        DISPATCH_TABLE.addBooleanProperty("alignindent", self -> self.janitorGetHostValue().isAlignindent(), (self, value) -> self.janitorGetHostValue().setAlignindent(value))
                .setMetaData(Janitor.MetaData.HELP, "alignindent: True if the indentation of all items is aligned; writable.");
        DISPATCH_TABLE.addIntegerProperty("first", self -> self.janitorGetHostValue().getFirst(), (self, value) -> self.janitorGetHostValue().setFirst(value))
                .setMetaData(Janitor.MetaData.HELP, "first: The number (or letter position) an ordered list starts with; writable.");
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationLeft: The left indentation of the list in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationRight: The right indentation of the list in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("symbolIndent", self -> self.janitorGetHostValue().getSymbolIndent(), (self, value) -> self.janitorGetHostValue().setSymbolIndent((float) value))
                .setMetaData(Janitor.MetaData.HELP, "symbolIndent: The indentation reserved for the list symbol in points; writable.");
        DISPATCH_TABLE.addBuilderMethod("setListSymbol", (self, process, args) -> self.janitorGetHostValue().setListSymbol(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setListSymbol(symbol): Sets the symbol (e.g. a bullet) used for the items of an unordered list; returns the list.");
        DISPATCH_TABLE.addBuilderMethod("setPreSymbol", (self, process, args) -> self.janitorGetHostValue().setPreSymbol(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPreSymbol(text): Sets the text shown before the number or letter of an ordered list; returns the list.");
        DISPATCH_TABLE.addBuilderMethod("setPostSymbol", (self, process, args) -> self.janitorGetHostValue().setPostSymbol(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPostSymbol(text): Sets the text shown after the number or letter of an ordered list; returns the list.");
    }

    public JPDFList(final @NotNull List list) {
        super(DISPATCH_TABLE, list);
    }

    public JPDFList() {
        super(DISPATCH_TABLE, new List());
    }

    public JPDFList(final boolean numbered) {
        super(DISPATCH_TABLE, new List(numbered));
    }

    public JPDFList(final boolean numbered, final boolean lettered) {
        super(DISPATCH_TABLE, new List(numbered, lettered));
    }

    /**
     * @return the wrapped List object
     */
    public List getList() {
        return wrapped;
    }

    static JPDFList fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFList();
            case 1 -> new JPDFList(args.getRequiredBooleanValue(0));
            case 2 -> new JPDFList(args.getRequiredBooleanValue(0), args.getRequiredBooleanValue(1));
            default -> throw new JanitorArgumentException(process, "List() takes 0, 1 (numbered) or 2 (numbered, lettered) arguments");
        };
    }

}
