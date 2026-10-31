// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import org.mustangproject.ReferencedDocument;

/**
 * A reference to another document (order, contract, delivery note, ...), used e.g. for
 * buyer/seller order references, contract references or additional invoice-related documents.
 */
public class MustangReferencedDocument extends JanitorWrapper<ReferencedDocument> {

    public static final WrapperDispatchTable<ReferencedDocument> DISPATCH = new WrapperDispatchTable<>(MustangReferencedDocument::new);

    static {
        DISPATCH.addBuilderMethod("setIssuerAssignedID", (self, process, args) -> self.janitorGetHostValue().setIssuerAssignedID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setIssuerAssignedID(id): Sets the ID (e.g. the order number) the issuer assigned to the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setUriID", (self, process, args) -> self.janitorGetHostValue().setUriID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setUriID(uri): Sets the URI of the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setLineID", (self, process, args) -> self.janitorGetHostValue().setLineID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setLineID(lineID): Sets the ID of the referenced line within the document; returns the reference.");
        DISPATCH.addBuilderMethod("setTypeCode", (self, process, args) -> self.janitorGetHostValue().setTypeCode(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTypeCode(code): Sets the type code of the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setName(name): Sets the name of the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setReferenceTypeCode", (self, process, args) -> self.janitorGetHostValue().setReferenceTypeCode(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setReferenceTypeCode(code): Sets the reference type code of the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setFormattedIssueDateTime", (self, process, args) ->
            self.janitorGetHostValue().setFormattedIssueDateTime(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())))
            .setMetaData(Janitor.MetaData.HELP, "setFormattedIssueDateTime(date): Sets the issue date of the referenced document; returns the reference.");
        DISPATCH.addBuilderMethod("setAttachmentBinaryObject", (self, process, args) ->
            self.janitorGetHostValue().setAttachmentBinaryObject(args.getRequired(0, MustangFileAttachment.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "setAttachmentBinaryObject(attachment): Attaches the referenced document as a FileAttachment; returns the reference.");

        DISPATCH.addStringProperty("issuerAssignedID", self -> self.janitorGetHostValue().getIssuerAssignedID(), (self, value) -> self.janitorGetHostValue().setIssuerAssignedID(value))
            .setMetaData(Janitor.MetaData.HELP, "issuerAssignedID: The ID (e.g. the order number) the issuer assigned to the referenced document; writable.");
        DISPATCH.addStringProperty("uriID", self -> self.janitorGetHostValue().getUriID(), (self, value) -> self.janitorGetHostValue().setUriID(value))
            .setMetaData(Janitor.MetaData.HELP, "uriID: The URI of the referenced document; writable.");
        DISPATCH.addStringProperty("lineID", self -> self.janitorGetHostValue().getLineID(), (self, value) -> self.janitorGetHostValue().setLineID(value))
            .setMetaData(Janitor.MetaData.HELP, "lineID: The ID of the referenced line within the document; writable.");
        DISPATCH.addStringProperty("typeCode", self -> self.janitorGetHostValue().getTypeCode(), (self, value) -> self.janitorGetHostValue().setTypeCode(value))
            .setMetaData(Janitor.MetaData.HELP, "typeCode: The type code of the referenced document; writable.");
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value))
            .setMetaData(Janitor.MetaData.HELP, "name: The name of the referenced document; writable.");
        DISPATCH.addStringProperty("referenceTypeCode", self -> self.janitorGetHostValue().getReferenceTypeCode(), (self, value) -> self.janitorGetHostValue().setReferenceTypeCode(value))
            .setMetaData(Janitor.MetaData.HELP, "referenceTypeCode: The reference type code of the referenced document; writable.");
        DISPATCH.addDateProperty("formattedIssueDateTime", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getFormattedIssueDateTime()),
            (self, value) -> self.janitorGetHostValue().setFormattedIssueDateTime(JDate.toLegacyJavaDate(value)))
            .setMetaData(Janitor.MetaData.HELP, "formattedIssueDateTime: The issue date of the referenced document, or null; writable.");
        DISPATCH.addObjectProperty("attachmentBinaryObject",
            self -> self.janitorGetHostValue().getAttachmentBinaryObject() == null ? null : new MustangFileAttachment(self.janitorGetHostValue().getAttachmentBinaryObject()),
            (self, value) -> self.janitorGetHostValue().setAttachmentBinaryObject(value == null ? null : value.janitorGetHostValue()),
            MustangFileAttachment::new)
            .setMetaData(Janitor.MetaData.HELP, "attachmentBinaryObject: The attached document as a FileAttachment, or null; writable.");
    }

    public MustangReferencedDocument() {
        super(DISPATCH, new ReferencedDocument());
    }

    public MustangReferencedDocument(final ReferencedDocument referencedDocument) {
        super(DISPATCH, referencedDocument);
    }

}
