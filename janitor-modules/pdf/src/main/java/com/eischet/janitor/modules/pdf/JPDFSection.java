package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Section;

/**
 * Wraps org.openpdf.text.Section: a part of a Chapter, which can itself contain nested Sections.
 * <p>
 * Section's Java constructor is protected -- OpenPDF only creates instances via
 * Chapter/Section.addSection(...). There is therefore no pdf.Section() factory on the module;
 * scripts get a Section by calling addSection() on a Chapter or another Section.
 */
public class JPDFSection extends JanitorWrapper<Section> {

    public static final WrapperDispatchTable<Section> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        addCommonSectionAttributes(DISPATCH_TABLE);
    }

    /**
     * Shared by JPDFSection and JPDFChapter (Chapter is-a Section in OpenPDF).
     */
    static <T extends Section> void addCommonSectionAttributes(final WrapperDispatchTable<T> table) {
        table.addObjectProperty("title",
                self -> new JPDFParagraph(self.janitorGetHostValue().getTitle()),
                (self, value) -> self.janitorGetHostValue().setTitle(value.getParagraph()),
                JPDFParagraph::new);
        table.addIntegerProperty("numberDepth", self -> self.janitorGetHostValue().getNumberDepth(), (self, value) -> self.janitorGetHostValue().setNumberDepth(value));
        table.addIntegerProperty("numberStyle", self -> self.janitorGetHostValue().getNumberStyle(), (self, value) -> self.janitorGetHostValue().setNumberStyle(value));
        table.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value));
        table.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value));
        table.addDoubleProperty("indentation", self -> self.janitorGetHostValue().getIndentation(), (self, value) -> self.janitorGetHostValue().setIndentation((float) value));
        table.addBooleanProperty("bookmarkOpen", self -> self.janitorGetHostValue().isBookmarkOpen(), (self, value) -> self.janitorGetHostValue().setBookmarkOpen(value));
        table.addBooleanProperty("triggerNewPage", self -> self.janitorGetHostValue().isTriggerNewPage(), (self, value) -> self.janitorGetHostValue().setTriggerNewPage(value));
        table.addBuilderMethod("add", (self, process, args) -> self.janitorGetHostValue().add(PdfElements.requireElement(process, args, 0)));
        table.addMethod("addSection", (self, process, args) -> {
            final String title = args.getRequiredStringValue(0);
            final Section section = args.size() >= 2
                    ? self.janitorGetHostValue().addSection(title, args.getRequiredIntValue(1))
                    : self.janitorGetHostValue().addSection(title);
            return new JPDFSection(section);
        });
    }

    public JPDFSection(final @NotNull Section section) {
        super(DISPATCH_TABLE, section);
    }

    public Section getSection() {
        return wrapped;
    }

}
