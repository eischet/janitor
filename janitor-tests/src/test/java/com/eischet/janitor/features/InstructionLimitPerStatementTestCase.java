// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorInstructionLimitExceededException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The instruction limit is a budget for the whole run: a script that executes more statements than the host allows fails,
 * whatever kind of statement it executes, not only in loops.
 */
public class InstructionLimitPerStatementTestCase extends JanitorTest {

    private static final int REPETITIONS = 1000;
    private static final int LIMIT = 500;

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            x = 1;                         |
            x += 1;                        | x = 0;
            1 + 2;                         |
            f();                           | function f() { }
            l.size();                      | l = [];
            x++;                           | x = 0;
            x--;                           | x = 0;
            ++x;                           | x = 0;
            --x;                           | x = 0;
            import collections;            |
            do { } while (false);          |
            for (i from 1 to 1) { }        |
            for (e in l) { }               | l = [1];
            """)
    void everyStatementCounts(final String statement, final String setup) {
        final String script = (setup == null ? "" : setup) + "\n" + (statement + "\n").repeat(REPETITIONS);
        assertDoesNotThrow(() -> evaluateWithConfigurableProcess(script, process -> process.setMaxInstructionCount(100 * REPETITIONS)));
        assertThrows(JanitorInstructionLimitExceededException.class,
                () -> evaluateWithConfigurableProcess(script, process -> process.setMaxInstructionCount(LIMIT)));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            do { } while (true);
            for (i from 1 to 1000000000) { }
            """)
    void endlessLoopsWithoutStatementsInTheirBodyEnd(final String loop) {
        assertThrows(JanitorInstructionLimitExceededException.class,
                () -> evaluateWithConfigurableProcess(loop, process -> process.setMaxInstructionCount(LIMIT)));
    }
}
