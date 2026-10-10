// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInstructionLimitExceededException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import org.jetbrains.annotations.NotNull;

/** A script process that is actually running a script, and counts the instructions that it executes. */
public class RunningScriptProcess extends AbstractScriptProcess {

    private final Script script;
    private long instructionCounter = 0;
    private long maxInstructionCount = 0;

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope parentScope, final @NotNull String processName, final Script script, final boolean wrapScope) {
        super(runtime, wrapScope ? Scope.createMainScope(parentScope) : parentScope, processName);
        this.script = script;
    }

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope globalScope, final @NotNull String processName, final Script script) {
        this(runtime, globalScope, processName, script, false);
    }

    @Override
    public void countInstruction() throws JanitorRuntimeException {
        ++instructionCounter;
        if (maxInstructionCount > 0 && instructionCounter > maxInstructionCount) {
            throw new JanitorInstructionLimitExceededException(this, maxInstructionCount);
        }
    }

    /**
     * @return the number of instructions executed so far
     */
    public long getInstructionCounter() {
        return instructionCounter;
    }

    /**
     * @return the maximum number of instructions that the script may execute, or 0 if there is no limit
     */
    public long getMaxInstructionCount() {
        return maxInstructionCount;
    }

    @Override
    public void setMaxInstructionCount(final long maxInstructionCount) {
        this.maxInstructionCount = maxInstructionCount;
    }

    @Override
    public void warn(String warning) {
        getRuntime().warn(warning);
    }

    /**
     * Runs the script.
     * @return the result of the script, i.e. the value that it returned or its last value
     * @throws JanitorRuntimeException if the script fails
     */
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
