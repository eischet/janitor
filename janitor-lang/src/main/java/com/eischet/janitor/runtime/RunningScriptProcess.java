package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import org.jetbrains.annotations.NotNull;

public class RunningScriptProcess extends AbstractScriptProcess {

    private final Script script;

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope parentScope, final @NotNull String processName, final Script script, final boolean wrapScope) {
        super(runtime, wrapScope ? Scope.createMainScope(parentScope) : parentScope, processName);
        this.script = script;
    }

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope globalScope, final @NotNull String processName, final Script script) {
        this(runtime, globalScope, processName, script, false);
    }

    @Override
    public void warn(String warning) {
        getRuntime().warn(warning);
    }

    public @NotNull JanitorObject run() throws JanitorRuntimeException {
        try {
            script.execute(this);
            return getScriptResult();
        } catch (ReturnStatement.Return e) {
            return e.getValue();
        } catch (JanitorControlFlowException e) {
            throw new JanitorInternalException(this, "invalid control flow: exited script at top level", e);
        } finally {
            getMainScope().janitorLeaveScope();
            processCleanups();
        }
    }


    @Override
    public String getSource() {
        return script.getSource();
    }

}
