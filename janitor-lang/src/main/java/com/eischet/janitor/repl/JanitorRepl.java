package com.eischet.janitor.repl;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.compiler.CompilerError;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.compiler.ast.Ast;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.runtime.RunningScriptProcess;
import com.eischet.janitor.version.Revision;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;

/**
 * Janitor REPL, read-evaluate-print-loop.
 * This will become an interactive interpreter, but is not finished/working yet.
 * TODO: finish the interactive interpreter.
 */
public class JanitorRepl {

    private static final String DEFAULT_PROMPT = "janitor> ";
    private static final String CONTINUE_PROMPT = "... ";

    // figlet -f small janitor
    private static final String LOGO = """
               _           _ _
              (_)__ _ _ _ (_) |_ ___ _ _
              | / _` | ' \\| |  _/ _ \\ '_|
             _/ \\__,_|_||_|_|\\__\\___/_|
            |__/
            "Did you stick a penny in there?"
            """;

    private final ScriptSource module;
    private final Scope globalScope;
    private final JanitorRuntime runtime;
    private final ReplIO io;

    private String logo = LOGO;
    private String defaultPrompt = DEFAULT_PROMPT;
    private String continuePrompt = CONTINUE_PROMPT;
    private boolean quit;
    final StringBuilder buffer = new StringBuilder();
    String prompt = defaultPrompt;

    public JanitorRepl(final JanitorRuntime runtime, final ReplIO io) {
        this.runtime = runtime;
        this.io = io;
        module = new ScriptSource("repl", "");
        globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module);
    }

    private Fragment parseFragment(final String text) {
        final CharStream stream = CharStreams.fromString(text);
        final JanitorLexer lexer = new JanitorLexer(stream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final JanitorParser parser = new JanitorParser(tokenStream);
        parser.setBuildParseTree(true);

        final boolean[] incomplete = {false};
        final boolean[] missingTerminator = {false};

        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer,
                                    final Object offendingSymbol,
                                    final int line,
                                    final int charPositionInLine,
                                    final String msg,
                                    final RecognitionException e) {
                if ("missing STMT_TERM at '<EOF>'".equals(msg)) {
                    missingTerminator[0] = true;
                    return;
                }
                if (msg != null && (msg.contains("extraneous input '<EOF>'") || msg.contains("missing") && msg.contains("at '<EOF>'"))) {
                    io.verbose("looks incomplete!");
                    incomplete[0] = true;
                }
                if (e != null) {
                    io.verbose("Recognition Exception:");
                    io.verbose(e.getMessage());
                    if (e instanceof InputMismatchException) {
                        incomplete[0] = true;
                        return;
                    }
                }
                io.error("Syntax Error: " + msg);
            }

            @Override
            public void reportAmbiguity(final Parser recognizer,
                                        final DFA dfa,
                                        final int startIndex,
                                        final int stopIndex,
                                        final boolean exact,
                                        final BitSet ambigAlts,
                                        final ATNConfigSet configs) {
                io.verbose("Ambiguity: " + ambigAlts.toString());
            }

            @Override
            public void reportAttemptingFullContext(final Parser recognizer,
                                                    final DFA dfa,
                                                    final int startIndex,
                                                    final int stopIndex,
                                                    final BitSet conflictingAlts,
                                                    final ATNConfigSet configs) {
                io.verbose("Attempting: " + conflictingAlts.toString());
            }

            @Override
            public void reportContextSensitivity(final Parser recognizer,
                                                 final DFA dfa,
                                                 final int startIndex,
                                                 final int stopIndex,
                                                 final int prediction,
                                                 final ATNConfigSet configs) {
                io.verbose("Context Sensitivity: " + prediction);
            }
        });
        if (missingTerminator[0]) {
            return new Fragment(true);
        }
        final JanitorParser.ScriptContext scriptContext = parser.script();
        if (incomplete[0]) {
            io.verbose("--> Incomplete!");
            return new Fragment(PartialParseResult.INCOMPLETE);
        }
        return new Fragment(scriptContext);
    }

    public PartialParseResult parse(final String text) throws JanitorControlFlowException, JanitorRuntimeException, CompilerError {
        if (endsInsideMultilineConstruct(text) || hasUnclosedBrackets(text)) {
            io.verbose("looks incomplete");
            return PartialParseResult.INCOMPLETE;
        }

        Fragment fragment = parseFragment(text);
        if (fragment.isMissingStatementTerminator()) {
            final Fragment betterFragment = parseFragment(text + ";");
            if (betterFragment.getScriptContext() != null) {
                fragment = betterFragment;
            } else {
                io.verbose("missing statement terminator, and adding one did not really help!");
            }
        }
        if (fragment.getParseResult() == PartialParseResult.INCOMPLETE) {
            io.verbose("Fragment incomplete");
            return PartialParseResult.INCOMPLETE;
        }

        // TODO: on 'missing STMT_TERM', try to parse the same text again


        final ScriptSource module = new ScriptSource("repl", text);
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(runtime.getEnvironment(), module, false, text);
        io.verbose("start compile");
        final Ast compiledText = compiler.visit(fragment.getScriptContext());
        if (compiledText instanceof Statement compiledStatement) {
            final Location loc = Location.startOf(module);
            final Script partialScript = new Script(loc, List.of(compiledStatement), text);
            final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, module.getName(), partialScript);
            try {
                partialScript.execute(process);
                final JanitorObject result = process.getScriptResult();
                if (result != JNull.NULL) {
                    io.println(String.valueOf(result));
                }
            } catch (ReturnStatement.Return ret) {
                final JanitorObject returnResult = ret.getValue();
                io.println("Return Result: " + returnResult);
            }
            // Catch all the globals defined by this script and stuff them into our global scope, making them available for the next round:
            globalScope.replEatScope(process.getMainScope());
            // process.getMainScope().

            // compiledStatement.execute(new RunningScriptProcess(null, null, partialScript);

        } else {
            io.verbose("Not a statement");
        }
        return PartialParseResult.OK;
    }



    /**
     * The states {@link #scan(String)} can be in while walking the text one character at a time.
     * Brackets are only counted in {@link #NORMAL}; everything else exists only to know when we
     * are allowed to leave it again (a closing quote/delimiter, or a newline where the grammar
     * does not permit one).
     */
    private enum ScanMode {
        NORMAL, LINE_COMMENT, BLOCK_COMMENT, SINGLE_QUOTED, DOUBLE_QUOTED, TRIPLE_SINGLE_QUOTED, TRIPLE_DOUBLE_QUOTED
    }

    /**
     * @param parenBalance     net count of unmatched '(' (positive) over the whole text
     * @param braceBalance     net count of unmatched '{' (positive) over the whole text
     * @param bracketBalance   net count of unmatched '[' (positive) over the whole text
     * @param endsMidConstruct true if the text ends while still inside a triple-quoted string or
     *                         a block comment, i.e. a construct that legitimately spans lines and
     *                         is simply not finished yet
     */
    private record ScanResult(int parenBalance, int braceBalance, int bracketBalance, boolean endsMidConstruct) {
    }

    /**
     * Walks the text once, tracking whether we are inside a string literal or comment, so that
     * brackets and triple-quote delimiters occurring inside those are not mistaken for real ones
     * (e.g. {@code print("(")} or {@code // (} must not affect the bracket balance).
     * <p>
     * This intentionally does not use the real {@link JanitorLexer}: on genuinely incomplete or
     * malformed input (which is the normal case here, since the REPL calls this on every
     * partially-typed line) the generated lexer's error recovery consumes/skips characters in
     * ways that are hard to reason about, whereas this hand-rolled scan only needs to answer one
     * narrow question -- "are we still inside something that is allowed to continue on the next
     * line" -- and always terminates cleanly, one char at a time, regardless of how broken the
     * input is.
     */
    private ScanResult scan(final String text) {
        ScanMode mode = ScanMode.NORMAL;
        int parens = 0, braces = 0, brackets = 0;
        final int length = text.length();
        int i = 0;
        while (i < length) {
            final char c = text.charAt(i);
            switch (mode) {
                case NORMAL -> {
                    if (regionMatches(text, i, "//")) {
                        mode = ScanMode.LINE_COMMENT;
                        i += 2;
                        continue;
                    }
                    if (regionMatches(text, i, "/*")) {
                        mode = ScanMode.BLOCK_COMMENT;
                        i += 2;
                        continue;
                    }
                    if (regionMatches(text, i, "'''")) {
                        mode = ScanMode.TRIPLE_SINGLE_QUOTED;
                        i += 3;
                        continue;
                    }
                    if (regionMatches(text, i, "\"\"\"")) {
                        mode = ScanMode.TRIPLE_DOUBLE_QUOTED;
                        i += 3;
                        continue;
                    }
                    switch (c) {
                        case '\'' -> mode = ScanMode.SINGLE_QUOTED;
                        case '"' -> mode = ScanMode.DOUBLE_QUOTED;
                        case '(' -> parens++;
                        case ')' -> parens--;
                        case '{' -> braces++;
                        case '}' -> braces--;
                        case '[' -> brackets++;
                        case ']' -> brackets--;
                        default -> { }
                    }
                    i++;
                }
                case LINE_COMMENT -> {
                    // a line comment runs to the end of the (physical) line, then we're back to normal
                    if (c == '\n' || c == '\r') {
                        mode = ScanMode.NORMAL;
                    }
                    i++;
                }
                case BLOCK_COMMENT -> {
                    if (regionMatches(text, i, "*/")) {
                        mode = ScanMode.NORMAL;
                        i += 2;
                        continue;
                    }
                    i++;
                }
                case SINGLE_QUOTED, DOUBLE_QUOTED -> {
                    final char quote = mode == ScanMode.SINGLE_QUOTED ? '\'' : '"';
                    if (c == '\n' || c == '\r') {
                        // the grammar does not allow these to span lines, so this is already a
                        // syntax error that the real parser will report; stop treating it as an
                        // open string and re-process this char in NORMAL mode instead of hanging
                        // the REPL in "..." over it forever
                        mode = ScanMode.NORMAL;
                        continue;
                    }
                    if (c == '\\' && i + 1 < length) {
                        i += 2; // skip the escaped character, whatever it is
                        continue;
                    }
                    if (c == quote) {
                        mode = ScanMode.NORMAL;
                    }
                    i++;
                }
                case TRIPLE_SINGLE_QUOTED, TRIPLE_DOUBLE_QUOTED -> {
                    final String closing = mode == ScanMode.TRIPLE_SINGLE_QUOTED ? "'''" : "\"\"\"";
                    if (c == '\\' && i + 1 < length) {
                        i += 2;
                        continue;
                    }
                    if (regionMatches(text, i, closing)) {
                        mode = ScanMode.NORMAL;
                        i += 3;
                        continue;
                    }
                    i++;
                }
            }
        }
        final boolean endsMidConstruct = mode == ScanMode.TRIPLE_SINGLE_QUOTED || mode == ScanMode.TRIPLE_DOUBLE_QUOTED || mode == ScanMode.BLOCK_COMMENT;
        return new ScanResult(parens, braces, brackets, endsMidConstruct);
    }

    private static boolean regionMatches(final String text, final int index, final String needle) {
        return text.regionMatches(index, needle, 0, needle.length());
    }

    /**
     * True if the text ends while still inside a triple-quoted string or a block comment --
     * constructs that legitimately span multiple lines, so the REPL should keep prompting for
     * more input instead of trying to parse (or reporting an error on) a truncated fragment.
     */
    private boolean endsInsideMultilineConstruct(String text) {
        return scan(text).endsMidConstruct();
    }

    private boolean hasUnclosedBrackets(String text) {
        final ScanResult result = scan(text);
        return result.parenBalance() > 0 || result.braceBalance() > 0 || result.bracketBalance() > 0;
    }

    /**
     * Synchronously run the REPL until the user quits.
     * Do not use this when you need to fetch input asynchronously. Use acceptLine(String) instead for that case.
     * @throws IOException when reading text (via ReplIO) fails
     */
    public void run() throws IOException {
        if (logo != null) {
            io.println(logo);
            io.println("Version " + Revision.REVISION + "\n");
        }
        prompt = defaultPrompt;
        while (!quit) {
            final String line = io.readLine(prompt);
            acceptText(line);
        }
    }

    /**
     * Feed some text into the REPL.
     * <p>
     * In asynchronous settings, use this method to drive the loop.
     * In synchronous settings, you can use the run() method instead, which does that for you.
     * </p>
     * @param text some script text, expression, etc.
     */
    public void acceptText(final @Nullable String text) {
        if (text == null) {
            quit = true; // EOF (Ctrl+D)
        }
        try {
            buffer.append(text).append("\n");
            PartialParseResult result = parse(buffer.toString());
            if (result == PartialParseResult.OK) {
                buffer.setLength(0); // Clear buffer for next statement
            } else {
                prompt = continuePrompt; // Indicate continuation
                return;
            }
            prompt = defaultPrompt;
        } catch (Exception e) {
            io.exception(e);
            buffer.setLength(0);
            prompt = defaultPrompt;
        }
    }

    /**
     * Discards any partially-entered input and returns to the default prompt, without touching
     * the global scope or the underlying runtime.
     * <p>
     * Useful for a host that finds the REPL stuck in a "..." continuation state -- e.g. because
     * the user typed something the incomplete-input heuristics in {@link #parse(String)}
     * misjudged, or simply changed their mind partway through a multi-line statement -- and wants
     * to recover from it without throwing away already-defined variables the way discarding and
     * recreating the whole {@link JanitorRepl} instance would.
     */
    public void resetBuffer() {
        buffer.setLength(0);
        prompt = defaultPrompt;
    }


    public String getLogo() {
        return logo;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    public ScriptSource getModule() {
        return module;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    public void setDefaultPrompt(final String defaultPrompt) {
        this.defaultPrompt = defaultPrompt;
    }

    public boolean isQuit() {
        return quit;
    }

    public String getContinuePrompt() {
        return continuePrompt;
    }

    public void setContinuePrompt(final String continuePrompt) {
        this.continuePrompt = continuePrompt;
    }

    public void setQuit(final boolean quit) {
        this.quit = quit;
    }

    public String getPrompt() {
        return prompt;
    }

}
