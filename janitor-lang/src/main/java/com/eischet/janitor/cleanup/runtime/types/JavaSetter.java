package com.eischet.janitor.cleanup.runtime.types;

@FunctionalInterface
public interface JavaSetter<T, V> {
    void setJavaValue(T instance, V value);
}
