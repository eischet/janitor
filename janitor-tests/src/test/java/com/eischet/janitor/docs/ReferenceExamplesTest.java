// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docs;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs every Janitor example of the language reference in {@code docs/reference}.
 * <p>
 * A block with the info string {@code janitor} is compiled and run with all standard modules registered. It must not fail,
 * and it may be followed by blocks that state what it does:
 * <ul>
 *     <li>{@code output}: exactly what the script prints,</li>
 *     <li>{@code result}: the script's result value, as a string.</li>
 * </ul>
 * {@code janitor error} must fail, either when compiling or when running; a following {@code error} block contains text that the
 * error message must include. {@code janitor compile-only} is only compiled, e.g. because it needs a network or host bindings.
 */
public class ReferenceExamplesTest extends JanitorTest {

    private static final Set<String> FLAGS = Set.of("", "error", "compile-only");

    @TestFactory
    List<DynamicTest> examples() {
        final List<DynamicTest> tests = new ArrayList<>();
        for (final Path file : ReferenceFiles.markdownFiles()) {
            final String name = file.getFileName().toString();
            final List<ReferenceFiles.CodeBlock> blocks = ReferenceFiles.codeBlocks(name, ReferenceFiles.read(file));
            for (int i = 0; i < blocks.size(); i++) {
                final ReferenceFiles.CodeBlock block = blocks.get(i);
                if (!block.language().equals("janitor")) {
                    continue;
                }
                final List<ReferenceFiles.CodeBlock> companions = new ArrayList<>();
                for (int j = i + 1; j < blocks.size() && Set.of("output", "result", "error").contains(blocks.get(j).language()); j++) {
                    companions.add(blocks.get(j));
                }
                tests.add(DynamicTest.dynamicTest(name + ":" + block.line(), () -> check(block, companions)));
            }
        }
        assertFalse(tests.isEmpty(), "no examples found");
        return tests;
    }

    private static String companion(final List<ReferenceFiles.CodeBlock> companions, final String language) {
        return companions.stream().filter(c -> c.language().equals(language)).map(ReferenceFiles.CodeBlock::content).findFirst().orElse(null);
    }

    private void check(final ReferenceFiles.CodeBlock block, final List<ReferenceFiles.CodeBlock> companions) throws Exception {
        assertTrue(FLAGS.contains(block.flags()), block.file() + ":" + block.line() + ": unknown flags '" + block.flags() + "'");
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(ReferenceGenerator::registerAllModules);
        final String where = block.file() + ":" + block.line();
        if (block.hasFlag("error")) {
            final Exception error = assertThrows(Exception.class, () -> runtime.compile(where, block.content()).run(), where + ": expected an error");
            assertTrue(error instanceof JanitorCompilerException || error instanceof JanitorRuntimeException,
                    where + ": expected a compiler or runtime error, got " + error);
            final String expected = companion(companions, "error");
            if (expected != null) {
                final String message = String.valueOf(error.getMessage());
                assertTrue(message.contains(expected.strip()), where + ": the error message should contain '" + expected.strip() + "', but is: " + message);
            }
            return;
        }
        final RunnableScript script = runtime.compile(where, block.content());
        if (block.hasFlag("compile-only")) {
            return;
        }
        final JanitorObject result = script.run();
        final String expectedOutput = companion(companions, "output");
        if (expectedOutput != null) {
            assertEquals(expectedOutput, runtime.getAllOutput(), where + ": output");
        }
        final String expectedResult = companion(companions, "result");
        if (expectedResult != null) {
            assertEquals(expectedResult.strip(), result.janitorToString(), where + ": result");
        }
    }
}
