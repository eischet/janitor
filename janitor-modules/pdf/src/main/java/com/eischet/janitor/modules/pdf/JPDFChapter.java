package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Chapter;

/**
 * Wraps org.openpdf.text.Chapter, the top-level sectioning element of a Document (Chapter is-a Section).
 */
public class JPDFChapter extends JanitorWrapper<Chapter> {

    public static final WrapperDispatchTable<Chapter> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        JPDFSection.addCommonSectionAttributes(DISPATCH_TABLE);
    }

    public JPDFChapter(final @NotNull Chapter chapter) {
        super(DISPATCH_TABLE, chapter);
    }

    public JPDFChapter(final int number) {
        super(DISPATCH_TABLE, new Chapter(number));
    }

    public JPDFChapter(final @NotNull String title, final int number) {
        super(DISPATCH_TABLE, new Chapter(title, number));
    }

    public JPDFChapter(final @NotNull JPDFParagraph title, final int number) {
        super(DISPATCH_TABLE, new Chapter(title.getParagraph(), number));
    }

    public Chapter getChapter() {
        return wrapped;
    }

    static JPDFChapter fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 1 -> new JPDFChapter(args.getRequiredIntValue(0));
            case 2 -> new JPDFChapter(args.getRequiredStringValue(0), args.getRequiredIntValue(1));
            default -> throw new JanitorArgumentException(process, "Chapter() takes 1 (number) or 2 (title, number) arguments");
        };
    }

}
