package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class JanitorScript implements RunnableScript {


    public static final ANTLRErrorListener LOGGING_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
            log.warn("line {}:{} --> {}", line, charPositionInLine, msg);
        }
    };


    private static final Logger log = LoggerFactory.getLogger(JanitorScript.class);

    private final JanitorRuntime runtime;
    private final ScriptModule module;
    private final Script scriptObject;
    private final List<String> issues;
    private Exception compilerException;

    @Override
    public Exception getCompilerException() {
        return compilerException;
    }

    public static String hostString(final JanitorObject obj) {
        return obj == null || obj == JNull.NULL ? null : obj.janitorToString();
    }

    @Override
    public @NotNull List<String> getIssues() {
        return issues;
    }

    public JanitorScript(final JanitorRuntime runtime,
                         final String moduleName,
                         final String source) throws JanitorCompilerException {
        this(runtime, moduleName, source, false);
    }

    public JanitorScript(final JanitorRuntime runtime,
                         final String moduleName,
                         final String source,
                         final boolean checking) throws JanitorCompilerException {
        this.runtime = runtime;
        this.module = new ScriptModule(moduleName, source);

        final JanitorANTLRErrorListener recorder = new JanitorANTLRErrorListener(source);
        final JanitorParser.ScriptContext script = parseScript(source, recorder);
        issues = recorder.getIssues();



        if (!issues.isEmpty()) {
            final String errorMessage = String.format("found %s issues compiling %s: \n  %s", issues.size(), moduleName, String.join("\n  ", issues));


            // log.error("found {} issues compiling {}: \n  {}", issues.size(), moduleName, errorMessage);
            if (checking) {
                this.compilerException = new JanitorCompilerException(errorMessage);
            } else {
                throw new JanitorCompilerException(errorMessage);
            }
        }

        if (!checking) {
            scriptObject = JanitorCompiler.build(runtime.getEnvironment(), module, script, source, runtime.getCompilerSettings());
        } else {
            Script myScript = null;
            try {
                myScript = JanitorCompiler.build(runtime.getEnvironment(), module, script, source, runtime.getCompilerSettings());
            } catch (RuntimeException compilerException) {
                this.compilerException = compilerException;
                log.info("compiler check error in script {}", moduleName, compilerException);
            }
            scriptObject = myScript;
        }
    }

    // LATER: eigentlich ist es bescheuert, die Exception beim Check nicht zu werfen, denn es ist ja trotzdem ein Fehler

    public static JanitorParser.ScriptContext parseScript(final @NotNull String text) throws JanitorCompilerException {
        return parseScript(text, LOGGING_LISTENER);
    }

    public static JanitorParser.ScriptContext parseScript(final @NotNull String text, final ANTLRErrorListener listener) throws JanitorCompilerException {
        final String modText = text.endsWith(";\n") ? text : text + ";\n";
        try {
            final CharStream stream = CharStreams.fromString(modText);
            final JanitorLexer lexer = new JanitorLexer(stream);

            if (listener != null) {
                lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
                lexer.addErrorListener(listener);
            }

            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            final JanitorParser parser = new JanitorParser(tokenStream);
            if (listener != null) {
                parser.addErrorListener(listener);
                parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
            }
            parser.setBuildParseTree(true);
            return parser.script();
        } catch (RuntimeException e) {
            throw new JanitorCompilerException(e);
        }
    }


    @Override
    public @NotNull JanitorObject run(final @NotNull Consumer<Scope> prepareGlobals) throws JanitorRuntimeException {
        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module); // new Scope(Location.at(module, 0, 0), BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        try {
            return runningScript.run();
        } finally {
            globalScope.janitorLeaveScope();
        }
    }

    @Override
    public @NotNull JanitorObject runInScope(final @NotNull Consumer<Scope> prepareGlobals, final Scope parentScope) throws JanitorRuntimeException {
        final Scope globalScope = Scope.createMainScope(parentScope); // GlobalScope(module); // new Scope(Location.at(module, 0, 0), parentScope, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        return runningScript.run();
    }

    @Override
    public @NotNull ResultAndScope runAndKeepGlobals(final @NotNull Consumer<Scope> prepareGlobals) throws JanitorRuntimeException {
        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module); // new Scope(Location.at(module, 0, 0), BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        return new ResultAndScope(runningScript.getMainScope(), runningScript.run());
    }

    public String getSource() {
        return scriptObject.getSource();
    }



}
