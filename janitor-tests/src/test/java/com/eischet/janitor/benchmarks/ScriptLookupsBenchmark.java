// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.benchmarks;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorEnvironmentProvider;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Measures the interpreter on the things typical host scripts do all the time: loops that call script functions and
 * lambdas, calls into an imported script module, template expansion, chains of member accesses and method calls on
 * built-in values. Each benchmark runs one precompiled script.
 * <p>
 * Run with {@code mvn -pl janitor-tests -am -Pbench -DskipTests test "-Djmh.args=ScriptLookupsBenchmark"}.
 * </p>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class ScriptLookupsBenchmark {

    private static final String MODULE = """
            titel = "Hallo";
            function gruss(name, hoeflich) {
                return titel + " " + name;
            }
            farben = ["rot", "gruen"];
            """;

    private static final String LOOPS = """
            function quad(x) { return x * x; }
            add = (a, b) -> a + b;
            summe = 0;
            for (i from 1 to 100000) {
                if (i > 0) {
                    summe = add(summe, quad(i % 10));
                    assert(true);
                }
            }
            return summe;
            """;

    private static final String MODULE_CALLS = """
            import zaehler as m;
            g = null;
            for (k from 1 to 50000) {
                g = m.gruss("x", true);
            }
            return g;
            """;

    private static final String TEMPLATES = """
            t = "";
            for (j from 1 to 2000) {
                t = "<%= wer %> <%= kunde %>".expand({"wer": j});
            }
            return t;
            """;

    private static final String MEMBER_ACCESS = """
            import zaehler as m;
            o = {"a": {"b": {"c": 1}}, "name": "x"};
            summe = 0;
            n = null;
            for (i from 1 to 100000) {
                summe = summe + o.a.b.c;
                n = o.name;
                n = m.titel;
            }
            return summe;
            """;

    private static final String MEMBER_CALLS = """
            s = "Hallo Welt";
            liste = [1, 2, 3];
            summe = 0;
            for (i from 1 to 100000) {
                summe = summe + s.length() + liste.size() + liste[1];
            }
            return summe;
            """;

    @Param({"false", "true"})
    public boolean guard;

    private OutputCatchingTestRuntime runtime;
    private RunnableScript loops;
    private RunnableScript moduleCalls;
    private RunnableScript templates;
    private RunnableScript memberAccess;
    private RunnableScript memberCalls;

    @Setup
    public void setup() throws Exception {
        runtime = OutputCatchingTestRuntime.fresh(env -> env.addModule(new ScriptModule("zaehler", MODULE, null)));
        final JanitorEnvironment environment = runtime.getEnvironment();
        Janitor.setUserProvider(new JanitorEnvironmentProvider() {
            @Override
            public JanitorEnvironment getCurrentEnvironment() {
                return environment;
            }
        });
        loops = runtime.compile("loops", LOOPS);
        moduleCalls = runtime.compile("moduleCalls", MODULE_CALLS);
        templates = runtime.compile("templates", TEMPLATES);
        memberAccess = runtime.compile("memberAccess", MEMBER_ACCESS);
        memberCalls = runtime.compile("memberCalls", MEMBER_CALLS);
    }

    @Benchmark
    public JanitorObject loops() throws Exception {
        return loops.run(runtime, p -> { if (guard) { p.setValueGuard((process, value) -> value); } });
    }

    @Benchmark
    public JanitorObject moduleCalls() throws Exception {
        return moduleCalls.run(runtime, p -> { if (guard) { p.setValueGuard((process, value) -> value); } });
    }

    @Benchmark
    public JanitorObject templates() throws Exception {
        return templates.run(runtime, p -> { if (guard) { p.setValueGuard((process, value) -> value); } p.getMainScope().bind("kunde", "ACME"); });
    }

    @Benchmark
    public JanitorObject memberAccess() throws Exception {
        return memberAccess.run(runtime, p -> { if (guard) { p.setValueGuard((process, value) -> value); } });
    }

    @Benchmark
    public JanitorObject memberCalls() throws Exception {
        return memberCalls.run(runtime, p -> { if (guard) { p.setValueGuard((process, value) -> value); } });
    }

}
