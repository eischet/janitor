package com.eischet.janitor.modules.janitor;

import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.version.Revision;

public class JanitorInternalsModule extends JanitorComposed<JanitorInternalsModule> implements com.eischet.janitor.api.modules.JanitorModule {

    private static final DispatchTable<JanitorInternalsModule> dispatcher = new DispatchTable<>(JanitorInternalsModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("janitor", JanitorInternalsModule::new);

    static {
        dispatcher.addStringProperty("revision", self -> Revision.REVISION);
    }

    public JanitorInternalsModule() {
        super(dispatcher);
    }

}
