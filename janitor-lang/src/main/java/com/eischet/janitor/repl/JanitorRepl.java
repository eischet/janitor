package com.eischet.janitor.repl;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.compiler.ast.Ast;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.runtime.RunningScriptProcess;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
import java.util.List;

/**
 * Janitor REPL, read-evaluate-print-loop.
 * This will become an interactive interpreter, but is not finished/working yet.
 * TODO: finish the interactive interpreter.
 */
public class JanitorRepl {

    // figlet -f small janitor
    private static final String LOGO = """
               _           _ _
              (_)__ _ _ _ (_) |_ ___ _ _
              | / _` | ' \\| |  _/ _ \\ '_|
             _/ \\__,_|_||_|_|\\__\\___/_|
            |__/
            """;

    private final ScriptModule module;
    private final Scope globalScope;
    private final JanitorRuntime runtime;
    private final ReplIO io;
    private static final String DEFAULT_PROMPT = "janitor> ";
    private String defaultPrompt = DEFAULT_PROMPT;


    public JanitorRepl(final JanitorRuntime runtime, final ReplIO io) {
        this.runtime = runtime;
        this.io = io;
        module = new ScriptModule("repl", "");
        globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module);
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    public void setDefaultPrompt(final String defaultPrompt) {
        this.defaultPrompt = defaultPrompt;
    }

    private static class Fragment {

        private final PartialParseResult parseResult;
        private final JanitorParser.ScriptContext scriptContext;
        private final boolean missingStatementTerminator;

        public Fragment(final PartialParseResult partialParseResult) {
            this.parseResult = partialParseResult;
            this.scriptContext = null;
            this.missingStatementTerminator = false;
        }

        public Fragment(final JanitorParser.ScriptContext scriptContext) {
            this.parseResult = PartialParseResult.OK;
            this.scriptContext = scriptContext;
            this.missingStatementTerminator = false;
        }

        public Fragment(final boolean missingStatementTerminator) {
            this.parseResult = null;
            this.scriptContext = null;
            this.missingStatementTerminator = missingStatementTerminator;
        }

        public PartialParseResult getParseResult() {
            return parseResult;
        }

        public JanitorParser.ScriptContext getScriptContext() {
            return scriptContext;
        }

        public boolean isMissingStatementTerminator() {
            return missingStatementTerminator;
        }
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

    public PartialParseResult parse(final String text) throws JanitorControlFlowException, JanitorRuntimeException {
        if (hasUnclosedMultilineString(text) || hasUnclosedBrackets(text)) {
            io.verbose("looks incomplete");
            return PartialParseResult.INCOMPLETE;
        }

        Fragment fragment = parseFragment(text);
        if (fragment.isMissingStatementTerminator()) {
            final Fragment betterFragment = parseFragment(text + ";");
            if (betterFragment.scriptContext != null) {
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


        final ScriptModule module = new ScriptModule("repl", text);
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(runtime.getEnvironment(), module, false, text);
        io.verbose("start compile");
        final Ast compiledText = compiler.visit(fragment.getScriptContext());
        if (compiledText instanceof Statement compiledStatement) {
            final Location loc = Location.startOf(module);
            final Script partialScript = new Script(loc, List.of(compiledStatement), text);
            final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, partialScript);
            try {
                partialScript.execute(process);
                final JanitorObject result = process.getScriptResult();
                if (result != JNull.NULL) {
                    System.out.println(result);
                }
            } catch (ReturnStatement.Return ret) {
                final JanitorObject returnResult = ret.getValue();
                System.out.println("Return Result: " + returnResult);
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


    public enum PartialParseResult {OK, INCOMPLETE}

    private boolean hasUnclosedMultilineString(String text) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf("\"\"\"", idx)) != -1) {
            count++;
            idx += 3;
        }
        return count % 2 != 0;
    }

    private boolean hasUnclosedBrackets(String text) {
        int parens = 0, braces = 0, brackets = 0;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '(': parens++; break;
                case ')': parens--; break;
                case '{': braces++; break;
                case '}': braces--; break;
                case '[': brackets++; break;
                case ']': brackets--; break;
            }
        }
        return parens > 0 || braces > 0 || brackets > 0;
    }

    public void run() {
        io.println(LOGO);

        StringBuilder buffer = new StringBuilder();
        String prompt = defaultPrompt;

        while (true) {
            try {
                String line = io.readLine(prompt);
                if (line == null) break; // EOF (Ctrl+D)
                buffer.append(line).append("\n");
                PartialParseResult result = parse(buffer.toString());
                if (result == PartialParseResult.OK) {
                    buffer.setLength(0); // Clear buffer for next statement
                } else {
                    prompt = "... "; // Indicate continuation
                    continue;
                }
                prompt = defaultPrompt;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                buffer.setLength(0);
                prompt = defaultPrompt;
            }
        }
    }

}
