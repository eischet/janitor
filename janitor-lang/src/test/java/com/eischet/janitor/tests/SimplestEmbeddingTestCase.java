package com.eischet.janitor.tests;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
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

}
