package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.env.JanitorDefaultEnvironment;

public class OutputCatchingTestRuntime extends BaseRuntime {

    private static final JanitorEnvironment ENV = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
        @Override
        public void warn(final String message) {
            System.err.println(message);
        }
    };

    final StringBuffer output = new StringBuffer();

    public OutputCatchingTestRuntime() {
        super(ENV);
    }

    public OutputCatchingTestRuntime(final JanitorEnvironment env) {
        super(env);
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
