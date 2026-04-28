package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.Item;

public class MustangItem extends JanitorWrapper<Item> {
    public static WrapperDispatchTable<Item> DISPATCH = new WrapperDispatchTable<>(MustangItem::new);

    static {
        // TODO: quantity, price, product, tax (as object properties and as methods)
        DISPATCH.addBuilderMethod("setQuantity", (self, process, args) ->
            self.janitorGetHostValue().setQuantity(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBuilderMethod("setPrice",(self, process, args) ->
            self.janitorGetHostValue().setPrice(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBuilderMethod("setTax", (self, process, args) ->
            self.janitorGetHostValue().setTax(args.getRequiredJNumber(0).toBigDecimal()));

    }

    public MustangItem() {
        super(DISPATCH, new Item());
    }
}