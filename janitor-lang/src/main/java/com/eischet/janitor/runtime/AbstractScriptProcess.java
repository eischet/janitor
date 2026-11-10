// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;


import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.metadata.MetaDataMap;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScopedVariable;
import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.template.TemplateParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Base class for script processes: it manages the scopes, the closure scopes and the cleanup of resources while a script runs. */
public abstract class AbstractScriptProcess implements JanitorScriptProcess {

    private static final JanitorLogger log = JanitorLogger.getLogger(AbstractScriptProcess.class);

    private final JanitorRuntime runtime;
    private final VariableScope mainScope;
    private final List<Scope> closureScopes = new LinkedList<>();
    private final @NotNull String processName;
    private final MetaDataMap metaData = new MetaDataMap();
    private VariableScope currentScope;
    private final LinkedList<JanitorCleanupRequired> cleanupList = new LinkedList<>();

    private JanitorObject scriptResult = JNull.NULL;

    /**
     * Constructor.
     * @param runtime the runtime to work with
     * @param mainScope the script scope to start with
     */
    public AbstractScriptProcess(final JanitorRuntime runtime, final VariableScope mainScope, final @NotNull String processName) {
        this.runtime = runtime;
        this.mainScope = mainScope;
        this.currentScope = mainScope;
        this.processName = processName;
    }

    @Override
    public @NotNull String getProcessName() {
        return processName;
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
        final K value = metaData.get(key);
        return value != null ? value : getEnvironment().getMetaData(key);
    }

    @Override
    public <K> void setMetaData(final @NotNull MetaDataKey<K> key, final @Nullable K value) {
        metaData.put(key, value);
    }

    @Override
    public @NotNull MetaDataMap copyMetaData() {
        final MetaDataMap copy = new MetaDataMap();
        copy.putAll(metaData);
        return copy;
    }

    @Override
    public void inheritMetaData(final @NotNull JanitorScriptProcess parent) {
        metaData.putAll(parent.copyMetaData());
    }

    @Override
    public JString expandTemplate(final JString template, final JCallArgs arguments) throws JanitorRuntimeException {
        return TemplateParser.expand(getRuntime(), this, template, arguments);
        // return runningScript.getRuntime().expand(runningScript, self, arguments);
        // return CSString.of(self.getHostValue().formatted(arguments.getList().stream().map(CSObj::getHostValue).toArray()));

    }

    @Override
    public void pushModuleScope(final Scope moduleScope) {
        log.debug("pushing module scope: {} on top of current scope {}", moduleScope, currentScope);
        currentScope = VariableScope.createFreshModuleScope(moduleScope, currentScope);
    }

    @Override
    public void popModuleScope(final Scope moduleScope) {
        log.debug("popping module scope: {}", moduleScope);
        if (currentScope.getModuleScope() != moduleScope) {
            log.warn("popModuleScope: currentScope.moduleScope = {}, but expected {}", currentScope.getModuleScope(), moduleScope);
        }
        currentScope = parentFrame(currentScope);
        log.debug("  current scope is now: {}", currentScope);
    }

    @Override
    public void pushClosureScope(final Scope closureScope) {
        closureScopes.add(closureScope);
    }

    @Override
    public void popClosureScope(final Scope closureScope) {
        final Scope poppedScope = closureScopes.remove(closureScopes.size()-1);
        if (poppedScope != closureScope) {
            trace(() -> String.format("popClosureScope: poppedScope = %s, but expected %s", poppedScope, closureScope));
        }
    }

    @Override
    public JanitorObject lookup(final String text) {
        return find(getCurrentScope(), text, closureScopes);
    }

    /**
     * Looks up a name for reading: in the scope, then in the closure scopes of the running functions, then in the scope
     * of the module whose function runs here, then the same way in the parent scope.
     *
     * @param scope         the scope to start in
     * @param name          the name
     * @param closures      the closure scopes to consult, or null
     * @return the value, or null if no scope defines the name
     */
    private @Nullable JanitorObject find(final @NotNull Scope scope, final @NotNull String name, final @Nullable List<Scope> closures) {
        Scope at = scope;
        while (at != null) {
            final JanitorObject own = at.get(this, name);
            if (own != null) {
                return own;
            }
            if (closures != null) {
                for (final Scope closureScope : closures) {
                    final JanitorObject closureVar = closureScope.get(this, name);
                    if (closureVar != null) {
                        return closureVar;
                    }
                }
            }
            if (at instanceof VariableScope variables && variables.getModuleScope() != null) {
                final JanitorObject inModule = find(variables.getModuleScope(), name, null);
                if (inModule != null) {
                    return inModule;
                }
                // LATER: *here* we fall through and allow the scope of the main script to be used as well.
                // This was not really intended, but it used to work like this in earlier versions ("Lua style"),
                // where there was no module scope at all, so a module ALWAYS ran in the main scope.
                // Disabling that behavior would break existing scripts, so it is still allowed here.
            }
            at = at.getParent();
        }
        return null;
    }

    // a block or module scope ends where it was entered, so its parent is the frame below it
    private static VariableScope parentFrame(final VariableScope scope) {
        return (VariableScope) scope.getParent();
    }

    @Override
    public Location getCurrentLocation() {
        return currentScope.getIp();
    }

    @Override
    public void setCurrentLocation(final Location ip) {
        currentScope.setIp(ip);
    }

    @Override
    public JanitorObject getScriptResult() {
        return scriptResult;
    }

    @Override
    public void setScriptResult(final JanitorObject scriptResult) {
        this.scriptResult = scriptResult;
    }

    @Override
    public VariableScope getMainScope() {
        return mainScope;
    }

    @Override
    @NotNull
    public VariableScope getCurrentScope() {
        return currentScope;
    }

    @Override
    public JanitorRuntime getRuntime() {
        return runtime;
    }

    @Override
    public void enterBlock(final Location location) {
        this.currentScope = VariableScope.createFreshBlockScope(location, currentScope);
    }

    @Override
    public void exitBlock() {
        currentScope.janitorLeaveScope();
        this.currentScope = parentFrame(currentScope);
    }

    /**
     * Looks up a name for an assignment: in the current scope, then in the closure scopes of the running functions, then
     * in the parent scopes; unlike {@link #lookup}, this does not consult the scope of a module whose function runs here.
     */
    @Override
    public @Nullable ScopedVariable lookupScopedVar(final String id) {
        Scope scope = getCurrentScope();
        // Is the variable in the local top level scope? Then we can make short work of this:
        JanitorObject variable = scope.get(this, id);
        if (variable != null) {
            return new ScopedVariable(scope, variable);
        }
        // before checking parent scopes, let's have a look at possible closure scopes we need to apply...
        // this fixes the issues in DispatchTests.java, where it was not possible to access variables from enclosing scopes
        // is that really right? because we do not iterate from one closure scope to the next... wow, this is complicated.
        for (int i = closureScopes.size()-1; i >= 0; i--) {
            Scope currentClosureScope = closureScopes.get(i);
            variable = currentClosureScope.get(this, id);
            if (variable != null) {
                return new ScopedVariable(currentClosureScope, variable);
            }
            while (currentClosureScope != null && (variable = find(currentClosureScope, id, closureScopes)) == null) {
                currentClosureScope = currentClosureScope.getParent();
            }
            if (variable != null) {
                return new ScopedVariable(currentClosureScope, variable);
            }
        }

        // Otherwise, iterate through the chain of parent scopes:
        scope = scope.getParent();
        while (scope != null && (variable = scope.get(this, id)) == null) {
            scope = scope.getParent();
        }
        if (scope != null) { // if scope != null, variable must be non-null
            return new ScopedVariable(scope, variable);
        } else {
            return null;
        }
    }

    @Override
    public List<Location> getStackTrace() {
        final List<Location> stack = new ArrayList<>();
        Scope scope = getCurrentScope();
        while (scope != null) {
            if (scope instanceof VariableScope variables) {
                if (variables.getIp() != null) {
                    stack.add(variables.getIp());
                } else if (variables.getLocation() != null) {
                    stack.add(variables.getLocation());
                }
            }
            scope = scope.getParent();
        }
        return stack;
    }


    @Override
    public void registerCleanable(final JanitorCleanupRequired cleanable) {
        if (!cleanupList.contains(cleanable)) {
            cleanupList.add(cleanable);
        } else {
            trace(() -> "cleanable already registered: " + cleanable);
        }
    }

    /** Calls the cleanup method of all objects that require cleanup, e.g. to close files that a script left open. */
    protected void processCleanups() {
        for (final JanitorCleanupRequired janitorCleanupRequired : cleanupList) {
            janitorCleanupRequired.janitorCleanup();
        }
    }

}
