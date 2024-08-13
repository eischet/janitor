package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

import java.util.function.BiFunction;

/**
 * Helper interface for the implementation of binary operations.
 * <p>
 * Binary operations take a left and right operand (argument) and produce a value from those.
 * For example, the "addition" operation returns the sum of its left and right argument: a + b = c.
 * We've learned about these operations in school and probably don't think much about them anymore.
 * </p>
 *
 * @param <L> left operand type
 * @param <R> right operand type
 * @param <T> result type
 */
@FunctionalInterface
public interface BinOp<L, R, T> {
    T apply(JanitorScriptProcess process, L left, R right) throws JanitorRuntimeException;

    /**
     * Wrap a BiFunction to match the BinOp signature, effectively removing the process argument.
     * This is useful for calling methods like Long::sum which, of course, do not know about the
     * JanitorScriptProcess.
     *
     * @param function a function to wrap
     * @return the function as a BinOp
     * @param <L> left operand type
     * @param <R> right operand type
     * @param <T> result type
     */
    static <L, R, T> BinOp<L, R, T> adapt(BiFunction<L, R, T> function) {
        return (process, left, right) -> function.apply(left, right);
    }

}
