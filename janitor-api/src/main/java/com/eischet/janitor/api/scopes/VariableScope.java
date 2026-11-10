// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.composed.JanitorAware;
import com.eischet.janitor.api.types.functions.JCallable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A scope that holds variables, the building block of how the interpreter operates.
 * <p>For example, at every '{' in the script, a new scope is created, and at the next '}', the scope is destroyed.
 * Scopes can be nested, and most scopes have a parent scope.</p>
 * Special scopes:
 * <ul>
 * <li>global: this scope is the default scope where your script runs in, and which can be configured from outside
 * but not from inside of a script</li>
 * <li>builtin: this is a special, write-protected scope where default funtions like print and assert live</li>
 * <li>main: the scope a script runs in, below a parent scope that a host chooses (see {@code RunnableScript.runInScope})</li>
 * <li>module scope: modules have their own scope, or else they'd share the running scripts main scope like in Lua
 * script where people "dofile", which I personally do not like</li>
 * <li>block scope: every block of code is represented by a block scope, and sometimes they are created for technical reasons.</li>
 * <li>closure scope: closures, e.g. "x -> foo * (x+2)", need to carry with them their original scope,
 * which is the block scope they were created in, to be able to look up "foo" in this case.</li>
 * </ul>
 */
public class VariableScope implements Scope {

    private final @Nullable Scope parent;
    private final Map<String, JanitorObject> _variables;
    private final @Nullable Location location;
    private final @Nullable Scope moduleScope;
    private final JanitorEnvironment env;
    private @Nullable Location ip;
    private boolean sealed = false;
    private volatile @Nullable Map<String, NameDoc> nameDocs;

    private VariableScope(final JanitorEnvironment env, final @Nullable Location location, final @Nullable Scope parent, final @Nullable Scope moduleScope, final boolean threadEnabled) {
        this.env = env;
        this.location = location;
        this.parent = parent;
        this.moduleScope = moduleScope;
        this._variables = threadEnabled ? new ConcurrentHashMap<>(4) : new HashMap<>(4);
        // Initially, we always used HashMap. 2025-11-05, I became aware of a potential issue where an app keeps updating the builtin scope in a background thread,
        // *while scripts might be running*, which would lead to a potential ConcurrentModificationException. Now, I'm not sure if it's a good idea to use ConcurrentHashMap
        // exclusively, even though I faintly remember from "Java Concurrency in Practice" that it's superior for most cases anyway. I'll come fix it once I've re-read that book.
        // TODO decide whether HashMap or ConcurrentHashMap is the better choice, or if we should keep the simple heuritstic that chooses between them.
    }

    /**
     * Create a global scope, below the environment's built-in scope.
     *
     * @param env    the environment
     * @param module the module
     * @return the global scope
     */
    public static VariableScope createGlobalScope(final JanitorEnvironment env, final ScriptSource module) {
        return new VariableScope(env, Location.startOf(module), env.getBuiltinScope(), null, true);
    }

    /**
     * Create a builtin scope.
     *
     * @param env      the environment
     * @param location the location
     * @return the builtin scope
     */
    public static VariableScope createBuiltinScope(final JanitorEnvironment env, final Location location) {
        return new VariableScope(env, location, null, null, true);
    }

    /**
     * Create a main scope.
     *
     * @param env    the environment
     * @param parent the scope that is asked for names the script does not define itself
     * @return the main scope
     */
    public static VariableScope createMainScope(final JanitorEnvironment env, final @Nullable Scope parent) {
        return new VariableScope(env, null, parent, null, true);
    }

    /**
     * Create a fresh module scope, in which a function of a module runs.
     *
     * @param moduleScope  the top-level scope of the module
     * @param currentScope the current scope
     * @return the fresh module scope
     */
    public static VariableScope createFreshModuleScope(final Scope moduleScope, final VariableScope currentScope) {
        final Location location = moduleScope instanceof VariableScope variables ? variables.getLocation() : null;
        return new VariableScope(currentScope.env, location, currentScope, moduleScope, true);
    }

    /**
     * Create a fresh block scope.
     *
     * @param location     the location
     * @param currentScope the current scope
     * @return the fresh block scope
     */
    public static VariableScope createFreshBlockScope(final Location location, final VariableScope currentScope) {
        return new VariableScope(currentScope.env, location, currentScope, null, false);
    }

    /**
     * Get the location of this scope.
     *
     * @return the location
     */
    public @Nullable Location getLocation() {
        return location;
    }

    /**
     * Get the currently executing location within this scope, if present.
     * (IP=Instruction Pointer, as in Assembly!)
     *
     * @return the currently executing location.
     */
    public @Nullable Location getIp() {
        return ip;
    }

    /**
     * Set the currently executing location within this scope.
     * (IP=Instruction Pointer, as in Assembly!)
     *
     * @param ip the currently executing location
     */
    public void setIp(final @Nullable Location ip) {
        this.ip = ip;
    }

    @Override
    public @Nullable Scope getParent() {
        return parent;
    }

    /**
     * @return the top-level scope of the module whose function runs in this scope, or null if this is not such a scope
     */
    public @Nullable Scope getModuleScope() {
        return moduleScope;
    }

    @Override
    public @Nullable JanitorObject get(final @NotNull JanitorScriptProcess process, final @NotNull String name) {
        return _variables.get(name);
    }

    /**
     * Looks up a variable in this scope only.
     * @param variableName the name of the variable
     * @return the value, or null if there is no such variable
     */
    public @Nullable JanitorObject getVariable(final String variableName) {
        return _variables.get(variableName);
    }

    /**
     * Find out if there's a local variable of the name; no other scope is consulted.
     *
     * @param variableName the variable name
     * @return true if the variable is present
     */
    public boolean hasLocal(final String variableName) {
        return _variables.containsKey(variableName);
    }

    /**
     * Stores a variable in this scope, and registers it for cleanup if it requires that.
     * @param variableName the name of the variable
     * @param value the value to store
     * @param process the running script process, if any
     */
    protected void setVariable(final @NotNull String variableName, final @NotNull JanitorObject value, final @Nullable JanitorScriptProcess process) {
        _variables.put(variableName, value);
        value.janitorEnterScope();
        if (process != null && value instanceof JanitorCleanupRequired cleanable) {
            process.registerCleanable(cleanable);
        }
    }

    /**
     * Assigns to a variable of this scope. The built-in scope is sealed: scripts cannot change it, so an assignment to one
     * of its names, e.g. {@code print = 1}, shadows the name in the script's main scope.
     */
    @Override
    public void assign(final @NotNull JanitorScriptProcess process, final @NotNull String name, final @NotNull JanitorObject value) {
        if (sealed) {
            process.getMainScope().bind(process, name, value);
        } else {
            bind(process, name, value);
        }
    }

    /**
     * Bind a variable in this scope, as the interpreter does for a script.
     *
     * @param process the running script
     * @param variableName  the variable name
     * @param variable      the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final @NotNull JanitorScriptProcess process, final @NotNull String variableName, final @Nullable JanitorObject variable) {
        final @NotNull String name = Objects.requireNonNull(process.getBuiltins().intern(variableName));
        process.trace(() -> "binding in" + (sealed ? " SEALED" : "") + " scope " + this + ": " + name + " = " + variable);
        if (sealed) {
            process.warn("tried to rebind '%s' as %s in sealed scope %s".formatted(name, variable, this));
            return this;
        }
        if (variable == null) {
            setVariable(name, JNull.NULL, process);
        } else {
            final JanitorObject existing = getVariable(name);
            if (existing != null) {
                process.trace(() -> "replacing existing value " + name + " = " + existing + " with " + variable);
                existing.janitorLeaveScope();
            }
            setVariable(name, variable, process);
        }
        return this;
    }

    /**
     * Records the documentation of a variable of this scope, combined with what was documented for it before.
     *
     * @param variableName the variable name
     * @param doc          the documentation from the doc comment of the statement that assigns the variable
     * @see NameDoc#mergedWith(NameDoc)
     */
    public void document(final @NotNull String variableName, final @NotNull NameDoc doc) {
        Map<String, NameDoc> docs = nameDocs;
        if (docs == null) {
            synchronized (this) {
                docs = nameDocs;
                if (docs == null) {
                    docs = new ConcurrentHashMap<>(4);
                    nameDocs = docs;
                }
            }
        }
        docs.merge(variableName, doc, NameDoc::mergedWith);
    }

    /**
     * Looks up the documentation of a variable of this scope; no other scope is consulted.
     *
     * @param variableName the variable name
     * @return the documentation, or null if it has none
     */
    @Override
    public @Nullable NameDoc getNameDoc(final @NotNull String variableName) {
        final Map<String, NameDoc> docs = nameDocs;
        return docs == null ? null : docs.get(variableName);
    }

    /**
     * Unbind a variable from this scope.
     * This is here to the JSR223 implementation and is not usually used.
     * The best strategy to remove a variable from a scope is not to bind it in the first place.
     * The second-best strategy is to throw the scope away and create a new one.
     * Attempts to unbind a variable that is not present are silently ignored.
     * @param variableName the name to unbind
     * @return the value that was bound, or null
     */
    public JanitorObject unbind(final String variableName) {
        final JanitorObject existing = _variables.remove(variableName);
        if (existing != null) {
            existing.janitorLeaveScope();
        }
        return existing;
    }

    /**
     * Remove all variables from this scope.
     */
    public void unbindAll() {
        _variables.values().forEach(JanitorObject::janitorLeaveScope);
        _variables.clear();
    }

    /**
     * Bind a variable in this scope, as a host does before a script runs; this works even in a sealed scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final JanitorObject variable) {
        setVariable(variableName, Janitor.nullableObject(variable), null);
        return this;
    }

    /**
     * Bind a function in this scope.
     *
     * @param functionName the variable name
     * @param function     the function
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bindF(final String functionName, final JCallable function) {
        setVariable(functionName, function.asObject(functionName), null);
        return this;
    }

    /**
     * Bind a function with a HELP text in this scope; editors and the help() function show the text.
     *
     * @param functionName the variable name
     * @param help         the HELP text, starting with the call form, e.g. "print(values...): Prints the values."
     * @param function     the function
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bindF(final String functionName, final @Nullable String help, final JCallable function) {
        setVariable(functionName, function.asObject(functionName, help), null);
        return this;
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final String variable) {
        return bind(variableName, env.getBuiltinTypes().string(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final long variable) {
        return bind(variableName, env.getBuiltinTypes().integer(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final int variable) {
        return bind(variableName, env.getBuiltinTypes().integer(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final boolean variable) {
        return bind(variableName, Janitor.toBool(variable));
    }

    /**
     * Bind a variable in this scope.
     * This variable is not a Janitor Object, but it is able to return its own equivalent Janitor Object.
     *
     * @param variableName the name to bind
     * @param variable the variable
     * @return this scope (for chained, builder-style calls)
     */
    public VariableScope bind(final String variableName, final JanitorAware variable) {
        return bind(variableName, variable.asJanitorObject());
    }

    @Override
    public @NotNull @Unmodifiable Collection<String> names() {
        return Collections.unmodifiableSet(_variables.keySet());
    }

    /**
     * Get the variables of this scope and of the variable scopes above it, recursively, as a JMap; a variable of this scope
     * replaces one of the same name further up. Object scopes in between are skipped, because they do not list values.
     *
     * @return a JMap of variable names and their values
     */
    public JMap toMap() {
        final JMap dump = env.getBuiltinTypes().map();
        Scope above = moduleScope != null ? moduleScope : parent;
        while (above != null && !(above instanceof VariableScope)) {
            above = above.getParent();
        }
        if (above instanceof VariableScope variables) {
            dump.putAll(variables.toMap());
        }
        _variables.forEach((key, value) -> dump.put(env.getBuiltinTypes().string(key), value));
        return dump;
    }

    @Override
    public @NotNull String janitorToString() {
        return String.valueOf(_variables);
    }

    /**
     * Define truthiness for a scope: it is not empty.
     * This is probably never going to be called by anyone, but I like how this maps to my mental model of scope = object.
     *
     * @return true if not-empty.
     */
    @Override
    public boolean janitorIsTrue() {
        return !_variables.isEmpty();
    }

    /**
     * Called by the interpreter when a scope is left / destroyed.
     * Some variables (sometimes called "auto variables") will want to be cleaned up, e.g. to quickly release resources
     * NOW instead of the next garbage collection event, so we tell all variables that they're leaving the scope.
     */
    @Override
    public void janitorLeaveScope() {
        if (!sealed) {
            _variables.values().stream().filter(Objects::nonNull).forEach(JanitorObject::janitorLeaveScope);
        }
    }

    /**
     * Seal this scope: scripts can no longer bind variables in it, and their assignments to its names shadow them in the
     * script's main scope. Hosts can still bind variables, e.g. in the built-in scope after it was set up.
     */
    public void seal() {
        this.sealed = true;
    }

    /**
     * For REPL implementation: copy all variables and their documentation from another scope, excluding its parent scopes.
     *
     * @param other another scope, probably the main scope of a previous REPL command execution
     */
    public void replEatScope(final VariableScope other) {
        other._variables.forEach((variableName, value) -> setVariable(variableName, value, null));
        final Map<String, NameDoc> otherDocs = other.nameDocs;
        if (otherDocs != null) {
            otherDocs.forEach(this::document);
        }
    }

}
