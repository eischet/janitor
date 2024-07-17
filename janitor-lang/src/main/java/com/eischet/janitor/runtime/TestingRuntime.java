package com.eischet.janitor.runtime;

import com.eischet.janitor.env.JanitorDefaultEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingRuntime extends SLFLoggingRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(TestingRuntime.class);

    public TestingRuntime() {
        super(new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                LOG.warn("{}", message);
            }
        }, LOG);
    }

}
