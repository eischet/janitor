package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing Janitor using <a href="https://sampleprograms.io/">SamplePrograms.io</a>.
 *
 * Why? Because reading sample code is very important to understand how a language works, looks and feels.
 * But most existing code is currently "buried" in various customer systems, and will not likely ever be available.
 * So here's some synthetic code that makes Janitor comparable to other languages. You probably wouldn't write
 * programs like these in reality.
 */
public class SampleProgramsIOTestCase {

    private static final String RELATIVE_LOCATION = "../sample-scripts/sampleprograms.io"; // where the scripts are located

    /**
     * Strip off the trailing newline to make assertEquals more readable for the usual one-liners.
     * This is a concession to the fact that Janitor's default print statement automatically emits a newline at the end.
     * @param text some text
     * @return the text with trailing newline removed
     */
    private String trimTrailingNewline(final String text) {
        if (text.endsWith("\n")) {
            return text.substring(0, text.length() - 1);
        } else {
            return text;
        }
    }

    private String runScriptAndReturnOutput(final String scriptFile, final List<String> args)
            throws IOException, JanitorCompilerException, JanitorRuntimeException {
        final Path scriptPath = Path.of(RELATIVE_LOCATION, scriptFile);
        try {
            @Language("Janitor") final String scriptCode = Files.readString(scriptPath, StandardCharsets.UTF_8);
            final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
            final RunnableScript script = runtime.compile(scriptPath.getFileName().toString(), scriptCode);
            script.run(globals -> globals.bind("args", runtime.getBuiltinTypes().list(args.stream().map(runtime.getBuiltinTypes()::string))));
            return trimTrailingNewline(runtime.getAllOutput());
        } catch (NoSuchFileException | FileNotFoundException e) {
            throw new RuntimeException("File not found: " + scriptPath.toAbsolutePath(), e);
        }
    }

    private String runScriptAndReturnOutput(final String scriptFile)
            throws IOException, JanitorCompilerException, JanitorRuntimeException {
        return runScriptAndReturnOutput(scriptFile, Collections.emptyList());
    }

    @Test
    public void helloWorld() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String file = "HelloWorld.janitor";
        assertEquals("Hello, World!", runScriptAndReturnOutput(file));
    }

    /**
     * <a href="https://sampleprograms.io/projects/even-odd/">Even Odd test</a>.
     * @throws JanitorRuntimeException on errors
     * @throws IOException on errors
     * @throws JanitorCompilerException on errors
     */
    @Test
    public void evenOdd() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String file = "EvenOdd.janitor";
        // Even Odd Valid Tests
        assertEquals("Even", runScriptAndReturnOutput(file, List.of("2")));
        assertEquals("Odd", runScriptAndReturnOutput(file, List.of("5")));
        assertEquals("Even", runScriptAndReturnOutput(file, List.of("-14")));
        assertEquals("Odd", runScriptAndReturnOutput(file, List.of("-27")));
        // Even Odd Invalid Tests
        final String usage = "Usage: please input a number";
        assertEquals(usage, runScriptAndReturnOutput(file, Collections.emptyList()));
        // assertEquals(usage, runScriptAndReturnOutput(file, List.of("")));
        assertEquals(usage, runScriptAndReturnOutput(file, List.of("a")));
    }

    @Test
    public void countDuplicateCharacters() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String file = "DuplicateCharacterCounter.janitor";
        // Duplicate Character Counter Valid Tests
        assertEquals("No duplicate characters", runScriptAndReturnOutput(file, List.of("hola")));
        assertEquals("""
                o: 2
                b: 2
                e: 2""", runScriptAndReturnOutput(file, List.of("goodbyeblues")));
        assertEquals("Usage: please provide a string", runScriptAndReturnOutput(file, Collections.emptyList()));
        assertEquals("Usage: please provide a string", runScriptAndReturnOutput(file, List.of("")));
    }

    /**
     * As simple as it gets: <a href="https://sampleprograms.io/projects/baklava/">Baklava pattern test</a>.
     * @throws JanitorRuntimeException on errors
     * @throws IOException on errors
     * @throws JanitorCompilerException on errors
     */
    @Test
    public void baklava() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String expected = """
                          *
                         ***
                        *****
                       *******
                      *********
                     ***********
                    *************
                   ***************
                  *****************
                 *******************
                *********************
                 *******************
                  *****************
                   ***************
                    *************
                     ***********
                      *********
                       *******
                        *****
                         ***
                          *""";
        //                 ^ The final newline is stripped off by the test runner, so we need to omit it here, too.
        assertEquals(expected, runScriptAndReturnOutput("Baklava.janitor"));
    }

    /**
     * <a href="https://sampleprograms.io/projects/remove-all-whitespace/">Remove all whitespace test</a>.
     * @throws JanitorRuntimeException when it feels like it
     * @throws IOException when it feels like it
     * @throws JanitorCompilerException seriously, why is this even required for unit tests in 2024?
     */
    @Test
    public void removeAllWhitespace() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String file = "RemoveAllWhitespace.janitor";
        final String good = "RemoveAllWhitespace";
        final String bad = "Usage: please provide a string";
        // Remove All Whitespace Valid Tests
        assertEquals(good, runScriptAndReturnOutput(file, List.of("RemoveAllWhitespace")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of(" RemoveAllWhitespace")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of("RemoveAllWhitespace ")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of("Remove All Whitespace")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of("\tRemove\tAll\tWhitespace\t")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of("\nRemove\nAll\nWhitespace\n")));
        assertEquals(good, runScriptAndReturnOutput(file, List.of("\rRemove\rAll\rWhitespace\r")));
        // Remove All Whitespace Invalid Tests
        assertEquals(bad, runScriptAndReturnOutput(file, Collections.emptyList()));
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("")));
    }

    /**
     * <a href="https://sampleprograms.io/projects/fibonacci/">Fibonacci number test</a>.
     * @throws JanitorRuntimeException on errors
     * @throws IOException on errors
     * @throws JanitorCompilerException guess when
     */
    @Test
    public void fibonacci() throws JanitorRuntimeException, IOException, JanitorCompilerException {
        final String file = "Fibonacci.janitor";
        final String bad = "Usage: please input the count of fibonacci numbers to output";
        // Fibonacci Valid Tests
        assertEquals("", runScriptAndReturnOutput(file, List.of("0")));
        assertEquals("1: 1", runScriptAndReturnOutput(file, List.of("1")));
        assertEquals("1: 1\n2: 1", runScriptAndReturnOutput(file, List.of("2")));
        assertEquals("""
                1: 1
                2: 1
                3: 2
                4: 3
                5: 5""", runScriptAndReturnOutput(file, List.of("5")));
        assertEquals("""
                1: 1
                2: 1
                3: 2
                4: 3
                5: 5
                6: 8
                7: 13
                8: 21
                9: 34
                10: 55""", runScriptAndReturnOutput(file, List.of("10")));

        // Fibonacci Invalid Tests
        assertEquals(bad, runScriptAndReturnOutput(file, Collections.emptyList()));
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("")));
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("a")));
    }

    /**
     * Looking for unbreakable string encryption? Go look elsewhere. ;-)
     * <a href="https://sampleprograms.io/projects/rot13/">Rot13 test</a>.
     * @throws Exception on errors
     */
    @Test
    public void rot13() throws Exception {
        final String file = "Rot13.janitor";
        final String bad = "Usage: please provide a string to encrypt";
        // Rot13 Valid Tests
        assertEquals("gur dhvpx oebja sbk whzcrq bire gur ynml qbt", runScriptAndReturnOutput(file, List.of("the quick brown fox jumped over the lazy dog")));
        assertEquals("GUR DHVPX OEBJA SBK WHZCRQ BIRE GUR YNML QBT", runScriptAndReturnOutput(file, List.of("THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG")));
        assertEquals("Gur dhvpx oebja sbk whzcrq. Jnf vg bire gur ynml qbt?", runScriptAndReturnOutput(file, List.of("The quick brown fox jumped. Was it over the lazy dog?")));
        // Rot13 Invalid Tests
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("")));
        assertEquals(bad, runScriptAndReturnOutput(file, Collections.emptyList()));
    }

    /**
     * <a href="https://sampleprograms.io/projects/longest-word/">Longest word test</a>.
     * Finds the longest word in a text and prints its length.
     * @throws Exception on errors
     */
    @Test
    public void longestWord() throws Exception {
        final String file = "LongestWord.janitor";
        final String bad = "Usage: please provide a string";
        // Longest Word Valid Tests
        assertEquals("5", runScriptAndReturnOutput(file, List.of("May the force be with you")));
        assertEquals("29", runScriptAndReturnOutput(file, List.of("Floccinaucinihilipilification")));
        assertEquals("5", runScriptAndReturnOutput(file, List.of("Hi,\nMy name is Paul!")));
        // Longest Word Invalid Tests
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("")));
        assertEquals(bad, runScriptAndReturnOutput(file, Collections.emptyList()));
    }

    @Test
    public void fizzBuzz() throws Exception {
        // First 15 lines out output are expected to be:
        final String expected = """
                1
                2
                Fizz
                4
                Buzz
                Fizz
                7
                8
                Fizz
                Buzz
                11
                Fizz
                13
                14
                FizzBuzz
                """;
        assertEquals(expected, runScriptAndReturnOutput("FizzBuzz.janitor").substring(0, expected.length()));
        assertTrue(runScriptAndReturnOutput("FizzBuzz.janitor").startsWith(expected));
    }

    /**
     * <a href="https://sampleprograms.io/projects/quine/">Quine</a>.
     * A quine is a program that prints its own source code. We're doing it Kobayashi Maru style here. ;-)
     * @throws Exception on errors
     */
    @Test
    public void quine() throws Exception {
        final Path scriptPath = Path.of(RELATIVE_LOCATION, "Quine.janitor");
        @Language("Janitor") final String scriptCode = Files.readString(scriptPath, StandardCharsets.UTF_8);
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile(scriptPath.getFileName().toString(), scriptCode);
        // Run the script, and provide a function "quine" to it that, when called, returns the script code we just loaded.
        // This can safely be considered as cheating, and I bet it would make Glen Matthews proud.
        // What the script actually does is totally irrelevant, as long as it does not print anything but the quine() call's result.
        script.run(globals -> globals.bindF("quine", (process, arguments) -> process.getBuiltins().string(scriptCode)));
        // We have to trim the final newline added by print(); alternatively, the quine function could remove the final newline
        // from the script code, but then this would break when the script file does not end on a newline.
        final String output = trimTrailingNewline(runtime.getAllOutput());
        assertEquals(scriptCode, output);
        // There's a valid point to be taken from this "quine": we do NOT have to do everything in the scripting language but can
        // delegate any boring or complicated stuff to the runtime, which we can freely customize for the task at hand.
    }

    /**
     * <a href="https://sampleprograms.io/projects/binary-search/">Binary Search</a>.
     * @throws Exception on errors
     */
    @Test
    public void binarySearch() throws Exception {
        final String file = "BinarySearch.janitor";
        final String bad = "Usage: please provide a list of sorted integers (\"1, 4, 5, 11, 12\") and the integer to find (\"11\")";
        // Binary Search Valid Tests
        assertEquals("true", runScriptAndReturnOutput(file, List.of("1, 3, 5, 7", "1")), "Sample Input: First True");
        assertEquals("true", runScriptAndReturnOutput(file, List.of("1, 3, 5, 7", "7")), "Sample Input: Last True");
        assertEquals("true", runScriptAndReturnOutput(file, List.of("1, 3, 5, 7", "5")), "Sample Input: Middle True");
        assertEquals("true", runScriptAndReturnOutput(file, List.of("5", "5")), "Sample Input: One True");
        assertEquals("false", runScriptAndReturnOutput(file, List.of("5", "7")), "Sample Input: One False");
        assertEquals("false", runScriptAndReturnOutput(file, List.of("1, 3, 5, 6", "7")), "Sample Input: Many False");
        assertEquals("true", runScriptAndReturnOutput(file, List.of("1, 2, 3, 4, 5, 6, 7", "3")), "Sample Input: Middle True");




        // Binary Search Invalid Tests
        assertEquals(bad, runScriptAndReturnOutput(file, Collections.emptyList()), "No Input");
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("1,2,3,4")), "Missing Input: Target");
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("", "5")), "Missing Input: List");
        assertEquals(bad, runScriptAndReturnOutput(file, List.of("3,5,1,2", "3")), "Out Of Order Input");
    }
}
