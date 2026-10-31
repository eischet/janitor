// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.FileAttachment;

/**
 * A binary file attached to an invoice, either embedded into the XML or as a PDF/A-3 attachment.
 */
public class MustangFileAttachment extends JanitorWrapper<FileAttachment> {

    public static final WrapperDispatchTable<FileAttachment> DISPATCH = new WrapperDispatchTable<>(MustangFileAttachment::new);

    static {
        DISPATCH.addBuilderMethod("setFilename", (self, process, args) -> self.janitorGetHostValue().setFilename(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setFilename(filename): Sets the file name of the attachment. Returns this attachment.");
        DISPATCH.addBuilderMethod("setMimetype", (self, process, args) -> self.janitorGetHostValue().setMimetype(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setMimetype(mimetype): Sets the MIME type, e.g. \"application/pdf\". Returns this attachment.");
        DISPATCH.addBuilderMethod("setRelation", (self, process, args) -> self.janitorGetHostValue().setRelation(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setRelation(relation): Sets the PDF/A-3 relationship of the attachment to the document. Returns this attachment.");
        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setDescription(description): Sets a description of the attachment. Returns this attachment.");

        DISPATCH.addStringProperty("filename", self -> self.janitorGetHostValue().getFilename(), (self, value) -> self.janitorGetHostValue().setFilename(value))
                .setMetaData(Janitor.MetaData.HELP, "filename: The file name of the attachment; writable.");
        DISPATCH.addStringProperty("mimetype", self -> self.janitorGetHostValue().getMimetype(), (self, value) -> self.janitorGetHostValue().setMimetype(value))
                .setMetaData(Janitor.MetaData.HELP, "mimetype: The MIME type, e.g. \"application/pdf\"; writable.");
        DISPATCH.addStringProperty("relation", self -> self.janitorGetHostValue().getRelation(), (self, value) -> self.janitorGetHostValue().setRelation(value))
                .setMetaData(Janitor.MetaData.HELP, "relation: The PDF/A-3 relationship of the attachment to the document; writable.");
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value))
                .setMetaData(Janitor.MetaData.HELP, "description: A description of the attachment; writable.");

        DISPATCH.addBuilderMethod("setData", (self, process, args) ->
            self.janitorGetHostValue().setData(args.getRequired(0, JBinary.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setData(data): Sets the content of the file as binary data. Returns this attachment.");
        DISPATCH.addMethod("getData", (self, process, args) -> {
            final byte[] data = self.janitorGetHostValue().getData();
            return data == null ? Janitor.NULL : Janitor.binary(data);
        }).setMetaData(Janitor.MetaData.HELP, "getData(): Returns the content of the file as binary data, or null if none is set.");
    }

    public MustangFileAttachment() {
        super(DISPATCH, new FileAttachment());
    }

    public MustangFileAttachment(final FileAttachment fileAttachment) {
        super(DISPATCH, fileAttachment);
    }

}
