package com.eischet.janitor.compiler.ast.statement.assignment;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.statement.Statement;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Any assignment: left operator right.
 * Concrete subclasses will implement the actual assignment operation.
 */
public abstract class Assignment extends Statement {
    private final Expression left;
    private final Expression right;

    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right operand
     */
    public Assignment(final Location location, final Expression left, final Expression right) {
        super(location);
        this.left = left;
        this.right = right;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        runningScript.trace(() -> "executing " + this + " with left " + left + " and right " + right);
        if (left instanceof Identifier) {
            final String id = ((Identifier) left).getText();
            runningScript.trace(() -> "assigning to identifier " + id);

            // LATER: turn into :   runningScript.lookupScopedVar(id);

            Scope scope = runningScript.getCurrentScope();

            final JanitorObject current = scope.lookupLocally(runningScript, id);
            runningScript.trace(() -> "current value of " + id + " is " + current);

            while (scope != null && scope.lookupLocally(runningScript, id) == null) {
                scope = scope.getParent();
            }
            // LATER: hier muss auch beachtet werden, dass es einen module scope gibt!
            // das funktioniert hier wahrscheinlich nicht, wenn eine Function aus einem Modul versucht,
            // an eine eigene (im Modul definierte) Variable etwas zuzuweisen. Das ist aber ohnehin keine
            // gute Idee, weil die Module in mehreren Skripten parallel im Einsatz sein könnten.
            JanitorObject valueToAssign = produce(left, right, runningScript).janitorUnpack();
            if (scope == null) {
                runningScript.trace(() -> "  will assign " + id + " = " + valueToAssign + " in current scope of scritp = " + runningScript.getCurrentScope());
                runningScript.getCurrentScope().bind(runningScript, id, valueToAssign);
                return;
            } else {
                final Scope finalScope = scope;
                runningScript.trace(() -> "  will assign " + id + " = " + valueToAssign + " in its original scope " + finalScope);
                if (scope.getParent() == null) {
                    runningScript.trace(() -> "warning: trying to assign something to the top level scope!");
                }
                scope.bind(runningScript, id, valueToAssign);
                return; // FEHLTE! dadurch wurden calls doppelt gemoppelt!!!
            }
        }

        final JanitorObject evalLeft = left.evaluate(runningScript); // hier NICHT auspacken, weil wir sonst nicht mehr wissen, wohin wir zuweisen sollen!
        final JanitorObject evalRight = right.evaluate(runningScript).janitorUnpack();

        runningScript.trace(() -> "assigning " + evalRight + " to " + evalLeft);
        if (evalLeft instanceof JAssignable assignable) {
            JanitorSemantics.assign(runningScript, assignable, evalRight);
        } else {

            // runningScript.getCurrentScope().lookup()
            runningScript.trace(() -> "TODO: assign left " + left + " with right " + right);
            runningScript.trace(() -> "eval left " + evalLeft + " eval right " + evalRight);

            final String diag = "cannot assign value " + simpleClassNameOf(evalRight) + " " + evalRight + " to " + simpleClassNameOf(evalLeft) + " " + evalLeft + " in " + getClass().getSimpleName() + "; left expression: " + left;
            runningScript.trace(() -> diag);

            throw new JanitorNameException(runningScript, diag);

            // throw new JanitorAssignmentException(runningScript, "cannot assign rvalue " + evalRight + " to non-lvalue " + evalLeft + " in " + getClass().getSimpleName());
        }
    }

    protected abstract JanitorObject produce(final Expression left, final Expression right, final JanitorScriptProcess runningScript) throws JanitorRuntimeException;

}
