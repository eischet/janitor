package com.eischet.janitor;

import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import com.eischet.janitor.runtime.modules.CollectionsModule;

import java.util.function.Consumer;

public class TestEnv {
    public static final JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
        @Override
        public void warn(final String message) {
            System.err.println("WARN: " + message);
        }
    };
    public static final Consumer<Scope> NO_GLOBALS = globals -> {
    };

    static {
        // LATER: move these to a saner place!
        env.registerModule(CollectionsModule.REGISTRATION);
    }
}
