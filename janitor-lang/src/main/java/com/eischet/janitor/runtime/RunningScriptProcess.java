// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorCancelledException;
import com.eischet.janitor.api.errors.runtime.JanitorInstructionLimitExceededException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorTimeLimitExceededException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A script process that is actually running a script, and counts the instructions that it executes.
 * Every instruction is checked against the instruction limit; the time limit and the cancellation token are only checked
 * every 1024 instructions, because reading the clock costs more than counting.
 */
public class RunningScriptProcess extends AbstractScriptProcess {

    private static final long ABORT_CHECK_MASK = 1023;

    private final Script script;
    private long instructionCounter = 0;
    private long maxInstructionCount = 0;
    private @Nullable Duration maxRunTime;
    private @Nullable Duration deadlineLimit = null; // the limit that the running deadline was started with
    private long deadline = 0;
    private @NotNull AtomicBoolean cancellation = new AtomicBoolean();
    // once an abort was detected, check on every instruction, so that code which swallows the error cannot keep going
    private boolean aborting = false;

    public RunningScriptProcess(final JanitorRuntime runtime, final Scope parentScope, final @NotNull String processName, final Script script, final boolean wrapScope) {
        super(runtime, wrapScope ? Scope.createMainScope(parentScope) : parentScope, processName);
        this.script = script;
        // every process starts with the environment's default limits; hosts can override them per run in the setup callback
        this.maxInstructionCount = runtime.getEnvironment().getMaxInstructionCount();
        this.maxRunTime = runtime.getEnvironment().getMaxRunTime();
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
        if (aborting || (instructionCounter & ABORT_CHECK_MASK) == 0) {
            checkAbort();
        }
    }

    @Override
    public void checkAbort() throws JanitorRuntimeException {
        if (cancellation.get()) {
            aborting = true;
            throw new JanitorCancelledException(this);
        }
        // compare by difference, as the nanoTime() docs recommend, which also works when the values overflow
        if (deadlineLimit != null && System.nanoTime() - deadline >= 0) {
            aborting = true;
            throw new JanitorTimeLimitExceededException(this, deadlineLimit);
        }
    }

    @Override
    public void setMaxRunTime(final @Nullable Duration maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    /**
     * @return the maximum time that the script may run, or null or zero if there is no limit
     */
    public @Nullable Duration getMaxRunTime() {
        return maxRunTime;
    }

    @Override
    public void setCancellation(final @NotNull AtomicBoolean token) {
        this.cancellation = token;
    }

    @Override
    public void cancel() {
        cancellation.set(true);
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
        // the clock starts here, so that the host's setup callback does not count
        if (maxRunTime != null && maxRunTime.isPositive()) {
            deadline = System.nanoTime() + maxRunTime.toNanos();
            deadlineLimit = maxRunTime;
        }
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
