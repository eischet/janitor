package com.eischet.janitor.api.scopes;


import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorAware;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A scope is the basic building block of how the interpreter operates.
 * <p>For example, at every '{' in the script, a new scope is created, and at the next '}', the scope is destroyed.
 * Scopes can be nested, and most scopes have a parent scope.</p>
 * Special scopes:
 * <ul>
 * <li>global: this scope is the default scope where your script runs in, and which can be configured from outside
 * but not from inside of a script</li>
 * <li>builtin: this is a special, write-protected scope where default funtions like print and assert live</li>
 * <li>main: this scope is the first child of the global scope, which can be written to by a script</li>
 * <li>module scope: modules have their own scope, or else they'd share the running scripts main scope like in Lua
 * script where people "dofile", which I personally do not like</li>
 * <li>block scope: every block of code is represented by a block scope, and sometimes they are created for technical reasons.</li>
 * <li>closure scope: closures, e.g. "x -> foo * (x+2)", need to carry with them their original scope,
 * which is the block scope they were created in, to be able to look up "foo" in this case.</li>
 * </ul>
 * <p>There's a concept of an "implicit object", which can be attached to a scope. When a lookup fails, the implicit object
 * is looked at instead. This enables hosts to quickly plug in e.g. a JMap of things they want to provide to the script,
 * instead of having to implement anything special.</p>
 */
public class Scope implements JanitorObject {

    private final @Nullable Scope parent;
    private final Map<String, JanitorObject> variables = new HashMap<>(4);
    private final @Nullable Location location;
    private final @Nullable Scope moduleScope;
    private final JanitorEnvironment env;
    private @Nullable Location ip;
    private @Nullable JanitorObject implicitObject;
    private boolean sealed = false;

    private Scope(final JanitorEnvironment env, final @Nullable Location location, final @Nullable Scope parent, final @Nullable Scope moduleScope) {
        this.env = env;
        this.location = location;
        this.parent = parent;
        this.moduleScope = moduleScope;
    }


    /**
     * Create a global scope.
     *
     * @param module the module
     * @return the global scope
     */
    public static Scope createGlobalScope(final JanitorEnvironment env, final ScriptModule module) {
        return new Scope(env, Location.startOf(module), env.getBuiltinScope(), null);
    }

    /**
     * Create a builtin scope.
     *
     * @param location the location
     * @return the builtin scope
     */
    public static Scope createBuiltinScope(final JanitorEnvironment env, final Location location) {
        return new Scope(env, location, null, null);
    }

    /**
     * Create a main scope.
     *
     * @param globalScope the global scope
     * @return the main scope
     */
    public static Scope createMainScope(final Scope globalScope) {
        return new Scope(globalScope.env, null, globalScope, null);
    }

    /**
     * Create a fresh module scope.
     *
     * @param moduleScope  the parent module scope
     * @param currentScope the current scope
     * @return the fresh module scope
     */
    public static Scope createFreshModuleScope(final Scope moduleScope, final Scope currentScope) {
        return new Scope(moduleScope.env, moduleScope.getLocation(), currentScope, moduleScope);
    }

    /**
     * Create a fresh block scope.
     *
     * @param location     the location
     * @param currentScope the current scope
     * @return the fresh block scope
     */
    public static Scope createFreshBlockScope(final Location location, final Scope currentScope) {
        return new Scope(currentScope.env, location, currentScope, null);
    }

    /**
     * Lookup a method/variable within the scope.
     *
     * @param obj           the object
     * @param process       the running script
     * @param methodName    the method name
     * @return the method or null
     */
    @Nullable
    public static JanitorObject getOptionalMethod(final JanitorObject obj, JanitorScriptProcess process, String methodName) {
        try {
            return obj.janitorGetAttribute(process, methodName, false);
        } catch (JanitorRuntimeException e) {
            process.warn("contract violation: getOptionalMethod threw NameError for " + methodName);
            return JNull.NULL;
        }
    }

    /**
     * Get the implicit object.
     * This object is used as a fallback for lookups in the scope.
     *
     * @return the implicit object
     */
    public @Nullable JanitorObject getImplicitObject() {
        return implicitObject;
    }

    /**
     * Set the implicit object.
     * This object is used as a fallback for lookups in the scope.
     *
     * @param implicitObject the implicit object
     */
    public void setImplicitObject(final @Nullable JanitorObject implicitObject) {
        this.implicitObject = implicitObject;
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

    /**
     * Get the parent scope.
     *
     * @return the parent scope
     */
    public @Nullable Scope getParent() {
        return parent;
    }

    /**
     * Get the module scope.
     *
     * @return the module scope
     */
    public @Nullable Scope getModuleScope() {
        return moduleScope;
    }

    /**
     * Look up a variable in this scope's implicit object or in its variables.
     * No other scope (parent, module) is consulted. The implicit object takes precedence.
     *
     * @param process       the running script
     * @param variableName  the variable name
     * @return the variable or null
     */
    public JanitorObject lookupLocally(final JanitorScriptProcess process, final String variableName) {
        if (implicitObject != null) {
            final JanitorObject impVar = getOptionalMethod(implicitObject, process, variableName);
            // log.info("implicit local lookup: <{} {}>.{} --> {}", implicitObject.getClass().getSimpleName(), implicitObject, variableName, impVar);
            if (impVar != JNull.NULL) {
                return impVar;
            }
        }
        return variables.get(variableName);
    }

    /**
     * Find out if there's a local variable of the name.
     * This does not look at the implicit object nor at parent or other scopes!
     *
     * @param variableName the variable name
     * @return true if the variable is present
     */
    public boolean hasLocal(final String variableName) {
        return variables.containsKey(variableName);
    }

    /**
     * Retrieve a local variable.
     * This does not look at the implicit object nor at parent or other scopes!
     *
     * @param variableName the variable name
     * @return the variable or null
     */
    public JanitorObject retrieveLocal(final String variableName) {
        return variables.get(variableName);
    }

    /**
     * Look up a variable in this scope or a parent of this scope.
     *
     * @param process the running script
     * @param variableName  the variable name
     * @param closureScopes the closure scopes
     * @return the variable or null
     */
    public JanitorObject lookup(final JanitorScriptProcess process, final String variableName, @Nullable final List<Scope> closureScopes) {
        if (implicitObject != null) {
            final JanitorObject impVar = getOptionalMethod(implicitObject, process, variableName);
            // log.debug("implicit lookup: <{} {}>.{} --> {}", implicitObject.getClass().getSimpleName(), implicitObject, variableName, impVar);
            if (impVar != null) {
                return impVar;
            }
        }
        final JanitorObject existing = variables.get(variableName);
        if (existing != null) {
            return existing;
        }

        if (closureScopes != null) {
            // System.err.println("looking for " + variableName + " in " + this + " with closures " + closureScopes);
            for (final Scope closureScope : closureScopes) {
                final JanitorObject closureVar = closureScope.lookupLocally(process, variableName);
                if (closureVar != null) {
                    // System.err.println("found " + variableName + " in " + closureScope + ": " + closureVar);
                    return closureVar;
                }
            }
            // System.err.println("not found in closures: " + variableName);
        }

        if (moduleScope != null) {
            // log.debug("diverting lookup of {} to module scope {}!", variableName, moduleScope);
            final JanitorObject v = moduleScope.lookup(process, variableName, /* closureScopes = */ null);
            if (v != null) {
                return v;
            }
            // LATER: *hier* wird per Fallthrough erlaubt, dass auch der Scope des Hauptskripts angewendet wird.
            // Das wollte ich eigentlich nicht, es war aber bis Cockpit 1.9.260 so ("Lua Style").
            // Und da fehlte ganz der Module Scope, so dass das Modul IMMER im Hauptscope lief.
            // Das aber abzuklemmen macht alle Mailimporte des BV kaputt, daher hier wieder erlaubt.
        }
        // für debugging des o.g. Sachverhalts: log.info("failed lookup: {} in scope {} -> trying parent {}",  this, variableName, parent);
        return parent == null ? null : parent.lookup(process, variableName, closureScopes);
        // });
    }

    /**
     * Bind a variable in this scope.
     *
     * @param process the running script
     * @param variableName  the variable name
     * @param variable      the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final JanitorScriptProcess process, final String variableName, final JanitorObject variable) {
        final String name = process.getBuiltins().intern(variableName);
        process.trace(() -> "binding in" + (sealed ? " SEALED" : "") + " scope " + this + ": " + name + " = " + variable);
        //log.debug("binding in scope {}: {} = {}", this.getLocation(), name, variable);
        if (sealed) {
            process.warn("tried to rebind '%s' as %s in sealed scope %s".formatted(name, variable, this));
        }
        if (variable == null) {
            // ist kein Fehler mehr: log.debug("tried to bind null as {} in scope {}", variableName, this);
            variables.put(name, JNull.NULL);
        } else {
            final JanitorObject existing = variables.get(name);
            if (existing != null) {
                process.trace(() -> "replacing existing value " + name + " = " + existing + " with " + variable);
                existing.janitorLeaveScope();
            }
            variables.put(name, variable);
        }
        return this;
    }

    /**
     * Unbind a variable from this scope.
     * This is here to the JSR223 implementation and is not usually used.
     * The best strategy to remove a variable from a scope is not to bind it in the first place.
     * The second-best strategy is to throw the scope away and create a new one.
     * Attempts to unbind a variable that is not present are silently ignored.
     * @param variableName the name to unbind
     */
    public JanitorObject unbind(final String variableName) {
        final JanitorObject existing = variables.remove(variableName);
        if (existing != null) {
            existing.janitorLeaveScope();
        }
        return existing;
    }

    /**
     * Remove all variables from this scope.
     */
    public void unbindAll() {
        variables.values().forEach(JanitorObject::janitorLeaveScope);
        variables.clear();
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final String variableName, final JanitorObject variable) {
        // process.trace(() -> "binding in" + (sealed ? " SEALED" : "") + " scope " + this + ": " + variableName + " = " + variable);
        // log.debug("binding in scope {}: {} = {}", this.getLocation(), variableName, variable);
        // if (sealed) {
        //   process.warn("tried to rebind '%s' as %s in sealed scope %s".formatted(name, variable, this));
        // }

        variables.put(variableName, Objects.requireNonNullElse(variable, JNull.NULL));
        return this;
    }

    /**
     * Bind a function in this scope.
     *
     * @param functionName the variable name
     * @param function     the function
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bindF(final String functionName, final JCallable function) {
        //log.debug("binding in scope {}: function {} = {}", this.getLocation(), functionName, function);
        // if (sealed) {
        // log.warn("tried to rebind '{}' as {} in sealed scope {}", function, function, this);
        // }
        variables.put(functionName, function.asObject(functionName));
        return this;
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final String variableName, final String variable) {
        return bind(variableName, env.getBuiltinTypes().string(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final String variableName, final long variable) {
        return bind(variableName, env.getBuiltinTypes().integer(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final String variableName, final int variable) {
        return bind(variableName, env.getBuiltinTypes().integer(variable));
    }

    /**
     * Bind a variable in this scope.
     *
     * @param variableName the variable name
     * @param variable     the variable
     * @return this scope (for chained, builder-style calls)
     */
    public Scope bind(final String variableName, final boolean variable) {
        return bind(variableName, Janitor.toBool(variable));
    }

    /**
     * Bind a variable in this scope.
     * This variable is not a Janitor Object, but it is able to return its own equivalent Janitor Object.
     *
     * @param variableName the name to bind
     * @param variable the variable
     * @return this scope (for chanied, builder-style calls)
     */
    public Scope bind(final String variableName, final JanitorAware variable) {
        return bind(variableName, variable.asJanitorObject());
    }


    /**
     * Get a list of variables defined in the scope.
     * Inspired by the Python "dir()" function, but not yet as useful.
     *
     * @return a list of variable names
     */
    public List<String> dir() {
        return variables.keySet().stream().toList();
    }

    /**
     * Get a list of variables defined in the scope, as a JMap.
     * This includes module scope and parent scope, recursively (!)
     *
     * @return a JMap of variable names and their values
     */
    public JMap toMap() {
        final JMap dump = env.getBuiltinTypes().map();
        if (moduleScope != null) {
            dump.putAll(moduleScope.toMap());
        } else if (parent != null) {
            dump.putAll(parent.toMap());
        }
        variables.forEach((key, value) -> dump.put(env.getBuiltinTypes().string(key), value));
        return dump;

    }

    @Override
    public @NotNull String janitorToString() {
        return String.valueOf(variables);
    }

    /**
     * Define truthiness for a scope: it is not empty.
     * This is probably never going to be called by anyone, but I like how this maps to my mental model of scope = object.
     *
     * @return true if not-empty.
     */
    @Override
    public boolean janitorIsTrue() {
        return !variables.isEmpty();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "scope";
    }

    /**
     * Called by the interpreter when a scope is left / destroyed.
     * Some variables (sometimes called "auto variables") will want to be cleaned up, e.g. to quickly release resources
     * NOW instead of the next garbage collection event, so we tell all variables that they're leaving the scope.
     */
    @Override
    public void janitorLeaveScope() {
        if (!sealed) {
            variables.values().stream().filter(Objects::nonNull).forEach(JanitorObject::janitorLeaveScope);
        }
    }

    /**
     * Seal this scope, making it read-only.
     * TODO: This does NOTHING AT ALL currently. Sealing scopes it not currently active / implemented.
     */
    public void seal() {
        this.sealed = true;
    }

    /**
     * For REPL implementation: copy all variables from another scope, excluding the implicit object or parent scopes.
     *
     * @param other another scope, probably the main scope of a previous REPL command execution
     */
    public void replEatScope(Scope other) {
        variables.putAll(other.variables);
    }

    /**
     * Capture local variables for a closure.
     *
     * @return a closure scope
     */
    public Scope capture() {
        return this;
        /* LATER: can we "capture" the scope in a more intelligent way? (or in a less stupid way, at least?)
        final Scope myClone = new Scope(env, location, this, moduleScope);
        myClone.variables.putAll(variables);
        return myClone;
         */
        // return new Scope.captureOf(this);
        // boolean heldByClosure = true;
        // hier muss ggf. noch was getan werden, damit wir einen Scope nicht leer räumen, der noch in Benutzung ist.
        // ALT: return this;
    }

    /**
     * Bind all variables from another scope into this scope.
     * It's probably better to call replEatScope instead.
     * TODO: does not seem to be used anywhere, should be deleted; it's redundant, too.
     *
     * @param evalScope the other scope
     * @see Scope#replEatScope
     */
    public void bindAll(final Scope evalScope) {
        evalScope.variables.forEach(this::bind);
    }

}

