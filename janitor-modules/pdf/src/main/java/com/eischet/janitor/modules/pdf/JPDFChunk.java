package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Chunk;

/**
 * Wraps org.openpdf.text.Chunk, the smallest significant piece of text: a String with a Font.
 */
public class JPDFChunk extends JanitorWrapper<Chunk> {

    public static final WrapperDispatchTable<Chunk> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addStringProperty("content", self -> self.janitorGetHostValue().getContent());
        DISPATCH_TABLE.addBooleanProperty("empty", self -> self.janitorGetHostValue().isEmpty());
        DISPATCH_TABLE.addObjectProperty("font",
                self -> new JPDFFont(self.janitorGetHostValue().getFont()),
                (self, value) -> self.janitorGetHostValue().setFont(value.getFont()),
                JPDFFont::new);
        DISPATCH_TABLE.addDoubleProperty("characterSpacing", self -> self.janitorGetHostValue().getCharacterSpacing(), (self, value) -> self.janitorGetHostValue().setCharacterSpacing((float) value));
        DISPATCH_TABLE.addDoubleProperty("horizontalScaling", self -> self.janitorGetHostValue().getHorizontalScaling(), (self, value) -> self.janitorGetHostValue().setHorizontalScaling((float) value));
        DISPATCH_TABLE.addDoubleProperty("textRise", self -> self.janitorGetHostValue().getTextRise(), (self, value) -> self.janitorGetHostValue().setTextRise((float) value));
        DISPATCH_TABLE.addBuilderMethod("setUnderline", (self, process, args) -> {
            final float thickness = PdfElements.optionalFloatArg(process, args, 0, 1f);
            final float yPosition = PdfElements.optionalFloatArg(process, args, 1, -2f);
            self.janitorGetHostValue().setUnderline(thickness, yPosition);
        });
        DISPATCH_TABLE.addBuilderMethod("setBackground", (self, process, args) -> {
            final JPDFColor color = args.getRequired(0, JPDFColor.class);
            self.janitorGetHostValue().setBackground(color.getColor());
        });
        DISPATCH_TABLE.addBuilderMethod("setAnchor", (self, process, args) -> self.janitorGetHostValue().setAnchor(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setLocalGoto", (self, process, args) -> self.janitorGetHostValue().setLocalGoto(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setLocalDestination", (self, process, args) -> self.janitorGetHostValue().setLocalDestination(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setNewPage", (self, process, args) -> self.janitorGetHostValue().setNewPage());
        DISPATCH_TABLE.addBuilderMethod("setGenericTag", (self, process, args) -> self.janitorGetHostValue().setGenericTag(args.getRequiredStringValue(0)));
        // TODO: setRemoteGoto, setAction(PdfAction), setAnnotation(PdfAnnotation), setTextRenderMode, setSkew, setSplitCharacter, setHyphenation -- need PdfAction/PdfAnnotation/HyphenationEvent wrappers first.
        // TODO: Chunk(Image, offsetX, offsetY) and Chunk(DrawInterface separator, ...) constructors -- not mapped yet (need JPDFImage / a script-side DrawInterface bridge).
    }

    public JPDFChunk(final @NotNull Chunk chunk) {
        super(DISPATCH_TABLE, chunk);
    }

    public JPDFChunk() {
        super(DISPATCH_TABLE, new Chunk());
    }

    public JPDFChunk(final @NotNull String content) {
        super(DISPATCH_TABLE, new Chunk(content));
    }

    public JPDFChunk(final @NotNull String content, final @NotNull JPDFFont font) {
        super(DISPATCH_TABLE, new Chunk(content, font.getFont()));
    }

    public Chunk getChunk() {
        return wrapped;
    }

    static JPDFChunk fromArgs(final com.eischet.janitor.api.JanitorScriptProcess process, final com.eischet.janitor.api.types.functions.JCallArgs args) throws com.eischet.janitor.api.errors.runtime.JanitorRuntimeException {
        return switch (args.size()) {
            case 0 -> new JPDFChunk();
            case 1 -> new JPDFChunk(args.getRequiredStringValue(0));
            case 2 -> new JPDFChunk(args.getRequiredStringValue(0), args.getRequired(1, JPDFFont.class));
            default -> throw new JanitorArgumentException(process, "Chunk() takes 0, 1 (text) or 2 (text, font) arguments");
        };
    }

}
