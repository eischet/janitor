package com.eischet.janitor.demo;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.types.JNull;
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
            script.run(g -> {});
        } catch (Exception e) {

            e.printStackTrace(System.err);
        }
    }


    public static class DemoEnvironment extends JanitorDefaultEnvironment {

        public DemoEnvironment() {
            super(new JanitorFormattingLocale(Locale.getDefault()));
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
        public JanitorObject print(final JanitorScriptProcess rs, final JCallArgs args) {
            for (final JanitorObject janitorObject : args.getList()) {
                System.out.print(janitorObject.janitorToString());
            }
            System.out.println();
            return JNull.NULL;
        }

        @Override
        public void warn(final String warning) {
            getEnviroment().warn(warning);
        }
    }

}