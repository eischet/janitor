package com.eischet.janitor;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import org.intellij.lang.annotations.Language;

public class TestEnv {
    public static final JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
        @Override
        public void warn(final String message) {
            System.err.println("WARN: " + message);
        }
    };

    static {
        // LATER: move these to a saner place!
        env.addModule(CollectionsModule.REGISTRATION);
    }

    interface ScriptConsumer {
        void accept(@Language("Janitor") String script) throws JanitorRuntimeException;
    }
}
