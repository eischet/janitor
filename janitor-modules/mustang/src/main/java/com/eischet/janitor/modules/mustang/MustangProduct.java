package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.Product;

public class MustangProduct extends JanitorWrapper<Product> {
    public static WrapperDispatchTable<Product> DISPATCH = new WrapperDispatchTable<>(MustangProduct::new);

    static {

    }

    public MustangProduct() {
        super(DISPATCH, new Product());
    }

}
