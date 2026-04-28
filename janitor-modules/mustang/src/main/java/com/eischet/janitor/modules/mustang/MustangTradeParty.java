package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.TradeParty;

public class MustangTradeParty extends JanitorWrapper<TradeParty> {
    public static WrapperDispatchTable<TradeParty> DISPATCH = new WrapperDispatchTable<>(MustangTradeParty::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) ->
            self.janitorGetHostValue().setName(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(),
            (self, value) -> self.janitorGetHostValue().setName(value));
        DISPATCH.addBuilderMethod("setStreet", (self, process, args) ->
            self.janitorGetHostValue().setStreet(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("street", self -> self.janitorGetHostValue().getStreet(),
            (self, value) -> self.janitorGetHostValue().setStreet(value));
        DISPATCH.addBuilderMethod("setLocation", (self, process, args) ->
            self.janitorGetHostValue().setLocation(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("location", self -> self.janitorGetHostValue().getLocation(),
            (self, value) -> self.janitorGetHostValue().setLocation(value));
        DISPATCH.addBuilderMethod("setCountry", (self, process, args) ->
            self.janitorGetHostValue().setCountry(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("country", self -> self.janitorGetHostValue().getLocation(),
            (self, value) -> self.janitorGetHostValue().setCountry(value));
        DISPATCH.addBuilderMethod("setVatId", (self, process, args) ->
            self.janitorGetHostValue().setVATID(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("vatId", self -> self.janitorGetHostValue().getVATID(),
            (self, value) -> self.janitorGetHostValue().setVATID(value));
        DISPATCH.addBuilderMethod("setEmail", (self, process, args) ->
            self.janitorGetHostValue().setEmail(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("email", self -> self.janitorGetHostValue().getEmail(),
            (self, value) -> self.janitorGetHostValue().setEmail(value));
        DISPATCH.addBuilderMethod("setZip", (self, process, args) ->
            self.janitorGetHostValue().setZIP(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("zip", self -> self.janitorGetHostValue().getZIP(),
            (self, value) -> self.janitorGetHostValue().setZIP(value));

    }

    public MustangTradeParty() {
        super(DISPATCH, new TradeParty());
    }

    public MustangTradeParty(final TradeParty tradeParty) {
        super(DISPATCH, tradeParty);
    }

}
