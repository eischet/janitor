package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import org.slf4j.Logger;

public class SLFLoggingRuntime extends BaseRuntime {

    protected final Logger log;

    public SLFLoggingRuntime(final JanitorEnvironment env, final Logger log) {
        super(env);
        this.log = log;
    }

    @Override
    public void warn(String warning) {
        log.warn(warning);
    }

    @Override
    public JanitorObject print(final JanitorScriptProcess rs, final JCallArgs args) {
        final StringBuilder output = new StringBuilder();
        final int sz = args.size();
        final int last = sz -1;
        for (int i = 0; i < sz; i++) {
            final JanitorObject argument = args.get(i);
            output.append(argument.janitorToString());
            if (i != last) {
                output.append(" ");
            }
        }
        log.info(output.toString());
        return JNull.NULL;
    }

}
