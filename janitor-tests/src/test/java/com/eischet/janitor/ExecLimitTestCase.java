package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorInstructionLimitExceededException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExecLimitTestCase extends JanitorTest{

    private static final String LOOPING = """
            limit = 100
            counter = 0
            while (counter < limit) {
                counter++
            }
        """;

    @Test
    void testAnArbitraryLimit() throws JanitorRuntimeException, JanitorCompilerException {
        evaluateWithConfigurableProcess(LOOPING, process -> {});
        evaluateWithConfigurableProcess(LOOPING, process -> process.setMaxInstructionCount(1000));
        assertThrows(JanitorRuntimeException.class, () -> evaluateWithConfigurableProcess(LOOPING, process -> process.setMaxInstructionCount(50)));

    }

    @Test void testWhileTrue() throws JanitorRuntimeException, JanitorCompilerException {
        assertThrows(JanitorInstructionLimitExceededException.class, () -> evaluateWithConfigurableProcess("while (true) { }", proc -> proc.setMaxInstructionCount(1000)));
    }


}
