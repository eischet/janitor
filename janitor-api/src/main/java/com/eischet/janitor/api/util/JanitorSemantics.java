package com.eischet.janitor.api.util;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArithmeticException;
import com.eischet.janitor.api.errors.runtime.JanitorNotImplementedException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorTypeException;
import com.eischet.janitor.api.strings.SingleWildCardMatcher;
import com.eischet.janitor.api.traits.JAssignable;
import com.eischet.janitor.api.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * The semantics of the Janitor language.
 * This class contains the implementation of many of the built-in functions and operators.
 * TODO this should be moved into the janitor-lang module because it is not really a part of the API
 */
public class JanitorSemantics {

    private static final List<JanitorComparison<?, ?>> POSSIBLE_COMPARISONS;

    static {
        POSSIBLE_COMPARISONS = List.of(
            new JanitorComparison<>(JInt.class, JInt.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.janitorGetHostValue(), right.janitorGetHostValue()))),
            new JanitorComparison<>(JDuration.class, JDuration.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.toSeconds(), right.toSeconds()))),
            new JanitorComparison<>(JDateTime.class, JDateTime.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))),
            new JanitorComparison<>(JDate.class, JDate.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))),
            new JanitorComparison<>(JString.class, JString.class, (left, right) -> {
                final String a1 = left.janitorGetHostValue();
                final String leftString = a1 == null ? "" : a1;
                final String a = right.janitorGetHostValue();
                final String rightString = a == null ? "" : a;
                return ComparisonResult.adaptJava(leftString.compareTo(rightString));
            }),
            new JanitorComparison<>(JDateTime.class, JDate.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))),
            new JanitorComparison<>(JDate.class, JDateTime.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))),
            new JanitorComparison<>(JInt.class, JFloat.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getAsDouble(), right.getValue()))),
            new JanitorComparison<>(JFloat.class, JFloat.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getValue(), right.getValue()))),
            new JanitorComparison<>(JFloat.class, JInt.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getValue(), right.getAsDouble())))

        );
    }

    /**
     * Perform a logical NOT operation.
     * @param parameter the parameter
     * @return the result
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static @NotNull JanitorObject logicNot(final JanitorObject parameter) throws JanitorRuntimeException {
        return JBool.map(!isTruthy(parameter));
    }

    /**
     * Figure out if an object is true or false in a boolean context.
     * @param conditionValue the value
     * @return true if the value is truthy, false otherwise
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static boolean isTruthy(final JanitorObject conditionValue) throws JanitorRuntimeException {
        if (conditionValue == null) {
            return false;
        } else {
            final JanitorObject unpacked = conditionValue.janitorUnpack();
            if (unpacked != null && unpacked != conditionValue) {
                return isTruthy(unpacked);
            }
            return conditionValue.janitorIsTrue();
        }
    }

    /**
     * Check two objects for equality.
     * @param leftValue the left value
     * @param rightValue the right value
     * @return TRUE if the values are equals, or FALSE if not.
     */
    public static @NotNull JBool areEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return JBool.map(leftValue == rightValue
                         || leftValue.janitorGetHostValue() == rightValue.janitorGetHostValue()
                         || Objects.equals(leftValue.janitorGetHostValue(), rightValue.janitorGetHostValue()));
    }

    /**
     * Check two objects for equality.
     * @param leftValue the left value
     * @param rightValue the right value
     * @return FALSE if the values are equals, or TRUE if not.
     */
    public static @NotNull JBool areNotEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return areEquals(leftValue, rightValue).opposite();
    }

    /**
     * Perform a logical "-" operation.
     * @param process the process
     * @param currentValue the value
     * @return the result
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static @NotNull JanitorObject negate(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt v) {
            return JInt.of(-v.getValue());
        } else if (currentValue instanceof JFloat f) {
            return JFloat.of(-f.getValue());
        } else {
            throw new JanitorNotImplementedException(process, "we can only negate numbers");
        }
    }

    /**
     * Perform a numeric operation on two objects.
     * This is really like the "center of math" in the current implementation.
     *
     * @param process the process
     * @param name name of the operation
     * @param leftValue left value / object
     * @param rightValue right value / object
     * @param intOp the operation to apply if both values are integers
     * @param floatOp the operation to apply if both values are floats
     * @param dateOp the operation to apply to a date and a duration
     * @param dateTimeOp the operation to apply to a datetime and a duration
     * @param dateDateOp the operation to apply if both values are dates
     * @param dateTimeDateTimeOp the operation to apply if both values are date times
     * @return the result
     * @throws JanitorRuntimeException on errors
     */
    private static JanitorObject numericOperation(final JanitorScriptProcess process, final String name,
                                                  final JanitorObject leftValue, final JanitorObject rightValue,
                                                  final BiFunction<Long, Long, Long> intOp,
                                                  final BiFunction<Double, Double, Double> floatOp,
                                                  final BiFunction<JDate, JDuration, JanitorObject> dateOp,
                                                  final BiFunction<JDateTime, JDuration, JanitorObject> dateTimeOp,
                                                  final BiFunction<JDate, JDate, JanitorObject> dateDateOp,
                                                  final BiFunction<JDateTime, JDateTime, JanitorObject> dateTimeDateTimeOp
    ) throws JanitorRuntimeException {
        try {
            if (leftValue instanceof JInt leftInteger && rightValue instanceof JInt rightInteger) {
                return JInt.of(intOp.apply(leftInteger.getValue(), rightInteger.getValue()));
            } else if (leftValue instanceof JFloat leftFloat && rightValue instanceof JFloat rightFloat) {
                return JFloat.of(floatOp.apply(leftFloat.getValue(), rightFloat.getValue()));
            } else if (leftValue instanceof JInt leftInteger && rightValue instanceof JFloat rightFloat) {
                return JFloat.of(floatOp.apply(leftInteger.getAsDouble(), rightFloat.getValue()));
            } else if (leftValue instanceof JFloat leftFloat && rightValue instanceof JInt rightInteger) {
                return JFloat.of(floatOp.apply(leftFloat.getValue(), rightInteger.getAsDouble()));
            } else if (leftValue instanceof JDateTime leftDate && rightValue instanceof JDateTime rightDate) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s datetimes; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateTimeDateTimeOp.apply(leftDate, rightDate);
            } else if (leftValue instanceof JDate leftDate && rightValue instanceof JDate rightDate) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s dates; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateDateOp.apply(leftDate, rightDate);
            } else if (leftValue instanceof JDuration duration && rightValue instanceof JDateTime dateTime) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s datetimes and durations; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateTimeOp.apply(dateTime, duration);
            } else if (leftValue instanceof JDateTime dateTime && rightValue instanceof JDuration duration) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s datetimes and durations; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateTimeOp.apply(dateTime, duration);
            } else if (leftValue instanceof JDuration duration && rightValue instanceof JDate date) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s dates and durations; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateOp.apply(date, duration);
            } else if (leftValue instanceof JDate date && rightValue instanceof JDuration duration) {
                if (dateOp == null) {
                    throw new JanitorTypeException(process, "we cannot %s dates and durations; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
                }
                return dateOp.apply(date, duration);
            } else {
                throw new JanitorTypeException(process, "we can only %s numbers at the moment; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
            }
        } catch (ArithmeticException e) {
            throw new JanitorArithmeticException(process, e.getMessage() != null ? e.getMessage() : "arithmetic error", e);
        }
    }

    /**
     * Multiply two values.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return the multiplication result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject multiply(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString leftString && rightValue instanceof JInt rightInt) {
            return process.getEnvironment().getBuiltins().string(repeat(leftString.janitorGetHostValue(), rightInt.getValue()));
        } else if (leftValue instanceof JInt leftInt && rightValue instanceof JString rightString) {
            return process.getEnvironment().getBuiltins().string(repeat(rightString.janitorGetHostValue(), leftInt.getValue()));
        }
        return numericOperation(process, "multiply", leftValue, rightValue, (a, b) -> a * b, (a, b) -> a * b, null, null, null, null);
    }

    /**
     * Divide two values.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return the division result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject divide(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "divide", leftValue, rightValue, (a, b) -> a / b, (a, b) -> a / b, null, null, null, null);
    }

    /**
     * Multiply two values, returning the remainder.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return the multiplication result's remainder
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject modulo(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "modulo", leftValue, rightValue, (a, b) -> a % b, (a, b) -> a % b, null, null, null, null);
    }

    /**
     * Subtract two values.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return the subtraction result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject subtract(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "subtract", leftValue, rightValue, (a, b) -> a - b, (a, b) -> a - b, JDuration::subtract, JDuration::subtract, JDuration::between, JDuration::between);
    }

    /**
     * Perform a logical OR operation.
     * @param leftValue left value
     * @param rightValue right value
     * @return OR
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject logicOr(final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        // this works like in Python: null or 17 --> 17
        if (isTruthy(leftValue)) {
            return leftValue;
        } else {
            return rightValue;
        }
    }

    /**
     * Perform a wildcard match operation on a string.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject matches(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
            }
            final SingleWildCardMatcher wc = new SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return JBool.map(regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches (~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }

    /**
     * Perform an inverse wildcard match operation on a string.
     * @param process the running script
     * @param leftValue the left value
     * @param rightValue the right value
     * @return TRUE if NOT MATCHED, or FALSE if matched
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject matchesNot(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
            }
            final SingleWildCardMatcher wc = new SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return JBool.map(!regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches (~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }

    /**
     * Compare two objects.
     * @param process the runnign script process
     * @param leftValue the left object
     * @param rightValue the right object
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject lessThan(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isLessThan();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }

    /**
     * Assign a value to an assignable.
     * @param process the running script
     * @param assignable the assignable
     * @param evalRight the value to assign
     * @throws JanitorRuntimeException on errors
     */
    public static void assign(JanitorScriptProcess process, final JAssignable assignable, final JanitorObject evalRight) throws JanitorRuntimeException {
        if (!assignable.assign(evalRight)) {
            throw new JanitorTypeException(process, "you cannot assign " + evalRight + " [" + ObjectUtilities.simpleClassNameOf(evalRight) + "] to " + assignable.describeAssignable());
        }
    }

    /**
     * Compare two objects.
     * @param process the runnign script process
     * @param leftValue the left object
     * @param rightValue the right object
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject lessThanOrEquals(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isLessThanOrEquals();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }

    /**
     * Compare two objects.
     * @param process the running script process
     * @param _leftValue the left object
     * @param _rightValue the right object
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject greaterThan(JanitorScriptProcess process, final JanitorObject _leftValue, final JanitorObject _rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(_leftValue, _rightValue);
        if (result != null) {
            return result.isGreaterThan();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", _leftValue, _rightValue));
    }

    /**
     * Add two values.
     * @param process the running script process
     * @param leftValue the left value
     * @param rightValue the right value
     * @return the addition result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject add(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString || rightValue instanceof JString) {
            return process.getEnvironment().getBuiltins().string(leftValue.janitorGetHostValue() + String.valueOf(rightValue.janitorGetHostValue()));
        }
        return numericOperation(process, "add", leftValue, rightValue, Long::sum, Double::sum, JDuration::add, JDuration::add, null, null);
    }

    /**
     * Increment a value.
     * @param process the running script process
     * @param currentValue the value
     * @return value++
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject increment(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return JInt.of(currentInteger.getValue() + 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only increment numbers at the moment");
        }
    }

    /**
     * Increment a value.
     * @param process the running script process
     * @param currentValue the value
     * @return value--
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject decrement(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return JInt.of(currentInteger.getValue() - 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only decrement numbers at the moment");
        }
    }

    /**
     * Compare two objects.
     * @param process the runnign script process
     * @param leftValue the left object
     * @param rightValue the right object
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject greaterThanOrEquals(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isGreaterThanOrEquals();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }

    /**
     * Compare two objects, trying to make them compatible when needed.
     * @param leftX left object
     * @param rightX right object
     * @return the result of comparing them
     */
    private static ComparisonResult bestEffortComparison(final JanitorObject leftX, final JanitorObject rightX) {
        final JanitorObject left = leftX.janitorUnpack();
        final JanitorObject right = rightX.janitorUnpack();
        for (final JanitorComparison<?, ?> comparison : POSSIBLE_COMPARISONS) {
            final ComparisonResult result = comparison.compare(left, right);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Repeat the string s num times, i.e. s * num like in Python.
     * @param s the string
     * @param num the number of repetitions
     * @return the repeated string
     */
    public static String repeat(final String s, final long num) {
        final StringBuilder repetitions = new StringBuilder();
        for (long i = 0; i < num; i++) {
            repetitions.append(s);
        }
        return repetitions.toString();
    }

    /**
     * Interface for comparison operations.
     * @param <LEFT> left type
     * @param <RIGHT> right type
     */
    @FunctionalInterface
    public interface Comparer<LEFT extends JanitorObject, RIGHT extends JanitorObject> {
        ComparisonResult compare(LEFT left, RIGHT right);
    }

}
