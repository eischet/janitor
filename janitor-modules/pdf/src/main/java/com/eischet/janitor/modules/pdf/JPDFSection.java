// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.Janitor;
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
                JPDFParagraph::new)
                .setMetaData(Janitor.MetaData.HELP, "title: The title of the section as a Paragraph; writable.");
        table.addIntegerProperty("numberDepth", self -> self.janitorGetHostValue().getNumberDepth(), (self, value) -> self.janitorGetHostValue().setNumberDepth(value))
                .setMetaData(Janitor.MetaData.HELP, "numberDepth: How many levels of the section number are shown in the title, e.g. 2 for \"1.2.\"; 0 shows no number; writable.");
        table.addIntegerProperty("numberStyle", self -> self.janitorGetHostValue().getNumberStyle(), (self, value) -> self.janitorGetHostValue().setNumberStyle(value));
        table.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationLeft: The left indentation in points; writable.");
        table.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationRight: The right indentation in points; writable.");
        table.addDoubleProperty("indentation", self -> self.janitorGetHostValue().getIndentation(), (self, value) -> self.janitorGetHostValue().setIndentation((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentation: The indentation of the section content in points; writable.");
        table.addBooleanProperty("bookmarkOpen", self -> self.janitorGetHostValue().isBookmarkOpen(), (self, value) -> self.janitorGetHostValue().setBookmarkOpen(value))
                .setMetaData(Janitor.MetaData.HELP, "bookmarkOpen: True if the section's bookmark (outline entry) is shown expanded; writable.");
        table.addBooleanProperty("triggerNewPage", self -> self.janitorGetHostValue().isTriggerNewPage(), (self, value) -> self.janitorGetHostValue().setTriggerNewPage(value))
                .setMetaData(Janitor.MetaData.HELP, "triggerNewPage: True if the section starts on a new page; writable.");
        table.addBuilderMethod("add", (self, process, args) -> self.janitorGetHostValue().add(PdfElements.requireElement(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "add(element): Adds a pdf element (Paragraph, List, PTable, ...) to the section; returns the section.");
        table.addMethod("addSection", (self, process, args) -> {
            final String title = args.getRequiredStringValue(0);
            final Section section = args.size() >= 2
                    ? self.janitorGetHostValue().addSection(title, args.getRequiredIntValue(1))
                    : self.janitorGetHostValue().addSection(title);
            return new JPDFSection(section);
        }).setMetaData(Janitor.MetaData.HELP, "addSection(title, numberDepth): Adds a nested Section with the title and returns it; numberDepth is optional, see numberDepth.");
    }

    public JPDFSection(final @NotNull Section section) {
        super(DISPATCH_TABLE, section);
    }

    /**
     * @return the wrapped Section object
     */
    public Section getSection() {
        return wrapped;
    }

}
