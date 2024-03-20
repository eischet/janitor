package com.eischet.janitor.cleanup.runtime;

import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import org.slf4j.Logger;

public class SLFLoggingRuntime extends BaseRuntime {

    protected final Logger log;

    public SLFLoggingRuntime(final Logger log) {
        super(new JanitorFormattingGerman());
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
