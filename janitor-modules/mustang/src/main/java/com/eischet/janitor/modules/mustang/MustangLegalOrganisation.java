package com.eischet.janitor.modules.mustang;

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
            self.janitorGetHostValue().setSchemedID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()));
        DISPATCH.addBuilderMethod("setTradingBusinessName", (self, process, args) ->
            self.janitorGetHostValue().setTradingBusinessName(args.getRequiredStringValue(0)));

        DISPATCH.addObjectProperty("schemedID",
            self -> self.janitorGetHostValue().getSchemedID() == null ? null : new MustangSchemedID(self.janitorGetHostValue().getSchemedID()),
            (self, value) -> self.janitorGetHostValue().setSchemedID(value == null ? null : value.janitorGetHostValue()),
            MustangSchemedID::new);
        DISPATCH.addStringProperty("tradingBusinessName", self -> self.janitorGetHostValue().getTradingBusinessName(),
            (self, value) -> self.janitorGetHostValue().setTradingBusinessName(value));
    }

    public MustangLegalOrganisation() {
        super(DISPATCH, new LegalOrganisation());
    }

    public MustangLegalOrganisation(final LegalOrganisation legalOrganisation) {
        super(DISPATCH, legalOrganisation);
    }

}
