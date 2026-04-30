package com.eischet.janitor.runtime;

import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.logging.JanitorLogger;

public class TestingRuntime extends SLFLoggingRuntime {

    private static final JanitorLogger LOG = JanitorLogger.getLogger(TestingRuntime.class);

    public TestingRuntime() {
        super(new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                LOG.warn("{}", message);
            }
        }, LOG);
    }

}
