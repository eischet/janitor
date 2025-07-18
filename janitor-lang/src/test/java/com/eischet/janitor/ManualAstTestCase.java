package com.eischet.janitor;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.ExpressionList;
import com.eischet.janitor.compiler.ast.expression.VariableLookupExpression;
import com.eischet.janitor.compiler.ast.expression.binary.Addition;
import com.eischet.janitor.compiler.ast.expression.literal.IntegerLiteral;
import com.eischet.janitor.compiler.ast.statement.FunctionCallStatement;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManualAstTestCase extends JanitorTest  {

    @Test
    public void stackingScopes() {

        final JanitorDefaultEnvironment environment = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
            }
        };

        final ScriptModule dummy = new ScriptModule("dummy", "// no source");

        final Scope processScope = Scope.createBuiltinScope(environment, Location.at(dummy, 1, 1, 1, 1)); // new Scope(Location.at(dummy, 1, 1), null, null);

        final JanitorScriptProcess proc = new AbstractScriptProcess(new TestingRuntime(), processScope) {
            @Override
            public String getSource() {
                return null;
            }

            @Override
            public @NotNull JanitorObject run() throws JanitorRuntimeException {
                return null; // why is this even here?
            }


            @Override
            public void warn(String warning) {
                getRuntime().warn(warning);
            }
        };

        final Scope global = Scope.createGlobalScope(environment, dummy); // new Scope(null, null, null);
        final Scope local1 = Scope.createFreshBlockScope(null, global); // new Scope(null, global, null);

        global.bind("foo", environment.getBuiltinTypes().integer(100));
        local1.bind("bar", environment.getBuiltinTypes().integer(200));
        local1.bind("foo", environment.getBuiltinTypes().integer(300));

        assertEquals(300L, local1.lookup(proc, "foo", null).janitorGetHostValue());
        assertEquals(200L, local1.lookup(proc, "bar", null).janitorGetHostValue());
        assertEquals(100L, global.lookup(proc, "foo", null).janitorGetHostValue());

    }


    @Test
    public void manualAstWithPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(new VariableLookupExpression(null, "x"));
        //final IR.Statement.PrintStatement printStatement = new IR.Statement.PrintStatement(null, expressionList);
        //final IR.Script script = new IR.Script(null, Lists.immutable.of(printStatement), null);
        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, List.of(printCall), null);


        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("x", runtime.getEnvironment().getBuiltinTypes().integer(17));

        final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, script);
        process.run();
        assertEquals("17\n", runtime.getAllOutput());

        runtime.resetOutput();
        globalScope.bind("x", runtime.getEnvironment().getBuiltinTypes().integer(29));
        final RunningScriptProcess runningScript2 = new RunningScriptProcess(runtime, globalScope, script);
        runningScript2.run();
        assertEquals("29\n", runtime.getAllOutput());
    }

    @Test
    public void manualAstAddLiteralsAndPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final Addition addition = new Addition(null,
            new VariableLookupExpression(null, "a"),
            new IntegerLiteral(null,  runtime.getEnvironment().getBuiltinTypes().integer(4)));
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(addition);
        //final IR.Statement.PrintStatement printStatement = new IR.Statement.PrintStatement(null, expressionList);
        //final IR.Script script = new IR.Script(null, Lists.immutable.of(printStatement), null);

        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, List.of(printCall), null);


        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("a", runtime.getEnvironment().getBuiltinTypes().integer(17));
        final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, script);
        process.run();
        assertEquals("21\n", runtime.getAllOutput());
    }


    @Test
    public void manualAstAddVarsAndPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final Addition addition = new Addition(null,
            new VariableLookupExpression(null, "a"),
            new VariableLookupExpression(null, "b"));
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(addition);
        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, List.of(printCall), null);

        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("a", runtime.getEnvironment().getBuiltinTypes().integer(17));
        globalScope.bind("b", runtime.getEnvironment().getBuiltinTypes().integer(4));
        final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, script);
        process.run();
        assertEquals("21\n", runtime.getAllOutput());
    }



}
