package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Paragraph;

/**
 * Wraps org.openpdf.text.Paragraph, a Phrase with extra layout properties (alignment, indentation, spacing).
 */
public class JPDFParagraph extends JanitorWrapper<Paragraph> {

    public static final WrapperDispatchTable<Paragraph> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        JPDFPhrase.addCommonPhraseAttributes(DISPATCH_TABLE);
        DISPATCH_TABLE.addIntegerProperty("alignment", self -> self.janitorGetHostValue().getAlignment(), (self, value) -> self.janitorGetHostValue().setAlignment(value));
        DISPATCH_TABLE.addBooleanProperty("keepTogether", self -> self.janitorGetHostValue().getKeepTogether(), (self, value) -> self.janitorGetHostValue().setKeepTogether(value));
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value));
        DISPATCH_TABLE.addDoubleProperty("firstLineIndent", self -> self.janitorGetHostValue().getFirstLineIndent(), (self, value) -> self.janitorGetHostValue().setFirstLineIndent((float) value));
        DISPATCH_TABLE.addDoubleProperty("spacingBefore", self -> self.janitorGetHostValue().getSpacingBefore(), (self, value) -> self.janitorGetHostValue().setSpacingBefore((float) value));
        DISPATCH_TABLE.addDoubleProperty("spacingAfter", self -> self.janitorGetHostValue().getSpacingAfter(), (self, value) -> self.janitorGetHostValue().setSpacingAfter((float) value));
        DISPATCH_TABLE.addDoubleProperty("extraParagraphSpace", self -> self.janitorGetHostValue().getExtraParagraphSpace(), (self, value) -> self.janitorGetHostValue().setExtraParagraphSpace((float) value));
        DISPATCH_TABLE.addDoubleProperty("multipliedLeading", self -> self.janitorGetHostValue().getMultipliedLeading(), (self, value) -> self.janitorGetHostValue().setMultipliedLeading((float) value));
        DISPATCH_TABLE.addIntegerProperty("runDirection", self -> self.janitorGetHostValue().getRunDirection(), (self, value) -> self.janitorGetHostValue().setRunDirection(value));
    }

    public JPDFParagraph(final @NotNull Paragraph paragraph) {
        super(DISPATCH_TABLE, paragraph);
    }

    public JPDFParagraph() {
        super(DISPATCH_TABLE, new Paragraph());
    }

    public JPDFParagraph(final @NotNull String text) {
        super(DISPATCH_TABLE, new Paragraph(text));
    }

    public JPDFParagraph(final @NotNull String text, final @NotNull JPDFFont font) {
        super(DISPATCH_TABLE, new Paragraph(text, font.getFont()));
    }

    public JPDFParagraph(final @NotNull JPDFChunk chunk) {
        super(DISPATCH_TABLE, new Paragraph(chunk.getChunk()));
    }

    public JPDFParagraph(final @NotNull JPDFPhrase phrase) {
        super(DISPATCH_TABLE, new Paragraph(phrase.getPhrase()));
    }

    public Paragraph getParagraph() {
        return wrapped;
    }

    static JPDFParagraph fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFParagraph();
            case 1 -> new JPDFParagraph(args.getRequiredStringValue(0));
            case 2 -> new JPDFParagraph(args.getRequiredStringValue(0), args.getRequired(1, JPDFFont.class));
            default -> throw new JanitorArgumentException(process, "Paragraph() takes 0, 1 (text) or 2 (text, font) arguments");
        };
    }

}
