package com.eischet.janitor.api.metadata;

import java.util.Objects;

/**
 * A typed, named key for meta-data entries.
 * @param <T> the type of the value stored under this key
 */
public class MetaDataKey<T> {

    private final String name;
    private final Class<T> type;

    public MetaDataKey(String name, Class<?> type) {
        this.name = name;
        //noinspection unchecked
        this.type = (Class<T>) type;
    }

    /**
     * @return the name of this key
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type of the values stored under this key
     */
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
