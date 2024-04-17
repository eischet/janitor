package com.eischet.janitor;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.calls.JNativeMethod;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.JanitorNativeModule;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.repl.JanitorRepl;
import com.eischet.janitor.runtime.*;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FirstParserTestCase {

    private static final Logger log = LoggerFactory.getLogger(FirstParserTestCase.class);

    private static final Consumer<Scope> NO_GLOBALS = globals -> {
    };

    static {
        // LATER: move these to a saner place!
        BaseRuntime.addDefaultModule(CollectionsModule.REGISTRATION);
    }

    @Test
    public void testLogging() {
        log.info("this should show up as a logging message");
    }

    @Test
    public void justParse() throws JanitorCompilerException, JanitorRuntimeException {
        final String source = "print('hello, world');";
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(source);
        final ScriptModule module = ScriptModule.unnamed(source);
        final Script scriptObject = JanitorCompiler.build(module, script, source);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("x", 17);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript.run();
        assertEquals("hello, world\n", runtime.getAllOutput());
    }

    @Test
    public void printIfElse() throws JanitorCompilerException, JanitorRuntimeException {
        final String source = "if (x > 10) { print('high'); } else { print('low'); }";
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(source);
        final ScriptModule module = ScriptModule.unnamed(source);
        final Script scriptObject = JanitorCompiler.build(module, script, source);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("x", 17);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript.run();
        assertEquals("high\n", runtime.getAllOutput());

        runtime.resetOutput();

        globalScope.bind("x", 5);
        final RunningScriptProcess runningScript2 = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript2.run();
        assertEquals("low\n", runtime.getAllOutput());
    }

    private String getOutput(final String scriptSource) throws JanitorCompilerException, JanitorRuntimeException {
        return getOutput(scriptSource, g -> {
        });
    }

    private String getOutput(final String scriptSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("parsing: " + scriptSource + "\n");
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(scriptSource);
        final ScriptModule module = ScriptModule.unnamed(scriptSource);
        final Script scriptObject = JanitorCompiler.build(module, script, scriptSource);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript.run();
        return runtime.getAllOutput();
    }

    private JanitorObject evaluate(final String expressionSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("parsing: " + expressionSource + "\n");
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(expressionSource);
        final ScriptModule module = ScriptModule.unnamed(expressionSource);
        final Script scriptObject = JanitorCompiler.build(module, script, expressionSource);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        return runningScript.run();
    }

    @Test
    public void compactIfElse() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("less\n", getOutput("if (1<4) { print('less'); } else { print('more'); }", NO_GLOBALS));
        assertEquals("less\n", getOutput("if (1<4) {\n print('less'); \n} else {\n print('more'); \n}", NO_GLOBALS)); // same, but with line breaks
        assertEquals("less\n", getOutput("if (a<b) { print('less'); } else { print('more'); }", globals -> {
            globals.bind("a", 1);
            globals.bind("b", 100);
        }));
        assertEquals("more\n", getOutput("if (a<b) { print('less'); } else { print('more'); }", globals -> {
            globals.bind("a", 100);
            globals.bind("b", 99);
        }));
    }

    @Test
    public void ifElseIfElse() throws JanitorCompilerException, JanitorRuntimeException {
        final String script = """
            if (x>10) {
                print("high");
            } else if (x>5) {
                print("middle");
            } else if (x > 0) {
                print("low");
            } else {
                print("very low");
            }
            """;
        assertEquals("high\n", getOutput(script, g -> g.bind("x", JInt.of(11))));
        assertEquals("high\n", getOutput(script, g -> g.bind("x", JInt.of(12))));
        assertEquals("middle\n", getOutput(script, g -> g.bind("x", JInt.of(6))));
        assertEquals("low\n", getOutput(script, g -> g.bind("x", JInt.of(5))));
        assertEquals("low\n", getOutput(script, g -> g.bind("x", JInt.of(1))));
        assertEquals("very low\n", getOutput(script, g -> g.bind("x", JInt.of(0))));
        assertEquals("very low\n", getOutput(script, g -> g.bind("x", JInt.of(-100))));
    }


    @Test
    public void scriptBindsVar() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("100\n", getOutput("x = 50; print(2*x);", NO_GLOBALS));
        assertEquals("100\n", getOutput("x = 50; print(x+x);", NO_GLOBALS));
        assertEquals("100\n", getOutput("x = 50; if (3>1) { x = 100; } print(x);", NO_GLOBALS));
        assertThrows(JanitorNameException.class, () -> getOutput("print(yodel);", NO_GLOBALS), "name 'yodel' is not defined");
        assertThrows(JanitorNameException.class, () -> getOutput("if (1<2) { x = 100; } print(x);", NO_GLOBALS), "name 'x' is not defined");
    }

    /* TODO: reenable
    @Test
    public void loopingWhile() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("1\n2\n3\n", getOutput("i = 1; do { print(i); i = i + 1; } while (i < 4);", NO_GLOBALS));
        assertEquals("1\n2\n3\n", getOutput("i = 1; do { print(i); i += 1; } while (i < 4);", NO_GLOBALS));
        assertEquals("1\n2\n3\n", getOutput("i = 1; do { print(i); i++; } while (i < 4);", NO_GLOBALS));
        assertEquals("3\n2\n1\n", getOutput("i = 3; while (i>0) { print(i); i = i - 1;  }", NO_GLOBALS));
        assertEquals("3\n2\n1\n", getOutput("i = 3; while (i>0) { print(i); i -= 1;  }", NO_GLOBALS));
        // test the break statement
        assertEquals("1\n2\n3\n", getOutput("i = 1; do { print(i); i = i + 1; if(i>3) { break; } } while (i < 8);", NO_GLOBALS));
        // continue
        assertEquals("2\n4\n6\n8\n10\n", getOutput("i = 0; do { i = i + 1; if (i % 2==1) { continue; } print(i);  } while (i < 10);", NO_GLOBALS));


    }

     */

    @Test
    public void booleans() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("true\n", getOutput("print(true);", NO_GLOBALS));
        assertEquals("false\n", getOutput("print(false);", NO_GLOBALS));
        assertEquals("true\n", getOutput("print(true and true);", NO_GLOBALS));
        assertEquals("true\n", getOutput("print(not false);", NO_GLOBALS));
        assertEquals("false\n", getOutput("print(not true);", NO_GLOBALS));
        assertEquals("false\n", getOutput("print(not 17);", NO_GLOBALS));
        assertEquals("false\n", getOutput("print(not 'much');", NO_GLOBALS));
    }

    @Test
    public void blocks() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("17\n18\n", getOutput("{ print(17); } { print(18); }", NO_GLOBALS));
        assertThrows(JanitorNameException.class, () -> getOutput("{ yodel=8; } { print(yodel); }", NO_GLOBALS), "name 'yodel' is not defined");
        assertThrows(JanitorNameException.class, () -> getOutput("{ yodel=8; } print(yodel);", NO_GLOBALS), "name 'yodel' is not defined");
    }

    @Test
    public void parensExpression() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("21\n", getOutput("x = (17 + (4)); print(x);", NO_GLOBALS));
    }

    @Test
    public void ternaryExpression() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("21\n", getOutput("x = 17 + ( a ? 4 : 0 ); print(x);", globals -> globals.bind("a", 1)));
        assertEquals("17\n", getOutput("x = 17 + ( a ? 4 : 0 ); print(x);", globals -> globals.bind("a", 0)));
        // + binds more tightly than ?: !!!
        assertEquals("4\n", getOutput("x = 17 + a ? 4 : 0 ; print(x);", globals -> globals.bind("a", 1)));
    }

    @Test
    public void nullingAndEquality() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("ok\n", getOutput("if (a == null) { print('ok'); }", globals -> globals.bind("a", JNull.NULL)));
        assertEquals("ok\n", getOutput("if (null == null) { print('ok'); }", NO_GLOBALS));
        assertEquals("ok\n", getOutput("if (null != true) { print('ok'); }", NO_GLOBALS));
        assertEquals("ok\n", getOutput("if (false != true) { print('ok'); }", NO_GLOBALS));
    }

    @Test
    public void assertStatement() throws JanitorCompilerException, JanitorRuntimeException {
        getOutput("assert(1);", NO_GLOBALS);
        getOutput("assert(true);", NO_GLOBALS);
        assertThrows(JanitorAssertionException.class, () -> getOutput("assert(0);", NO_GLOBALS));
        assertThrows(JanitorAssertionException.class, () -> getOutput("assert(false);", NO_GLOBALS));
    }

    @Test
    public void definingFunctions() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("", getOutput("function foo(x) { print(x); }", NO_GLOBALS));
        assertEquals("4\n", getOutput("function foo(x) { print(4); } foo(4);", NO_GLOBALS)); // beachte: x wird ignoriert!
        assertEquals("17\n", getOutput("function foo() { print(17); } foo();", NO_GLOBALS));
        assertEquals("17\n", getOutput("function foo() { return 17; } print(foo());", NO_GLOBALS));
        assertEquals("2\n3\n4\n", getOutput("x = 1; function pp() { x = x + 1; print(x); } pp(); pp(); pp();", NO_GLOBALS));

        assertEquals("99\n", getOutput("function foo() { return 99; } print(foo());", NO_GLOBALS));
        assertEquals("99\n", getOutput("x = 88 + 11; function foo() { return x; } print(foo());", NO_GLOBALS));

        assertEquals("4\n", getOutput("function foo(x) { print(x); } foo(4);", NO_GLOBALS));

        assertEquals("4\n", getOutput("function a(x) { return x+1; } print(a(a(a(1)))); ", NO_GLOBALS));

        assertEquals("4\n", getOutput("a = x -> x + 1; print(a(a(a(1)))); ", NO_GLOBALS));
        assertEquals("4\n", getOutput("a = x -> { return x + 1; }; print(a(a(a(1)))); ", NO_GLOBALS));
        assertEquals("55\n", getOutput("z = (x -> 5*x); print(z(11));", NO_GLOBALS));
        // das ist vielleicht auch etwas schräg: assertEquals("55\n", getOutput("z = a -> { return b -> a*b; }; print((z(11))(5));", NO_GLOBALS));


    }


    @Test
    public void postfixing() throws JanitorCompilerException, JanitorRuntimeException {
        // i++ is an expression, not a statement, and that should probably be unified away:
        assertEquals("4\n", getOutput("i = 1; i++; i++; i++; print(i);", NO_GLOBALS));
        assertEquals("3\n", getOutput("i = 1; assert(1==i++); assert(2==i++); print(i);", NO_GLOBALS));
        assertEquals("14\n", getOutput("function foo(x) { return 2*x; } bar = foo(7); assert(bar==14); print(bar);", NO_GLOBALS));
        assertEquals("", getOutput("i=10; while(i>0) { i--; }", NO_GLOBALS));
        assertEquals("100\n", getOutput("while (i<100) { i++; } print(i);", globals -> globals.bind("i", 0)));
    }

    @Test
    public void prefixing() throws JanitorCompilerException, JanitorRuntimeException {
        // assertEquals("", getOutput("i = 0; assert((--i)==-1);", NO_GLOBALS));
        assertEquals("", getOutput("i = 1; assert((--i)==0);", NO_GLOBALS));
    }

    @Test
    public void negation() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("-1\n", getOutput("i = 2 - 3; print(i);", NO_GLOBALS));
        assertEquals("-1\n", getOutput("i = -1; print(i);", NO_GLOBALS));
        assertEquals("", getOutput("assert(-10<-5 and 10>5);", NO_GLOBALS));
        assertThrows(JanitorAssertionException.class, () -> getOutput("assert(not (-10<-5));", NO_GLOBALS));
    }

    @Test
    public void stringMethods() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("hallo\n", getOutput("print('hallo');", NO_GLOBALS));
        assertEquals("5\n", getOutput("print('hallo'.length());", NO_GLOBALS));

        assertEquals("5\n", getOutput("lf = 'hallo'.length; print(lf());", NO_GLOBALS));

        assertEquals("5\n", getOutput("s = 'hallo'; lenFun = s.length; print(lenFun());", NO_GLOBALS));

        assertEquals("oo\n", getOutput("print('foo'.substring(1));", NO_GLOBALS));

        assertEquals("123\n", getOutput("print('0000123'.removeLeadingZeros());", NO_GLOBALS));
    }

    @Test
    public void justEvaluateExpressions() throws JanitorCompilerException, JanitorRuntimeException {
        // evaluate("1+1", NO_GLOBALS);
        assertEquals(2L, evaluate("1+1;", NO_GLOBALS).janitorGetHostValue());
        assertEquals(2L, evaluate("1+1", NO_GLOBALS).janitorGetHostValue());
        assertEquals(2L, evaluate("function foo() { return 1+1; } foo();", NO_GLOBALS).janitorGetHostValue());
        assertEquals(2L, evaluate("function foo() { return 1+1; } foo()", NO_GLOBALS).janitorGetHostValue());
        assertEquals(17L, evaluate("17", NO_GLOBALS).janitorGetHostValue());


    }

    @Test
    public void playWithWhitespace() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(3L, evaluate("1+1+1", NO_GLOBALS).janitorGetHostValue());
        assertEquals(3L, evaluate("1+(\n1+1\n)", NO_GLOBALS).janitorGetHostValue());
        assertEquals(4L, evaluate("'foo'\n.length()+1", NO_GLOBALS).janitorGetHostValue());
    }

    @Test
    public void forLoops() throws JanitorCompilerException, JanitorRuntimeException {
        // einfache Schleife a la Python:
        assertEquals("1\n2\n3\n", getOutput("for (i in list) { print(i); }", globals -> globals.bind("list", new JList(List.of(
            JInt.of(1),
            JInt.of(2),
            JInt.of(3)
        )))));
        // i im inneren Block ist separat vom i im äußeren Block:
        assertEquals("1\n2\n3\n4\n", getOutput("i=4; for (i in list) { print(i); } print(i);", globals -> globals.bind("list", new JList(List.of(
            JInt.of(1),
            JInt.of(2),
            JInt.of(3)
        )))));

        assertEquals("1\n2\n3\n4\n", getOutput("i=4; for (i in [1,2,3]) { print(i); } print(i);", NO_GLOBALS));


    }

    @Test
    public void fib() throws JanitorCompilerException, JanitorRuntimeException {
        final String FIB = """
            function fib(n) {
                if (n <= 1) {
                    return n;
                } else {
                    return fib(n-1) + fib(n-2);
                }
            }
            fib(x)
            """;

        assertEquals(0L, evaluate(FIB, g -> g.bind("x", 0)).janitorGetHostValue());
        assertEquals(1L, evaluate(FIB, g -> g.bind("x", 1)).janitorGetHostValue());
        assertEquals(1L, evaluate(FIB, g -> g.bind("x", 2)).janitorGetHostValue());
        assertEquals(2L, evaluate(FIB, g -> g.bind("x", 3)).janitorGetHostValue());
        assertEquals(3L, evaluate(FIB, g -> g.bind("x", 4)).janitorGetHostValue());
        assertEquals(5L, evaluate(FIB, g -> g.bind("x", 5)).janitorGetHostValue());
        assertEquals(8L, evaluate(FIB, g -> g.bind("x", 6)).janitorGetHostValue());
        assertEquals(13L, evaluate(FIB, g -> g.bind("x", 7)).janitorGetHostValue());
        assertEquals(21L, evaluate(FIB, g -> g.bind("x", 8)).janitorGetHostValue());
        assertEquals(34L, evaluate(FIB, g -> g.bind("x", 9)).janitorGetHostValue());
        assertEquals(55L, evaluate(FIB, g -> g.bind("x", 10)).janitorGetHostValue());
    }

    @Test
    public void someFloatingPoints() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("0.0\n", getOutput("print(0.0);", NO_GLOBALS));
        assertEquals("1.5\n", getOutput("print(3.0/2.0);", NO_GLOBALS));
        assertEquals("3.0\n", getOutput("print(2*1.5);", NO_GLOBALS));
    }

    @Test
    public void weCanMultiplyStrings() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("*****", evaluate("'*'*5", NO_GLOBALS).janitorGetHostValue());
        assertEquals("x***x", evaluate("'x'+3*'*'+'x'", NO_GLOBALS).janitorGetHostValue());
    }


    @Test
    public void javaFunctionCalls() throws JanitorCompilerException, JanitorRuntimeException {

        final JNativeMethod foo = JNativeMethod.of(args -> JString.of("hallo"));

        assertEquals("hallo", evaluate("""
            foo();
            """, g -> g.bind("foo", foo)).janitorGetHostValue());
    }

    @Test
    public void stackTracing() throws JanitorCompilerException {
        try {
            evaluate("""
                // some comment
                function fail() {
                    return 1/0;
                }
                fail();
                """, NO_GLOBALS);
        } catch (JanitorRuntimeException e) {
            log.info(e.getMessage());
        }
        /*
        Noch nicht perfekt, sieht aktuell so aus:
        Script Error (CockpitScriptRuntimeException): math failure
          at unnamed [line 3 col 11]: return 1/0;
          at unnamed [line 2 col 16]: function fail() {
          at unnamed [line 5 col 0]: fail();
          at unnamed [line 2 col 0]: function fail() {
        caused by Java Exception java.lang.ArithmeticException: / by zero

        Script Error (CockpitScriptRuntimeException): math failure
          at unnamed [line 3 col 11]: return 1/0;
          at unnamed [line 2 col 16]: function fail() {
          at unnamed [line 5 col 0]: fail();
        caused by Java Exception java.lang.ArithmeticException: / by zero

        aber sollte so aussehen, meine ich:


         */
    }


    @Test
    public void modulesTest() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.registerModule(new JanitorModuleRegistration("foo", FooModule::new));

        final RunnableScript s = rt.compile("test", """
            import foo;
            print(foo.x);
            """);
        s.run();
        assertEquals("17\n", rt.getAllOutput());

        assertThrows(JanitorRuntimeException.class, () -> rt.compile("test", "import bar;").run(), "Module not found: bar");
        // rt.compile("test", "import foo; y = foo.z;").run();
        // warum geht das nicht??? --> geht mittlerweile, puh :-)
        // TODO: why not? assertThrows(JanitorRuntimeException.class, () -> rt.compile("test", "import foo; y = foo.z;").run());


        assertEquals(17L, rt.compile("test", "import foo; y = foo.x; y;").run().janitorGetHostValue());

    }


    @Test
    public void multiImport() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        for (final String moduleName : List.of("foo", "bar", "baz")) {
            rt.registerModule(new JanitorModuleRegistration(moduleName, () -> new JanitorModule() {
                @Override
                public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
                    if ("name".equals(name)) {
                        return JString.of(moduleName);
                    }
                    return JanitorModule.super.janitorGetAttribute(runningScript, name, required);
                }
            }));
        }
        final RunnableScript s = rt.compile("test", """
            import foo;
            import bar;
            import baz;
            assert(foo.name == 'foo');
            assert(bar.name == 'bar');
            assert(baz.name == 'baz');
            """);
        s.run();

        final RunnableScript s2 = rt.compile("test", """
            import foo, bar, baz;
            assert(foo.name == 'foo');
            assert(bar.name == 'bar');
            assert(baz.name == 'baz');
            """);
        s2.run();

        final RunnableScript s3 = rt.compile("test", """
            import foo, bar, baz, baz as gumbo;
            assert(foo.name == 'foo');
            assert(bar.name == 'bar');
            assert(baz.name == 'baz');
            assert(gumbo.name == 'baz');
            """);
        s3.run();

        final RunnableScript s4 = rt.compile("test", """
            import foo as gnobb, bar, baz, baz as gumbo;
            // should fail: assert(foo.name == 'foo'); --> JanitorNameException: name 'foo' is not defined
            assert(bar.name == 'bar');
            assert(baz.name == 'baz');
            assert(gumbo.name == 'baz');
            assert(gnobb.name == 'foo');
            """);
        s4.run();

        final RunnableScript s5 = rt.compile("test", """
            import "dummy" as dummy;
            assert(dummy.name == 'dummy');
            """);
        assertThrows(JanitorRuntimeException.class, s5::run, "Module not found: dummy");

        rt.addModuleResolver(new BaseRuntime.ModuleResolver() {
            @Override
            public @Nullable JanitorModule resolveModuleByStringName(final JanitorScriptProcess process, final String name) {
                log.info("resolveModuleByStringName: " + name);
                if ("dummy".equals(name)) {
                    return new JanitorModule() {
                        @Override
                        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
                            if ("name".equals(name)) {
                                return JString.of("dummy");
                            }
                            return JanitorModule.super.janitorGetAttribute(runningScript, name, required);
                        }
                    };
                }
                return null;
            }
        });


        s5.run();

    }



    @Test
    public void iterateOverSomeLists() throws JanitorCompilerException, JanitorRuntimeException {
        final JList jList = new JList(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map(JInt::of));
        assertEquals("1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n", getOutput("for (id in records) { print(id); }", g -> g.bind("records", jList)));
    }

    @Test
    public void dating() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(LocalDate.of(1976, 1, 10), rt.compile("test", "@1976-01-10").run(NO_GLOBALS).janitorGetHostValue());
        assertEquals(LocalDateTime.of(1976, 1, 10, 11, 17, 30), rt.compile("test", "@1976-01-10-11:17:30").run(NO_GLOBALS).janitorGetHostValue());
        assertEquals(LocalDateTime.of(1976, 1, 10, 11, 17, 0), rt.compile("test", "@1976-01-10-11:17").run(NO_GLOBALS).janitorGetHostValue());


        final JanitorObject fiveDays = rt.compile("test", "@5d").run(NO_GLOBALS);
        assertInstanceOf(JDuration.class, fiveDays);
        assertEquals(5L, ((JDuration) fiveDays).getAmount());
        assertEquals(JDuration.JDurationKind.DAYS, ((JDuration) fiveDays).getUnit());

        assertEquals(LocalDate.of(1976, 1, 11), rt.compile("test", "@1976-01-10 + @1d").run(NO_GLOBALS).janitorGetHostValue());

        assertEquals(LocalDate.of(1976, 2, 10), rt.compile("test", "@1976-01-10 + @1mo").run(NO_GLOBALS).janitorGetHostValue());

        assertEquals(LocalDateTime.of(1976, 1, 10, 12, 30, 45), rt.compile("test", "@1976-01-10 + @12h + @30mi + @45s").run(NO_GLOBALS).janitorGetHostValue());

        // Dates müssen auch SUBTRAHIERBAR sein!
        assertEquals(JDuration.of("1d"), rt.compile("test", "@1976-01-11 - @1976-01-10").run(NO_GLOBALS));
        assertEquals(JDuration.of("2d"), rt.compile("test", "@1976-01-12 - @1976-01-10").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "( @1976-01-12 - @1976-01-10 ) > @1d").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "( @1976-01-12 - @1976-01-10 ) < @5d").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "@2d > @1h").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "@2d > @1d").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "@2d == @2d").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "@3d < @1w").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "@3d <= @1w").run(NO_GLOBALS));


    }

    @Test
    public void wildcards() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ 'f*'").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'bar' ~ 'f*'").run().janitorGetHostValue());
    }

    @Test
    public void regexes() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ re/foo/").run().janitorGetHostValue());
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ re/f../").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'foo' ~ re/\\d+/").run().janitorGetHostValue());
        assertEquals(Boolean.TRUE, rt.compile("test", "'12345' ~ re/\\d+/").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'12345' !~ re/\\d+/").run().janitorGetHostValue());

        assertEquals("245005", rt.compile("test", "re/[^1-9]+([0-9]+).*/.extract('SW245005-POS04')").run().janitorGetHostValue());




        assertEquals(JBool.TRUE, rt.compile("test", " 'foo/bar' ~ re/foo.bar/").run());
        assertEquals(JBool.FALSE, rt.compile("test", " 'foo/bar' !~ re/foo.bar/").run());

        assertEquals(JBool.TRUE, rt.compile("test", " 'foo/bar' ~ re/foo\\/bar/").run());
        assertEquals(JBool.FALSE, rt.compile("test", " 'foo/bar' !~ re/foo\\/bar/").run());


        final RunnableScript phoneMatcher = rt.compile("test", "re/\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|" +
                                                               "2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|" +
                                                               "4[987654310]|3[9643210]|2[70]|7|1)\\d{1,14}$/.extract(text)");


    }


    @Test
    public void lists() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.compile("foo", "for (i in [1,2,3]) { print(i); }").run(NO_GLOBALS);
        assertEquals("1\n2\n3\n", rt.getAllOutput());
    }

    @Test
    public void doMapsCollideWithBlocks() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.compile("foo", "if (true) { } else { }").run(NO_GLOBALS);
        final Object map = rt.compile("bar", "return {};").run(NO_GLOBALS);
        assertTrue(map instanceof JMap);
    }

    @Test
    public void maps() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final Object map = rt.compile("bar", "return {};").run(NO_GLOBALS);
        log.info("empty map: " + map);
        assertTrue(map instanceof JMap);

        final JanitorObject foo = rt.compile("foo", "return {'id': 17, 'sc': 'dumbo'};").run(NO_GLOBALS);
        log.info("foo map: " + foo);
        final JMap fooMap = (JMap) foo;
        assertEquals(17L, fooMap.get(JString.of("id")).janitorGetHostValue());
        assertEquals("dumbo", fooMap.get(JString.of("sc")).janitorGetHostValue());
    }

    @Test
    public void accidentalJsonParser() throws JanitorCompilerException, JanitorRuntimeException {
        final String JSON = """
            [
                {
                    "className": "CockpitTool",
                    "shortCode": "ACTPROC-BV",
                    "name": "Action Processor Bankverlag",
                    "remarks": "Unter Einstellungen bitte noch die Datenbank auswählen!\\nAchtung, aktuell gibt es noch ein konzeptionelles Problem mit der Maske: \\ndie \\"Zuletzt verarbeitete ACT_REG_ID\\" wird mit gespeichert! F5 drücken vor Skriptänderungen!\\n",
                    "toolType": "action-processor",
                    "keepRunLogs": 10,
                    "jsonConfig": "{\\n  \\"dataSourceGroup\\": \\"ASSYST1075\\"\\n}",
                    "jsCode": "if (action.actionType.id == 6) {\\n    svd = assyst.getAssignedServDeptBeforeReopen(action.eventId, \\"SERVICE-DESK\\");\\n    if (svd) {\\n        print(\\"das ticket wurde wurde wiedereröffnet und geht zurück an:\\", svd);\\n        a = assyst.newAction();\\n        a.eventId = action.eventId;\\n        a.actionTypeId = 1;\\n        a.assignedServiceDepartment = svd;\\n        a.remarks = \\"Wiedereröffnung: zurück zu \\" + svd;\\n        assyst.createAction(a);\\n    } else {\\n        print(\\"das ticket wurde wiedereröffnet, aber wir wissen nicht wohin es soll\\");\\n    }\\n} else {\\n    print(\\"irrelevanter Aktionstyp:\\", action.actionType.shortCode);\\n}\\n"
                }
            ]
            """;
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JList list = (JList) rt.compile("test", JSON).run(NO_GLOBALS);
        final JMap map = (JMap) list.get(JInt.of(0));
        assertEquals(JInt.of(10), map.get(JString.of("keepRunLogs")));
        assertEquals("ACTPROC-BV", map.get(JString.of("shortCode")).janitorGetHostValue());
    }




    @Test
    public void longStringsCollide() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("test", """
            a = '''hallo''';
            c = \"\"\"ignore\"\"\";
            b = '''welt''';
            d = \"\"\"ignore\"\"\";
                        
            return a + b;
            """
        );
        assertEquals("hallowelt", script.run(NO_GLOBALS).janitorGetHostValue());

    }

    @Test
    public void parseLiteral() throws Exception {
        assertEquals("foo", JanitorAntlrCompiler.parseLiteral("foo").janitorGetHostValue());
        assertEquals("foo\\bar", JanitorAntlrCompiler.parseLiteral("foo\\\\bar").janitorGetHostValue());
        assertEquals("\"", JanitorAntlrCompiler.parseLiteral("\\\"").janitorGetHostValue());

    }

    @Test
    public void escapism() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("test", "return 'foo\\\\bar';");
        assertEquals("foo\\bar", script.run(NO_GLOBALS).janitorGetHostValue());

        final RunnableScript script2 = rt.compile("test", "return \"'blah'\";");
        assertEquals("'blah'", script2.run(NO_GLOBALS).janitorGetHostValue());

        assertEquals("\n", rt.compile("test", "return '\\n';").run(NO_GLOBALS).janitorGetHostValue());

        final RunnableScript script3 = rt.compile("test2", """
            return \"select usr_id from usr where regexp_like(usr_sc, '^\\\\d+$')";""");
        assertEquals("select usr_id from usr where regexp_like(usr_sc, '^\\d+$')", script3.run(NO_GLOBALS).janitorGetHostValue());

    }

    @Test
    public void collections() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("test", """
            import collections;
                        
            set = collections.set(1, 2, 3);
            set.add(4);
            set.add(5);
                        
            set.add(99);
            set.remove(99);
                        
            return set;
            """);
        final JanitorObject result = script.run(NO_GLOBALS);

        assertTrue(result instanceof JSet);

        JSet set = (JSet) result;

        assertTrue(set.janitorGetHostValue().contains(JInt.of(1)));
        assertTrue(set.janitorGetHostValue().contains(JInt.of(2)));
        assertTrue(set.janitorGetHostValue().contains(JInt.of(3)));
        assertTrue(set.janitorGetHostValue().contains(JInt.of(4)));
        assertTrue(set.janitorGetHostValue().contains(JInt.of(5)));

        assertFalse(set.janitorGetHostValue().contains(JInt.of(0)));
        assertFalse(set.janitorGetHostValue().contains(JInt.of(6)));
        assertFalse(set.janitorGetHostValue().contains(JInt.of(99)));

        assertEquals(5, set.janitorGetHostValue().size());


        assertEquals(JBool.TRUE, rt.compile("test", """
            import collections;
                        
            set = collections.set();
            assert(set.add(1));
            assert(set.contains(1));
            assert(set.remove(1));
            assert(not set.remove(1));
                        
            return set.isEmpty();
            """).run());

    }


    @Test
    public void symbolsScriptDoesNotCompile() throws Exception {
        final String source = """
            if (display.shortCode == 'KUDI INFO') {
                        
                branch = contact?.branch?.shortCode;
                section = contact?.section?.shortCode;
                department = contact?.department?.shortCode;
                        
                symbols.addSkill(findSkillWithShortCode('KUDI.KVP_RET_OG'), 'skills/KUDI.KVP_RETOURE_OG.png');
                symbols.addSkill(findSkillWithShortCode('KUDI.BACKSHOP'), 'skills/KUDI.BACKSHOP.png');
                symbols.addSkill(findSkillWithShortCode('ALLE.EH_KUU_INT'), 'skills/ALLE.EH_KUU_INT.png');
                        
                if (section and sction.startsWith("EMMA")) {
                    symbols.add("skills/EMMA.png", section);
                }
                        
                if (department) {
                    if ( department == 'WULFF' ) { symbols.add('skills/WULFF.png', department); }
                    if ( department == 'SCHAPFL_MARKT' ) { symbols.add('skills/SCHAPFL_MARKT.png', department); }
                    if ( department == 'EBUS-ZENTRALE' ) { symbols.add('skills/EBUS-ZENTRALE.png', department); }
                    if ( department == 'RWWS_MARKT' ) { symbols.add('skills/RWWS_MARKT.png', department); }
                    if ( department.startsWith('RWWS_LIGHT') ) { symbols.add('skills/RWWS_LIGHT.png', department); }
                    if ( department.startsWith('EBUS-MARKT') ) { symbols.add('skills/EBUS-MARKT.png', department); }
                    if ( department.startsWith('EBUS_GK-MARKT') ) { symbols.add('skills/EBUS_GK-MARKT.png', department); }
                }
            } else if (display.shortCode == 'TEST' || display.shortCode == 'EH INFO') {
                        
                if (contact?.contact?.shortCode == 'TEST') { symbols.add('skills/test-skill-1.png'); }
                        
                symbols.addSkill(findSkillWithShortCode('IT.EH_MAB40'), 'skills/IT.EH_MAB40.png');
                symbols.addSkill(findSkillWithShortCode('ALLE.EH_KUU_INT'), 'skills/ALLE.EH_KUU_INT.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_ECT_EXTE'), 'skills/IT.EH_ECT_EXTE.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_NOWWS_MDE'), 'skills/IT.EH_NOWWS_MDE.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_PILOTMKT'), 'skills/IT.EH_PILOTMKT.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_MKTBES'), 'skills/IT.EH_MKTBES.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_VIP'), 'skills/IT.EH_VIP.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_IPSEC'), 'skills/IT.EH_IPSEC.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_CITRIX'), 'skills/IT.EH_CITRIX.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_THINCLIEN'), 'skills/IT.EH_THINCLIEN.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_SMARK_E24'), 'skills/IT.EH_SMARK_E24.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_KAL_MSW'), 'skills/IT.EH_KAL_MSW.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_KALW_MSW'), 'skills/IT.EH_KALW_MSW.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_KAL_MSL'), 'skills/IT.EH_KAL_MSL.png');
                symbols.addSkill(findSkillWithShortCode('IT.EH_KAW_MSW'), 'skills/IT.EH_KAW_MSW.png');
                        
                        
                branch = contact?.branch?.shortCode;
                section = contact?.section?.shortCode;
                department = contact?.department?.shortCode;
                        
                if (section) {
                    if ( section.startsWith("EMMA") ) { symbols.add("skills/EMMA.png", section); }
                }
                        
                if (department) {
                    if ( department == 'WULFF' ) { symbols.add('skills/WULFF.png', department); }
                    if ( department == 'SCHAPFL_MARKT' ) { symbols.add('skills/SCHAPFL_MARKT.png', department); }
                    if ( department == 'EBUS-ZENTRALE' ) { symbols.add('skills/EBUS-ZENTRALE.png', department); }
                    if ( department == 'RWWS_MARKT' ) { symbols.add('skills/RWWS_MARKT.png', department); }
                    if ( department == 'RWWS_LIGHT' ) { symbols.add('skills/RWWS_LIGHT.png', department); }
                    if ( department.startsWith('EBUS-MARKT') ) { symbols.add('skills/EBUS-MARKT.png', department); }
                    if ( department.startsWith('EBUS_GK-MARKT') ) { symbols.add('skills/EBUS_GK-MARKT.png', department); }
                }
                        
                if (branch) {
                    if ( branch == 'BAECKERBUB GMBH') { symbols.add('skills/BAECKERBUB GMBH.png', branch); }
                    if ( branch == 'BAECKERHAUS ECKER GMBH') { symbols.add('skills/BAECKERHAUS ECKER GMBH.png', branch); }
                    if ( branch == 'EDEKA FOODS. GMBH & CO KG') { symbols.add('skills/EDEKA FOODS. GMBH & CO KG.png', branch); }
                    if ( branch == 'EDEKA FOODS. STIFT & COKG') { symbols.add('skills/EDEKA FOODS. STIFT & COKG.png', branch); }
                    if ( branch == 'EDEKA HG SUEDWEST MBH') { symbols.add('skills/EDEKA HG SUEDWEST MBH.png', branch); }
                    if ( branch == 'EDEKA SW FLEISCH GMBH') { symbols.add('skills/EDEKA SW FLEISCH GMBH.png', branch); }
                    if ( branch == 'EDEKA SW GETR.GMBH&CO.OHG') { symbols.add('skills/EDEKA SW GETR.GMBH&CO.OHG.png', branch); }
                    if ( branch == 'FRISCHK. & DELIKAT.S.GMBH') { symbols.add('skills/FRISCHK. & DELIKAT.S.GMBH', branch); }
                    if ( branch == 'GETRAENKEHANDEL KEMPF') { symbols.add('skills/GETRAENKEHANDEL KEMPF.png', branch); }
                    if ( branch == 'K & U BAECKEREI GMBH') { symbols.add('skills/K & U BAECKEREI GMBH.png', branch); }
                    if ( branch == 'NAME DER FIRMA') { symbols.add('skills/NAME DER FIRMA.png', branch); }
                    if ( branch == 'NEUKAUF MARKT GMBH') { symbols.add('skills/NEUKAUF MARKT GMBH.png', branch); }
                    if ( branch == 'ORTENAUER WEINKELLEREI') { symbols.add('skills/ORTENAUER WEINKELLEREI.png', branch); }
                    if ( branch == 'SCHWARZWALDSPRUDEL GMBH') { symbols.add('skills/SCHWARZWALDSPRUDEL GMBH.png', branch); }
                    if ( branch == 'EDEKA RECHENZ. SUED GMBH') { symbols.add('skills/EDEKA RECHENZ. SUED GMBH.png', branch); }
                    if ( branch == 'SCHWWHOF FL.U.WURSTW.GMBH') { symbols.add('skills/SCHWWHOF FL.U.WURSTW.GMBH.png', branch); }
                    if ( branch == 'TREFF DISCOUNT GMBH') { symbols.add('skills/TREFF DISCOUNT GMBH.png', branch); }
                    if ( branch == 'UNION SB GROSSMARKT GMBH') { symbols.add('skills/UNION SB GROSSMARKT GMBH.png', branch); }
                    if ( branch == 'STEUERBERATER') { symbols.add('skills/STEUERBERATER.png', branch); }
                    if ( branch == 'LIEFERANTEN') { symbols.add('skills/LIEFERANTEN.png', branch); }
                    if ( branch == 'NOSCAN') { symbols.add('skills/NOSCAN.png', branch); }
                }
                        
            }
            """;
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("foo", source);
    }

    @Test
    public void datingFormats() throws JanitorCompilerException, JanitorRuntimeException {
        final LocalDateTime testDate = LocalDateTime.of(2021, 11, 15, 12, 4);
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(JDate.of(2021, 11, 15), rt.compile("test", "d.date()").run(g -> g.bind("d", JDateTime.ofNullable(testDate))));
        assertEquals(JString.of("12:04"), rt.compile("test", "d.time()").run(g -> g.bind("d", JDateTime.ofNullable(testDate))));
        assertEquals(JString.of("15.11.2021, 12:04:00"), rt.compile("test", "d.string()").run(g -> g.bind("d", JDateTime.ofNullable(testDate))));
        assertEquals(JBool.TRUE, rt.compile("test", "@now > @now.date()").run(NO_GLOBALS));
        assertEquals(JBool.FALSE, rt.compile("test", "@now < @now.date()").run(NO_GLOBALS));
        Long.compare(1, 2);
    }

    @Test
    public void communicatingScopes() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript firstScript = rt.compile("first", "i = 17;");
        final ResultAndScope ras = firstScript.runAndKeepGlobals(NO_GLOBALS);
        final RunnableScript secondScript = rt.compile("second", "print(i);");

        final JanitorObject localI = ras.getScope().retrieveLocal("i");
        assertEquals(JInt.of(17), localI);

        secondScript.runInScope(NO_GLOBALS, ras.getScope());
        assertEquals("17\n", rt.getAllOutput());
    }

    @Test
    public void emptyScriptsDoNothingAtAll() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript firstScript = rt.compile("first", "");
        assertEquals(JNull.NULL, firstScript.run(NO_GLOBALS));
        final RunnableScript secondScript = rt.compile("second", null);
        assertEquals(JNull.NULL, secondScript.run(NO_GLOBALS));
    }

    @Test
    public void comparingStuff() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(JBool.TRUE, rt.compile("test", "1<=2").run(NO_GLOBALS));
        assertEquals(JBool.TRUE, rt.compile("test", "2>=0").run(NO_GLOBALS));
        assertEquals(JBool.FALSE, rt.compile("test", "2<0").run(NO_GLOBALS));

    }

    @Test
    public void stringFormat() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(JString.of("foo=bar"), rt.compile("test", "'%s=%s'.format('foo', 'bar')").run(NO_GLOBALS));
    }

    @Test
    public void stringTemplating() throws Exception {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("test", """
            a = 1;
            b = 2;
            c = 3;
            template = 'a=${a}, b=${b}, c=${c}!';
            return template.expand();
            """);
        assertEquals("a=1, b=2, c=3!", script.run(NO_GLOBALS).janitorGetHostValue());


        final RunnableScript script2 = rt.compile("test", """
            a = 1;
            print('start');
            for (i in [1, 2, 3]) {
                print('a=${a} i=${i}'.expand());
            }
            print('end');
            """);
        script2.run();
        assertEquals("""
            start
            a=1 i=1
            a=1 i=2
            a=1 i=3
            end
            """, rt.getAllOutput());

    }


    @Test
    public void tryCatching() throws JanitorCompilerException, JanitorRuntimeException {

        final OutputCatchingTestRuntime rt3 = new OutputCatchingTestRuntime();
        final RunnableScript s3 = rt3.compile("tryCatching3", """
            try {
                print("tried");
            } catch (e) {
                print("failed");
            } finally {
                print("always");
            }
            """);
        s3.run();
        assertEquals("tried\nalways\n", rt3.getAllOutput());

        final OutputCatchingTestRuntime rt4 = new OutputCatchingTestRuntime();
        final RunnableScript s4 = rt4.compile("tryCatching4", """
            try {
                print("tried");
                i = 1/0;
            } catch (e) {
                print("failed", e);
            } finally {
                print("always");
            }
            """);
        s4.run();
        assertEquals("""
            tried
            failed JanitorArithmeticException: Traceback (most recent call last):
              Module 'tryCatching4'
              Module 'tryCatching4', line 1, column 4
                try {
              Module 'tryCatching4', line 3, column 8
                i = 1/0;
            JanitorArithmeticException: / by zero
             caused by ArithmeticException: / by zero
            always
            """, rt4.getAllOutput());


        final OutputCatchingTestRuntime rt2 = new OutputCatchingTestRuntime();
        final RunnableScript s2 = rt2.compile("tryCatching2", """
            try { return 4/2; print("ok"); } catch (e) { print("div by zero"); }
            """);
        final JanitorObject result2 = s2.run();
        assertEquals("", rt2.getAllOutput());
        assertEquals(JInt.of(2), result2);

        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("tryCatching", """
            try { return 1/0; print("ok"); } catch (e) { print("div by zero"); }
            """);
        final JanitorObject result = script.run();
        assertEquals("div by zero\n", rt.getAllOutput());

    }

    @Test

    public void detectIllegalAssignment() throws Exception {
        // LATER: mir wäre eigentlich lieber, wenn hier die neue AssignmentException geworfen würde
        assertThrows(JanitorNameException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
                rt.getCompilerSettings().setVerbose(true);
                final RunnableScript script = rt.compile("illegalAssignment", "1=1");
                script.run();
            }
        });
    }


    @Test
    public void epochal() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("@1970-01-01-01:00:00\n", getOutput("print((0).epoch);", NO_GLOBALS));
        assertEquals("@2022-03-30-11:48:46\n", getOutput("print((1648633726).epoch);", NO_GLOBALS));
        assertEquals("1648633726\n", getOutput("print((@2022-03-30-11:48:46).epoch);", NO_GLOBALS));



    }


    @Test
    public void typeTags() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("bool\n", getOutput("print(false._type);", g -> {
        }));
        assertEquals("string\n", getOutput("print('foo'._type);", g -> {
        }));
        assertEquals("list\n", getOutput("print(['foo', 'bar', 'baz']._type);", g -> {
        }));

    }

    @Test
    public void parseList() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("[a, b, c]\n", getOutput("""
            print([].parseJson('''
            ["a", "b", "c"]
            '''));
            """, g -> {
        }));
        assertEquals("[a, b, 1.0]\n", getOutput("""
            print([].parseJson('''
            ["a", "b", 1]
            '''));
            """, g -> {
        }));
        assertEquals("[a, b, 1.0, {}]\n", getOutput("""
            print([].parseJson('''
            ["a", "b", 1, {}]
            '''));
            """, g -> {
        }));

        final JanitorObject emptyMap = evaluate("""
            {}.parseJson('''{}''');
            """, g -> {
        });
        assertInstanceOf(JMap.class, emptyMap);
        assertTrue(((JMap) emptyMap).isEmpty());

        final JanitorObject map1 = evaluate("""
            {}.parseJson('''{"a":"a", "b":"b", "c":"z"}''');
            """, g -> {
        });
        assertInstanceOf(JMap.class, map1);
        JMap jMap1 = (JMap) map1;
        assertEquals(JString.of("a"), jMap1.get(JString.of("a")));
        assertEquals(JString.of("b"), jMap1.get(JString.of("b")));
        assertEquals(JString.of("z"), jMap1.get(JString.of("c")));

        final JanitorObject map2 = evaluate("""
            {}.parseJson('''{"a":"a", "b":[], "c":["c1", "c2"]}''');
            """, g -> {
        });
        assertInstanceOf(JMap.class, map2);
        JMap jMap2 = (JMap) map2;
        assertEquals(JString.of("a"), jMap2.get(JString.of("a")));
        JList blist = (JList) jMap2.get(JString.of("b"));
        JList clist = (JList) jMap2.get(JString.of("c"));
        assertEquals(0, blist.size());
        assertEquals(2, clist.size());
        assertEquals(JString.of("c1"), clist.get(0));
        assertEquals(JString.of("c2"), clist.get(1));

    }

    @Test
    public void indexingLists() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        assertEquals(1L, rt.compile("test", "[1,2,3][0]").run().janitorGetHostValue());
        assertEquals(2L, rt.compile("test", "[1,2,3][1]").run().janitorGetHostValue());
        assertEquals(3L, rt.compile("test", "[1,2,3][2]").run().janitorGetHostValue());

        assertEquals(3L, rt.compile("test", "[1,2,3][-1]").run().janitorGetHostValue());
        assertEquals(2L, rt.compile("test", "[1,2,3][-2]").run().janitorGetHostValue());
        assertEquals(1L, rt.compile("test", "[1,2,3][-3]").run().janitorGetHostValue());

        assertEquals("[1, 2]\n", getOutput("print( ([1,2,3])[0:2] )", g -> {
        }));
        assertEquals("[1, 2]\n", getOutput("print( ([1,2,3])[0:-1] )", g -> {
        }));
        assertEquals("[2]\n", getOutput("print( ([1,2,3])[1:-1] )", g -> {
        }));
        assertEquals("[1, 2, 3]\n", getOutput("print( ([1,2,3])[0:3] )", g -> {
        }));
        assertEquals("[1, 2]\n", getOutput("print( ([1,2,3])[:-1] )", g -> {
        }));
        assertEquals("[3]\n", getOutput("print( ([1,2,3])[-1:] )", g -> {
        }));
        assertEquals("[2, 3]\n", getOutput("print( ([1,2,3])[1:] )", g -> {
        }));

        assertEquals("[3, 2, 1]\n", getOutput("print( ([1,2,3])[3:0] )", g -> {
        }));

        assertEquals("foo\n", getOutput("print('bazfoobar'[3:-3]);", g -> {
        }));
        assertEquals("foo\n", getOutput("print('bazfoobar'[3:6]);", g -> {
        }));
        assertEquals("z\n", getOutput("print('baz'[-1]);", g -> {
        }));
        assertEquals("o\n", getOutput("print('foo'[-1:]);", g -> {
        }));

        assertEquals("fo\n", getOutput("print('foo'[0:-1]);", g -> {
        }));
        assertEquals("fo\n", getOutput("print('foo'[:-1]);", g -> {
        }));
        assertEquals("oo\n", getOutput("print('foo'[1:]);", g -> {
        }));

        assertEquals("[1, 2, 3]\n", getOutput("liste = [0,1,2,3,4]; print(liste[1:-1]);", g -> {
        }));
        assertEquals("[3, 2, 1]\n", getOutput("liste = [0,1,2,3,4]; print(liste[-1:1]);", g -> {
        }));
    }

    @Test
    public void problemsWithColons() throws JanitorRuntimeException, JanitorCompilerException {
        // Denksportaufgabe. Als print() noch ein Statement war, da waren diese beiden Aufrufe gültig.
        // Jetzt, wo es eine Function ist, sind sie es nicht mehr. Das ist schon sonderbar.

        // Da das Konstrukt [:] ungebräuchlich ist, merkt das erstmal niemand, aber ich wüsste schon
        // gerne wo das herkommt.

        // Des Rätsels Lösung: [:] fehlte in der Grammatik. Da dort aber das Print-Statement drin war, wurde der Fehler offenbar irgendwie übertüncht..?
        log.info("---- problematic code follows ----");

        assertEquals("[1, 2, 3]\n", getOutput("print( ([1,2,3]) );", g -> {
        }));
        assertEquals("[1, 2, 3]\n", getOutput("l = ([1,2,3])[:]; print( l );", g -> {
        })); // das geht noch?!

        assertEquals("[1, 2, 3]\n", getOutput("print([1,2,3][0:] );", g -> {
        }));


        assertEquals("[1, 2, 3]\n", getOutput("print( ([1,2,3])[0:] );", g -> {
        }));


        assertEquals("[1, 2, 3]\n", getOutput("print( ([1,2,3])[:] );", g -> {
        }));
        assertEquals("foo\n", getOutput("print('foo'[:]);", g -> {
        }));

    }

    @Test
    public void numericStrings() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("true\n", getOutput("print('1234'.isNumeric());"));
        assertEquals("false\n", getOutput("print('1234x'.isNumeric());"));
        assertEquals("false\n", getOutput("print('x1234x'.isNumeric());"));
        assertEquals("false\n", getOutput("print('x1234'.isNumeric());"));
        assertEquals("true\n", getOutput("print('1234x'.startsWithNumbers());"));
        assertEquals("true\n", getOutput("print('0kaffee'.startsWithNumbers());"));
        assertEquals("false\n", getOutput("print('a1234x'.startsWithNumbers());"));
        assertEquals("false\n", getOutput("print('a1234'.startsWithNumbers());"));
    }

    @Test void dateDiff() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("@3600s\n", getOutput("print(@2022-10-01-12:00:00 - @2022-10-01-11:00:00)"));

        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("diff", "return @2022-10-01-12:00:00 - @2022-10-01-11:00:00");
        final JanitorObject result = script.run();

        assertInstanceOf(JDuration.class, result);

        final JDuration dur = (JDuration) result;
        assertEquals(3600, dur.toSeconds());


        final String by60 = getOutput("""
            diff = @2022-10-01-12:00:00 - @2022-10-01-11:00:00;
            assert(diff == @3600s);
            assert(diff == @60mi);
            assert(diff == @1h);
            assert(diff.minutes == 60);
            assert(diff.seconds == 3600);
            assert(diff.hours == 1);
            assert(diff.days == 0);
            by60 = @45mi.minutes / 60.0;
            assert(by60 == 0.75);
            assert(@45mi.hours == 0);
            """);
        System.out.println("by60:"+by60);


    }

    @Test
    public void dateParsing() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("@2022-10-12\n", getOutput("print('20221012'.parseDate('yyyyMMdd'));"));
        assertEquals("@2022-10-12\n", getOutput("print('2022-10-12'.parseDate('yyyy-MM-dd'));"));

        assertEquals("@2022-10-12-16:34:00\n", getOutput("print('2022-10-12 16:34:00'.parseDateTime('yyyy-MM-dd HH:mm:ss'));"));


    }

    @Test
    public void indexAssignmentOnMap() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.getCompilerSettings().setVerbose(true);
        final RunnableScript script = rt.compile("indexed_assignment", "a = {}; a['foo'] = 'bar'; print(a['foo']);");
        script.run();
        assertEquals("bar\n", rt.getAllOutput());
    }

    @Test
    public void indexAssignmentOnList() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.getCompilerSettings().setVerbose(true);
        final RunnableScript script = rt.compile("indexed_assignment", "a = ['baz']; a[0] = 'bar'; print(a[0]);");
        script.run();
        assertEquals("bar\n", rt.getAllOutput());
    }

    @Test
    public void localKeywordsFuckedUp() throws JanitorRuntimeException, JanitorCompilerException {
        // Problem: for (i from 1 to 10) { ... } reserviert sich das gesamte Keyword "from"
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();

        rt.compile("from_test_1", """
            print(from);
            """).run(g -> g.bind("from", 17));

        rt.compile("from_test_2", """
            for (i from 1 to 10) {
                print(i);
            }
            """);

        rt.compile("from_test_3", """
            x = thing.from;
            """).run(g -> g.bind("thing", new ObjectWithKeywordsAsProperties()));


        rt.compile("from_test_4", """
            print(thing.from);
            """).run(g -> g.bind("thing", new ObjectWithKeywordsAsProperties()));

        rt.compile("from_test_5", """
            x = thing.to;
            """).run(g -> g.bind("thing", new ObjectWithKeywordsAsProperties()));

        rt.compile("in_test", """
            in = true;
            assert(in == true);
            for (i in [1,2,3]) {
                print(i);
            }
            """).run();

        final JMap map = (JMap) rt.compile("mapkeys_test", """
            return {from: "here", to: "eternity"};
            """).run();
        System.out.println("got map: " + map);
        assertEquals("here", map.get(JString.of("from")).janitorToString());
        assertEquals("eternity", map.get(JString.of("to")).janitorToString());

        final RunnableScript script = rt.compile("from_test", """
            for (i from 1 to 10) {
                print(i);
            }
            print(from);
            from = 17;
            print(from);
            return from;
                        
            """);
        script.run(g -> g.bind("from", 18));
    }

    @Test
    public void partialParsing() throws JanitorCompilerException, JanitorControlFlowException, JanitorRuntimeException {
        /* so geht's natürlich nicht, weil der Parser hier ein ganzes File erwartet
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        rt.compile("partial_parsing", """
            x = 17;
            if (x < 20) {
            """);
         */

        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final JanitorRepl repl = new JanitorRepl(rt);
        assertEquals(JanitorRepl.PartialParseResult.OK, repl.parse("x = 17;"));


        assertEquals(JanitorRepl.PartialParseResult.INCOMPLETE, repl.parse("if (x < 20) {"));
        assertEquals(JanitorRepl.PartialParseResult.INCOMPLETE, repl.parse("if (x < 20) {\n  print(x);"));
        assertEquals(JanitorRepl.PartialParseResult.OK, repl.parse("if (x < 20) {\n  print(x);\n}"));


        repl.parse("if (x > 5) { return 'X is greater than five'; }");


        rt.resetOutput();
        repl.parse("print('x has value:', x);");
        System.out.println("output: " + rt.getAllOutput());

        assertEquals("x has value: 17\n", rt.getAllOutput());

        rt.resetOutput();
        repl.parse("function double(x) { return 2*x; }");
        repl.parse("print('double(17) =', double(17));");
        assertEquals("double(17) = 34\n", rt.getAllOutput());


    }

    @Test
    public void classProperty() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("class_property", """
            print((17).class);
            print(null.class);
            """);
        script.run();
        assertEquals("int\nnull\n", rt.getAllOutput());
    }


    @Test
    public void npeOnBoundNull() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript script = rt.compile("npe_on_bound_null", """
            print("hello, world");
            """);
        script.run(g -> g.bind("foo", (JanitorObject) null));
    }

    @Test
    public void invalidStringLiteral() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();

        final JanitorCompilerException thrown = assertThrows(JanitorCompilerException.class, () -> {
                rt.compile("invalid_string_literal", "failure = \"foo\\xbar\";");
            }
        );
        assertEquals(
            """
                found 3 issues compiling invalid_string_literal:\s
                  line 1:10 --> invalid string at: '"foo\\x'\s
                    failure = "foo\\xbar";
                  line 1:19 --> invalid string at: '";;\\n'\s
                    failure = "foo\\xbar";
                  line 2:0 --> missing ';' at end of file"""
            , thrown.getMessage());

        final JanitorCompilerException thrown2 = assertThrows(JanitorCompilerException.class, () -> {
            rt.compile("invalid_string_literal", "failure = 'foo\\xbar';");
        });
        assertEquals(
            """
                found 3 issues compiling invalid_string_literal:\s
                  line 1:10 --> invalid string at: ''foo\\x'\s
                    failure = 'foo\\xbar';
                  line 1:19 --> invalid string at: '';;\\n'\s
                    failure = 'foo\\xbar';
                  line 2:0 --> missing ';' at end of file"""
            , thrown2.getMessage());

    }

    @Test
    public void outputtingJson() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("[]\n", getOutput("print([].toJson());"));
        assertEquals("{}\n", getOutput("print({}.toJson());"));
        assertEquals("{\"a\":\"b\"}\n", getOutput("print({a:'b'}.toJson());"));
        assertEquals("[1,2,3]\n", getOutput("print([1,2,3].toJson());"));
        assertEquals("[\"a\",\"b\",\"c\"]\n", getOutput("print(['a','b','c'].toJson());"));
        assertEquals("[{\"a\":\"b\"}]\n", getOutput("print([{a:'b'}].toJson());"));

    }

    @Test
    public void base64decodeStrings() throws JanitorRuntimeException, JanitorCompilerException {
        final String foobarbazAsString = "foobarbaz";
        final String foobarbazAsBase64 = "Zm9vYmFyYmF6";
        final JBinary foobarbazAsBinary = new JBinary(foobarbazAsString.getBytes(StandardCharsets.UTF_8));

        assertEquals(foobarbazAsBase64 + "\n", getOutput("print(binary.encodeBase64());", g -> g.bind("binary", foobarbazAsBinary)));
        assertEquals(foobarbazAsString + "\n", getOutput("print(base.decodeBase64().string);", g -> g.bind("base", foobarbazAsBase64)));
        assertEquals(foobarbazAsString + "\n", getOutput("print(base.decodeBase64().toString());", g -> g.bind("base", foobarbazAsBase64)));

        //final String output = getOutput("print('Zm9vYmFyYmF6'.decodeBase64());");

        //System.err.println(output);
    }

    public static class FooModule extends JanitorNativeModule {

        @Override
        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
            if ("x".equals(name)) {
                return JInt.of(17);
            } else {
                return null;
            }
        }
    }


    private static class ObjectWithKeywordsAsProperties implements JanitorObject {


        @Override
        public Object janitorGetHostValue() {
            return this;
        }

        @Override
        public String janitorToString() {
            return toString();
        }


        @Override
        public @NotNull String janitorClassName() {
            return getClass().getSimpleName();
        }

        @Override
        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
            if (name.equals("from")) {
                System.out.println("someone asked for property 'from'!");
                return JString.of("foo");
            }
            if (name.equals("to")) {
                System.out.println("someone asked for property 'to'!");
                return JString.of("bar");
            }
            return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
        }
    }

    @Test
    public void someMoreFormatting() throws JanitorRuntimeException, JanitorCompilerException {
        assertEquals("2024-01-27T11:11\n", getOutput("""
            print(@2024-01-27-11:11:11.string("yyyy-MM-dd'T'HH:mm"));
            """));
        assertEquals("32\n", getOutput("""
            print(2*("16".int())));
            """));
        assertEquals("32\n", getOutput("""
            print(2*("16".toInt())));
            """));
        assertEquals("32.0\n", getOutput("""
            print(10*("3.2".toFloat())));
            """));
        assertEquals("2024-01-27T11:11 +0100\n", getOutput("""
            print(@2024-01-27-11:11:11.formatAtTimezone("Europe/Berlin", "yyyy-MM-dd'T'HH:mm Z"));
            """));

    }

    @Test public void deadlock() throws Exception {
        final String scriptSource = """
        print('hallo');
        x = 17;
        print(x);
        
        print("before while!!!");
        assert(x == 17);
        x = x + 1;
        assert(x == 18);
        if (x < 20) {
            print("less");
        } else {
            print("more");
        }
        
        do {
            //print("x before rebinding:", x);
            x = x + 1;
          
            //print("x after rebinding:", x);
            //print(x);
            assert(x == 19);
        } while (false);
        
        
        // i = 1; do { print(i); i = i + 1; } while (i < 4);
        """;
        // TODO: wenn man im do{} auf x Zugreift, dann hat das x=x+1 Erfolg, sonst nicht!?
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(scriptSource);
        final ScriptModule module = ScriptModule.unnamed(scriptSource);
        final Script scriptObject = JanitorCompiler.build(module, script, scriptSource);
        final JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                System.err.println("WARN: " + message);
            }
        };
        final SLFLoggingRuntime runtime = new SLFLoggingRuntime(env, LoggerFactory.getLogger(getClass()));

        runtime.setTraceListener(System.out::println);

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript.run();
    }


}
