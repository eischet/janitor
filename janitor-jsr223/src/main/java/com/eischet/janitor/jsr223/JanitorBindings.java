package com.eischet.janitor.jsr223;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import java.util.*;

public class JanitorBindings implements Bindings {
    private final Scope scope;
    private final JanitorEnvironment environment;

    public JanitorBindings(final Scope scope, final JanitorEnvironment environment) {
        this.scope = scope;
        this.environment = environment;
    }

    @Override
    public Object put(final String name, final Object value) {
        final JanitorObject oldValue = scope.retrieveLocal(name);
        scope.bind(name, environment.nativeToScript(value));
        return oldValue;
    }

    @Override
    public void putAll(final Map<? extends String, ?> toMerge) {
        toMerge.forEach(this::put);
    }

    @Override
    public void clear() {
        scope.unbindAll();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return Set.copyOf(scope.dir());
    }

    @Override
    public int size() {
        return scope.dir().size();
    }

    @Override
    public boolean isEmpty() {
        return scope.dir().isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        if (key instanceof String) {
            return scope.dir().contains(key);
        } else {
            return false;
        }
    }

    private Map<String, Object> extractVariables() {
        final Map<String, Object> variables = new HashMap<>();
        for (final String key : scope.dir()) {
            variables.put(key, scope.retrieveLocal(key));
        }
        return variables;
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final String key : scope.dir()) {
            if (Objects.equals(value, scope.retrieveLocal(key))) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return extractVariables().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return extractVariables().entrySet();
    }


    @Override
    public Object get(final Object key) {
        if (key instanceof String stringKey) {
            return scope.retrieveLocal(stringKey);
        } else {
            return null;
        }
    }

    @Override
    public Object remove(final Object key) {
        if (key instanceof String stringKey) {
            return scope.unbind(stringKey);
        } else {
            return null;
        }
    }

    public Scope getScope() {
        return scope;
    }
}
