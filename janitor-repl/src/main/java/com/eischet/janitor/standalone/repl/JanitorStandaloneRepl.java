package com.eischet.janitor.standalone.repl;

import com.eischet.janitor.api.*;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.docusign.DocusignModule;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.modules.common.JanitorModulesCommon;
import com.eischet.janitor.modules.commonmark.CommonMarkModule;
import com.eischet.janitor.modules.janitor.JanitorInternalsModule;
import com.eischet.janitor.modules.mustang.MustangModule;
import com.eischet.janitor.modules.pdf.PdfModule;
import com.eischet.janitor.repl.ConsoleReplIO;
import com.eischet.janitor.repl.JanitorRepl;
import com.eischet.janitor.repl.ReplIO;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingLocale;
import com.eischet.janitor.version.Revision;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Standalone launcher: "janitor" (or "janitor.exe" via install4j).
 * <p>
 * Command line handling mirrors java/python: {@code janitor [options] [script.janitor [script-args...]]}.
 * Without a script file, it starts an interactive REPL (jline-based by default, or a plain console with
 * -p). Options (-v, -p, -i, -h/--help) only apply when they appear before the script file; everything
 * after the script file is passed straight through to the script as janitor.args, not interpreted here.
 * <p>
 * -i mirrors "python -i script.py": run the script, then drop into the interactive shell with the
 * script's top-level scope (variables, imports, function defs, ...) still in place, instead of exiting.
 * -q suppresses the welcome banner even for a plain interactive shell; -V prints the revision and exits.
 */
public class JanitorStandaloneRepl {


    public static void main(String[] args) {
        final ReplCommandLine cli = ReplCommandLine.parse(args);
        if (cli.getError() != null) {
            System.err.println(cli.getError());
            printUsage(System.err);
            System.exit(2);
            return;
        }
        if (cli.isHelpRequested()) {
            printUsage(System.out);
            return;
        }
        if (cli.isVersionRequested()) {
            System.out.println(Revision.REVISION);
            return;
        }

        JanitorInternalsModule.host = JanitorInternalsModule.HOST_STANDALONE;
        JanitorInternalsModule.args = cli.getScriptArgs().toArray(new String[0]);

        final JanitorEnvironment env = createEnvironment();
        Janitor.setUserProvider(() -> env);

        final boolean ranAScript = cli.getScriptFile() != null;
        Scope seedScope = null;
        if (ranAScript) {
            seedScope = runScriptFile(env, Path.of(cli.getScriptFile()), cli.isVerbose(), cli.isInteractive());
            if (!cli.isInteractive()) {
                return; // runScriptFile() already exited(1) on failure; on success, we're simply done
            }
        }

        final boolean suppressBanner = ranAScript || cli.isQuiet();
        if (cli.isPlain()) {
            runPlainConsole(env, cli.isVerbose(), seedScope, suppressBanner);
        } else {
            try {
                runJlineConsole(env, cli.isVerbose(), seedScope, suppressBanner);
            } catch (EndOfFileException e) {
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static void printUsage(final PrintStream out) {
        out.println("""
                Usage: janitor [options] [script.janitor [script-args...]]

                Without a script file, starts an interactive REPL.

                Options (must appear before the script file, if any):
                  -v            verbose output
                  -p            use a plain console instead of the jline-based one
                  -i            after running the script, drop into an interactive shell that
                                retains the script's scope, instead of exiting
                  -q            suppress the welcome banner, even for a plain interactive shell
                  -h, --help    show this help and exit
                  -V, --version print the interpreter revision and exit

                Arguments after the script file are passed to the script (see janitor.args).""");
    }

    /**
     * Runs a script file, the way "java SomeClass args..." or "python script.py args..." run a program:
     * compile the file, run it once, print any uncaught error to stderr.
     * <p>
     * Without -i (interactive == false), this exits the process with a non-zero status on failure, and
     * returns null (the caller is expected to exit right after, on success too).
     * <p>
     * With -i, this never exits: on success it returns the script's resulting top-level scope, ready to
     * be merged into an interactive shell; on failure (matching "python -i", which still drops you into
     * a shell after a failed script) it also returns null, but the caller still starts an interactive
     * shell -- just with a fresh scope, since a script that failed mid-run doesn't hand back one.
     */
    static @Nullable Scope runScriptFile(final JanitorEnvironment env, final Path scriptPath, final boolean verbose, final boolean interactive) {
        if (!Files.exists(scriptPath)) {
            System.err.println("Script file does not exist: " + scriptPath);
            System.exit(1);
            return null;
        }
        final BaseRuntime runtime = createStdoutRuntime(env);
        try {
            final String source = Files.readString(scriptPath, StandardCharsets.UTF_8);
            final RunnableScript script = runtime.compile(scriptPath.getFileName().toString(), source);
            if (interactive) {
                return script.runAndKeepGlobals().getScope();
            } else {
                script.run(globals -> {
                });
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error reading script file '" + scriptPath + "': " + e.getMessage());
            if (!interactive) {
                System.exit(1);
            }
        } catch (JanitorCompilerException | JanitorRuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace(System.err);
            }
            if (!interactive) {
                System.exit(1);
            }
        }
        return null;
    }

    /**
     * Sets up an environment with all the modules the standalone REPL/launcher offers, shared by the
     * jline console, the plain console, and non-interactive script execution.
     */
    static JanitorEnvironment createEnvironment() {
        final JanitorEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingLocale(Locale.getDefault())) {
            @Override
            public void warn(final String message) {
                System.err.println(message);
            }
        };

        JanitorModulesCommon.registerCommonModules(env, true);

        // these are not included in "common" because they bring additional dependencies:
        env.addModule(CommonMarkModule.REGISTRATION);
        env.addModule(MustangModule.REGISTRATION);
        env.addModule(DocusignModule.REGISTRATION);
        env.addModule(PdfModule.REGISTRATION);

        return env;
    }

    /**
     * A runtime whose print() goes straight to System.out, for the plain console and script execution.
     */
    static BaseRuntime createStdoutRuntime(final JanitorEnvironment env) {
        return new BaseRuntime(env) {
            @Override
            public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
                for (final JanitorObject janitorObject : args.getList()) {
                    System.out.print(janitorObject.janitorToString());
                }
                System.out.println();
                return JNull.NULL;
            }
        };
    }

    protected static void runJlineConsole(final JanitorEnvironment env, final boolean verbose, final @Nullable Scope seedScope, final boolean suppressBanner) throws IOException {

        final Terminal terminal = TerminalBuilder.builder().system(true).build();
        final LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        final PrintWriter writer = terminal.writer();

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
                if (shouldPrintStackTrace(e)) {
                    e.printStackTrace(System.err);
                }
            }

            @Override
            public void verbose(final String text) {
                if (verbose) {
                    System.out.println("| " + text);
                }
            }
        };
        JanitorRepl repl = new JanitorRepl(runtime, io);
        seedRepl(repl, seedScope, suppressBanner);
        try {
            repl.run();
        } catch (IOException e) {
            io.exception(e);
        }

    }

    protected static void runPlainConsole(final JanitorEnvironment env, final boolean verbose, final @Nullable Scope seedScope, final boolean suppressBanner) {
        final JanitorRuntime runtime = createStdoutRuntime(env);
        final ConsoleReplIO io = new ConsoleReplIO() {
            @Override
            public void verbose(final String text) {
                if (verbose) {
                    System.out.println("| " + text);
                }
            }
        };
        JanitorRepl repl = new JanitorRepl(runtime, io);
        seedRepl(repl, seedScope, suppressBanner);
        try {
            repl.run();
        } catch (IOException e) {
            io.exception(e);
        }
    }

    /**
     * For -i: merges a just-run script's top-level scope into the freshly created REPL's global scope
     * (seedScope is null if the script failed before producing one -- see runScriptFile()).
     * <p>
     * suppressBanner skips the REPL's welcome banner -- either because the user already saw a script
     * run (-i), or because they explicitly asked for quiet (-q).
     */
    static void seedRepl(final JanitorRepl repl, final @Nullable Scope seedScope, final boolean suppressBanner) {
        if (seedScope != null) {
            repl.getGlobalScope().replEatScope(seedScope);
        }
        if (suppressBanner) {
            repl.setLogo(null);
        }
    }

}
