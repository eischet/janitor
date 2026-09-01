package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.repl.JanitorRepl;
import com.eischet.janitor.repl.PartialParseResult;
import com.eischet.janitor.repl.ReplIO;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplTestCase extends JanitorTest {


    static class FakeLoggingIO implements ReplIO {
        protected static final JanitorLogger log = JanitorLogger.getLogger(ReplTestCase.class);

        @Override
        public String readLine(final String prompt) throws IOException {
            return null;
        }

        @Override
        public void print(String message) {
            log.info("{}", message);
        }

        @Override
        public void println(final String text) {
            log.info("{}", text);
        }

        @Override
        public void error(final String text) {
            log.error("{}", text);
        }

        @Override
        public void exception(final Exception e) {
            log.error("exception", e);
        }
    }


    @Test
    public void partialParsing() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.OK, repl.parse("x = 17;"));


        assertEquals(PartialParseResult.INCOMPLETE, repl.parse("if (x < 20) {"));
        assertEquals(PartialParseResult.INCOMPLETE, repl.parse("if (x < 20) {\n  print(x);"));
        assertEquals(PartialParseResult.OK, repl.parse("if (x < 20) {\n  print(x);\n}"));


        repl.parse("if (x > 5) { return 'X is greater than five'; }");


        rt.resetOutput();
        repl.parse("print('x has value:', x);");
        log.info("output: {}", rt.getAllOutput());

        assertEquals("x has value: 17\n", rt.getAllOutput());

        rt.resetOutput();
        repl.parse("function double(x) { return 2*x; }");
        repl.parse("print('double(17) =', double(17));");
        assertEquals("double(17) = 34\n", rt.getAllOutput());


    }

    // Regression tests for the "()/{}/[] inside a string or comment permanently sticks the REPL
    // in '...' continuation mode" bug: the incomplete-input heuristics used to count brackets
    // and triple-quote delimiters over the raw characters, without knowing whether they were
    // inside a string literal or a comment.

    @Test
    public void bracketInsideAStringDoesNotTriggerContinuation() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.OK, repl.parse("print(\"(\");"));
        assertEquals(PartialParseResult.OK, repl.parse("x = \"{[(\";"));
    }

    @Test
    public void bracketInsideALineCommentDoesNotTriggerContinuation() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.OK, repl.parse("x = 1; // ("));
    }

    @Test
    public void bracketInsideABlockCommentDoesNotTriggerContinuation() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.OK, repl.parse("x = 1; /* ( { [ */"));
    }

    @Test
    public void tripleQuoteInsideAStringOrCommentDoesNotTriggerContinuation() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.OK, repl.parse("x = '\"\"\"'; // \"\"\""));
    }

    @Test
    public void aGenuinelyUnclosedBracketStillTriggersContinuation() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.INCOMPLETE, repl.parse("print("));
        assertEquals(PartialParseResult.OK, repl.parse("print(\n17);"));
    }

    @Test
    public void aGenuinelyUnclosedTripleQuotedStringStillTriggersContinuationUntilClosed() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());
        assertEquals(PartialParseResult.INCOMPLETE, repl.parse("x = \"\"\"line one"));
        assertEquals(PartialParseResult.OK, repl.parse("x = \"\"\"line one\nline two\"\"\";"));
    }

    @Test
    public void resetBufferRecoversFromAStuckContinuationWithoutLosingGlobals() throws JanitorControlFlowException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorRepl repl = new JanitorRepl(rt, new FakeLoggingIO());

        repl.acceptText("x = 42;");
        assertEquals(repl.getDefaultPrompt(), repl.getPrompt());

        repl.acceptText("print(");
        assertEquals(repl.getContinuePrompt(), repl.getPrompt(), "an unclosed '(' should put the REPL into continuation mode");

        repl.resetBuffer();
        assertEquals(repl.getDefaultPrompt(), repl.getPrompt());

        rt.resetOutput();
        repl.parse("print(x);");
        assertEquals("42\n", rt.getAllOutput(), "the global defined before getting stuck must still be there after resetBuffer()");
    }

}
