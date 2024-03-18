package com.eischet.janitor.runtime.types;

@FunctionalInterface
public interface JavaGetter<T, V> {
    V getJavaValue(T instance);
}
