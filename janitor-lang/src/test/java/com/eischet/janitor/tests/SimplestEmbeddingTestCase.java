package com.eischet.janitor.tests;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.cleanup.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplestEmbeddingTestCase {

    @Test
    public void simplestTestPossible() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorScript script = rt.compile("main", "print('Hello, ' + person + '!');");
        script.run(globals -> globals.bind("person", "JD"));
        assertEquals("Hello, JD!\n", rt.getAllOutput());
    }

    @Test
    public void simpleExpressionTest() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorScript script = rt.compile("main", "3 * x + 1");
        final JanitorObject result = script.run(globals -> globals.bind("x", 2));
        assertEquals(7L, result.janitorGetHostValue());
    }

    @Test
    public void toInt() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorScript script = rt.compile("main", """
                myFloat = 17.3 * 2;
                myInt = myFloat.int;
                assert(myInt == 34);
                """);
        script.run(globals -> globals.bind("x", 2));
    }

}
