package com.eischet.janitor.orm.lazy;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.SimpleResultSet;

import java.sql.SQLException;

public abstract class LazyProperty<T> {
    private final String propertyName;
    private T value;
    private boolean loaded;

    public LazyProperty(final String propertyName) {
        this.propertyName = propertyName;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public abstract T readFromResultSet(final SimpleResultSet resultSet) throws SQLException;

}
