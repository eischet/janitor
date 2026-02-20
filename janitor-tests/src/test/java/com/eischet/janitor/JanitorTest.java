package com.eischet.janitor;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorEnvironmentProvider;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.CompilerError;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.json.impl.DateTimeUtils;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.logging.JanitorUnitTestLogging;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.runtime.RunningScriptProcess;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class JanitorTest {

    public static final Consumer<Scope> NO_GLOBALS = globals -> {
    };

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeAll
    static void setUp() {
        JanitorUnitTestLogging.setup();
            // this initializes the logging subsystem, which is deliberately used in the tests,
            // but also sets the log level at WARN to avoid polluting logs.

        DateTimeUtils.getZoneId();
            // this prevents a race condition from tests running concurrently... might need to fix that with "synchronized"
            // the symptom is that messages like "cannot get server time zone" appear numerous times in the logs, because a
            // bunch of threads are trying to get the zone id at the same time.

        Janitor.setUserProvider(new JanitorEnvironmentProvider() {
            @Override
            public JanitorEnvironment getCurrentEnvironment() {
                return TestEnv.env;
            }
        });
    }

    protected String getOutput(final @Language("Janitor") String scriptSource) throws JanitorCompilerException, JanitorRuntimeException {
        return getOutput(scriptSource, NO_GLOBALS);
    }

    protected String getOutput(final @Language("Janitor") String scriptSource, final Consumer<Scope> prepareGlobals) throws JanitorRuntimeException, JanitorCompilerException {
        return getOutput(scriptSource, prepareGlobals, false);
    }

    protected String getOutput(final @Language("Janitor") String scriptSource, final Consumer<Scope> prepareGlobals, boolean verbose) throws JanitorCompilerException, JanitorRuntimeException {
        log.debug("parsing: {}\n", scriptSource);
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(scriptSource);
        final ScriptModule module = ScriptModule.unnamed(scriptSource);
        final Script scriptObject = JanitorCompiler.build(TestEnv.env, module, script, scriptSource, verbose);
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();

        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, "manual", scriptObject);
        process.run();
        return runtime.getAllOutput();
    }

    protected JanitorObject evaluate(final @Language("Janitor") String expressionSource) throws JanitorCompilerException, JanitorRuntimeException {
        return evaluate(expressionSource, NO_GLOBALS);
    }

    protected JanitorObject evaluate(final @Language("Janitor") String expressionSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("evaluating: {}\n", expressionSource);
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(expressionSource);
        final ScriptModule module = ScriptModule.unnamed(expressionSource);
        try {
            final Script scriptObject = JanitorCompiler.build(TestEnv.env, module, script, expressionSource);
            final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();

            final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
            prepareGlobals.accept(globalScope);
            final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, "manual", scriptObject);
            return process.run();
        } catch (CompilerError e) {
            throw new JanitorCompilerException(e); // TODO: this should not be done manually, but included in e.g. JanitorCompiler.build!
        }
    }

}
