// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.runtime.JanitorAbortException;
import com.eischet.janitor.api.errors.runtime.JanitorCancelledException;
import com.eischet.janitor.api.errors.runtime.JanitorTimeLimitExceededException;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hosts can abort scripts with a time limit and a cancellation token, besides the instruction limit; scripts cannot catch these aborts.
 */
public class AbortTestCase extends JanitorTest {

    private static final String ENDLESS = "while (true) { }";
    private static final String ENDLESS_CAUGHT = "try { while (true) { } } catch (e) { print('caught'); }";

    @Test
    void timeLimitPerRun() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("test", ENDLESS);
        assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
                assertThrows(JanitorTimeLimitExceededException.class, () -> script.run(process -> process.setMaxRunTime(Duration.ofMillis(50)))));
    }

    @Test
    void defaultTimeLimitOfTheEnvironment() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> ((JanitorDefaultEnvironment) env).setMaxRunTime(Duration.ofMillis(50)));
        assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
                assertThrows(JanitorTimeLimitExceededException.class, () -> runtime.compile("test", ENDLESS).run()));
        // a run can override the default, here with "no limit"
        assertEquals(5000L, runtime.compile("test", "n = 0; while (n < 5000) { n++; } return n;")
                .run(process -> process.setMaxRunTime(null)).janitorGetHostValue());
    }

    @Test
    void setupDoesNotCountForTheTimeLimit() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("test", "n = 0; while (n < 5000) { n++; } return n;");
        assertEquals(5000L, script.run(process -> {
            process.setMaxRunTime(Duration.ofMillis(50));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).janitorGetHostValue());
    }

    @Test
    void cancelFromAnotherThread() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("test", ENDLESS);
        final AtomicBoolean token = new AtomicBoolean();
        final CompletableFuture<Object> run = CompletableFuture.supplyAsync(() -> {
            try {
                return script.run(process -> process.setCancellation(token));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(50);
        assertFalse(run.isDone());
        token.set(true);
        final ExecutionException e = assertThrows(ExecutionException.class, () -> run.get(10, TimeUnit.SECONDS));
        assertInstanceOf(JanitorCancelledException.class, e.getCause().getCause());
    }

    @Test
    void cancelThroughTheProcess() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("test", ENDLESS);
        assertThrows(JanitorCancelledException.class, () -> script.run(process -> process.cancel()));
    }

    @Test
    void scriptCannotCatchAborts() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("test", ENDLESS_CAUGHT);
        assertThrows(JanitorCancelledException.class, () -> script.run(process -> process.setCancellation(new AtomicBoolean(true))));
        assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
                assertThrows(JanitorTimeLimitExceededException.class, () -> script.run(process -> process.setMaxRunTime(Duration.ofMillis(50)))));
        assertEquals("", runtime.getAllOutput());
    }

    @Test
    void oneTokenCancelsSeveralRuns() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final AtomicBoolean token = new AtomicBoolean(true);
        assertThrows(JanitorAbortException.class, () -> runtime.compile("a", ENDLESS).run(process -> process.setCancellation(token)));
        assertThrows(JanitorAbortException.class, () -> runtime.compile("b", ENDLESS).run(process -> process.setCancellation(token)));
    }

}
