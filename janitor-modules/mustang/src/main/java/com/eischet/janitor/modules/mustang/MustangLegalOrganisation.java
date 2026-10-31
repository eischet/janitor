// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.LegalOrganisation;

/**
 * The legal registration of a trade party, e.g. its trade register ID and trading business name.
 */
public class MustangLegalOrganisation extends JanitorWrapper<LegalOrganisation> {

    public static final WrapperDispatchTable<LegalOrganisation> DISPATCH = new WrapperDispatchTable<>(MustangLegalOrganisation::new);

    static {
        DISPATCH.addBuilderMethod("setSchemedID", (self, process, args) ->
            self.janitorGetHostValue().setSchemedID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "setSchemedID(schemedID): Sets the registration ID (a SchemedID) of the organisation; returns the organisation.");
        DISPATCH.addBuilderMethod("setTradingBusinessName", (self, process, args) ->
            self.janitorGetHostValue().setTradingBusinessName(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTradingBusinessName(name): Sets the trading business name; returns the organisation.");

        DISPATCH.addObjectProperty("schemedID",
            self -> self.janitorGetHostValue().getSchemedID() == null ? null : new MustangSchemedID(self.janitorGetHostValue().getSchemedID()),
            (self, value) -> self.janitorGetHostValue().setSchemedID(value == null ? null : value.janitorGetHostValue()),
            MustangSchemedID::new)
            .setMetaData(Janitor.MetaData.HELP, "schemedID: The registration ID of the organisation as a SchemedID, or null; writable.");
        DISPATCH.addStringProperty("tradingBusinessName", self -> self.janitorGetHostValue().getTradingBusinessName(),
            (self, value) -> self.janitorGetHostValue().setTradingBusinessName(value))
            .setMetaData(Janitor.MetaData.HELP, "tradingBusinessName: The trading business name of the organisation; writable.");
    }

    public MustangLegalOrganisation() {
        super(DISPATCH, new LegalOrganisation());
    }

    public MustangLegalOrganisation(final LegalOrganisation legalOrganisation) {
        super(DISPATCH, legalOrganisation);
    }

}
