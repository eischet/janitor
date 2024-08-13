package com.eischet.janitor.runtime;


import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.template.TemplateParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractScriptProcess implements JanitorScriptProcess {

    private static final Logger log = LoggerFactory.getLogger(AbstractScriptProcess.class);

    private final JanitorRuntime runtime;
    private final Scope mainScope;
    private final List<Scope> closureScopes = new LinkedList<>();
    private Scope currentScope;

    private JanitorObject scriptResult = JNull.NULL;

    /**
     * Constructor.
     * @param runtime the runtime to work with
     * @param mainScope the script scope to start with
     */
    public AbstractScriptProcess(final JanitorRuntime runtime, final Scope mainScope) {
        this.runtime = runtime;
        this.mainScope = mainScope;
        this.currentScope = mainScope;
    }

    /**
     * Expand a template with arguments.
     * @param template the template to expand
     * @param arguments the arguments to expand with
     * @return the expanded template
     * @throws JanitorRuntimeException on errors
     */
    @Override
    public JString expandTemplate(final JString template, final JCallArgs arguments) throws JanitorRuntimeException {
        return TemplateParser.expand(getRuntime(), this, template, arguments);
        // return runningScript.getRuntime().expand(runningScript, self, arguments);
        // return CSString.of(self.getHostValue().formatted(arguments.getList().stream().map(CSObj::getHostValue).toArray()));

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
    public void pushClosureScope(final Scope closureScope) {
        // do we really want to do this?
        closureScopes.add(closureScope);
    }

    @Override
    public void popClosureScope(final Scope closureScope) {
        // do we really want to do this?
        final Scope poppedScope = closureScopes.remove(closureScopes.size()-1);
        if (poppedScope != closureScope) {
            log.warn("popClosureScope: poppedScope = {}, but expected {}", poppedScope, closureScope);
        }
    }

    @Override
    public JanitorObject lookup(final String text) {
        // hat variablen in Modulen unsichtbar gemacht, weil push/popModuleScope noch nicht existierten!
        return getCurrentScope().lookup(this, text, closureScopes);
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
    public ResultAndScope lookupScopedVar(final String id) {
        Scope scope = getCurrentScope();
        // Is the variable in the local top level scope? Then we can make short work of this:
        JanitorObject variable = scope.lookupLocally(this, id);
        if (variable != null) {
            return new ResultAndScope(scope, variable);
        }
        // before checking parent scopes, let's have a look at possible closure scopes we need to apply...
        // this fixes the issues in DispatchTests.java, where it was not possible to access variables from enclosing scopes
        // ist that really right? because we do not iterate from one closure scope to the next... wow, this is complicated.
        for (int i = closureScopes.size()-1; i >= 0; i--) {
            Scope currentClosureScope = closureScopes.get(i);
            variable = currentClosureScope.lookupLocally(this, id);
            if (variable != null) {
                return new ResultAndScope(currentClosureScope, variable);
            }
            while (currentClosureScope != null && (variable = currentClosureScope.lookup(this, id, closureScopes)) == null) {
                currentClosureScope = currentClosureScope.getParent();
            }
            if (variable != null) {
                return new ResultAndScope(currentClosureScope, variable);
            }
        }

        // Otherwise, iterate through the chain of parent scopes:
        scope = scope.getParent();
        while (scope != null && (variable = scope.lookupLocally(this, id)) == null) {
            scope = scope.getParent();
        }
        if (scope != null) { // if scope != null, variable must be non-null
            return new ResultAndScope(scope, variable);
        } else {
            return null;
        }
    }

    @Override
    public List<Location> getStackTrace() {
        // LATER hier stimmt noch irgendwas nicht. Es werden sinnlose "stack frames" eingemischt.
        final List<Location> stack = new ArrayList<>();
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
        return stack;
    }

    @Override
    public JFloat requireFloat(final Object value) throws JanitorArgumentException {
        if (value instanceof JFloat alreadyMatches) {
            return alreadyMatches;
        }
        if (value instanceof Number num) {
            return getEnvironment().getBuiltinTypes().floatingPoint(num.doubleValue());
        }
        throw new JanitorArgumentException(this, "Expected float instead of " + value);
    }
}
