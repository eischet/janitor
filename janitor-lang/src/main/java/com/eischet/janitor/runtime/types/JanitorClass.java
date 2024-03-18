package com.eischet.janitor.runtime.types;

import com.eischet.janitor.api.types.JanitorObject;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.Nullable;

public class JanitorClass<T extends JanitorObject> {

    private final ImmutableMap<String, JUnboundMethod<T>> methods;
    private final @Nullable JanitorClass<T> parentClass;

    private final MutableMap<String, JUnboundMethod<T>> extensionMethods = Maps.mutable.empty();

    public JanitorClass(final @Nullable JanitorClass<T> parentClass,
                        final ImmutableMap<String, JUnboundMethod<T>> methods) {
        this.methods = methods;
        this.parentClass = parentClass;
    }

    public ImmutableMap<String, JUnboundMethod<T>> getMethods() {
        return methods;
    }

    public ImmutableMap<String, JUnboundMethod<T>> getExtensionMethods() {
        return extensionMethods.toImmutable();
    }

    public ImmutableMap<String, JUnboundMethod<T>> getAllMethods() {
        final MutableMap<String, JUnboundMethod<T>> allMethods = methods.toMap();
        allMethods.putAll(extensionMethods);
        return allMethods.toImmutable();
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
