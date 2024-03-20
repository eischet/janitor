package com.eischet.janitor.cleanup.runtime;


import com.eischet.janitor.cleanup.api.api.JanitorRuntime;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.runtime.scope.ScopedVar;
import com.eischet.janitor.cleanup.runtime.scope.Scope;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractScriptProcess implements JanitorScriptProcess {

    private static final Logger log = LoggerFactory.getLogger(AbstractScriptProcess.class);

    private final JanitorRuntime runtime;
    private final Scope globalScope;
    private final Scope mainScope;
    private Scope currentScope;

    private JanitorObject scriptResult = JNull.NULL;

    public AbstractScriptProcess(final JanitorRuntime runtime, final Scope globalScope) {
        this.runtime = runtime;
        this.globalScope = globalScope;
        this.mainScope = Scope.createMainScope(globalScope); // new Scope(null, globalScope, null);
        this.currentScope = mainScope;
    }

    @Override
    public void pushModuleScope(final Scope moduleScope) {
        log.debug("pushing module scope: {} on top of current scope {}", moduleScope, currentScope);
        currentScope = Scope.createFreshModuleScope(moduleScope, currentScope);
    }

    @Override
    public void popModuleScope(final Scope moduleScope) {
        log.debug("popping module scope: {}", moduleScope);
        if (currentScope.getModuleScope() != moduleScope) {
            log.warn("popModuleScope: currentScope.moduleScope = {}, but expected {}", currentScope.getModuleScope(), moduleScope);
        }
        currentScope = currentScope.getParent();
        log.debug("  current scope is now: {}", currentScope);
    }

    @Override
    public JanitorObject lookup(final String text) {
        // hat variablen in Modulen unsichtbar gemacht, weil push/popModuleScope noch nicht existierten!
        return getCurrentScope().lookup(this, text);
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
    public Scope getMainScope() {
        return mainScope;
    }

    @Override
    @NotNull
    public Scope getCurrentScope() {
        return currentScope;
    }

    @Override
    public JanitorRuntime getRuntime() {
        return runtime;
    }

    @Override
    public void enterBlock(final Location location) {
        this.currentScope = Scope.createFreshBlockScope(location, currentScope);
    }

    @Override
    public void exitBlock() {
        currentScope.janitorLeaveScope();
        this.currentScope = currentScope.getParent();
    }

    @Override
    public ScopedVar lookupScopedVar(final String id) {
        Scope scope = getCurrentScope();
        JanitorObject variable = null;
        while (scope != null && (variable = scope.lookupLocally(this, id)) == null) {
            scope = scope.getParent();
        }
        if (scope != null) { // if scope != null, variable must be non-null
            return new ScopedVar(scope, variable);
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<Location> getStackTrace() {
        // LATER hier stimmt noch irgendwas nicht. Es werden sinnlose "stack frames" eingemischt.
        final MutableList<Location> stack = Lists.mutable.empty();
        Scope scope = getCurrentScope();
        while (scope != null) {
            if (scope.getIp() != null) {
                stack.add(scope.getIp());
            } else {
                if (scope.getLocation() != null) {
                    stack.add(scope.getLocation());
                }
            }
            scope = scope.getParent();
        }
        return stack.toImmutable();
    }

}
