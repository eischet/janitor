package com.eischet.janitor.modules.brrr;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class BrrrModule extends JanitorComposed<BrrrModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("brrr", BrrrModule::new);
    public static final DispatchTable<BrrrModule> DISPATCH = new DispatchTable<>(BrrrModule::new);

    static {
        DISPATCH.addMethod("Message", (self, process, args) -> {
            final BrrrMessage message = new BrrrMessage();
            if (args.size() > 0) {
                final JMap map = args.getRequired(0, JMap.class);
                map.applyTo(process, message);
            }
            return message;
        });
    }

    public BrrrModule() {
        super(DISPATCH);
    }

}
