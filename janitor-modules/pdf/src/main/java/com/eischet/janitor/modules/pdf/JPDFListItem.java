package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.ListItem;

/**
 * Wraps org.openpdf.text.ListItem, a Paragraph that can be added to a List.
 */
public class JPDFListItem extends JanitorWrapper<ListItem> {

    public static final WrapperDispatchTable<ListItem> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        JPDFPhrase.addCommonPhraseAttributes(DISPATCH_TABLE);
        DISPATCH_TABLE.addIntegerProperty("alignment", self -> self.janitorGetHostValue().getAlignment(), (self, value) -> self.janitorGetHostValue().setAlignment(value));
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value));
        DISPATCH_TABLE.addObjectProperty("listSymbol",
                self -> new JPDFChunk(self.janitorGetHostValue().getListSymbol()),
                (self, value) -> self.janitorGetHostValue().setListSymbol(value.getChunk()),
                JPDFChunk::new);
    }

    public JPDFListItem(final @NotNull ListItem listItem) {
        super(DISPATCH_TABLE, listItem);
    }

    public JPDFListItem() {
        super(DISPATCH_TABLE, new ListItem());
    }

    public JPDFListItem(final @NotNull String text) {
        super(DISPATCH_TABLE, new ListItem(text));
    }

    public JPDFListItem(final @NotNull String text, final @NotNull JPDFFont font) {
        super(DISPATCH_TABLE, new ListItem(text, font.getFont()));
    }

    public ListItem getListItem() {
        return wrapped;
    }

    static JPDFListItem fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFListItem();
            case 1 -> new JPDFListItem(args.getRequiredStringValue(0));
            case 2 -> new JPDFListItem(args.getRequiredStringValue(0), args.getRequired(1, JPDFFont.class));
            default -> throw new JanitorArgumentException(process, "ListItem() takes 0, 1 (text) or 2 (text, font) arguments");
        };
    }

}
