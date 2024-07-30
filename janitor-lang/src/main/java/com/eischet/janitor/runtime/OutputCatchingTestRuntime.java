package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.modules.CollectionsModule;

public class OutputCatchingTestRuntime extends BaseRuntime {

    public static OutputCatchingTestRuntime fresh() {
        final JanitorEnvironment ENV = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                System.err.println(message);
            }
        };
        ENV.registerModule(CollectionsModule.REGISTRATION);
        return new OutputCatchingTestRuntime(ENV);
    }

    final StringBuffer output = new StringBuffer();

    private OutputCatchingTestRuntime(final JanitorEnvironment ENV) {
        super(ENV);
    }

    @Override
    public JanitorObject print(final JanitorScriptProcess rs, final JCallArgs args) {
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

    public String getAllOutput() {
        return output.toString();
    }

    public void resetOutput() {
        output.setLength(0);
    }
}
