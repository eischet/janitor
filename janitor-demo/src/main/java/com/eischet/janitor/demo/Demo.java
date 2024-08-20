package com.eischet.janitor.demo;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingLocale;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Demo {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar janitor-demo.jar <script.jan>");
            System.exit(1);
        }
        try {
            final String scriptName = args[0];
            if (scriptName == null || scriptName.isBlank()) {
                System.err.println("Script name is empty");
                System.exit(1);
            }
            final Path scriptPath = Path.of(scriptName);
            if (!Files.exists(scriptPath)) {
                System.err.println("Script file does not exist: " + scriptPath);
                System.exit(1);
            }
            final String fullScript = Files.readString(scriptPath, StandardCharsets.UTF_8);
            // create an environment -- usually, a host will have one of these, maybe more for different kinds of sandboxes:
            final DemoEnvironment env = new DemoEnvironment();
            // create a runtime -- this is the thing that actually runs the scripts:
            final DemoRuntime runtime = new DemoRuntime(env);
            final RunnableScript script = runtime.compile(scriptPath.getFileName().toString(), fullScript);

            // Allow command line arguments to be passed to the script:
            final JList scriptArgs = runtime.getBuiltinTypes().list();
            for (int i = 1; i < args.length; i++) {
                scriptArgs.add(runtime.getBuiltinTypes().string(args[i]));
            }

            script.run(g -> g.bind("args", scriptArgs));
        } catch (Exception e) {

            e.printStackTrace(System.err);
        }
    }


    public static class DemoEnvironment extends JanitorDefaultEnvironment {

        public DemoEnvironment() {
            super(new JanitorFormattingLocale(Locale.getDefault()));

            // Add some built-in symbols, which will be available to all scripts.
            // Better don't make them modifiable, because modifications will stick!
            setupBuiltinScope(globals -> {
                // put a "version" string into the global scope
                globals.bind("version", "0.9.6");
                // place an "exit()" function in the global scope.
                // note that I wouldn't want to use System.exit() in a real world use case,
                // but for the demo I guess it's fine.
                globals.bind("exit", ((JCallable) (runningScript, args) -> {
                    System.err.println("hard exit!");
                    System.exit(0);
                    return JNull.NULL;
                }).asObject("exit"));
            });
        }

        @Override
        public void warn(final String message) {
            System.out.println("WARN: " + message);
        }
    }

    public static class DemoRuntime extends BaseRuntime {

        public DemoRuntime(final JanitorEnvironment environment) {
            super(environment);
        }

        @Override
        public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
            for (final JanitorObject janitorObject : args.getList()) {
                System.out.print(janitorObject.janitorToString());
            }
            System.out.println();
            return JNull.NULL;
        }

        @Override
        public void warn(final String warning) {
            getEnvironment().warn(warning);
        }
    }

}
