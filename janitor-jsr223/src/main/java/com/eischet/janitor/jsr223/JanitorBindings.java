// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.jsr223;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import java.util.*;

/** JSR 223 bindings that are backed by a Janitor scope. */
public class JanitorBindings implements Bindings {
    private final VariableScope scope;
    private final JanitorEnvironment environment;

    public JanitorBindings(final VariableScope scope, final JanitorEnvironment environment) {
        this.scope = scope;
        this.environment = environment;
    }

    @Override
    public Object put(final String name, final Object value) {
        final JanitorObject oldValue = scope.getVariable(name);
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
        return Set.copyOf(scope.names());
    }

    @Override
    public int size() {
        return scope.names().size();
    }

    @Override
    public boolean isEmpty() {
        return scope.names().isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        if (key instanceof String) {
            return scope.names().contains(key);
        } else {
            return false;
        }
    }

    private Map<String, Object> extractVariables() {
        final Map<String, Object> variables = new HashMap<>();
        for (final String key : scope.names()) {
            variables.put(key, scope.getVariable(key));
        }
        return variables;
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final String key : scope.names()) {
            if (Objects.equals(value, scope.getVariable(key))) {
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
            return scope.getVariable(stringKey);
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

    /**
     * @return the scope that holds the variables
     */
    public VariableScope getScope() {
        return scope;
    }
}
