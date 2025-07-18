package com.eischet.janitor;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorEnvironmentProvider;
import com.eischet.janitor.api.Janitor;
import org.junit.jupiter.api.BeforeAll;

public abstract class JanitorTest {

    @BeforeAll
    static void setUp() {
        Janitor.setUserProvider(new JanitorEnvironmentProvider() {
            @Override
            public JanitorEnvironment getCurrentEnvironment() {
                return TestEnv.env;
            }

            @Override
            public int priority() {
                return 0;
            }
        });
    }

}
