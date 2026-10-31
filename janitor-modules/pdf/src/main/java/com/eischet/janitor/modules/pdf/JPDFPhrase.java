// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Phrase;

/**
 * Wraps org.openpdf.text.Phrase: a series of Chunks sharing a leading and (initial) Font.
 */
public class JPDFPhrase extends JanitorWrapper<Phrase> {

    public static final WrapperDispatchTable<Phrase> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        addCommonPhraseAttributes(DISPATCH_TABLE);
    }

    /**
     * Shared by JPDFPhrase and the classes that are-a Phrase in OpenPDF (Paragraph, Anchor), so those
     * don't have to repeat the same handful of lookups. We can't use Dispatcher.inherit() here because
     * each of those wrapper classes wraps a different concrete Java type (Phrase vs. Paragraph vs. Anchor).
     */
    static <T extends Phrase> void addCommonPhraseAttributes(final WrapperDispatchTable<T> table) {
        table.addDoubleProperty("leading", self -> self.janitorGetHostValue().getLeading(), (self, value) -> self.janitorGetHostValue().setLeading((float) value))
                .setMetaData(Janitor.MetaData.HELP, "leading: The line spacing (distance between baselines) in points; writable.");
        table.addBooleanProperty("empty", self -> self.janitorGetHostValue().isEmpty())
                .setMetaData(Janitor.MetaData.HELP, "empty: True if the phrase contains no content; read-only.");
        table.addObjectProperty("font",
                self -> new JPDFFont(self.janitorGetHostValue().getFont()),
                (self, value) -> self.janitorGetHostValue().setFont(value.getFont()),
                JPDFFont::new)
                .setMetaData(Janitor.MetaData.HELP, "font: The Font of the phrase; writable.");
        table.addBuilderMethod("add", (self, process, args) -> self.janitorGetHostValue().add(PdfElements.requireElement(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "add(element): Adds a pdf element (Chunk, Phrase, ...) to the phrase; returns the phrase.");
        table.addIntegerProperty("size", self -> self.janitorGetHostValue().size())
                .setMetaData(Janitor.MetaData.HELP, "size: The number of elements in the phrase; read-only.");
    }

    public JPDFPhrase(final @NotNull Phrase phrase) {
        super(DISPATCH_TABLE, phrase);
    }

    public JPDFPhrase() {
        super(DISPATCH_TABLE, new Phrase());
    }

    public JPDFPhrase(final @NotNull String text) {
        super(DISPATCH_TABLE, new Phrase(text));
    }

    public JPDFPhrase(final @NotNull String text, final @NotNull JPDFFont font) {
        super(DISPATCH_TABLE, new Phrase(text, font.getFont()));
    }

    public JPDFPhrase(final @NotNull JPDFChunk chunk) {
        super(DISPATCH_TABLE, new Phrase(chunk.getChunk()));
    }

    /**
     * @return the wrapped Phrase object
     */
    public Phrase getPhrase() {
        return wrapped;
    }

    static JPDFPhrase fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFPhrase();
            case 1 -> new JPDFPhrase(args.getRequiredStringValue(0));
            case 2 -> new JPDFPhrase(args.getRequiredStringValue(0), args.getRequired(1, JPDFFont.class));
            default -> throw new JanitorArgumentException(process, "Phrase() takes 0, 1 (text) or 2 (text, font) arguments");
        };
    }

}
