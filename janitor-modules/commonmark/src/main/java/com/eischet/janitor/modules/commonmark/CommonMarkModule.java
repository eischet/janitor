package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.commonmark.parser.Parser;

public class CommonMarkModule extends JanitorComposed<CommonMarkModule> implements JanitorModule {

    private static final DispatchTable<CommonMarkModule> dispatch = new DispatchTable<>(CommonMarkModule::new);
    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("commonmark", CommonMarkModule::new);

    static {
        // Factory methods for the various node types
        dispatch.addConstructor("BlockQuote", CMBlockQuote::new);
        dispatch.addConstructor("BulletList", CMBulletList::new);
        dispatch.addConstructor("Document", CMDocument::new);
        dispatch.addConstructor("Emphasis", CMEmphasis::new);
        dispatch.addConstructor("FencedCodeBlock", CMFencedCodeBlock::new);
        dispatch.addConstructor("HardLineBreak", CMHardLineBreak::new);
        dispatch.addConstructor("Heading", CMHeading::new);
        dispatch.addConstructor("HtmlBlock", CMHtmlBlock::new);
        dispatch.addConstructor("HtmlInline", CMHtmlInline::new);
        dispatch.addConstructor("Image", CMImage::new);
        dispatch.addConstructor("IndentedCodeBlock", CMIndentedCodeBlock::new);
        dispatch.addConstructor("Link", CMLink::new);
        dispatch.addConstructor("LinkReferenceDefinition", CMLinkReferenceDefinition::new);
        dispatch.addConstructor("ListItem", CMListItem::new);
        dispatch.addConstructor("OrderedList", CMOrderedList::new);
        dispatch.addConstructor("Paragraph", CMParagraph::new);
        dispatch.addConstructor("SoftLineBreak", CMSoftLineBreak::new);
        dispatch.addConstructor("StrongEmphasis", CMStrongEmphasis::new);
        dispatch.addConstructor("Text", CMText::new);
        dispatch.addConstructor("ThematicBreak", CMThematicBreak::new);

        // main parser entry point
        dispatch.addMethod("parse", (self, process, args) -> {
            final var parser = Parser.builder().build();
            final var text = args.getRequiredStringValue(0);
            final var doc = parser.parse(text);
            return CMNode.createNullable(doc);

        });
    }

    public CommonMarkModule() {
        super(dispatch);
    }

}
