package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@FunctionalInterface
public interface JoinLoader<T, V> {
    @NotNull @Unmodifiable
    List<T> load(final @NotNull DatabaseConnection conn, V dao) throws DatabaseError;
}
