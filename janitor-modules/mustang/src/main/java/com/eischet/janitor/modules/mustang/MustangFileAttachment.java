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
        DISPATCH.addBuilderMethod("setFilename", (self, process, args) -> self.janitorGetHostValue().setFilename(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setMimetype", (self, process, args) -> self.janitorGetHostValue().setMimetype(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setRelation", (self, process, args) -> self.janitorGetHostValue().setRelation(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)));

        DISPATCH.addStringProperty("filename", self -> self.janitorGetHostValue().getFilename(), (self, value) -> self.janitorGetHostValue().setFilename(value));
        DISPATCH.addStringProperty("mimetype", self -> self.janitorGetHostValue().getMimetype(), (self, value) -> self.janitorGetHostValue().setMimetype(value));
        DISPATCH.addStringProperty("relation", self -> self.janitorGetHostValue().getRelation(), (self, value) -> self.janitorGetHostValue().setRelation(value));
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value));

        DISPATCH.addBuilderMethod("setData", (self, process, args) ->
            self.janitorGetHostValue().setData(args.getRequired(0, JBinary.class).janitorGetHostValue()));
        DISPATCH.addMethod("getData", (self, process, args) -> {
            final byte[] data = self.janitorGetHostValue().getData();
            return data == null ? Janitor.NULL : Janitor.binary(data);
        });
    }

    public MustangFileAttachment() {
        super(DISPATCH, new FileAttachment());
    }

    public MustangFileAttachment(final FileAttachment fileAttachment) {
        super(DISPATCH, fileAttachment);
    }

}
