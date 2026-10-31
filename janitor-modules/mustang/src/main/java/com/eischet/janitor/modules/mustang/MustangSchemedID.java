// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.SchemedID;

/**
 * A scheme-qualified identifier, e.g. a GLN or a buyer/seller-assigned ID with its coding scheme.
 */
public class MustangSchemedID extends JanitorWrapper<SchemedID> {

    public static final WrapperDispatchTable<SchemedID> DISPATCH = new WrapperDispatchTable<>(MustangSchemedID::new);

    static {
        DISPATCH.addBuilderMethod("setScheme", (self, process, args) -> self.janitorGetHostValue().setScheme(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setScheme(scheme): Sets the identification scheme of the ID; returns the SchemedID.");
        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setId(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setId(id): Sets the identifier value; returns the SchemedID.");

        DISPATCH.addStringProperty("scheme", self -> self.janitorGetHostValue().getScheme(), (self, value) -> self.janitorGetHostValue().setScheme(value))
            .setMetaData(Janitor.MetaData.HELP, "scheme: The identification scheme of the ID; writable.");
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getID(), (self, value) -> self.janitorGetHostValue().setId(value))
            .setMetaData(Janitor.MetaData.HELP, "id: The identifier value; writable.");
    }

    public MustangSchemedID() {
        super(DISPATCH, new SchemedID());
    }

    public MustangSchemedID(final SchemedID schemedID) {
        super(DISPATCH, schemedID);
    }

}
