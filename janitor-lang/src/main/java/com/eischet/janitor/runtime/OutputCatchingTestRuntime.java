// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.modules.CollectionsModule;

import java.util.function.Consumer;

/** A runtime for tests, which collects everything a script prints instead of writing it to the console. */
public class OutputCatchingTestRuntime extends BaseRuntime {

    /**
     * @return a new runtime with a default environment
     */
    public static OutputCatchingTestRuntime fresh() {
        return fresh(null);
    }

    /**
     * @param environmentConfigurer configures the environment, e.g. by adding modules; may be null
     * @return a new runtime with a default environment
     */
    public static OutputCatchingTestRuntime fresh(final Consumer<JanitorEnvironment> environmentConfigurer) {
        final JanitorEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                System.err.println(message);
            }
        };
        if (environmentConfigurer != null) {
            environmentConfigurer.accept(env);
        }
        // TODO: I'd like to do this, but that would be a circular dependency because the modules (unwisely) depend on lang, where they should ideally depend on the api only: JanitorModulesCommon.registerCommonModules(env, true);
        env.addModule(CollectionsModule.REGISTRATION);
        return new OutputCatchingTestRuntime(env);
    }

    final StringBuffer output = new StringBuffer();

    private OutputCatchingTestRuntime(final JanitorEnvironment ENV) {
        super(ENV);
    }

    @Override
    public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
        int sz = args.size();
        for (int i = 0; i < sz; i++) {
            final JanitorObject argument = args.get(i);
            output.append(argument.janitorToString());
            if (i != sz - 1) {
                output.append(" ");
            }
        }
        output.append("\n");
        return JNull.NULL;
    }


    @Override
    public void warn(String warning) {
        output.append("WARNING: ").append(warning).append("\n");
    }

    /**
     * @return everything that was printed so far
     */
    public String getAllOutput() {
        return output.toString();
    }

    /** Discards everything that was printed so far. */
    public void resetOutput() {
        output.setLength(0);
    }
}
