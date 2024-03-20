package com.eischet.janitor.cleanup.runtime.types;

@FunctionalInterface
public interface JavaGetter<T, V> {
    V getJavaValue(T instance);
}
