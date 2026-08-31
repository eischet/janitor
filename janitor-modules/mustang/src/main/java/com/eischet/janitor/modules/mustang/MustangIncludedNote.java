package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.IncludedNote;
import org.mustangproject.SubjectCode;

/**
 * A free-text note attached to an invoice or item, optionally qualified with a UNTDID 4451
 * subject code (e.g. "AAI" for general information, "REG" for regulatory information).
 * See {@link SubjectCode} for the full list of accepted codes.
 */
public class MustangIncludedNote extends JanitorWrapper<IncludedNote> {

    public static final WrapperDispatchTable<IncludedNote> DISPATCH = new WrapperDispatchTable<>(MustangIncludedNote::new);

    static {
        DISPATCH.addBuilderMethod("setContent", (self, process, args) -> self.janitorGetHostValue().setContent(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setSubjectCode", (self, process, args) ->
            self.janitorGetHostValue().setSubjectCode(toSubjectCode(args.getOptionalStringValue(0, null))));

        DISPATCH.addStringProperty("content", self -> self.janitorGetHostValue().getContent(), (self, value) -> self.janitorGetHostValue().setContent(value));
        DISPATCH.addStringProperty("subjectCode",
            self -> self.janitorGetHostValue().getSubjectCode() == null ? null : self.janitorGetHostValue().getSubjectCode().name(),
            (self, value) -> self.janitorGetHostValue().setSubjectCode(toSubjectCode(value)));
    }

    private static SubjectCode toSubjectCode(final String value) {
        return value == null || value.isBlank() ? null : SubjectCode.valueOf(value.trim().toUpperCase());
    }

    public MustangIncludedNote() {
        super(DISPATCH, new IncludedNote());
    }

    public MustangIncludedNote(final IncludedNote includedNote) {
        super(DISPATCH, includedNote);
    }

}
