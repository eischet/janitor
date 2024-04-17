package com.eischet.janitor.api.util;

import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JanitorClass<T extends JanitorObject> {

    private final Map<String, JUnboundMethod<T>> methods;
    private final @Nullable JanitorClass<T> parentClass;

    private final Map<String, JUnboundMethod<T>> extensionMethods = new HashMap<>();

    public JanitorClass(final @Nullable JanitorClass<T> parentClass,
                        final Map<String, JUnboundMethod<T>> methods) {
        this.methods = methods;
        this.parentClass = parentClass;
    }

    public Map<String, JUnboundMethod<T>> getMethods() {
        return methods;
    }

    public Map<String, JUnboundMethod<T>> getExtensionMethods() {
        return extensionMethods;
    }

    public Map<String, JUnboundMethod<T>> getAllMethods() {
        final Map<String, JUnboundMethod<T>> allMethods = new HashMap<>(methods);
        allMethods.putAll(extensionMethods);
        return allMethods;
    }

    public void extend(final String name, final JUnboundMethod<T> method) {
        extensionMethods.put(name, method);
    }

    public @Nullable JanitorClass<T> getParentClass() {
        return parentClass;
    }

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
