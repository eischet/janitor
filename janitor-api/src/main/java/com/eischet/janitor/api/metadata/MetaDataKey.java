package com.eischet.janitor.api.metadata;

import java.util.Objects;

public class MetaDataKey<T> {

    private final String name;
    private final Class<T> type;

    public MetaDataKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AttributeKey{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final MetaDataKey<?> that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
