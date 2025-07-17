package com.eischet.janitor.standalone.repl;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.repl.ConsoleReplIO;
import com.eischet.janitor.repl.JanitorRepl;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingLocale;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JanitorStandaloneRepl {


    public static void main(String[] args) {
        final List<String> argList = Arrays.asList(args);
        final boolean verbose = argList.contains("-v");

        JanitorEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingLocale(Locale.getDefault())) {
            @Override
            public void warn(final String message) {
                System.err.println(message);
            }
        };
        JanitorRuntime runtime = new BaseRuntime(env) {
            @Override
            public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
                for (final JanitorObject janitorObject : args.getList()) {
                    System.out.print(janitorObject.janitorToString());
                }
                System.out.println();
                return JNull.NULL;
            }
        };
        final ConsoleReplIO io = new ConsoleReplIO() {
            @Override
            public void verbose(final String text) {
                if (verbose) {
                    System.out.println("| " + text);
                }
            }
        };
        JanitorRepl repl = new JanitorRepl(runtime, io);
        try {
            repl.run();
        } catch (IOException e) {
            io.exception(e);
        }
    }

}
