package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
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
        });
        DISPATCH_TABLE.addIntegerProperty("size", self -> self.janitorGetHostValue().size());
        DISPATCH_TABLE.addBooleanProperty("empty", self -> self.janitorGetHostValue().isEmpty());
        DISPATCH_TABLE.addBooleanProperty("numbered", self -> self.janitorGetHostValue().isNumbered(), (self, value) -> self.janitorGetHostValue().setNumbered(value));
        DISPATCH_TABLE.addBooleanProperty("lettered", self -> self.janitorGetHostValue().isLettered(), (self, value) -> self.janitorGetHostValue().setLettered(value));
        DISPATCH_TABLE.addBooleanProperty("lowercase", self -> self.janitorGetHostValue().isLowercase(), (self, value) -> self.janitorGetHostValue().setLowercase(value));
        DISPATCH_TABLE.addBooleanProperty("autoindent", self -> self.janitorGetHostValue().isAutoindent(), (self, value) -> self.janitorGetHostValue().setAutoindent(value));
        DISPATCH_TABLE.addBooleanProperty("alignindent", self -> self.janitorGetHostValue().isAlignindent(), (self, value) -> self.janitorGetHostValue().setAlignindent(value));
        DISPATCH_TABLE.addIntegerProperty("first", self -> self.janitorGetHostValue().getFirst(), (self, value) -> self.janitorGetHostValue().setFirst(value));
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value));
        DISPATCH_TABLE.addDoubleProperty("symbolIndent", self -> self.janitorGetHostValue().getSymbolIndent(), (self, value) -> self.janitorGetHostValue().setSymbolIndent((float) value));
        DISPATCH_TABLE.addBuilderMethod("setListSymbol", (self, process, args) -> self.janitorGetHostValue().setListSymbol(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setPreSymbol", (self, process, args) -> self.janitorGetHostValue().setPreSymbol(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setPostSymbol", (self, process, args) -> self.janitorGetHostValue().setPostSymbol(args.getRequiredStringValue(0)));
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
