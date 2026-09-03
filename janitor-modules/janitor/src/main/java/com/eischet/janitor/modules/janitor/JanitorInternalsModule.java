package com.eischet.janitor.modules.janitor;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.version.Revision;

import java.util.Arrays;

public class JanitorInternalsModule extends JanitorComposed<JanitorInternalsModule> implements com.eischet.janitor.api.modules.JanitorModule {

    public static final DispatchTable<JanitorInternalsModule> dispatcher = new DispatchTable<>(JanitorInternalsModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("janitor", JanitorInternalsModule::new);

    /**
     * The "host" is the hosting application.
     * You can set this to a name identifying your own application.
     *
     */
    public static String host = "unknown";

    /**
     * Host name for the standalone interpreter (module "janitor-repl").
     */
    public static final String HOST_STANDALONE = "standalone";

    /**
     * Host name for the maven plugin (module "janitor-maven-plugin").
     */
    public static final String HOST_MAVEN = "maven";

    /**
     * Command line arguments, to be set externally.
     */
    public static String[] args;

    static {
        dispatcher.addStringProperty("revision", self -> Revision.REVISION);
        dispatcher.addStringProperty("host", self -> host);
        dispatcher.addListProperty("args", self -> {
            if (args == null) {
                return Janitor.list();
            } else {
                return Janitor.list(Arrays.stream(args).map(Janitor::string));
            }
        });
    }

    public JanitorInternalsModule() {
        super(dispatcher);
    }

}
