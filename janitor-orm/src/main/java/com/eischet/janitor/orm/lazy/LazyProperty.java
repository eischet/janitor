// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.lazy;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.SimpleResultSet;

import java.sql.SQLException;

/**
 * A property whose value is loaded from the database only when it is first needed.
 * @param <T> the type of the value
 */
public abstract class LazyProperty<T> {
    private final String propertyName;
    private T value;
    private boolean loaded;

    public LazyProperty(final String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the value, which is only meaningful if the property was loaded
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value the value
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * @return true if the value was loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Sets whether the value was loaded.
     * @param loaded true if the value was loaded
     */
    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * @return the name of the property
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Reads the value from the current row of a result set.
     * @param resultSet the result set
     * @return the value
     * @throws SQLException on database errors
     */
    public abstract T readFromResultSet(final SimpleResultSet resultSet) throws SQLException;

}
