package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallbackTestCase {

    /**
     * Test the new callback functionality, where we can retrieve a JCallable from the Java side and call it freely when needed again.
     *
     * @throws JanitorCompilerException if the test fails
     * @throws JanitorRuntimeException if the test fails
     */
    @Test
    void simpleCallback() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();

        final RunnableScript firstScript = rt.compile("first", "callback = it -> it * 2;");
        final ResultAndScope ras = firstScript.runAndKeepGlobals(TestEnv.NO_GLOBALS);
        JCallable callback = (JCallable) ras.getScope().retrieveLocal("callback");

        final JanitorObject thirtyFour = rt.executeCallback(ras.getScope(), callback, List.of(TestEnv.env.getBuiltinTypes().integer(17)));
        assertEquals(34L, thirtyFour.janitorGetHostValue());

        final JanitorObject oneHundredAndEighty = rt.executeCallback(ras.getScope(), callback, List.of(TestEnv.env.getBuiltinTypes().integer(90)));
        assertEquals(180L, oneHundredAndEighty.janitorGetHostValue());

    }

}
