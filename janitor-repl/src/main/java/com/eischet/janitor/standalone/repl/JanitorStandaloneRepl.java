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
import com.eischet.janitor.repl.ReplIO;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingLocale;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JanitorStandaloneRepl {


    public static void main(String[] args) {
        final List<String> argList = Arrays.asList(args);
        final boolean verbose = argList.contains("-v");
        final boolean plain = argList.contains("-p");
        if (plain) {
            runPlainConsole(verbose);
        } else {
            try {
                runJlineConsole(verbose);
            } catch (EndOfFileException e) {
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    protected static void runJlineConsole(final boolean verbose) throws IOException {
        final Terminal terminal = TerminalBuilder.builder().system(true).build();
        final LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        final PrintWriter writer = terminal.writer();

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
                    writer.print(janitorObject.janitorToString());
                }
                writer.println();
                return JNull.NULL;
            }
        };
        final ReplIO io = new ReplIO() {
            @Override
            public String readLine(final String prompt) throws IOException {
                return reader.readLine(prompt);
            }

            @Override
            public void print(final String text) {
                writer.print(text);
            }

            @Override
            public void println(final String text) {
                writer.println(text);
            }

            @Override
            public void error(final String text) {
                System.err.println(text);
            }

            @Override
            public void exception(final Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }

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

    protected static void runPlainConsole(final boolean verbose) {
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
