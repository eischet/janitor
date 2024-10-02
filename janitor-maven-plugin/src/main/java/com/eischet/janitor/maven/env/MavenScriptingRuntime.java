package com.eischet.janitor.maven.env;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.runtime.BaseRuntime;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class MavenScriptingRuntime extends BaseRuntime {

    private final SystemStreamLog log;

    public MavenScriptingRuntime(final JanitorEnvironment environment) {
        super(environment);
        this.log = new SystemStreamLog();
    }

    @Override
    public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
        final StringBuilder output = new StringBuilder();
        final int sz = args.size();
        final int last = sz - 1;
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
