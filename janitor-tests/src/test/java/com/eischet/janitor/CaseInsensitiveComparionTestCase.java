package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CaseInsensitiveComparionTestCase extends JanitorTest {

    @Test
    void testCaseInsensitiveComparison() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        Assertions.assertEquals(JBool.TRUE, rt.compile("test", "'x' ~= 'X'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.TRUE, rt.compile("test", "'X' ~= 'X'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.TRUE, rt.compile("test", "'x' ~= 'x'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.TRUE, rt.compile("test", "'X' ~= 'x'").run(JanitorTest.NO_GLOBALS));

        Assertions.assertEquals(JBool.FALSE, rt.compile("test", "'x' ~= 'Y'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.FALSE, rt.compile("test", "'X' ~= 'Y'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.FALSE, rt.compile("test", "'x' ~= 'y'").run(JanitorTest.NO_GLOBALS));
        Assertions.assertEquals(JBool.FALSE, rt.compile("test", "'X' ~= 'y'").run(JanitorTest.NO_GLOBALS));

    }

}
