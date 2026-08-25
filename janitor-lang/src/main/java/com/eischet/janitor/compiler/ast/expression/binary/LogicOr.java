package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import org.jetbrains.annotations.NotNull;

/**
 * Logic OR of operands: or.
 * Deprecated but usable: ||.
 */
public class LogicOr extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public LogicOr(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (leftValue, rightValue, rightValue2) -> JanitorSemantics.logicOr(rightValue, rightValue2));
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        final JanitorObject leftValue = left.evaluate(process).janitorUnpack();
        if (JanitorSemantics.isTruthy(leftValue)) {
            return leftValue;
        }
        return right.evaluate(process).janitorUnpack();
    }

}
