package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.calls.JVoidMethod;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JConstructor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class GenericDispatchTable<T extends JanitorObject> implements Dispatcher<T> {

    private interface Delegate<T> {
        JanitorObject delegate(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException;
    }

    private final Map<String, AttributeLookupHandler<T>> map = new HashMap<>();
    private final Delegate<T> parentLookupHandler;
    private JConstructor<T> constructor;

    public GenericDispatchTable() {
        parentLookupHandler = null;
    }

    public <P extends JanitorObject> GenericDispatchTable(final Dispatcher<P> parent, final Function<T, P> caster) {
        parentLookupHandler = (instance, process, name) -> parent.dispatch(caster.apply(instance), process, name);
    }

    public JConstructor<T> getConstructor() {
        return constructor;
    }

    public void setConstructor(JConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Adds a method to the dispatch table.
     * @param name the name of the method
     * @param method the method
     */
    public void addMethod(final String name, final JUnboundMethod<T> method) {
        map.put(name, (instance, process) -> new JBoundMethod<>(name, instance, method));
    }

    /**
     * Adds a method to the dispatch table that automatically returns the instance, useful for fluent interfaces.
     * @param name the name of the method
     * @param method the method
     */
    public void addBuilderMethod(final String name, final JVoidMethod<T> method) {
        map.put(name, (instance, process) -> new JBoundMethod<>(name, instance, (self, p1, arguments) -> {
            method.call(instance, p1, arguments);
            return instance;
        }));
    }

    /**
     * Adds a read-only integer property.
     * @param name property name
     * @param getter property getter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter) {
        map.put(name, (instance, process) -> process.getBuiltins().integer(getter.apply(instance)));
    }

    /**
     * Adds a read-write integer property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter, final BiConsumer<T, Integer> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getEnvironment().getBuiltins().integer(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(process, value).janitorGetHostValue().intValue())));
    }

    /**
     * Adds a read-only long property.
     * @param name property name
     * @param getter property getter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter) {
        map.put(name, (instance, process) -> process.getEnvironment().getBuiltins().integer(getter.apply(instance)));
    }

    /**
     * Adds a read-write long property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter, final BiConsumer<T, Long> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getEnvironment().getBuiltins().integer(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(process, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only double property.
     * @param name property name
     * @param getter property getter
     */
    public void addDoubleProperty(final String name, final Function<T, Double> getter, final BiConsumer<T, Double> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getEnvironment().getBuiltins().floatingPoint(getter.apply(instance)), value -> setter.accept(instance, process.requireFloat(value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only list property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addListProperty(final String name, final Function<T, JList> getter) {
        map.put(name, (instance, process) -> getter.apply(instance));
    }

    public <E> void addListProperty(final String name, final Function<T, List<E>> getter, final BiConsumer<T, List<E>> setter, final TwoWayConverter<E> converter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(ConverterToJanitor.toJanitorList(process, getter.apply(instance), converter), value -> {
            if (!(value instanceof JList argList)) {
                throw new IllegalArgumentException("Expected a list");
            }
            final List<E> list =  ConverterFromJanitor.toList(process, argList, converter);
            setter.accept(instance, list);
        }));
    }

    public void addListOfStringsProperty(final String name, final Function<T, List<String>> getter, final BiConsumer<T, List<String>> setter) {
        addListProperty(name, getter, setter, StringConverter.INSTANCE);
    }

    public void addListOfIntegersProperty(final String name, final Function<T, List<Integer>> getter, final BiConsumer<T, List<Integer>> setter) {
        addListProperty(name, getter, setter, IntegerConverter.INSTANCE);
    }

    public void addListOfDoublesProperty(final String name, final Function<T, List<Double>> getter, final BiConsumer<T, List<Double>> setter) {
        addListProperty(name, getter, setter, FloatConverter.INSTANCE);
    }


    /**
     * Adds a read-only boolean property.
     * This variant maps false to null! If you want to preserve null values for scripts, use addNullableBooleanProperty instead!
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter) {
        map.put(name, (instance, process) -> JBool.of(getter.apply(instance)));
    }

    /**
     * Adds a read-only boolean property.
     * This variant allows scripts to see true, false and null, while the simpler method maps false to null!
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addNullableBooleanProperty(final String name, Function<T, Boolean> getter) {
        map.put(name, (instance, process) -> JBool.nullableBooleanOf(getter.apply(instance)));
    }

    /**
     * Adds a read-write boolean property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter, final BiConsumer<T, Boolean> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(JBool.of(getter.apply(instance)), value -> setter.accept(instance, JBool.require(process, value).janitorIsTrue())));
    }

    /**
     * Adds a read-write boolean property.
     * This variant allows scripts to see and supply true, false and null, while the simpler method maps false to null!
     * Any other values are mapped to true/false according to their built-in "truthiness".
     *
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addNullableBooleanProperty(final String name, final Function<T, Boolean> getter, final BiConsumer<T, Boolean> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(JBool.nullableBooleanOf(getter.apply(instance)), value -> setter.accept(instance, toNullableBoolean(value))));
    }

    /**
     * Special case for setting Boolean (not boolean!) fields.
     * Janitor's NULL is mapped to null, while everyhing else is mapped according to its regular "truthiness", which
     * implies that true maps to true and false to false.
     * @param value the value to map
     * @return the mapped value, null, TRUE or FALSE
     */
    private @Nullable Boolean toNullableBoolean(final @NotNull JanitorObject value) {
        if (value == JNull.NULL) {
            return null;
        }
        return value.janitorIsTrue();
    }


    /**
     * Adds a read-only string property.
     * @param name property name
     * @param getter property getter
     */
    public void addStringProperty(final String name, final Function<T, String> getter) {
        map.put(name, (instance, process) -> process.getBuiltins().nullableString(getter.apply(instance)));
    }

    /**
     * Adds a read-write string property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addStringProperty(final String name, final Function<T, String> getter, final BiConsumer<T, String> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().nullableString(getter.apply(instance)), value -> setter.accept(instance, JString.require(process, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter) {
        map.put(name, (instance, process) -> process.getBuiltins().nullableDate(getter.apply(instance)));
    }

    /**
     * Adds a read-write date property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter, final BiConsumer<T, LocalDate> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().date(getter.apply(instance)), value -> setter.accept(instance, JDate.require(process, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date-time property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter) {
        map.put(name, (instance, process) -> process.getBuiltins().nullableDateTime(getter.apply(instance)));
    }

    /**
     * Adds a read-write date-time property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter, final BiConsumer<T, LocalDateTime> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().nullableDateTime(getter.apply(instance)), value -> setter.accept(instance, JDateTime.require(process, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only object property.
     * @param name property name
     * @param getter property getter
     */
    public void addObjectProperty(final String name, final Function<T, JanitorObject> getter) {
        map.put(name, (instance, process) -> getter.apply(instance));
    }

    /**
     * Adds a read-write object property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addObjectProperty(final String name, final Function<T, JanitorObject> getter, final BiConsumer<T, JanitorObject> setter) {
        map.put(name, (instance, process) -> new TemporaryAssignable(getter.apply(instance), value -> setter.accept(instance, value)));
    }

    /**
     * Dispatches a call to the instance, using the dispatch table to figure out what to do.
     * @param process the running script
     * @param name the name of the attribute
     * @param required whether the attribute is required
     * @param instance the object to dispatch the call to
     * @return the result of the lookup
     */
    @SuppressWarnings("unchecked")
    public JanitorObject dispatch(final JanitorScriptProcess process, final String name, final boolean required, final JanitorWrapper<? extends T> instance) throws JanitorRuntimeException {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            //noinspection unchecked
            return handler.lookupAttribute((T) instance, process);
        }
        return null;
    }

    /**
     * Dispatches a call to the instance, using the dispatch table to figure out what to do.
     * @param instance the JanitorObject to dispatch the method call to
     * @param process the running script
     * @param name the name of the method to call
     * @return the result of the lookup
     */
    @Override
    public JanitorObject dispatch(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            return handler.lookupAttribute(instance, process);
        }
        if (parentLookupHandler != null) {
            return parentLookupHandler.delegate(instance, process, name);
        }
        return null;
    }
    
}
