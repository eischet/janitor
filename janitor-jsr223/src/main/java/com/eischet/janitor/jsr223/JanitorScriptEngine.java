package com.eischet.janitor.jsr223;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import static javax.script.ScriptContext.ENGINE_SCOPE;

public class JanitorScriptEngine implements ScriptEngine {

    private final JanitorScriptEngineFactory factory;
    private ScriptContext context;
    private Bindings bindings;

    public JanitorScriptEngine(final JanitorScriptEngineFactory factory) {
        this.factory = factory;
        this.bindings = createBindings();
    }

    private JanitorObject runScript(final String script, final @Nullable ScriptContext context, final @Nullable Bindings bindings) throws ScriptException {
        try {
            final JanitorRuntime runtime = factory.getRuntime();
            final RunnableScript compiled = runtime.compile("eval", script);

            Scope runtimeScope = null;
            if (bindings != null) {
                if (bindings instanceof JanitorBindings janitorBindings) {
                    runtimeScope = janitorBindings.getScope();
                } else {
                    throw new RuntimeException("unsupported bindings, should be JanitorBindings but was " + bindings.getClass());
                }
            }
            if (runtimeScope == null) {
                runtimeScope = factory.getGlobalScope();
            }
            if (context != null) {
                // TODO: use context, whatever that means technically ...
            }
            final @NotNull ResultAndScope resultAndScope = compiled.runInScopeAndKeepGlobals(runtimeScope);
            // System.out.println("scope after run: " + resultAndScope.getScope().dir());

            if (resultAndScope.getScope() != runtimeScope) {
                throw new RuntimeException("unexpected scope change");
            }

            // TODO: store the scope somewhere?
            return resultAndScope.getVariable();
            // final @NotNull JanitorObject result = compiled.runInScope(g -> {}, runtimeScope);
            // return result;
        } catch (JanitorCompilerException | JanitorRuntimeException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        return runScript(script, context, null);
    }

    private String readFully(final Reader reader) throws ScriptException {
        try {
            final StringWriter w = new StringWriter();
            reader.transferTo(w);
            return w.toString();
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        return runScript(readFully(reader), context, null);
    }

    @Override
    public Object eval(final Reader reader) throws ScriptException {
        return runScript(readFully(reader), null, null);
    }

    @Override
    public Object eval(final String script, final Bindings bindings) throws ScriptException {
        return runScript(script, null, bindings);
    }

    @Override
    public Object eval(final Reader reader, final Bindings bindings) throws ScriptException {
        return runScript(readFully(reader), null, bindings); // TODO: bindings
    }

    @Override
    public void put(final String key, final Object value) {
        getBindings(ENGINE_SCOPE).put(key, value); // "must have the same effect as"
    }

    @Override
    public Object get(final String key) {
        return getBindings(ENGINE_SCOPE).get(key); // "must have the same effect as"
    }

    @Override
    public Bindings getBindings(final int scope) {
        if (scope == ENGINE_SCOPE) {
            return bindings;
        } else if (scope == ScriptContext.GLOBAL_SCOPE) {
            return factory.getBindings();
        } else {
            throw new RuntimeException("unknown scope " + scope);
        }
    }

    @Override
    public void setBindings(final Bindings bindings, final int scope) {
        if (scope == ENGINE_SCOPE) {
            this.bindings = bindings;
        } else if (scope == ScriptContext.GLOBAL_SCOPE) {
            factory.setBindings(bindings);
        } else {
            throw new RuntimeException("unknown scope " + scope);
        }
    }

    @Override
    public Bindings createBindings() {
        return factory.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(final ScriptContext context) {
        this.context = context;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new JanitorScriptEngineFactory();
    }


    @Override
    public Object eval(final String script) throws ScriptException {
        return runScript(script, null, null);
    }

}
