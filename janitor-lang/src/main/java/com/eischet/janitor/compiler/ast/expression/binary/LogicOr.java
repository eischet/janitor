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
     * <p>
     * Note: this constructor's functor is only used by the inherited {@link BinaryOperation}
     * machinery for JSON export / introspection purposes; actual evaluation always goes through
     * the overridden {@link #evaluate(JanitorScriptProcess)} below, which is the one that matters.
     */
    public LogicOr(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (leftValue, rightValue, rightValue2) -> JanitorSemantics.logicOr(rightValue, rightValue2));
    }

    /**
     * Unlike most binary operators, "or" must short-circuit: the right-hand side must not be
     * evaluated at all if the left-hand side is already truthy (e.g. {@code x == null || x.foo}
     * must not evaluate {@code x.foo} when {@code x} is null). The default {@link BinaryOperation#evaluate}
     * always evaluates both operands first, which is wrong here -- {@link LogicAnd} has the same
     * kind of override for the same reason.
     * <p>
     * This also has to preserve Janitor's Python-like "or" semantics (see
     * {@link JanitorSemantics#logicOr}: {@code null or 17 --> 17}): it returns the actual
     * left/right value, not a coerced boolean, so patterns like {@code x = a or default} keep
     * working.
     */
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
