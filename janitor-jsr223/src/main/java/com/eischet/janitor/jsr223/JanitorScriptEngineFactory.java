package com.eischet.janitor.jsr223;

import com.eischet.janitor.api.*;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingLocale;
import com.google.auto.service.AutoService;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * JSR224 (javax.scripting) wrappers for Janitor.
 * <p>This factory is automatically discovered by the Java ServiceLoader mechanism used by the ScriptEngineManager.</p>
 * <p>Note that this is currently just a by-product, not the main artifact, of the Janitor language implementation.</p>
 */
@AutoService(ScriptEngineFactory.class)
public class JanitorScriptEngineFactory implements ScriptEngineFactory {

    private final JanitorDefaultEnvironment environment = new JanitorDefaultEnvironment(new JanitorFormattingLocale(Locale.getDefault())) {
        @Override
        public void warn(final String message) {
            System.err.println(message);
        }
    };
    private final Scope globalScope;
    private Bindings bindings;

    public JanitorScriptEngineFactory() {
        this.globalScope = Scope.createGlobalScope(environment, ScriptModule.builtin());
        this.bindings = new JanitorBindings(globalScope, environment);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new JanitorScriptEngine(this);
    }

    public Bindings createBindings() {
        return new JanitorBindings(Scope.createGlobalScope(environment, ScriptModule.builtin()), environment);
    }

    @Override
    public String getEngineName() {
        return "Janitor";
    }

    @Override
    public String getEngineVersion() {
        return "1.0";
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("jan");
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(getEngineName());
    }

    @Override
    public String getLanguageName() {
        return getEngineName();
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public Object getParameter(final String key) {
        return null;
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String m, final String... args) {
        return ""; // TODO: implement this, even though we know it will never work because we're NEVER going to call arbitrary Java code?
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return "print('" + toDisplay.replace("'", "\\'") + "')";
    }

    @Override
    public String getProgram(final String... statements) {
        return String.join("\n", statements);
    }

    public JanitorRuntime getRuntime() {
        return new BaseRuntime(environment) {
            @Override
            public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
                System.out.println(args.getList().stream().map(JanitorObject::janitorToString).collect(Collectors.joining(" ")));
                return JNull.NULL;
            }
        };
    }

    public JanitorDefaultEnvironment getEnvironment() {
        return environment;
    }

    public void setBindings(final Bindings bindings) {
        this.bindings = bindings;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }
}
