package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JSet;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.Set;

/**
 * Operations for Set objects.
 */
public class JSetClass {

    /**
     * Script method {@code set.add(x)}: adds an element to this set.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the set did not contain the element yet
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __add(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.add(arguments.require(1).get(0)));
    }

    /**
     * Script method {@code set.remove(x)}: removes an element from this set.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the set contained the element
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __remove(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.remove(arguments.require(1).get(0)));
    }

    /**
     * Script method {@code set.contains(x)}: checks whether this set contains an element.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the element was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __contains(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.contains(arguments.require(1).get(0)));
    }

    /**
     * Script method {@code set.toSet()}: creates a copy of this set.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the copy
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JSet __toSet(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().set(self.janitorGetHostValue().stream());
    }

    /**
     * Script method {@code set.toList()}: creates a list from the elements of this set.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the list
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __toList(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().list(self.janitorGetHostValue().stream());
    }

    /**
     * Script method {@code set.size()}: the number of elements in this set.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of elements
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt __size(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().integer(self.size());
    }

    /**
     * Script method {@code set.isEmpty()}: checks whether this set has no elements.
     * @param _self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the set is empty
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __isEmpty(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

    /**
     * Registers the standard methods and properties of sets.
     * @param setDispatcher the dispatch table to add them to
     */
    public static void applyDefaults(WrapperDispatchTable<Set<JanitorObject>> setDispatcher) {
        setDispatcher.addMethod("add", JSetClass::__add);
        setDispatcher.addMethod("remove", JSetClass::__remove);
        setDispatcher.addMethod("contains", JSetClass::__contains);
        setDispatcher.addMethod("toList", JSetClass::__toList);
        setDispatcher.addMethod("toSet", JSetClass::__toSet); // copies the set
        setDispatcher.addMethod("size", JSetClass::__size);
        setDispatcher.addMethod("isEmpty", JSetClass::__isEmpty);
    }

}
