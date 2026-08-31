package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
import org.mustangproject.ReferencedDocument;

/**
 * A reference to another document (order, contract, delivery note, ...), used e.g. for
 * buyer/seller order references, contract references or additional invoice-related documents.
 */
public class MustangReferencedDocument extends JanitorWrapper<ReferencedDocument> {

    public static final WrapperDispatchTable<ReferencedDocument> DISPATCH = new WrapperDispatchTable<>(MustangReferencedDocument::new);

    static {
        DISPATCH.addBuilderMethod("setIssuerAssignedID", (self, process, args) -> self.janitorGetHostValue().setIssuerAssignedID(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setUriID", (self, process, args) -> self.janitorGetHostValue().setUriID(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setLineID", (self, process, args) -> self.janitorGetHostValue().setLineID(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setTypeCode", (self, process, args) -> self.janitorGetHostValue().setTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setReferenceTypeCode", (self, process, args) -> self.janitorGetHostValue().setReferenceTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setFormattedIssueDateTime", (self, process, args) ->
            self.janitorGetHostValue().setFormattedIssueDateTime(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));
        DISPATCH.addBuilderMethod("setAttachmentBinaryObject", (self, process, args) ->
            self.janitorGetHostValue().setAttachmentBinaryObject(args.getRequired(0, MustangFileAttachment.class).janitorGetHostValue()));

        DISPATCH.addStringProperty("issuerAssignedID", self -> self.janitorGetHostValue().getIssuerAssignedID(), (self, value) -> self.janitorGetHostValue().setIssuerAssignedID(value));
        DISPATCH.addStringProperty("uriID", self -> self.janitorGetHostValue().getUriID(), (self, value) -> self.janitorGetHostValue().setUriID(value));
        DISPATCH.addStringProperty("lineID", self -> self.janitorGetHostValue().getLineID(), (self, value) -> self.janitorGetHostValue().setLineID(value));
        DISPATCH.addStringProperty("typeCode", self -> self.janitorGetHostValue().getTypeCode(), (self, value) -> self.janitorGetHostValue().setTypeCode(value));
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value));
        DISPATCH.addStringProperty("referenceTypeCode", self -> self.janitorGetHostValue().getReferenceTypeCode(), (self, value) -> self.janitorGetHostValue().setReferenceTypeCode(value));
        DISPATCH.addDateProperty("formattedIssueDateTime", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getFormattedIssueDateTime()),
            (self, value) -> self.janitorGetHostValue().setFormattedIssueDateTime(JDate.toLegacyJavaDate(value)));
        DISPATCH.addObjectProperty("attachmentBinaryObject",
            self -> self.janitorGetHostValue().getAttachmentBinaryObject() == null ? null : new MustangFileAttachment(self.janitorGetHostValue().getAttachmentBinaryObject()),
            (self, value) -> self.janitorGetHostValue().setAttachmentBinaryObject(value == null ? null : value.janitorGetHostValue()),
            MustangFileAttachment::new);
    }

    public MustangReferencedDocument() {
        super(DISPATCH, new ReferencedDocument());
    }

    public MustangReferencedDocument(final ReferencedDocument referencedDocument) {
        super(DISPATCH, referencedDocument);
    }

}
