package com.eischet.janitor.compiler.ast.statement.assignment;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Division assignment: foo /= bar.
 */
public class DivAssignment extends Assignment {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right operand
     */
    public DivAssignment(final Location location, final Expression left, final Expression right) {
        super(location, left, right);
    }

    @Override
    protected JanitorObject produce(final Expression left, final Expression right, final JanitorScriptProcess process) throws JanitorRuntimeException {
        return JanitorSemantics.divide(process, left.evaluate(process), right.evaluate(process).janitorUnpack());
    }
}
