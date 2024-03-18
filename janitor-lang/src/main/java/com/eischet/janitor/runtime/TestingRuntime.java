package com.eischet.janitor.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingRuntime extends SLFLoggingRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(TestingRuntime.class);

    public TestingRuntime() {
        super(LOG);
    }

}
