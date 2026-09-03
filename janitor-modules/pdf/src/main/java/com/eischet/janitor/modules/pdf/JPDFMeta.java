package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Meta;

/**
 * Wraps org.openpdf.text.Meta: a piece of document meta-information (title, author, ...). Usually you'll
 * want the Document.addTitle()/addAuthor()/... convenience methods instead of building one of these
 * directly, but Meta(tag, content) is available for arbitrary/custom meta tags.
 */
public class JPDFMeta extends JanitorWrapper<Meta> {

    public static final WrapperDispatchTable<Meta> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addStringProperty("name", self -> self.janitorGetHostValue().getName());
        DISPATCH_TABLE.addStringProperty("content", self -> self.janitorGetHostValue().getContent());
    }

    public JPDFMeta(final @NotNull Meta meta) {
        super(DISPATCH_TABLE, meta);
    }

    public JPDFMeta(final @NotNull String tag, final @NotNull String content) {
        super(DISPATCH_TABLE, new Meta(tag, content));
    }

    public Meta getMeta() {
        return wrapped;
    }

}
