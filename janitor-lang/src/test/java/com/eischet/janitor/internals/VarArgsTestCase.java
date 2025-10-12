package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VarArgsTestCase extends JanitorTest {

    @Test
    void noArgs() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function noArgs() {
                return "ok"
            }
            
            return noArgs()
            """;
        assertEquals("ok", evaluate(script).janitorGetHostValue());
    }

    @Test
    void onePositionalArg() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(text) {
                return text
            }
            
            return func("ok")
            """;
        assertEquals("ok", evaluate(script).janitorGetHostValue());
    }

    @Test
    void twoPositionalArgs() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(text1, text2) {
                return text1 + text2
            }
            
            return func("o", "k")
            """;
        assertEquals("ok", evaluate(script).janitorGetHostValue());
    }

    @Test
    void onlyOneVararg() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(*text) {
                accum = ""
                for (element in text) {
                    accum += element
                }
                return accum
            }
            
            return func("ok")
            """;
        assertEquals("ok", evaluate(script).janitorGetHostValue());
    }

    @Test
    void onlyTwoVararg() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(*text) {
                accum = ""
                for (element in text) {
                    accum += element
                    accum += " "
                }
                return accum.trim()
            }
            
            return func("ok", "computer")
            """;
        assertEquals("ok computer", evaluate(script).janitorGetHostValue());
    }

    @Test
    void onlyFourVarargs() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(*text) {
                accum = ""
                for (element in text) {
                    accum += element
                    accum += " "
                }
                return accum.trim()
            }
            
            return func("run", "to", "the", "hills")
            """;
        assertEquals("run to the hills", evaluate(script).janitorGetHostValue());
    }

    @Test
    void positionalAndVarargs() throws JanitorRuntimeException, JanitorCompilerException {
        final String script = """
            function func(a, b, *c) {
                li = [a, b]
                for (element in c) {
                    li.add(element)
                }
                return li.join("-")
            }
            
            return func("1", "2", "3", "4", "5")
            """;
        assertEquals("1-2-3-4-5", evaluate(script).janitorGetHostValue());
    }

    @Test
    void preventVarargsWithinPositionalArgs() {
        final String script = """
            function func(a, *b, c) {
                return a + b + c
            }
            return func("1", "2", "3", "4", "5")
            """;
        assertThrows(JanitorCompilerException.class, () -> evaluate(script));
    }

    /*
    @Test
    void preventKwargsBeforeRegular() {
        final String script = """
                function func(**kwargs, x) {
                }
                """;
        assertThrows(JanitorCompilerException.class, () -> evaluate(script));
    }
    TODO: at the moment, this somehow throws a class cast exception.... needs further work.
     */

    @Test
    void oneDefaultedArg() throws JanitorRuntimeException, JanitorCompilerException {
        @Language("Janitor") final String script = """
                function move(dir0, dir1="down") {
                    return dir0 + " & " + dir1
                }
                return move("up")
                """;
        assertEquals("up & down", evaluate(script).janitorGetHostValue());
    }

    @Test
    void oneDefaultedArgWithActualValueByPosition() throws JanitorRuntimeException, JanitorCompilerException {
        @Language("Janitor") final String script = """
                function move(dir0, dir1="down") {
                    return dir0 + " & " + dir1
                }
                return move("up", "down")
                """;
        assertEquals("up & down", evaluate(script).janitorGetHostValue());
    }

    @Test
    void oneDefaultedArgWithActualValueByName() throws JanitorRuntimeException, JanitorCompilerException {
        @Language("Janitor") final String script = """
                function move(dir0, dir1="down") {
                    return dir0 + " & " + dir1
                }
                return move("down", dir1="out")
                """;
        assertEquals("down & out", evaluate(script).janitorGetHostValue());
    }

    @Test
    void twoDefaultedArgsWithActualValueByName() throws JanitorRuntimeException, JanitorCompilerException {
        @Language("Janitor") final String script = """
                function move(dir0="up", dir1="down") {
                    return dir0 + " & " + dir1
                }
                return move(dir0="left", dir1="right")
                """;
        assertEquals("left & right", evaluate(script).janitorGetHostValue());
    }

    @Test
    void onlyKwargs() throws JanitorRuntimeException, JanitorCompilerException {
        @Language("Janitor") final String script = """
                function move(**kwargs) {
                    return kwargs.dir0 + " & " + kwargs.dir1
                }
                return move(dir0="left", dir1="right")
                """;
        assertEquals("left & right", evaluate(script).janitorGetHostValue());
    }


}
