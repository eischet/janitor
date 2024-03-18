package com.eischet.janitor.runtime;

import com.eischet.janitor.api.errors.runtime.*;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.runtime.types.JAssignable;
import com.eischet.janitor.runtime.types.JCallArgs;
import com.eischet.janitor.tools.JStringUtilities;
import com.eischet.janitor.tools.ObjectUtilities;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public class JanitorSemantics {

    public static @NotNull JanitorObject logicNot(final JanitorObject parameter) throws JanitorRuntimeException {
        return JBool.map(!isTruthy(parameter));
    }

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

    public static @NotNull JBool areEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return JBool.map(leftValue == rightValue
            || leftValue.janitorGetHostValue() == rightValue.janitorGetHostValue()
            || Objects.equals(leftValue.janitorGetHostValue(), rightValue.janitorGetHostValue()));
    }

    public static @NotNull JBool areNotEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
        return areEquals(leftValue, rightValue).opposite();
    }

    public static @NotNull JanitorObject negate(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt v) {
            return JInt.of(-v.getValue());
        } else if (currentValue instanceof JFloat f) {
            return JFloat.of(-f.getValue());
        } else {
            throw new JanitorNotImplementedException(process, "we can only negate numbers");
        }
    }

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

    public static @NotNull JanitorObject multiply(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString leftString && rightValue instanceof JInt rightInt) {
            return JString.of(JStringUtilities.repeat(leftString.janitorGetHostValue(), rightInt.getValue()));
        } else if (leftValue instanceof JInt leftInt && rightValue instanceof  JString rightString) {
            return JString.of(JStringUtilities.repeat(rightString.janitorGetHostValue(), leftInt.getValue()));
        }
        return numericOperation(process, "multiply", leftValue, rightValue, (a, b) -> a*b, (a, b) -> a*b, null, null, null, null);
    }

    public static @NotNull JanitorObject divide(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "divide", leftValue, rightValue, (a, b) -> a / b, (a, b) -> a / b, null, null, null, null);
    }

    public static @NotNull JanitorObject modulo(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "modulo", leftValue, rightValue, (a, b) -> a%b, (a, b) -> a%b, null, null, null, null);
    }

    public static @NotNull JanitorObject subtract(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        return numericOperation(process, "subtract", leftValue, rightValue, (a, b) -> a-b, (a, b) -> a-b, JDuration::subtract, JDuration::subtract, JDuration::between, JDuration::between);
    }

    public static @NotNull JanitorObject logicOr(final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        // this works like in Python: null or 17 --> 17
        if (isTruthy(leftValue)) {
            return leftValue;
        } else {
            return rightValue;
        }
    }

    public static @NotNull JanitorObject matches(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
            }
            final JStringUtilities.SingleWildCardMatcher wc = new JStringUtilities.SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.TRUE : JBool.FALSE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return JBool.map(regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches (~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }


    public static @NotNull JanitorObject matchesNot(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString && rightValue instanceof JString) {
            if (((JString) rightValue).getWildCardMatcher() != null) {
                return ((JString) rightValue).getWildCardMatcher().matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
            }
            final JStringUtilities.SingleWildCardMatcher wc = new JStringUtilities.SingleWildCardMatcher((String) rightValue.janitorGetHostValue());
            ((JString) leftValue).setWildCardMatcher(wc);
            return wc.matches((String) leftValue.janitorGetHostValue()) ? JBool.FALSE : JBool.TRUE;
        }
        if (leftValue instanceof JString str && rightValue instanceof JRegex regex) {
            return JBool.map(! regex.janitorGetHostValue().matcher(str.janitorGetHostValue()).matches());
        }
        throw new JanitorTypeException(process, "matches (~): invalid left " + leftValue + " / right " + rightValue + ", both should be strings");
    }

    public static @NotNull JanitorObject lessThan(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isLessThan();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }

    public static void assign(JanitorScriptProcess process, final JAssignable assignable, final JanitorObject evalRight) throws JanitorRuntimeException {
        if (!assignable.assign(evalRight)) {
            throw new JanitorTypeException(process, "you cannot assign " + evalRight + " [" + ObjectUtilities.simpleClassNameOf(evalRight) + "] to " + assignable.describeAssignable());
        }
    }

    public static @NotNull JanitorObject lessThanOrEquals(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isLessThanOrEquals();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }

    public static @NotNull JanitorObject greaterThan(JanitorScriptProcess process, final JanitorObject _leftValue, final JanitorObject _rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(_leftValue, _rightValue);
        if (result != null) {
            return result.isGreaterThan();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", _leftValue, _rightValue));
    }

    public static @NotNull JanitorObject add(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        if (leftValue instanceof JString || rightValue instanceof JString) {
            return JString.of(leftValue.janitorGetHostValue() + String.valueOf(rightValue.janitorGetHostValue()));
        }
        return numericOperation(process, "add", leftValue, rightValue, Long::sum, Double::sum, JDuration::add, JDuration::add, null, null);
    }

    public static @NotNull JanitorObject increment(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return JInt.of(currentInteger.getValue() + 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only increment numbers at the moment");
        }
    }

    public static @NotNull JanitorObject decrement(JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        if (currentValue instanceof JInt currentInteger) {
            return JInt.of(currentInteger.getValue() - 1);
        } else {
            throw new JanitorNotImplementedException(process, "we can only decrement numbers at the moment");
        }
    }

    public static @NotNull JanitorObject greaterThanOrEquals(JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException {
        final ComparisonResult result = bestEffortComparison(leftValue, rightValue);
        if (result != null) {
            return result.isGreaterThanOrEquals();
        }
        throw new JanitorNotImplementedException(process, String.format("the interpreter cannot compare these values at the moment: left=%s, right=%s.", leftValue, rightValue));
    }


    private static final ImmutableList<JanitorComparison<?, ?>> POSSIBLE_COMPARISONS;

    static {
        final MutableList<JanitorComparison<?, ?>> comparisons = Lists.mutable.empty();
        comparisons.add(new JanitorComparison<>(JInt.class, JInt.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.janitorGetHostValue(), right.janitorGetHostValue()))));
        comparisons.add(new JanitorComparison<>(JDuration.class, JDuration.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.toSeconds(), right.toSeconds()))));
        comparisons.add(new JanitorComparison<>(JDateTime.class, JDateTime.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))));
        comparisons.add(new JanitorComparison<>(JDate.class, JDate.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))));
        comparisons.add(new JanitorComparison<>(JString.class, JString.class, (left, right) -> {
            final String a1 = left.janitorGetHostValue();
            final String leftString = a1 == null ? "" : a1;
            final String a = right.janitorGetHostValue();
            final String rightString = a == null ? "" : a;
            return ComparisonResult.adaptJava(leftString.compareTo(rightString));
        }));
        comparisons.add(new JanitorComparison<>(JDateTime.class, JDate.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))));
        comparisons.add(new JanitorComparison<>(JDate.class, JDateTime.class, (left, right) -> ComparisonResult.adaptJava(Long.compare(left.getInternalRepresentation(), right.getInternalRepresentation()))));
        comparisons.add(new JanitorComparison<>(JInt.class, JFloat.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getAsDouble(), right.getValue()))));
        comparisons.add(new JanitorComparison<>(JFloat.class, JFloat.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getValue(), right.getValue()))));
        comparisons.add(new JanitorComparison<>(JFloat.class, JInt.class, (left, right) -> ComparisonResult.adaptJava(Double.compare(left.getValue(), right.getAsDouble()))));
        POSSIBLE_COMPARISONS = comparisons.toImmutable();
    }

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

    @FunctionalInterface
    public interface Comparer<LEFT extends JanitorObject, RIGHT extends JanitorObject> {
        ComparisonResult compare(LEFT left, RIGHT right);
    }

    public static JanitorObject doAssert(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final JanitorObject condition = args.require(0, 1).get(0);
        final String message = args.getOptionalStringValue(1, "");
        if (!JanitorSemantics.isTruthy(condition)) {
            if (message == null || message.isBlank()) {
                throw new JanitorAssertionException(process, "assertion failed!");
            } else {
                throw new JanitorAssertionException(process, "assertion failed: " + message);
            }
        }
        return condition;
    }

}
