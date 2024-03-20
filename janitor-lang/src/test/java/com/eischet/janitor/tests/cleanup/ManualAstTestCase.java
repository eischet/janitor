package com.eischet.janitor.tests.cleanup;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.scopes.ScriptModule;
import com.eischet.janitor.cleanup.api.api.types.JInt;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.expression.ExpressionList;
import com.eischet.janitor.cleanup.compiler.ast.expression.Identifier;
import com.eischet.janitor.cleanup.compiler.ast.expression.VariableLookupExpression;
import com.eischet.janitor.cleanup.compiler.ast.expression.binary.Addition;
import com.eischet.janitor.cleanup.compiler.ast.expression.literal.IntegerLiteral;
import com.eischet.janitor.cleanup.compiler.ast.statement.FunctionCallStatement;
import com.eischet.janitor.cleanup.compiler.ast.statement.Script;
import com.eischet.janitor.cleanup.runtime.AbstractScriptProcess;
import com.eischet.janitor.cleanup.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.cleanup.runtime.RunningScriptProcess;
import com.eischet.janitor.cleanup.runtime.TestingRuntime;
import com.eischet.janitor.cleanup.runtime.scope.Scope;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManualAstTestCase {

    @Test
    public void stackingScopes() {

        final ScriptModule dummy = new ScriptModule("dummy", "// no source");

        final Scope processScope = Scope.createBuiltinScope(Location.at(dummy, 1, 1, 1, 1)); // new Scope(Location.at(dummy, 1, 1), null, null);

        final JanitorScriptProcess proc = new AbstractScriptProcess(new TestingRuntime(), processScope) {
            @Override
            public String getSource() {
                return null;
            }

            @Override
            public void warn(String warning) {
                getRuntime().warn(warning);
            }
        };

        final Scope global = Scope.createGlobalScope(dummy); // new Scope(null, null, null);
        final Scope local1 = Scope.createFreshBlockScope(null, global); // new Scope(null, global, null);

        global.bind("foo", JInt.of(100));
        local1.bind("bar", JInt.of(200));
        local1.bind("foo", JInt.of(300));

        assertEquals(300L, local1.lookup(proc, "foo").janitorGetHostValue());
        assertEquals(200L, local1.lookup(proc, "bar").janitorGetHostValue());
        assertEquals(100L, global.lookup(proc, "foo").janitorGetHostValue());

    }


    @Test
    public void manualAstWithPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(new VariableLookupExpression(null, "x"));
        //final IR.Statement.PrintStatement printStatement = new IR.Statement.PrintStatement(null, expressionList);
        //final IR.Script script = new IR.Script(null, Lists.immutable.of(printStatement), null);
        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, Lists.immutable.of(printCall), null);

        final Scope globalScope = Scope.createGlobalScope(null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("x", JInt.of(17));

        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, script);
        runningScript.run();
        assertEquals("17\n", runtime.getAllOutput());

        runtime.resetOutput();
        globalScope.bind("x", JInt.of(29));
        final RunningScriptProcess runningScript2 = new RunningScriptProcess(runtime, globalScope, script);
        runningScript2.run();
        assertEquals("29\n", runtime.getAllOutput());
    }

    @Test
    public void manualAstAddLiteralsAndPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();
        final Addition addition = new Addition(null,
            new VariableLookupExpression(null, "a"),
            new IntegerLiteral(null, 4));
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(addition);
        //final IR.Statement.PrintStatement printStatement = new IR.Statement.PrintStatement(null, expressionList);
        //final IR.Script script = new IR.Script(null, Lists.immutable.of(printStatement), null);

        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, Lists.immutable.of(printCall), null);


        final Scope globalScope = Scope.createGlobalScope(null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("a", JInt.of(17));
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, script);
        runningScript.run();
        assertEquals("21\n", runtime.getAllOutput());
    }


    @Test
    public void manualAstAddVarsAndPrint() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();
        final Addition addition = new Addition(null,
            new VariableLookupExpression(null, "a"),
            new VariableLookupExpression(null, "b"));
        final ExpressionList expressionList = new ExpressionList(null);
        expressionList.addExpression(addition);
        // kein Keyword mehr: final IR.Statement.PrintStatement printStatement = new IR.Statement.PrintStatement(null, expressionList);
        // final IR.Script script = new IR.Script(null, Lists.immutable.of(printStatement), null);
        final Expression print = new Identifier(null, "print");

        final FunctionCallStatement printCall = new FunctionCallStatement(null, "print", null, expressionList);
        final Script script = new Script(null, Lists.immutable.of(printCall), null);

        final Scope globalScope = Scope.createGlobalScope(null); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        globalScope.bind("a", JInt.of(17));
        globalScope.bind("b", JInt.of(4));
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, script);
        runningScript.run();
        assertEquals("21\n", runtime.getAllOutput());
    }



}
