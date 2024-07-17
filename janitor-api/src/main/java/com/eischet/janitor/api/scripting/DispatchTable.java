package com.eischet.janitor.api.scripting;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.calls.JVoidMethod;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.types.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A dispatch table for JanitorObjects.
 * <p>This class is a helper for quickly making existing Java objects compatible with Janitor.
 * The idea is to create a single dispatcher and populated it in builder style, e.g. by adding methods.</p>
 * <p>Some programming languages, e.g. Jython or Nashorn/Rhino, employ runtime reflection to achieve similar results.
 * I'm not particularly fond of that approach, prefering hand written wrappers instead. Note that it's still perfectly
 * possible to write an alternative Dispatcher implementation based on runtime type reflection if you insist.</p>
 *
 * @param <T> the type of the Java object
 */
public class DispatchTable<T> implements Dispatcher<JanitorWrapper<T>> {

    private final Map<String, AttributeLookupHandler<JanitorWrapper<T>>> map = new HashMap<>();

    /**
     * Constructs a new DispatchTable.
     */
    public DispatchTable() {
    }

    /**
     * Adds a method to the dispatch table.
     * @param name the name of the method
     * @param method the method
     */
    public void addMethod(final String name, final JUnboundMethod<JanitorWrapper<T>> method) {
        map.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    /**
     * Adds a method to the dispatch table that automatically returns the instance, useful for fluent interfaces.
     * @param name the name of the method
     * @param method the method
     */
    public void addBuilderMethod(final String name, final JVoidMethod<JanitorWrapper<T>> method) {
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
    public void addIntegerProperty(final String name, final Function<JanitorWrapper<T>, Integer> getter) {
        map.put(name, (instance, runningScript) -> JInt.of(getter.apply(instance)));
    }

    /**
     * Adds a read-write integer property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addIntegerProperty(final String name, final Function<JanitorWrapper<T>, Integer> getter, final BiConsumer<JanitorWrapper<T>, Integer> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JInt.of(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(runningScript, value).janitorGetHostValue().intValue())));
    }

    /**
     * Adds a read-only long property.
     * @param name property name
     * @param getter property getter
     */
    public void addLongProperty(final String name, final Function<JanitorWrapper<T>, Long> getter) {
        map.put(name, (instance, runningScript) -> JInt.of(getter.apply(instance)));
    }

    /**
     * Adds a read-write long property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addLongProperty(final String name, final Function<JanitorWrapper<T>, Long> getter, final BiConsumer<JanitorWrapper<T>, Long> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JInt.of(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only double property.
     * @param name property name
     * @param getter property getter
     */
    public void addDoubleProperty(final String name, final Function<JanitorWrapper<T>, Double> getter, final BiConsumer<JanitorWrapper<T>, Double> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JFloat.of(getter.apply(instance)), value -> setter.accept(instance, runningScript.requireFloat(value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only list property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addListProperty(final String name, final Function<JanitorWrapper<T>, JList> getter) {
        map.put(name, (instance, runningScript) -> getter.apply(instance));
    }

    /**
     * Adds a read-only boolean property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addBooleanProperty(final String name, final Function<JanitorWrapper<T>, Boolean> getter) {
        map.put(name, (instance, runningScript) -> JBool.of(getter.apply(instance)));
    }

    /**
     * Adds a read-write boolean property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addBooleanProperty(final String name, final Function<JanitorWrapper<T>, Boolean> getter, final BiConsumer<JanitorWrapper<T>, Boolean> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JBool.of(getter.apply(instance)), value -> setter.accept(instance, JBool.require(runningScript, value).janitorIsTrue())));
    }

    /**
     * Adds a read-only string property.
     * @param name property name
     * @param getter property getter
     */
    public void addStringProperty(final String name, final Function<JanitorWrapper<T>, String> getter) {
        map.put(name, (instance, runningScript) -> runningScript.getRuntime().nullableString(getter.apply(instance)));
    }

    /**
     * Adds a read-write string property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addStringProperty(final String name, final Function<JanitorWrapper<T>, String> getter, final BiConsumer<JanitorWrapper<T>, String> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(runningScript.getRuntime().nullableString(getter.apply(instance)), value -> setter.accept(instance, JString.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateProperty(final String name, final Function<JanitorWrapper<T>, LocalDate> getter) {
        map.put(name, (instance, runningScript) -> JDate.ofNullable(getter.apply(instance)));
    }

    /**
     * Adds a read-write date property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateProperty(final String name, final Function<JanitorWrapper<T>, LocalDate> getter, final BiConsumer<JanitorWrapper<T>, LocalDate> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JDate.ofNullable(getter.apply(instance)), value -> setter.accept(instance, JDate.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only date-time property.
     * @param name property name
     * @param getter property getter
     */
    public void addDateTimeProperty(final String name, final Function<JanitorWrapper<T>, LocalDateTime> getter) {
        map.put(name, (instance, runningScript) -> JDateTime.ofNullable(getter.apply(instance)));
    }

    /**
     * Adds a read-write date-time property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateTimeProperty(final String name, final Function<JanitorWrapper<T>, LocalDateTime> getter, final BiConsumer<JanitorWrapper<T>, LocalDateTime> setter) {
        map.put(name, (instance, runningScript) -> new TemporaryAssignable(JDateTime.ofNullable(getter.apply(instance)), value -> setter.accept(instance, JDateTime.require(runningScript, value).janitorGetHostValue())));
    }

    /**
     * Adds a read-only object property.
     * @param name property name
     * @param getter property getter
     */
    public void addObjectProperty(final String name, final Function<JanitorWrapper<T>, JanitorObject> getter) {
        map.put(name, (instance, runningScript) -> getter.apply(instance));
    }

    /**
     * Adds a read-write object property.
     * @param name property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addObjectProperty(final String name, final Function<JanitorWrapper<T>, JanitorObject> getter, final BiConsumer<JanitorWrapper<T>, JanitorObject> setter) {
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
        final AttributeLookupHandler<JanitorWrapper<T>> handler = map.get(name);
        if (handler != null) {
            //noinspection unchecked
            return handler.lookupAttribute((JanitorWrapper<T>) instance, runningScript);
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
    public JanitorObject dispatch(final JanitorWrapper<T> instance, final JanitorScriptProcess process, final String name) {
        final AttributeLookupHandler<JanitorWrapper<T>> handler = map.get(name);
        if (handler != null) {
            return handler.lookupAttribute(instance, process);
        }
        return null;
    }

}
