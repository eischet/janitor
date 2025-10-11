package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalSemicolonTestCase extends JanitorTest {

    @Test
    void simpleExpressionsWithoutSemicolon() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("foo\nok\n", getOutput("function doit() { print('foo') }\n\ndoit()\n\nprint('ok')"));
        assertEquals("a\nb\nc\n", getOutput("""
                
                a = 'a'
                c = 'c'
                b = 'foob'[-1:]
                
                print(a)
                i = 17 + 4
                if (i == 21) {
                    print(b)
                }
                print(c)
                """));
    }


}
