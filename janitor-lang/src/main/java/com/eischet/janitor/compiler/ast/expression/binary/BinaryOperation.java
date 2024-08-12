package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Binary operation.
 */
public abstract class BinaryOperation extends AstNode implements Expression {
    protected final Expression left;
    protected final Expression right;
    private final BinaryOperationDelegate functor;

    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     * @param functor what to do with the operands
     */
    public BinaryOperation(final Location location,
                           final Expression left,
                           final Expression right,
                           final BinaryOperationDelegate functor) {
        super(location);
        this.left = left;
        this.right = right;
        this.functor = functor;
    }


    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        final JanitorObject leftObject = left.evaluate(process);
        final JanitorObject rightObject = right.evaluate(process);
        process.trace(() -> "evaluating " + this + " with left = " + left + " -> " + leftObject + ", right = " + right + " -> " + rightObject);
        if (leftObject == null || rightObject == null) {
            throw new JanitorArgumentException(process, String.format("null value in binary operation: left=%s=>%s, right=%s=>%s", left, leftObject, right, rightObject));
        }
        try {
            final JanitorObject leftValue = leftObject.janitorUnpack();
            final JanitorObject rightValue = rightObject.janitorUnpack();
            process.trace(() -> "  left = " + leftValue + ", right = "+ rightValue);
            if (leftValue == null || rightValue == null) {
                throw new JanitorArgumentException(process, String.format("null value in binary operation: left=%s=>%s, right=%s=>%s", left, leftObject, right, rightObject));
            }
            final JanitorObject result = functor.perform(process, leftValue, rightValue).janitorUnpack();
            process.trace(() -> "  result = " + result);
            return result;
        } catch (RuntimeException e) {
            throw new JanitorArgumentException(process, String.format("runtime error in binary operation: left=%s=>%s, right=%s=>%s", left, leftObject, right, rightObject), e);
        }
    }


}
