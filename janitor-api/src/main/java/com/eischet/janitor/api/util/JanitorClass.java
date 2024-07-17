package com.eischet.janitor.api.util;

import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.scripting.DispatchTable;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A class object, representing a class of Janitor objects.
 * This is actually a "legacy" thing, and should probably not be used further.
 * The main issue here was that it's really hard to create "generic class hierarchies" in Java.
 * @see DispatchTable for a better solution.
 * @param <T> any type of Janitor object
 */
public class JanitorClass<T extends JanitorObject> {

    private final Map<String, JUnboundMethod<T>> methods;
    private final @Nullable JanitorClass<T> parentClass;
    private final Map<String, JUnboundMethod<T>> extensionMethods = new HashMap<>();

    /**
     * Create a new JanitorClass.
     * @param parentClass the parent class
     * @param methods the methods
     */
    public JanitorClass(final @Nullable JanitorClass<T> parentClass,
                        final Map<String, JUnboundMethod<T>> methods) {
        this.methods = methods;
        this.parentClass = parentClass;
    }

    /**
     * Get the methods.
     * @return the methods
     */
    public Map<String, JUnboundMethod<T>> getMethods() {
        return methods;
    }

    /**
     * Get the extension methods.
     * @return the extension methods
     * TODO: these should be removed and replaced with DispatchTable, where hosts can freely add methods and properties in a much simpler way
     */
    public Map<String, JUnboundMethod<T>> getExtensionMethods() {
        return extensionMethods;
    }

    /**
     * Get all methods.
     * @return all methods
     */
    public Map<String, JUnboundMethod<T>> getAllMethods() {
        final Map<String, JUnboundMethod<T>> allMethods = new HashMap<>(methods);
        allMethods.putAll(extensionMethods);
        return allMethods;
    }

    /**
     * Extend the class with a new method.
     * @param name the name
     * @param method the method
     */
    public void extend(final String name, final JUnboundMethod<T> method) {
        extensionMethods.put(name, method);
    }

    /**
     * Get the parent class.
     * @return the parent class
     */
    public @Nullable JanitorClass<T> getParentClass() {
        return parentClass;
    }

    /**
     * Get a bound method.
     * @param name the name
     * @param self the self
     * @return the bound method
     */
    public @Nullable JanitorObject getBoundMethod(final String name, final T self) {
        final JUnboundMethod<T> method = methods.get(name);
        if (method != null) {
            return new JBoundMethod<>(name, self, method);
        }
        // if (parentClass != null) {
        //    return parentClass.getBoundMethod(name, self);
        //}
        return null;
    }

}
