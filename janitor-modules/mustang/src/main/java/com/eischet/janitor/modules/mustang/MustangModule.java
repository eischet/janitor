package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Date;

public class MustangModule extends JanitorComposed<MustangModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("mustang", MustangModule::new);

    public static final DispatchTable<MustangModule> DISPATCH = new DispatchTable<>(MustangModule::new);

    static {
        DISPATCH.addConstructor("BankDetails", MustangBankDetails::new);
        DISPATCH.addConstructor("Invoice", MustangInvoice::new);
        DISPATCH.addConstructor("Item", MustangItem::new);
        DISPATCH.addConstructor("Product", MustangProduct::new);
        DISPATCH.addConstructor("TradeParty", MustangTradeParty::new);
        DISPATCH.addConstructor("Exporter", MustangExporter::new);
    }

    public MustangModule() {
        super(DISPATCH);
    }


}
