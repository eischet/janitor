package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;

public class RunningScriptProcess extends AbstractScriptProcess {

    private final Script script;

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope globalScope, final Script script) {
        super(runtime, globalScope);
        this.script = script;
    }

    @Override
    public void warn(String warning) {
        getRuntime().warn(warning);
    }

    public JanitorObject run() throws JanitorRuntimeException {
        try {
            script.execute(this);
            return getScriptResult();
        } catch (ReturnStatement.Return e) {
            return e.getValue();
        } catch (JanitorControlFlowException e) {
            throw new JanitorInternalException(this, "invalid control flow: exited script at top level", e);
        } finally {
            getMainScope().janitorLeaveScope();
        }
    }

    @Override
    public String getSource() {
        return script.getSource();
    }

}
