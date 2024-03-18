package com.eischet.janitor.runtime;

import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.runtime.types.JCallArgs;

public class OutputCatchingTestRuntime extends BaseRuntime {


    final StringBuffer output = new StringBuffer();

    public OutputCatchingTestRuntime() {
        super(new JanitorFormattingGerman());
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
