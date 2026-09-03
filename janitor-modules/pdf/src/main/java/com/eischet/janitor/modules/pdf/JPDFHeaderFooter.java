package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.HeaderFooter;

/**
 * Wraps org.openpdf.text.HeaderFooter, used via Document.setHeader()/setFooter().
 * A HeaderFooter is "before" text, optionally a page number, then "after" text.
 */
public class JPDFHeaderFooter extends JanitorWrapper<HeaderFooter> {

    public static final WrapperDispatchTable<HeaderFooter> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addBooleanProperty("numbered", self -> self.janitorGetHostValue().isNumbered());
        DISPATCH_TABLE.addObjectProperty("before", self -> new JPDFPhrase(self.janitorGetHostValue().getBefore()));
        DISPATCH_TABLE.addObjectProperty("after", self -> new JPDFPhrase(self.janitorGetHostValue().getAfter()));
        DISPATCH_TABLE.addBuilderMethod("setAlignment", (self, process, args) -> self.janitorGetHostValue().setAlignment(args.getRequiredIntValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setPageNumber", (self, process, args) -> self.janitorGetHostValue().setPageNumber(args.getRequiredIntValue(0)));
        DISPATCH_TABLE.addBuilderMethod("setPadding", (self, process, args) -> self.janitorGetHostValue().setPadding(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addBuilderMethod("addPadding", (self, process, args) -> self.janitorGetHostValue().addPadding(PdfElements.floatArg(process, args, 0)));
    }

    public JPDFHeaderFooter(final @NotNull HeaderFooter headerFooter) {
        super(DISPATCH_TABLE, headerFooter);
    }

    public JPDFHeaderFooter(final @NotNull JPDFPhrase before, final boolean numbered) {
        super(DISPATCH_TABLE, new HeaderFooter(before.getPhrase(), numbered));
    }

    public JPDFHeaderFooter(final @NotNull JPDFPhrase before, final @NotNull JPDFPhrase after) {
        super(DISPATCH_TABLE, new HeaderFooter(before.getPhrase(), after.getPhrase()));
    }

    public HeaderFooter getHeaderFooter() {
        return wrapped;
    }

    static JPDFHeaderFooter fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        if (args.size() != 2) {
            throw new JanitorArgumentException(process, "HeaderFooter() takes 2 arguments: (before, numbered) or (before, after)");
        }
        final JPDFPhrase before = args.getRequired(0, JPDFPhrase.class);
        if (args.get(1) instanceof com.eischet.janitor.api.types.builtin.JBool) {
            return new JPDFHeaderFooter(before, args.getRequiredBooleanValue(1));
        }
        return new JPDFHeaderFooter(before, args.getRequired(1, JPDFPhrase.class));
    }

}
