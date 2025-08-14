package com.eischet.janitor.runtime;


import com.eischet.janitor.api.*;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.compiler.ast.statement.Script;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseRuntime implements JanitorRuntime {

    private final JanitorEnvironment enviroment;
    private Consumer<String> traceListener;

    public BaseRuntime(final JanitorEnvironment environment) {
        this.enviroment = environment;
    }

    public void setTraceListener(final Consumer<String> listener) {
        this.traceListener = listener;
    }

    @Override
    public void trace(final Supplier<String> traceMessageSupplier) {
        if (traceListener != null) {
            final String message = traceMessageSupplier.get();
            traceListener.accept(message);
        }
    }

    @Override
    public JanitorEnvironment getEnvironment() {
        return enviroment;
    }

    @Override
    public RunnableScript compile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }

    @Override
    public RunnableScript checkCompile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source, true, false);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }


    /**
     * Run script code without throwing a script runtime exception on errors.
     * The environment may report an exception, but it may not throw.
     * @param title a name for the protected code block, shown in an exception report
     * @param call the code to execute
     */
    @Override
    public void protect(final @NotNull String title, final @NotNull JanitorScriptProcess.ProtectedCall call) {
        try {
            call.call();
        } catch (JanitorRuntimeException e) {
            exception("Protected call " + title + " failed: " + e.getMessage(), e);
        }
    }

    protected void exception(final String s, final JanitorRuntimeException e) {
        warn(s); // TODO: print stack trace
    }


    @Override
    public JanitorObject executeCallback(final Scope scope, final JCallable callable, final List<JanitorObject> args) throws JanitorRuntimeException {
        final RunningScriptProcess process = new RunningScriptProcess(this, scope, "callback", Script.wrapperForCallback(callable, args));
        return process.run();
    }

}
