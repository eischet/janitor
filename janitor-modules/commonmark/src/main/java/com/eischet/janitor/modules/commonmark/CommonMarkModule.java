// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.commonmark.parser.Parser;

/** The script module "commonmark", which parses Markdown and offers the document as a tree of nodes. */
public class CommonMarkModule extends JanitorComposed<CommonMarkModule> implements JanitorModule {

    private static final DispatchTable<CommonMarkModule> dispatch = new DispatchTable<>(CommonMarkModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("commonmark", CommonMarkModule::new);

    static {
        // Factory methods for the various node types
        dispatch.addConstructor("BlockQuote", CMBlockQuote::new)
                .setMetaData(Janitor.MetaData.HELP, "BlockQuote(properties): Creates a new, empty block quote node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("BulletList", CMBulletList::new)
                .setMetaData(Janitor.MetaData.HELP, "BulletList(properties): Creates a new, empty bullet list node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Document", CMDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "Document(properties): Creates a new, empty document node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Emphasis", CMEmphasis::new)
                .setMetaData(Janitor.MetaData.HELP, "Emphasis(properties): Creates a new, empty emphasis node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("FencedCodeBlock", CMFencedCodeBlock::new)
                .setMetaData(Janitor.MetaData.HELP, "FencedCodeBlock(properties): Creates a new, empty fenced code block node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("HardLineBreak", CMHardLineBreak::new)
                .setMetaData(Janitor.MetaData.HELP, "HardLineBreak(properties): Creates a new, empty hard line break node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Heading", CMHeading::new)
                .setMetaData(Janitor.MetaData.HELP, "Heading(properties): Creates a new, empty heading node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("HtmlBlock", CMHtmlBlock::new)
                .setMetaData(Janitor.MetaData.HELP, "HtmlBlock(properties): Creates a new, empty HTML block node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("HtmlInline", CMHtmlInline::new)
                .setMetaData(Janitor.MetaData.HELP, "HtmlInline(properties): Creates a new, empty inline HTML node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Image", CMImage::new)
                .setMetaData(Janitor.MetaData.HELP, "Image(properties): Creates a new, empty image node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("IndentedCodeBlock", CMIndentedCodeBlock::new)
                .setMetaData(Janitor.MetaData.HELP, "IndentedCodeBlock(properties): Creates a new, empty indented code block node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Link", CMLink::new)
                .setMetaData(Janitor.MetaData.HELP, "Link(properties): Creates a new, empty link node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("LinkReferenceDefinition", CMLinkReferenceDefinition::new)
                .setMetaData(Janitor.MetaData.HELP, "LinkReferenceDefinition(properties): Creates a new, empty link reference definition node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("ListItem", CMListItem::new)
                .setMetaData(Janitor.MetaData.HELP, "ListItem(properties): Creates a new, empty list item node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("OrderedList", CMOrderedList::new)
                .setMetaData(Janitor.MetaData.HELP, "OrderedList(properties): Creates a new, empty ordered list node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Paragraph", CMParagraph::new)
                .setMetaData(Janitor.MetaData.HELP, "Paragraph(properties): Creates a new, empty paragraph node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("SoftLineBreak", CMSoftLineBreak::new)
                .setMetaData(Janitor.MetaData.HELP, "SoftLineBreak(properties): Creates a new, empty soft line break node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("StrongEmphasis", CMStrongEmphasis::new)
                .setMetaData(Janitor.MetaData.HELP, "StrongEmphasis(properties): Creates a new, empty strong emphasis node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("Text", CMText::new)
                .setMetaData(Janitor.MetaData.HELP, "Text(properties): Creates a new, empty text node; an optional map argument is applied to its properties.");
        dispatch.addConstructor("ThematicBreak", CMThematicBreak::new)
                .setMetaData(Janitor.MetaData.HELP, "ThematicBreak(properties): Creates a new, empty thematic break node; an optional map argument is applied to its properties.");

        // main parser entry point
        dispatch.addMethod("parse", (self, process, args) -> {
            final var parser = Parser.builder().build();
            final var text = args.getRequiredStringValue(0);
            final var doc = parser.parse(text);
            return CMNode.createNullable(doc);

        })
                .setMetaData(Janitor.MetaData.HELP, "parse(markdown): Parses the Markdown text and returns the document node of the resulting tree.");
    }

    public CommonMarkModule() {
        super(dispatch);
    }

}
