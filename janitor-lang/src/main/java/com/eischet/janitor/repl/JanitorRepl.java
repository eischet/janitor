package com.eischet.janitor.repl;

import com.eischet.janitor.api.JanitorCompilerSettings;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.compiler.ast.Ast;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.runtime.RunningScriptProcess;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
import java.util.List;

public class JanitorRepl {

    private final ScriptModule module;
    private final Scope globalScope;
    private final JanitorRuntime runtime;

    public JanitorRepl(final JanitorRuntime runtime) {
        this.runtime = runtime;
        module = new ScriptModule("repl", "");
        globalScope = Scope.createGlobalScope(module);
    }


    public PartialParseResult parse(final String text) throws JanitorControlFlowException, JanitorRuntimeException {
        System.out.println("> parsing: " + text);
        final CharStream stream = CharStreams.fromString(text);
        final JanitorLexer lexer = new JanitorLexer(stream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final JanitorParser parser = new JanitorParser(tokenStream);
        parser.setBuildParseTree(true);

        final boolean[] incomplete = {false};

        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer,
                                    final Object offendingSymbol,
                                    final int line,
                                    final int charPositionInLine,
                                    final String msg,
                                    final RecognitionException e) {
                System.out.println("Syntax Error: " + msg);
                if (e != null) {
                    System.out.println("  Recognition Exception: " + e);
                    if (e instanceof InputMismatchException) {
                        incomplete[0] = true;
                    }
                } else {
                    System.out.println("  No Exception");
                    if (msg != null && msg.contains("extraneous input '<EOF>'")) {
                        incomplete[0] = true;
                    }
                }
            }

            @Override
            public void reportAmbiguity(final Parser recognizer,
                                        final DFA dfa,
                                        final int startIndex,
                                        final int stopIndex,
                                        final boolean exact,
                                        final BitSet ambigAlts,
                                        final ATNConfigSet configs) {
                System.out.println("Ambiguity: " + ambigAlts.toString());
            }

            @Override
            public void reportAttemptingFullContext(final Parser recognizer,
                                                    final DFA dfa,
                                                    final int startIndex,
                                                    final int stopIndex,
                                                    final BitSet conflictingAlts,
                                                    final ATNConfigSet configs) {
                System.out.println("Attempting: " + conflictingAlts.toString());
            }

            @Override
            public void reportContextSensitivity(final Parser recognizer,
                                                 final DFA dfa,
                                                 final int startIndex,
                                                 final int stopIndex,
                                                 final int prediction,
                                                 final ATNConfigSet configs) {
                System.out.println("Context Sensitivity: " + prediction);
            }
        });

        final JanitorParser.TopLevelStatementContext tls = parser.topLevelStatement();
        System.out.println(tls.toStringTree(parser));

        if (incomplete[0]) {
            System.out.println("Incomplete");
            return PartialParseResult.INCOMPLETE;
        }
        final ScriptModule module = new ScriptModule("repl", text);
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(module, JanitorCompilerSettings.RELAXED, text);
        final Ast compiledText = compiler.visit(tls);
        System.out.println("Compiled: " + compiledText.toString());
        //final RuleContext parseTree = root.getRuleContext();
        //compiler.visit(parseTree)
        //return (IR.Script) compiler.visit(parseTree);

        if (compiledText instanceof Statement compiledStatement) {
            System.out.println("Statement");

            final Location loc = Location.startOf(module);
            final Script partialScript = new Script(loc, List.of(compiledStatement), text);

            final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, partialScript);

            try {
                partialScript.execute(runningScript);
                final JanitorObject result = runningScript.getScriptResult();
                System.out.println("Result: " + result);
            } catch (ReturnStatement.Return ret) {
                final JanitorObject returnResult = ret.getValue();
                System.out.println("Return Result: " + returnResult);
            }
            // Catch all the globals defined by this script and stuff them into our global scope, making them available for the next round:
            globalScope.replEatScope(runningScript.getMainScope());
            // runningScript.getMainScope().

            // compiledStatement.execute(new RunningScriptProcess(null, null, partialScript);

        } else {
            System.out.println("Not a statement");
        }


        return PartialParseResult.OK;


    }


    public enum PartialParseResult {OK, INCOMPLETE}


}
