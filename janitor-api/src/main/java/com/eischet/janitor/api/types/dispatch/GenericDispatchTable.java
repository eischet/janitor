package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.calls.JVoidMethod;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class GenericDispatchTable<T extends JanitorObject> implements Dispatcher<T> {

    private interface Delegate<T> {
        JanitorObject delegate(final T instance, final JanitorScriptProcess process, final String name);
    }


    private final Map<String, AttributeLookupHandler<T>> map = new HashMap<>();
    private final Delegate<T> parentLookupHandler;

    public GenericDispatchTable() {
        parentLookupHandler = null;
    }

    public <P extends JanitorObject> GenericDispatchTable(final Dispatcher<P> parent, final Function<T, P> caster) {
        parentLookupHandler = (instance, process, name) -> parent.dispatch(caster.apply(instance), process, name);
    }

    /**
     * Adds a method to the dispatch table.
     * @param name the name of the method
     * @param method the method
     */
    public void addMethod(final String name, final JUnboundMethod<T> method) {
        map.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    /**
     * Adds a method to the dispatch table that automatically returns the instance, useful for fluent interfaces.
     * @param name the name of the method
     * @param method the method
     */
    public void addBuilderMethod(final String name, final JVoidMethod<T> method) {
        map.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, (self, runningScript1, arguments) -> {
            method.call(instance, runningScript1, arguments);
            return instance;
        }));
    }

    /**
     * Adds a read-only integer property.
     * @param name property name
     * @param getter property getter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getEnvironment().getBuiltins().integer(getter.apply(instance)));
    }

    /**
     * Adds a read-write integer property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter, final BiConsumer<T, Integer> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getEnvironment().getBuiltins().integer(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(runningScript, value).janitorGetHostValue().intValue())));
    }

    /**
     * Adds a read-only long property.
     * @param name property name
     * @param getter property getter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getEnvironment().getBuiltins().integer(getter.apply(instance)));
    }

    /**
     * Adds a read-write long property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter, final BiConsumer<T, Long> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getEnvironment().getBuiltins().integer(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only double property.
     * @param name property name
     * @param getter property getter
     */
    public void addDoubleProperty(final String name, final Function<T, Double> getter, final BiConsumer<T, Double> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getEnvironment().getBuiltins().floatingPoint(getter.apply(instance)), value -> setter.accept(instance, runningScript.requireFloat(value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only list property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addListProperty(final String name, final Function<T, JList> getter) {
        map.put(name, (instance, runningScript) -> getter.apply(instance));
    }

    /**
     * Adds a read-only boolean property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter) {
        map.put(name, (instance, runningScript) -> JBool.of(getter.apply(instance)));
    }

    /**
     * Adds a read-write boolean property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter, final BiConsumer<T, Boolean> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JBool.of(getter.apply(instance)), value -> setter.accept(instance, JBool.require(runningScript, value).janitorIsTrue())));
    }

    /**
     * Adds a read-only string property.
     * @param name property name
     * @param getter property getter
     */
    public void addStringProperty(final String name, final Function<T, String> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getEnvironment().getBuiltins().nullableString(getter.apply(instance)));
    }

    /**
     * Adds a read-write string property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addStringProperty(final String name, final Function<T, String> getter, final BiConsumer<T, String> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getEnvironment().getBuiltins().nullableString(getter.apply(instance)), value -> setter.accept(instance, JString.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getBuiltins().nullableDate(getter.apply(instance)));
    }

    /**
     * Adds a read-write date property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter, final BiConsumer<T, LocalDate> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getBuiltins().date(getter.apply(instance)), value -> setter.accept(instance, JDate.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date-time property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getBuiltins().nullableDateTime(getter.apply(instance)));
    }

    /**
     * Adds a read-write date-time property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter, final BiConsumer<T, LocalDateTime> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getBuiltins().nullableDateTime(getter.apply(instance)), value -> setter.accept(instance, JDateTime.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only object property.
     * @param name property name
     * @param getter property getter
     */
    public void addObjectProperty(final String name, final Function<T, JanitorObject> getter) {
        map.put(name, (instance, runningScript) -> getter.apply(instance));
    }

    /**
     * Adds a read-write object property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addObjectProperty(final String name, final Function<T, JanitorObject> getter, final BiConsumer<T, JanitorObject> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(getter.apply(instance), value -> setter.accept(instance, value)));
    }

    /**
     * Dispatches a call to the instance, using the dispatch table to figure out what to do.
     * @param runningScript the running script
     * @param name the name of the attribute
     * @param required whether the attribute is required
     * @param instance the object to dispatch the call to
     * @return the result of the lookup
     */
    public JanitorObject dispatch(final JanitorScriptProcess runningScript, final String name, final boolean required, final JanitorWrapper<? extends T> instance) {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            //noinspection unchecked
            return handler.lookupAttribute((T) instance, runningScript);
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
    public JanitorObject dispatch(final T instance, final JanitorScriptProcess process, final String name) {
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
