package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.SchemedID;

/**
 * A scheme-qualified identifier, e.g. a GLN or a buyer/seller-assigned ID with its coding scheme.
 */
public class MustangSchemedID extends JanitorWrapper<SchemedID> {

    public static final WrapperDispatchTable<SchemedID> DISPATCH = new WrapperDispatchTable<>(MustangSchemedID::new);

    static {
        DISPATCH.addBuilderMethod("setScheme", (self, process, args) -> self.janitorGetHostValue().setScheme(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setId(args.getRequiredStringValue(0)));

        DISPATCH.addStringProperty("scheme", self -> self.janitorGetHostValue().getScheme(), (self, value) -> self.janitorGetHostValue().setScheme(value));
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getID(), (self, value) -> self.janitorGetHostValue().setId(value));
    }

    public MustangSchemedID() {
        super(DISPATCH, new SchemedID());
    }

    public MustangSchemedID(final SchemedID schemedID) {
        super(DISPATCH, schemedID);
    }

}
