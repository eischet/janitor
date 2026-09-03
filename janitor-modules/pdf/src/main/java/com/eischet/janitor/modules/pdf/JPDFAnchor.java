package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Anchor;

/**
 * Wraps org.openpdf.text.Anchor: a Phrase that can be a hyperlink source (reference) and/or a named
 * destination (name), used together with e.g. Chunk.setLocalGoto(name).
 */
public class JPDFAnchor extends JanitorWrapper<Anchor> {

    public static final WrapperDispatchTable<Anchor> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        JPDFPhrase.addCommonPhraseAttributes(DISPATCH_TABLE);
        DISPATCH_TABLE.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value));
        DISPATCH_TABLE.addStringProperty("reference", self -> self.janitorGetHostValue().getReference(), (self, value) -> self.janitorGetHostValue().setReference(value));
    }

    public JPDFAnchor(final @NotNull Anchor anchor) {
        super(DISPATCH_TABLE, anchor);
    }

    public JPDFAnchor() {
        super(DISPATCH_TABLE, new Anchor());
    }

    public JPDFAnchor(final @NotNull String text) {
        super(DISPATCH_TABLE, new Anchor(text));
    }

    public JPDFAnchor(final @NotNull String text, final @NotNull JPDFFont font) {
        super(DISPATCH_TABLE, new Anchor(text, font.getFont()));
    }

    public Anchor getAnchor() {
        return wrapped;
    }

    static JPDFAnchor fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFAnchor();
            case 1 -> new JPDFAnchor(args.getRequiredStringValue(0));
            case 2 -> new JPDFAnchor(args.getRequiredStringValue(0), args.getRequired(1, JPDFFont.class));
            default -> throw new JanitorArgumentException(process, "Anchor() takes 0, 1 (text) or 2 (text, font) arguments");
        };
    }

}
