package com.eischet.janitor.standalone.repl;

import java.util.Arrays;
import java.util.List;

/**
 * Parses the "janitor" launcher's command line the way java/python do it: option flags come first and
 * are handled by the launcher itself; the first argument that isn't a recognized flag is taken as a
 * script file to run, and everything after that belongs to the script (exposed as janitor.args in
 * JanitorInternalsModule), not to the launcher -- so a flag-looking string after the script file (e.g.
 * "janitor foo.janitor -v") is passed straight through to the script rather than being consumed here.
 */
final class ReplCommandLine {

    private final boolean verbose;
    private final boolean plain;
    private final boolean interactive;
    private final boolean quiet;
    private final boolean helpRequested;
    private final boolean versionRequested;
    private final String scriptFile;
    private final List<String> scriptArgs;
    private final String error;

    private ReplCommandLine(final boolean verbose, final boolean plain, final boolean interactive, final boolean quiet,
                            final boolean helpRequested, final boolean versionRequested,
                            final String scriptFile, final List<String> scriptArgs, final String error) {
        this.verbose = verbose;
        this.plain = plain;
        this.interactive = interactive;
        this.quiet = quiet;
        this.helpRequested = helpRequested;
        this.versionRequested = versionRequested;
        this.scriptFile = scriptFile;
        this.scriptArgs = scriptArgs;
        this.error = error;
    }

    static ReplCommandLine parse(final String[] args) {
        boolean verbose = false;
        boolean plain = false;
        boolean interactive = false;
        boolean quiet = false;
        boolean helpRequested = false;
        boolean versionRequested = false;
        String scriptFile = null;

        int i = 0;
        for (; i < args.length; i++) {
            final String arg = args[i];
            if (!arg.startsWith("-")) {
                scriptFile = arg;
                i++;
                break;
            }
            switch (arg) {
                case "-v" -> verbose = true;
                case "-p" -> plain = true;
                case "-i" -> interactive = true;
                case "-q" -> quiet = true;
                case "-h", "--help" -> helpRequested = true;
                case "-V", "--version" -> versionRequested = true;
                default -> {
                    return new ReplCommandLine(verbose, plain, false, false, false, false, null, List.of(), "unrecognized option: " + arg);
                }
            }
        }

        final List<String> scriptArgs = i < args.length ? Arrays.asList(args).subList(i, args.length) : List.of();
        return new ReplCommandLine(verbose, plain, interactive, quiet, helpRequested, versionRequested, scriptFile, scriptArgs, null);
    }

    boolean isVerbose() {
        return verbose;
    }

    boolean isPlain() {
        return plain;
    }

    /**
     * True if -i was given: like "python -i script.py", after running the script file, drop into an
     * interactive shell that retains the script's top-level scope (its variables, imports, function
     * defs, ...), instead of exiting. Has no extra effect without a script file, since that case is
     * already interactive.
     */
    boolean isInteractive() {
        return interactive;
    }

    /**
     * True if -q was given: suppress the REPL's welcome banner (logo + version), even for a plain
     * interactive shell with no script file. -i already suppresses the banner on its own when a script
     * ran first; -q additionally covers the "just start the REPL" case.
     */
    boolean isQuiet() {
        return quiet;
    }

    boolean isHelpRequested() {
        return helpRequested;
    }

    /**
     * True if -V/--version was given: print the interpreter revision and exit immediately, like
     * "java -version" or "python -V", without touching a script file or starting anything.
     */
    boolean isVersionRequested() {
        return versionRequested;
    }

    /**
     * The script file to run, or {@code null} to start the interactive REPL instead.
     */
    String getScriptFile() {
        return scriptFile;
    }

    /**
     * The arguments following the script file, meant for the script itself -- empty if there is no
     * script file, or the script file was given no further arguments.
     */
    List<String> getScriptArgs() {
        return scriptArgs;
    }

    /**
     * A human-readable message if parsing failed (an unrecognized option before the script file), or
     * {@code null} if parsing succeeded.
     */
    String getError() {
        return error;
    }

}
