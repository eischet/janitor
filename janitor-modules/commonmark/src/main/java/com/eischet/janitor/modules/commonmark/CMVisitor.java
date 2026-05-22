package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
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
import org.commonmark.node.Visitor;

public class CMVisitor implements Visitor {

    private final JanitorScriptProcess process;
    private final JCallable callable;

    public CMVisitor(final JanitorScriptProcess process, final JCallable callable) {
        this.process = process;
        this.callable = callable;
    }

    public void visitAny(final Node node) {
        try {
            callable.call(process, JCallArgs.ofSingleArgument(process, CMNode.createNullable(node)));
        } catch (JanitorRuntimeException e) {
            throw new JanitorError("visiting node " + node + " failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void visit(final BlockQuote blockQuote) {
        visitAny(blockQuote);
    }

    @Override
    public void visit(final BulletList bulletList) {
        visitAny(bulletList);
    }

    @Override
    public void visit(final Code code) {
        visitAny(code);
    }

    @Override
    public void visit(final Document document) {
        visitAny(document);
    }

    @Override
    public void visit(final Emphasis emphasis) {
        visitAny(emphasis);
    }

    @Override
    public void visit(final FencedCodeBlock fencedCodeBlock) {
        visitAny(fencedCodeBlock);
    }

    @Override
    public void visit(final HardLineBreak hardLineBreak) {
        visitAny(hardLineBreak);
    }

    @Override
    public void visit(final Heading heading) {
        visitAny(heading);
    }

    @Override
    public void visit(final ThematicBreak thematicBreak) {
        visitAny(thematicBreak);
    }

    @Override
    public void visit(final HtmlInline htmlInline) {
        visitAny(htmlInline);
    }

    @Override
    public void visit(final HtmlBlock htmlBlock) {
        visitAny(htmlBlock);
    }

    @Override
    public void visit(final Image image) {
        visitAny(image);
    }

    @Override
    public void visit(final IndentedCodeBlock indentedCodeBlock) {
        visitAny(indentedCodeBlock);
    }

    @Override
    public void visit(final Link link) {
        visitAny(link);
    }

    @Override
    public void visit(final ListItem listItem) {
        visitAny(listItem);
    }

    @Override
    public void visit(final OrderedList orderedList) {
        visitAny(orderedList);
    }

    @Override
    public void visit(final Paragraph paragraph) {
        visitAny(paragraph);
    }

    @Override
    public void visit(final SoftLineBreak softLineBreak) {
        visitAny(softLineBreak);
    }

    @Override
    public void visit(final StrongEmphasis strongEmphasis) {
        visitAny(strongEmphasis);
    }

    @Override
    public void visit(final Text text) {
        visitAny(text);
    }

    @Override
    public void visit(final LinkReferenceDefinition linkReferenceDefinition) {
        visitAny(linkReferenceDefinition);
    }

    @Override
    public void visit(final CustomBlock customBlock) {
        visitAny(customBlock);
    }

    @Override
    public void visit(final CustomNode customNode) {
        visitAny(customNode);
    }
}
