package com.eischet.janitor.api.scopes;


import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.traits.JCallable;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.api.util.ShortStringInterner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Scope implements JanitorObject {

    private static final Scope BUILTIN_SCOPE = createBuiltinScope(Location.at(ScriptModule.builtin(), 0, 0, 0, 0)); //  new Scope(Location.at(ScriptModule.builtin(), 0, 0), null, null);

    static {
        Scope.BUILTIN_SCOPE.bindF("print", (rs, args) -> rs.getRuntime().print(rs, args));
        Scope.BUILTIN_SCOPE.bindF("assert", Scope::doAssert);
        Scope.BUILTIN_SCOPE.bind("__builtin__", Scope.BUILTIN_SCOPE);
        Scope.BUILTIN_SCOPE.seal();
    }

    private final @Nullable Scope parent;
    private final Map<String, JanitorObject> variables = new HashMap<>(4);
    private final @Nullable Location location;
    private final @Nullable Scope moduleScope;
    private @Nullable Location ip;
    private @Nullable JanitorObject implicitObject;
    private boolean sealed = false;

    private Scope(final @Nullable Location location, final @Nullable Scope parent, final @Nullable Scope moduleScope) {
        this.location = location;
        this.parent = parent;
        this.moduleScope = moduleScope;
    }

    public static Scope createGlobalScope(final ScriptModule module) {
        return new Scope(Location.at(module, 0, 0, 0, 0), Scope.BUILTIN_SCOPE, null);
    }

    public static Scope createBuiltinScope(final Location location) {
        return new Scope(location, null, null);
    }

    public static Scope createMainScope(final Scope globalScope) {
        return new Scope(null, globalScope, null);
    }

    public static Scope createFreshModuleScope(final Scope moduleScope, final Scope currentScope) {
        return new Scope(moduleScope.getLocation(), currentScope, moduleScope);
    }

    public static Scope createFreshBlockScope(final Location location, final Scope currentScope) {
        return new Scope(location, currentScope, null);
    }

    @Nullable
    public static JanitorObject getOptionalMethod(final JanitorObject obj, JanitorScriptProcess runningScript, String methodName) {
        try {
            return obj.janitorGetAttribute(runningScript, methodName, false);
        } catch (JanitorNameException e) {
            runningScript.warn("contract violation: getOptionalMethod threw NameError for " + methodName);
            return JNull.NULL;
        }
    }

    public @Nullable JanitorObject getImplicitObject() {
        return implicitObject;
    }

    public void setImplicitObject(final @Nullable JanitorObject implicitObject) {
        this.implicitObject = implicitObject;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public @Nullable Location getIp() {
        return ip;
    }

    public void setIp(final @Nullable Location ip) {
        this.ip = ip;
    }

    public @Nullable Scope getParent() {
        return parent;
    }

    public @Nullable Scope getModuleScope() {
        return moduleScope;
    }

    public JanitorObject lookupLocally(final JanitorScriptProcess runningScript, final String variableName) {
        if (implicitObject != null) {
            final JanitorObject impVar = getOptionalMethod(implicitObject, runningScript, variableName);
            // log.info("implicit local lookup: <{} {}>.{} --> {}", implicitObject.getClass().getSimpleName(), implicitObject, variableName, impVar);
            if (impVar != JNull.NULL) {
                return impVar;
            }
        }
        return variables.get(variableName);
    }

    public JanitorObject retrieveLocal(final String variableName) {
        return variables.get(variableName);
    }

    public JanitorObject lookup(final JanitorScriptProcess runningScript, final String variableName) {
        if (implicitObject != null) {
            final JanitorObject impVar = getOptionalMethod(implicitObject, runningScript, variableName);
            // log.debug("implicit lookup: <{} {}>.{} --> {}", implicitObject.getClass().getSimpleName(), implicitObject, variableName, impVar);
            if (impVar != null) {
                return impVar;
            }
        }
        final JanitorObject existing = variables.get(variableName);
        if (existing != null) {
            return existing;
        }

        // TODO: computeIfAbsent ist NICHT GLEICH getIfAbsent
        // return variables.computeIfAbsent(variableName, key -> {
            // Bis 1.9.261 konnten Module nicht auf ihren eigenen Scope zugreifen, weil auch dort unser "parent" galt,
            // der natürlich aus dem Hauptskript stammt. Geändert: ein Modul wird jeweils "vorgeschoben" und besitzt
            // dann seinen eigenen Parent.
            if (moduleScope != null) {
                // log.debug("diverting lookup of {} to module scope {}!", variableName, moduleScope);
                final JanitorObject v = moduleScope.lookup(runningScript, variableName);
                if (v != null) {
                    return v;
                }
                // LATER: *hier* wird per Fallthrough erlaubt, dass auch der Scope des Hauptskripts angewendet wird.
                // Das wollte ich eigentlich nicht, es war aber bis Cockpit 1.9.260 so ("Lua Style").
                // Und da fehlte ganz der Module Scope, so dass das Modul IMMER im Hauptscope lief.
                // Das aber abzuklemmen macht alle Mailimporte des BV kaputt, daher hier wieder erlaubt.
            }
            // für debugging des o.g. Sachverhalts: log.info("failed lookup: {} in scope {} -> trying parent {}",  this, variableName, parent);
            return parent == null ? null : parent.lookup(runningScript, variableName);
        // });
    }

    public Scope bind(final JanitorScriptProcess runningScript, final String variableName, final JanitorObject variable) {
        final String name = ShortStringInterner.maybeIntern(variableName);
        runningScript.trace(() -> "binding in" + (sealed ? " SEALED" : "") + " scope " + this + ": " + name + " = " + variable);
        //log.debug("binding in scope {}: {} = {}", this.getLocation(), name, variable);
        if (sealed) {
            runningScript.warn("tried to rebind '%s' as %s in sealed scope %s".formatted(name, variable, this));
        }
        if (variable == null) {
            // ist kein Fehler mehr: log.debug("tried to bind null as {} in scope {}", variableName, this);
            variables.put(name, JNull.NULL);
        } else {
            final JanitorObject existing = variables.get(name);
            if (existing != null) {
                runningScript.trace(() -> "replacing existing value " + name+ " = " + existing + " with " + variable);
                existing.janitorLeaveScope();
            }
            variables.put(name, variable);
        }
        return this;
    }

    public Scope bind(final String variableName, final JanitorObject variable) {
        // runningScript.trace(() -> "binding in" + (sealed ? " SEALED" : "") + " scope " + this + ": " + variableName + " = " + variable);
        //log.debug("binding in scope {}: {} = {}", this.getLocation(), variableName, variable);
        if (sealed) {
            // runningScript.warn("tried to rebind '%s' as %s in sealed scope %s".formatted(name, variable, this));
        }
        final String name = ShortStringInterner.maybeIntern(variableName);
        if (variable == null) {
            // ist kein Fehler mehr: log.debug("tried to bind null as {} in scope {}", variableName, this);
            variables.put(name, JNull.NULL);
        } else {
            variables.put(name, variable);
        }
        return this;
    }

    public Scope bindF(final String functionName, final JCallable function) {
        //log.debug("binding in scope {}: function {} = {}", this.getLocation(), functionName, function);
        if (sealed) {
            //log.warn("tried to rebind '{}' as {} in sealed scope {}", function, function, this);
        }
        variables.put(ShortStringInterner.maybeIntern(functionName), function.asObject(functionName));
        return this;
    }

    public Scope bind(final String variableName, final String variable) {
        return bind(variableName, JString.of(variable));
    }

    public Scope bind(final String variableName, final long variable) {
        return bind(variableName, JInt.of(variable));
    }

    public Scope bind(final String variableName, final int variable) {
        return bind(variableName, JInt.of(variable));
    }

    public Scope bind(final String variableName, final boolean variable) {
        return bind(variableName, JBool.of(variable));
    }


    public List<String> dir() {
        return variables.keySet().stream().toList();
    }

    public JMap toMap() {
        final JMap dump = new JMap();
        if (moduleScope != null) {
            dump.putAll(moduleScope.toMap());
        } else if (parent != null) {
            dump.putAll(parent.toMap());
        }
        variables.forEach((key, value) -> dump.put(JString.of(key), value));
        return dump;
    }

    @Override
    public String janitorToString() {
        return String.valueOf(variables);
    }

    @Override
    public boolean janitorIsTrue() {
        return !variables.isEmpty();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "scope";
    }

    @Override
    public void janitorLeaveScope() {
        if (!sealed) {
            variables.values().stream().filter(Objects::nonNull).forEach(JanitorObject::janitorLeaveScope);
        }
    }

    public void seal() {
        this.sealed = true;
    }


    public void replEatScope(Scope other) {
        variables.putAll(other.variables);
    }

    public static JanitorObject doAssert(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final JanitorObject condition = args.require(0, 1).get(0);
        final String message = args.getOptionalStringValue(1, "");
        if (!JanitorSemantics.isTruthy(condition)) {
            if (message == null || message.isBlank()) {
                throw new JanitorAssertionException(process, "assertion failed!");
            } else {
                throw new JanitorAssertionException(process, "assertion failed: " + message);
            }
        }
        return condition;
    }

}
