package com.eischet.janitor.runtime.types;

@FunctionalInterface
public interface JavaSetter<T, V> {
    void setJavaValue(T instance, V value);
}
