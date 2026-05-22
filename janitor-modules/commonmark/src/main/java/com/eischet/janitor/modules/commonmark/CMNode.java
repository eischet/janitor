package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.logging.JanitorLogger;
import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.slf4j.Logger;

public abstract class CMNode extends JanitorWrapper<Node> {

    protected static final Logger LOG = JanitorLogger.getLogger(CMNode.class);

    public static final WrapperDispatchTable<Node> dispatch = new WrapperDispatchTable<>();

    static {
        dispatch.addStringProperty("type", self -> self.janitorGetHostValue().getClass().getSimpleName());
        dispatch.addObjectProperty("firstChild", self -> CMNode.createNullable(self.janitorGetHostValue().getFirstChild()));
        dispatch.addObjectProperty("lastChild", self -> CMNode.createNullable(self.janitorGetHostValue().getLastChild()));
        dispatch.addObjectProperty("next", self -> CMNode.createNullable(self.janitorGetHostValue().getNext()));
        dispatch.addObjectProperty("previous", self -> CMNode.createNullable(self.janitorGetHostValue().getPrevious()));
        dispatch.addObjectProperty("parent", self -> CMNode.createNullable(self.janitorGetHostValue().getParent()));
        dispatch.addVoidMethod("unlink", (self, process, args) -> self.janitorGetHostValue().unlink());
        dispatch.addVoidMethod("insertBefore", (self, process, args) -> {
            self.janitorGetHostValue().insertBefore(args.getRequired(0, CMNode.class).janitorGetHostValue());
        });
        dispatch.addVoidMethod("insertAfter", (self, process, args) -> {
            self.janitorGetHostValue().insertAfter(args.getRequired(0, CMNode.class).janitorGetHostValue());
        });
        dispatch.addVoidMethod("appendChild", (self, process, args) -> {
            self.janitorGetHostValue().appendChild(args.getRequired(0, CMNode.class).janitorGetHostValue());
        });
        dispatch.addVoidMethod("prependChild", (self, process, args) -> {
            self.janitorGetHostValue().prependChild(args.getRequired(0, CMNode.class).janitorGetHostValue());
        });
        dispatch.addMethod("toHtml", (self, process, args) -> {
            var renderer = HtmlRenderer.builder().build();
            return Janitor.nullableString(renderer.render(self.janitorGetHostValue()));
        }).setMetaData(Janitor.MetaData.HELP, "Converts the node to html representation");
        dispatch.addMethod("toMarkdown", (self, process, args) -> {
            var renderer = MarkdownRenderer.builder().build();
            return Janitor.nullableString(renderer.render(self.janitorGetHostValue()));
        }).setMetaData(Janitor.MetaData.HELP, "Converts the node to markdown representation");
        dispatch.addMethod("toText", (self, process, args) -> {
            var renderer = TextContentRenderer.builder().build();
            return Janitor.nullableString(renderer.render(self.janitorGetHostValue()));
        }).setMetaData(Janitor.MetaData.HELP, "Converts the node to plain text representation");
        dispatch.addVoidMethod("visit", (self, process, args) -> {
            final JanitorObject callback = args.getRequired(0, JanitorObject.class);
            if (callback instanceof JCallable callable) {
                final var v = new CMVisitor(process, callable);
                self.janitorGetHostValue().accept(v);
            }
            throw new JanitorArgumentException(process, "visit() expects a function(node) as first argument");
        });
    }

    public CMNode(final Node node) {
        super(dispatch, node);
    }

    public CMNode(final Dispatcher<JanitorWrapper<Node>> dispatcher, final Node wrapped) {
        super(dispatcher, wrapped);
    }

    public String getType() {
        return wrapped.getClass().getSimpleName();
    }

    public static JanitorObject createNullable(final Node node) {
        if (node == null) {
            return Janitor.NULL;
        } else {
            return create(node);
        }
    }

    public String toHtml() {
        var renderer = HtmlRenderer.builder().build();
        return renderer.render(janitorGetHostValue());
    }

    public static CMNode create(final Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        } else if (node instanceof BlockQuote blockQuote) {
            return new CMBlockQuote(blockQuote);
        } else if (node instanceof BulletList bulletList) {
            return new CMBulletList(bulletList);
        } else if (node instanceof Code code) {
            return new CMCode(code);
        } else if (node instanceof CustomBlock customBlock) {
            return new CMCustomBlock(customBlock);
        } else if (node instanceof CustomNode customNode) {
            return new CMCustomNode(customNode);
        } else if (node instanceof Document document) {
            return new CMDocument(document);
        } else if (node instanceof Emphasis emphasis) {
            return new CMEmphasis(emphasis);
        } else if (node instanceof FencedCodeBlock fencedCodeBlock) {
            return new CMFencedCodeBlock(fencedCodeBlock);
        } else if (node instanceof HardLineBreak hardLineBreak) {
            return new CMHardLineBreak(hardLineBreak);
        } else if (node instanceof Heading heading) {
            return new CMHeading(heading);
        } else if (node instanceof HtmlBlock htmlBlock) {
            return new CMHtmlBlock(htmlBlock);
        } else if (node instanceof HtmlInline htmlInline) {
            return new CMHtmlInline(htmlInline);
        } else if (node instanceof Image image) {
            return new CMImage(image);
        } else if (node instanceof IndentedCodeBlock indentedCodeBlock) {
            return new CMIndentedCodeBlock(indentedCodeBlock);
        } else if (node instanceof Link link) {
            return new CMLink(link);
        } else if (node instanceof LinkReferenceDefinition linkReferenceDefinition) {
            return new CMLinkReferenceDefinition(linkReferenceDefinition);
        } else if (node instanceof ListItem listItem) {
            return new CMListItem(listItem);
        } else if (node instanceof OrderedList orderedList) {
            return new CMOrderedList(orderedList);
        } else if (node instanceof Paragraph paragraph) {
            return new CMParagraph(paragraph);
        } else if (node instanceof SoftLineBreak softLineBreak) {
            return new CMSoftLineBreak(softLineBreak);
        } else if (node instanceof StrongEmphasis strongEmphasis) {
            return new CMStrongEmphasis(strongEmphasis);
        } else if (node instanceof Text text) {
            return new CMText(text);
        } else if (node instanceof ThematicBreak thematicBreak) {
            return new CMThematicBreak(thematicBreak);
        } else if (node instanceof Block block) {
            return new CMBlock(block);
        } else {
            LOG.warn("Node type not yet implemented: " + node.getClass());
            return new CMUnknown(node);
        }
    }

    public Node getNode() {
        return wrapped;
    }


}
