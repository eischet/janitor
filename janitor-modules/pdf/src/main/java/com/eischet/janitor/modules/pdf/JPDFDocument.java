package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Document;

public class JPDFDocument extends JanitorWrapper<Document> {

    public static final WrapperDispatchTable<Document> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        // TODO: add properties/methods to match Document
    }

    public JPDFDocument(@NotNull Document doc) {
        super(DISPATCH_TABLE, doc);
    }

    public JPDFDocument() {
        super(DISPATCH_TABLE, new Document());
    }

}
