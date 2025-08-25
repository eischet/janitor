package com.eischet.janitor.runtime;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArithmeticException;
import com.eischet.janitor.api.errors.runtime.JanitorNotImplementedException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorTypeException;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.util.ObjectUtilities;
import com.eischet.janitor.runtime.wildcard.SingleWildCardMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

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
     *
     * @param parameter the parameter
     * @return the result
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static @NotNull JanitorObject logicNot(final JanitorObject parameter) throws JanitorRuntimeException {
        return Janitor.toBool(!isTruthy(parameter));
    }

    /**
     * Figure out if an object is true or false in a boolean context.
     *
     * @param conditionValue the value
     * @return true if the value is truthy, false otherwise
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static boolean isTruthy(final @Nullable JanitorObject conditionValue) throws JanitorRuntimeException {
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
     *
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return TRUE if the values are equals, or FALSE if not.
     */
    public static @NotNull JBool areEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return Janitor.toBool(leftValue == rightValue
                              || leftValue.janitorGetHostValue() == rightValue.janitorGetHostValue()
                              || Objects.equals(leftValue.janitorGetHostValue(), rightValue.janitorGetHostValue())
                              || (leftValue instanceof JNumber leftNumber && rightValue instanceof JNumber rightNumber && 0 == compareNumbers(leftNumber, rightNumber))

        );
    }

    private static int compareNumbers(final JNumber leftNumber, final JNumber rightNumber) {
        return Double.compare(leftNumber.toDouble(), rightNumber.toDouble());
    }

    /**
     * Check two objects for equality.
     *
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return FALSE if the values are equals, or TRUE if not.
     */
    public static @NotNull JBool areNotEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return areEquals(leftValue, rightValue).opposite();
    }

    /**
     * Perform a logical "-" operation.
     *
     * @param process      the process
     * @param currentValue the value
     * @return the result
     * @throws JanitorRuntimeException if something goes wrong
     */
    public static @NotNull JanitorObject negate(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt v) {
            return process.getEnvironment().getBuiltinTypes().integer(-v.getValue());
        } else if (currentValue instanceof JFloat f) {
            return process.getEnvironment().getBuiltinTypes().floatingPoint(-f.getValue());
        } else {
            throw new JanitorNotImplementedException(process, "we can only negate numbers");
        }
    }


    /**
     * Perform a numeric operation on two objects.
     * This is really like the "center of math" in the current implementation.
     *
     * @param process            the process
     * @param name               name of the operation
     * @param leftValue          left value / object
     * @param rightValue         right value / object
     * @param intOp              the operation to apply if both values are integers
     * @param floatOp            the operation to apply if both values are floats
     * @param dateOp             the operation to apply to a date and a duration
     * @param dateTimeOp         the operation to apply to a datetime and a duration
     * @param dateDateOp         the operation to apply if both values are dates
     * @param dateTimeDateTimeOp the operation to apply if both values are date times
     * @return the result
     * @throws JanitorRuntimeException on errors
     */
    private static JanitorObject numericOperation(final JanitorScriptProcess process, final String name,
                                                  final JanitorObject leftValue, final JanitorObject rightValue,
                                                  final BinOp<Long, Long, Long> intOp,
                                                  final BinOp<Double, Double, Double> floatOp,
                                                  final BinOp<JDate, JDuration, JanitorObject> dateOp,
                                                  final BinOp<JDateTime, JDuration, JanitorObject> dateTimeOp,
                                                  final BinOp<JDate, JDate, JanitorObject> dateDateOp,
                                                  final BinOp<JDateTime, JDateTime, JanitorObject> dateTimeDateTimeOp,
                                                  final BinOp<JDuration, JDuration, JDuration> durationOp,
                                                  final BinOp<JDuration, Double, JDuration> durationNumberOp,
                                                  final boolean durationCommutative
    ) throws JanitorRuntimeException {
        try {
            if (leftValue instanceof JInt leftInteger && rightValue instanceof JInt rightInteger) {
                return process.getEnvironment().getBuiltinTypes().integer(intOp.apply(process, leftInteger.getValue(), rightInteger.getValue()));
            } else if (leftValue instanceof JFloat leftFloat && rightValue instanceof JFloat rightFloat) {
                return process.getEnvironment().getBuiltinTypes().floatingPoint(floatOp.apply(process, leftFloat.getValue(), rightFloat.getValue()));
            } else if (leftValue instanceof JInt leftInteger && rightValue instanceof JFloat rightFloat) {
                return process.getEnvironment().getBuiltinTypes().floatingPoint(floatOp.apply(process, leftInteger.getAsDouble(), rightFloat.getValue()));
            } else if (leftValue instanceof JFloat leftFloat && rightValue instanceof JInt rightInteger) {
                return process.getEnvironment().getBuiltinTypes().floatingPoint(floatOp.apply(process, leftFloat.getValue(), rightInteger.getAsDouble()));
            } else if (dateOp != null && leftValue instanceof JDateTime leftDate && rightValue instanceof JDateTime rightDate) {
                return dateTimeDateTimeOp.apply(process, leftDate, rightDate);
            } else if (dateOp != null && leftValue instanceof JDate leftDate && rightValue instanceof JDate rightDate) {
                return dateDateOp.apply(process, leftDate, rightDate);
            } else if (dateOp != null && leftValue instanceof JDuration duration && rightValue instanceof JDateTime dateTime) {
                return dateTimeOp.apply(process, dateTime, duration);
            } else if (dateOp != null && leftValue instanceof JDateTime dateTime && rightValue instanceof JDuration duration) {
                return dateTimeOp.apply(process, dateTime, duration);
            } else if (dateOp != null && leftValue instanceof JDuration duration && rightValue instanceof JDate date) {
                return dateOp.apply(process, date, duration);
            } else if (dateOp != null && leftValue instanceof JDate date && rightValue instanceof JDuration duration) {
                return dateOp.apply(process, date, duration);
            } else if (durationOp != null && leftValue instanceof JDuration leftDuration && rightValue instanceof JDuration rightDuration) {
                return durationOp.apply(process, leftDuration, rightDuration);
            } else if (durationNumberOp != null && leftValue instanceof JDuration leftDuration && rightValue instanceof JNumber rightNumber) {
                return durationNumberOp.apply(process, leftDuration, rightNumber.toDouble());
            } else if (durationNumberOp != null && leftValue instanceof JNumber leftNumber && rightValue instanceof JDuration rightDuration && durationCommutative) {
                return durationNumberOp.apply(process, rightDuration, leftNumber.toDouble());
            } else {
                throw new JanitorTypeException(process, "we can only %s numbers at the moment; got %s [%s] and %s [%s]".formatted(name, leftValue, ObjectUtilities.simpleClassNameOf(leftValue), rightValue, ObjectUtilities.simpleClassNameOf(rightValue)));
            }
        } catch (ArithmeticException e) {
            throw new JanitorArithmeticException(process, e.getMessage() != null ? e.getMessage() : "arithmetic error", e);
        }
    }

    /**
     * Multiply two values.
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return the multiplication result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject multiply(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString leftString && rightValue instanceof JInt rightInt) {
            return process.getEnvironment().getBuiltinTypes().string(repeat(leftString.janitorGetHostValue(), rightInt.getValue()));
        } else if (leftValue instanceof JInt leftInt && rightValue instanceof JString rightString) {
            return process.getEnvironment().getBuiltinTypes().string(repeat(rightString.janitorGetHostValue(), leftInt.getValue()));
        }
        return numericOperation(process,
                "multiply",
                leftValue,
                rightValue,
                (p, a, b) -> a * b,
                (p, a, b) -> a * b,
                null,
                null,
                null,
                null,
                null,
                JDuration::multiply, true);
    }

    /**
     * Divide two values.
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return the division result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject divide(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process,
                "divide",
                leftValue,
                rightValue,
                (p, a, b) -> a / b,
                (p, a, b) -> a / b,
                null,
                null,
                null,
                null,
                null,
                JDuration::divide, false);
    }

    /**
     * Multiply two values, returning the remainder.
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return the multiplication result's remainder
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject modulo(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(
                process,
                "modulo",
                leftValue,
                rightValue,
                (p, a, b) -> a % b,
                (p, a, b) -> a % b,
                null,
                null,
                null,
                null,
                null,
                null, false);
    }

    /**
     * Subtract two values.
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return the subtraction result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject subtract(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process,
                "subtract",
                leftValue,
                rightValue,
                (p, a, b) -> a - b,
                (p, a, b) -> a - b,
                JDuration::subtract,
                JDuration::subtract,
                (proc, jDate, jDate2) -> durationBetween(process.getEnvironment().getBuiltinTypes(), jDate, jDate2),
                (proc, jDateTime, jDateTime2) -> durationBetween(process.getEnvironment().getBuiltinTypes(), jDateTime, jDateTime2),
                JDuration::subtract,
                null, false
        );
    }

    /**
     * Perform a logical OR operation.
     *
     * @param leftValue  left value
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
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return TRUE or FALSE
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject matches(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue == Janitor.NULL || rightValue == Janitor.NULL) {
            return Janitor.FALSE;
        }
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
            }
            final SingleWildCardMatcher wc = new SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return Janitor.toBool(regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches (~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }

    /**
     * Perform an inverse wildcard match operation on a string.
     *
     * @param process    the running script
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return TRUE if NOT MATCHED, or FALSE if matched
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject matchesNot(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue == Janitor.NULL || rightValue == Janitor.NULL) {
            return Janitor.FALSE;
        }
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
            }
            final SingleWildCardMatcher wc = new SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return Janitor.toBool(!regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches not (!~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }

    /**
     * Compare two objects.
     *
     * @param process    the runnign script process
     * @param leftValue  the left object
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
     *
     * @param process    the running script
     * @param assignable the assignable
     * @param evalRight  the value to assign
     * @throws JanitorRuntimeException on errors
     */
    public static void assign(JanitorScriptProcess process, final JAssignable assignable, final JanitorObject evalRight) throws JanitorRuntimeException {
        try {
            if (!assignable.assign(evalRight)) {
                throw new JanitorTypeException(process, "you cannot assign " + evalRight + " [" + ObjectUtilities.simpleClassNameOf(evalRight) + "] to " + assignable.describeAssignable());
            }
        } catch (JanitorGlueException e) {
            throw e.toRuntimeException(process);
        }
    }

    /**
     * Compare two objects.
     *
     * @param process    the runnign script process
     * @param leftValue  the left object
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
     *
     * @param process     the running script process
     * @param _leftValue  the left object
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
     *
     * @param process    the running script process
     * @param leftValue  the left value
     * @param rightValue the right value
     * @return the addition result
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject add(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString || rightValue instanceof JString) {
            return process.getEnvironment().getBuiltinTypes().string(leftValue.janitorGetHostValue() + String.valueOf(rightValue.janitorGetHostValue()));
        }
        return numericOperation(process,
                "add",
                leftValue,
                rightValue,
                longSum,
                doubleSum,
                JDuration::add,
                JDuration::add,
                null,
                null,
                JDuration::add,
                null, false);
    }

    private static final BinOp<Long, Long, Long> longSum = BinOp.adapt(Long::sum);
    private static final BinOp<Double, Double, Double> doubleSum = BinOp.adapt(Double::sum);


    /**
     * Increment a value.
     *
     * @param process      the running script process
     * @param currentValue the value
     * @return value++
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject increment(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return process.getEnvironment().getBuiltinTypes().integer(currentInteger.getValue() + 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only increment numbers at the moment");
        }
    }

    /**
     * Increment a value.
     *
     * @param process      the running script process
     * @param currentValue the value
     * @return value--
     * @throws JanitorRuntimeException on errors
     */
    public static @NotNull JanitorObject decrement(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return process.getEnvironment().getBuiltinTypes().integer(currentInteger.getValue() - 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only decrement numbers at the moment");
        }
    }

    /**
     * Compare two objects.
     *
     * @param process    the runnign script process
     * @param leftValue  the left object
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
     *
     * @param leftX  left object
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
     *
     * @param s   the string
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
     * Calculate the difference between two dates.
     * Time flies from the left to the right in this context, in case you're wondering about the parameter names.
     *
     * @param left  the left date
     * @param right the right date
     * @return the duration between the two dates
     */
    public static JDuration durationBetween(final BuiltinTypes builtins, final JDate left, final JDate right) {
        return builtins.duration(Duration.between(right.janitorGetHostValue().atStartOfDay(), left.janitorGetHostValue().atStartOfDay()).toDays(), JDuration.JDurationKind.DAYS);
    }

    /**
     * Calculate the difference between two datetimes.
     * Time flies from the left to the right in this context, in case you're wondering about the parameter names.
     *
     * @param left  the left date
     * @param right the right date
     * @return the duration between the two dates
     */
    public static JDuration durationBetween(final BuiltinTypes builtins, final JDateTime left, final JDateTime right) {
        return builtins.duration(Duration.between(right.janitorGetHostValue(), left.janitorGetHostValue()).toSeconds(), JDuration.JDurationKind.SECONDS);
    }

    /**
     * Interface for comparison operations.
     *
     * @param <LEFT>  left type
     * @param <RIGHT> right type
     */
    @FunctionalInterface
    public interface Comparer<LEFT extends JanitorObject, RIGHT extends JanitorObject> {
        ComparisonResult compare(LEFT left, RIGHT right);
    }

}
