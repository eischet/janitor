package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionalSemicolonTestCase extends JanitorTest {

    @Test
    void simpleExpressionsWithoutSemicolon() throws JanitorRuntimeException, JanitorCompilerException {
        Assertions.assertEquals("foo\nok\n", getOutput("function doit() { print('foo') }\n\ndoit()\n\nprint('ok')"));
        Assertions.assertEquals("a\nb\nc\n", getOutput("""
                
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
