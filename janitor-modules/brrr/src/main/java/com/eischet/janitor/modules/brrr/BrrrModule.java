package com.eischet.janitor.modules.brrr;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class BrrrModule extends JanitorComposed<BrrrModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("brrr", BrrrModule::new);
    public static final DispatchTable<BrrrModule> DISPATCH = new DispatchTable<>(BrrrModule::new);

    static {
        DISPATCH.addObjectProperty("Message", self -> new BrrrMessage());
    }

    public BrrrModule() {
        super(DISPATCH);
    }

}
