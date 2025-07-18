package com.eischet.janitor.api;

/**
 * Retrieves a JanitorEnvironment instance.
 * <p>
 * Most client apps will probably only ever use one of those, can can store them in a global variable.
 * Clients that in fact use more than one, e.g. for different levels of sandboxing, like "admin code vs. user code", can provide an appropriate
 * implementation.
 * </p>
 *
 */
public interface JanitorEnvironmentProvider {
    JanitorEnvironment getCurrentEnvironment();

    default int priority() {
        return 0;
    }

    static JanitorEnvironmentProvider returning(JanitorEnvironment env) {
        return new JanitorEnvironmentProvider() {
            @Override
            public JanitorEnvironment getCurrentEnvironment() {
                return env;
            }
        };
    }
}
