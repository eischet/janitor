package com.eischet.janitor.tests.cleanup;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.scopes.ScriptModule;
import com.eischet.janitor.cleanup.api.api.types.JBool;
import com.eischet.janitor.cleanup.api.api.types.JInt;
import com.eischet.janitor.cleanup.api.api.types.JString;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.compiler.JanitorCompiler;
import com.eischet.janitor.cleanup.compiler.ast.statement.Script;
import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.cleanup.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.cleanup.runtime.RunningScriptProcess;
import com.eischet.janitor.cleanup.runtime.scope.Scope;
import janitor.lang.JanitorParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class BackwardCompatTestCase {
    
    private static final Logger log = LoggerFactory.getLogger(BackwardCompatTestCase.class);

    private JanitorObject eval(final String expressionSource) throws JanitorCompilerException, JanitorRuntimeException {
        return eval(expressionSource, globals -> {
        });
    }

    private JanitorObject eval(final String expressionSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("parsing: " + expressionSource + "\n");
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(expressionSource);
        final ScriptModule module = ScriptModule.unnamed(expressionSource);
        final Script scriptObject = JanitorCompiler.build(module, script, null);
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
        assertEquals(JString.of("hello"), eval("\"hello\""));

        assertEquals(JString.of("hello"), eval("'hello'"));
        assertEquals(JInt.of(-7), eval("-7"));
        // syntax error: eval("+9");
        assertEquals(JInt.of(21), eval("17 + 4"));
        assertEquals(JInt.of(22), eval("(17+4)+1"));
        assertEquals(JInt.of(6), eval("1+2+3"));
        assertEquals(JString.of("stefan"), eval("currentUser", globals -> globals.bind("currentUser", JString.of("stefan"))));

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

    /* LATER auch diese Tests sollten laufen
        private static class FlatFoo implements CSObjectProxy {

            @Override
            public @NotNull CSValue asScriptingObject() {
                return new CSObjectFromProxy(this);
            }

            @Override
            public @NotNull Stream<CSProperty> getScriptProperties() {
                return Stream.of(CSProperty.ofString("foo", () -> "bar"));
            }

            @Override
            public String lookupMacro(final String key) {
                return null;
            }

            @Override
            public String toString() {
                return "FLATFOO";
            }

        }

        private static class NestedFoo implements CSObjectProxy {

            @Override
            public @NotNull CSValue asScriptingObject() {
                return new CSObjectFromProxy(this);
            }

            @Override
            public @NotNull Stream<CSProperty> getScriptProperties() {
                return Stream.of(CSProperty.ofObject("foo", FlatFoo::new, FlatFoo::new));
            }

            @Override
            public String lookupMacro(final String key) {
                return null;
            }

        }

        @Test
        public void objectNesting() throws CockpitScriptParsingError, CockpitScriptRuntimeException {
            final Interpreter ctx = new Interpreter();
            ctx.setProperty("currentUser", CSType.fromString("stefan"));
            ctx.setProperty("baz", CSType.fromObject(new FlatFoo()));
            assertEquals("STRING(bar)", eval("baz.foo"));
            assertEquals("STRING(bar)", eval("(baz.foo)"));
            assertEquals("STRING(barf)", eval("(baz.foo)+'f'"));
            assertEquals("STRING(backup)", eval("(bar.foo.glob or 'backup')"));
            assertEquals("OBJECT(FLATFOO)", eval("(baz or 'backup')"));

            ctx.setProperty("baz", CSType.fromObject(new NestedFoo()));
            assertEquals("STRING(bar)", eval("baz.foo.foo"));
            assertEquals("INT(3)", eval("len(baz.foo.foo)"));
            assertEquals("INT(5)", eval("1 + len(baz.foo.foo) + 1"));
            assertEquals("INT(5)", eval("len('x'+baz.foo.foo+'y')"));
            ctx.setProperty("pi", CSType.fromDouble(3.14));
            assertEquals("FLOAT(0.0)", eval("4*pi-2*pi-2*pi"));
            assertEquals("INT(3)", eval("int(pi)"));

            ctx.setImplicitObject(new NestedFoo());
            assertEquals("STRING(bar)", eval("baz.foo.foo"));
            assertEquals("STRING(bar)", eval("foo.foo"));


        }
    */
    @Test
    public void ternary() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(JString.of("bar"), eval("2 > 1 ? 'bar' : 'foo'"));
        assertEquals(JString.of("foo"), eval("2 < 1 ? 'bar' : 'foo'"));
        assertEquals(JBool.TRUE, eval("null or true"));
        assertEquals(JInt.of(9), eval("( null or true ) ? 9 : 'foo' "));
    }

    /*

    @Test
    public void builtins() throws CockpitScriptParsingError, CockpitScriptRuntimeException {
        interpreter.setProperty("pi", CSType.fromDouble(3.14));
        interpreter.addFunction("fmt", args -> CSType.fromString("6,28"));
        interpreter.addFunction("foo", args -> CSType.undefined());

        interpreter.addFunction("sum", args -> CSType.fromInt(args.stream().map(CSNumber::cast).mapToLong(CSNumber::getIntValue).sum()));


        assertEquals("UNDEFINED", eval("foo(1,2,3,4)"));
        assertEquals("STRING(6,28)", eval("fmt(2*pi)"));
        assertEquals("STRING(6,28)", eval("fmt(sum(pi,pi,pi)-pi)"));
        //assertEquals("FLOAT(3.0)")
        assertEquals("INT(102)", eval("sum(33,34,35)"));
        assertEquals("INT(103)", eval("1 + sum(33,34,35)"));
        assertEquals("FLOAT(3.0)", eval("1.0+2"));
        assertEquals("FLOAT(3.0)", eval("1.0+2"));
        assertEquals("FLOAT(3.0)", eval("1.5*2"));
        assertEquals("FLOAT(103.0)", eval("1.0 + sum(33,34,35)"));
        assertEquals("FLOAT(105.0)", eval("sum(33,34,35) + 1.5 * 2"));
    }

    @Test
    public void letsTryDates() throws CockpitScriptParsingError, CockpitScriptRuntimeException {
        final Interpreter interpreter = new Interpreter();
        interpreter.setProperty("birthday", CSType.fromLocalDateTime(LocalDateTime.of(1976, 1, 10, 11, 11)));
        assertEquals("(DT:1976-01-10T11:11)", eval("birthday"));
        assertEquals("INT(1976)", eval("birthday.year"));
        assertEquals("INT(2019)", eval("birthday.year + 43"));
    }
*/
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
                g -> g.bind("USR_SC", JString.of("245005"))).janitorGetHostValue().toString()
        );

        assertEquals("Achtung!|Markt geschlossen!",
            eval("if USR_SC ~ 'Z*_20*' then 'Achtung!|Markt geschlossen!' else 'nix'",
                g -> g.bind("USR_SC", JString.of("Z12345_20191122"))).janitorGetHostValue().toString()
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

        /*




        final CockpitScript filter = interpreter.compile("not (shortCode ~ 'MAIL_*' or shortCode ~ 'SMS_*')");
        interpreter.setProperty("shortCode", CSType.fromString("BAR"));
        assertTrue(filter.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAIL_FOO"));
        assertFalse(filter.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAILXFHFU"));
        assertTrue(filter.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("SMS_BAR"));
        assertFalse(filter.eval(interpreter).isTruthy());

        final CockpitScript filter2 = interpreter.compile("not ( shortCode ~ 'MAIL_*' ) and not ( shortCode ~ 'SMS_*' )");
        interpreter.setProperty("shortCode", CSType.fromString("BAR"));
        assertTrue(filter2.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAIL_FOO"));
        assertFalse(filter2.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAILXFHFU"));
        assertTrue(filter2.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("SMS_BAR"));
        assertFalse(filter2.eval(interpreter).isTruthy());

        final CockpitScript filter3 = interpreter.compile("shortCode !~ 'MAIL_*' and shortCode !~ 'SMS_*'");
        interpreter.setProperty("shortCode", CSType.fromString("BAR"));
        assertTrue(filter3.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAIL_FOO"));
        assertFalse(filter3.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("MAILXFHFU"));
        assertTrue(filter3.eval(interpreter).isTruthy());
        interpreter.setProperty("shortCode", CSType.fromString("SMS_BAR"));
        assertFalse(filter3.eval(interpreter).isTruthy());
        */
    }


}
