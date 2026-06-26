package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.janitor.orm.dao.Dao;
import org.jetbrains.annotations.NotNull;

/**
 * Addon-Interface for OrmEntity implementations that want to track changes of individual fields in UPDATE statements.
 * <p>
 *     By default, we update the whole table row when {@link Dao#update(DatabaseConnection, OrmEntity)} is called.
 *     When we want finer control over the update process, i.e. usually to update only the rows that actually changed,
 *     we can mix in this interface and use a {@link GenericChangeTrackedDao} as our Dao implementation.
 * </p>
 * <p>
 *     The {@link GenericChangeTrackedDao} will use the {@link ChangeTracker} to determine which fields
 *     have changed and only update those fields.
 * </p>
 */
public interface ChangeTrackedOrmEntity extends OrmEntity {

    /**
     * Retrieve the change tracker currently associated with this entity.
     * This should only ever by called by the {@link GenericChangeTrackedDao} implementation and by the {@link ChangeTrackedOrmEntity} implementation.
     * @return the change tracker currently associated with this entity
     */
    @NotNull ChangeTracker getChangeTracker();

    /**
     * Set the change tracker currently associated with this entity.
     * This should only every be called by the {@link GenericChangeTrackedDao} implementation.
     * @param changeTracker the change tracker currently associated with this entity
     */
    void setChangeTracker(@NotNull ChangeTracker changeTracker);

}
