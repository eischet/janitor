package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JBool;
import com.eischet.janitor.api.types.JInt;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.runtime.RunningScriptProcess;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleExpressionTestCase {

    private static final JanitorDefaultEnvironment ENV = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
        @Override
        public void warn(final String message) {
            log.warn("{}", message);
        }
    };

    private static final Logger log = LoggerFactory.getLogger(SimpleExpressionTestCase.class);

    private JanitorObject eval(final String expressionSource) throws JanitorCompilerException, JanitorRuntimeException {
        return eval(expressionSource, globals -> {
        });
    }

    private JanitorObject eval(final String expressionSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("parsing: " + expressionSource + "\n");
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(expressionSource);
        final ScriptModule module = ScriptModule.unnamed(expressionSource);
        final Script scriptObject = JanitorCompiler.build(ENV, module, script, null);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Location root = Location.at(module, 0, 0, 0, 0);

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(root, null, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        return runningScript.run();
    }


    @Test
    public void testCS() throws JanitorCompilerException, JanitorRuntimeException {


        assertEquals(JInt.of(17), eval("17"));
        assertEquals(ENV.getBuiltins().string("hello"), eval("\"hello\""));

        assertEquals(ENV.getBuiltins().string("hello"), eval("'hello'"));
        assertEquals(JInt.of(-7), eval("-7"));
        // syntax error: eval("+9");
        assertEquals(JInt.of(21), eval("17 + 4"));
        assertEquals(JInt.of(22), eval("(17+4)+1"));
        assertEquals(JInt.of(6), eval("1+2+3"));
        assertEquals(ENV.getBuiltins().string("stefan"), eval("currentUser", globals -> globals.bind("currentUser", ENV.getBuiltins().string("stefan"))));

        assertEquals(JBool.TRUE, eval("i < 17", globals -> {
            globals.bind("i", 10);
        }));
        assertEquals(JBool.FALSE, eval("i < 10", globals -> {
            globals.bind("i", 10);
        }));

        assertEquals(JBool.TRUE, eval("i < 17", globals -> {
            globals.bind("i", 10);
        }));
        assertEquals(JBool.TRUE, eval("i < 22", globals -> {
            globals.bind("i", 10);
        }));
        assertEquals(JBool.TRUE, eval("i > 8", globals -> {
            globals.bind("i", 10);
        }));

        assertEquals(JBool.TRUE, eval("'a' < 'b'"));
        assertEquals(JBool.FALSE, eval("'a' > 'b'"));
        assertEquals(JBool.FALSE, eval("'a' == 7"));
        assertEquals(JBool.TRUE, eval("17 == 15 + 2"));
        assertEquals(JBool.TRUE, eval("22 == 2 * (10 + 1)"));

        assertEquals(JBool.TRUE, eval("22 == 2 * (i + 1)", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("22 <= 2 * (i + 1)", globals -> {
            globals.bind("i", JInt.of(10));
        }));

        assertEquals(JBool.TRUE, eval("true"));
        assertEquals(JBool.FALSE, eval("false"));
        assertEquals(JBool.TRUE, eval("true and true"));
        assertEquals(JBool.TRUE, eval("not false"));
        assertEquals(JBool.FALSE, eval("not true"));

        assertEquals(JBool.FALSE, eval("i < 11 and i > 22", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.FALSE, eval("(i < 11) and (i > 22)", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.FALSE, eval("((i < 11) and (i > 22))", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("i < 11 and i > 8", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("(i < 11) and (i > 8)", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("((i < 11) and (i > 8))", globals -> {
            globals.bind("i", JInt.of(10));
        }));

        // LATER assertEquals("UNDEFINED", eval("(i < (11 and i) > 8))", globals -> { globals.bind("i", CSConstantInteger.of(10));}));
        // WRONG assertEquals("UNDEFINED", eval("1<2<3", globals -> { globals.bind("i", CSConstantInteger.of(10));}));

        assertEquals(JBool.TRUE, eval("1<2==true", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("i==10", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("i==10==true", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("(i==10)==true", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("(10==i)==true", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("(10==i)", globals -> {
            globals.bind("i", JInt.of(10));
        }));

        assertEquals(JBool.FALSE, eval("(10!=i)", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.FALSE, eval("(i!=10)", globals -> {
            globals.bind("i", JInt.of(10));
        }));
        assertEquals(JBool.TRUE, eval("not (i!=10)", globals -> {
            globals.bind("i", JInt.of(10));
        }));

    }

    @Test
    public void ternary() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(ENV.getBuiltins().string("bar"), eval("2 > 1 ? 'bar' : 'foo'"));
        assertEquals(ENV.getBuiltins().string("foo"), eval("2 < 1 ? 'bar' : 'foo'"));
        assertEquals(JBool.TRUE, eval("null or true"));
        assertEquals(JInt.of(9), eval("( null or true ) ? 9 : 'foo' "));
    }


    @Test
    public void ifThenElse() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("foo", eval("if 17 > 8 then 'foo'").janitorGetHostValue());
        assertEquals("foo", eval("if 17 > 8 then 'foo' else 'bar'").janitorGetHostValue());
        assertEquals("bar", eval("if 17 < 8 then 'foo' else 'bar'").janitorGetHostValue());

        assertEquals(9L, eval("if not false then 9 else 17").janitorGetHostValue());
        assertEquals(17L, eval("if false then 9 else 17").janitorGetHostValue());

    }



    @Test
    public void matchingOperator() throws JanitorCompilerException, JanitorRuntimeException {
        // final Interpreter interpreter = new Interpreter();
        assertTrue((Boolean) eval("'abc' ~ 'a*'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ 'a*c'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ '*c'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ 'abc'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ '*c*'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ '*b*'").janitorGetHostValue());
        assertTrue((Boolean) eval("'abc' ~ '*a*'").janitorGetHostValue());
        assertFalse((Boolean) eval("'abc' ~ '*d*'").janitorGetHostValue());

        assertEquals("nix",
            eval("if USR_SC ~ 'Z*_20*' then 'Achtung!|Markt geschlossen!' else 'nix'",
                g -> g.bind("USR_SC", ENV.getBuiltins().string("245005"))).janitorGetHostValue().toString()
        );

        assertEquals("Achtung!|Markt geschlossen!",
            eval("if USR_SC ~ 'Z*_20*' then 'Achtung!|Markt geschlossen!' else 'nix'",
                g -> g.bind("USR_SC", ENV.getBuiltins().string("Z12345_20191122"))).janitorGetHostValue().toString()
        );

        assertEquals(JBool.TRUE,
            eval("count == 1 and score == 100 and (not (shortCode ~ 'Z*_20*'))",
                g -> g.bind("count", 1)
                    .bind("score", 100)
                    .bind("shortCode", "12345")));

        assertEquals(JBool.FALSE,
            eval("count == 1 and score == 100 and (not (shortCode ~ 'Z*_20*'))",
                g -> g.bind("count", 1)
                .bind("score", 100)
                .bind("shortCode", "Z12345_20191121")));

    }


}
