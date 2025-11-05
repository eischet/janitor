package com.eischet.janitor.orm.dao;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoiner;
import com.eischet.janitor.orm.meta.EntityWrangler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GenericJoinManager<
        J extends OrmJoiner<L, R>,
        L extends OrmEntity,
        R extends OrmEntity,
        U extends Uplink> implements JoinManager<J, L, R> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final DispatchTable<J> dispatch;
    protected final EntityWrangler<L, U> leftWrangler;
    protected final EntityWrangler<R, U> rightWrangler;
    protected final @NotNull String className;
    protected final @NotNull String tableName;
    protected final @NotNull String idColumn;
    protected final Map<String, String> columnForField = new HashMap<>();
    protected final Map<String, String> fieldForColumn = new HashMap<>();
    protected final @NotNull @Unmodifiable List<String> columns;

    public GenericJoinManager(final DispatchTable<J> dispatch, final EntityWrangler<L, U> leftWrangler, final EntityWrangler<R, U> rightWrangler) {
        this.dispatch = dispatch;
        this.leftWrangler = leftWrangler;
        this.rightWrangler = rightWrangler;

        this.className = Objects.requireNonNull(dispatch.getMetaData(Janitor.MetaData.CLASS), "missing required CLASS");
        this.tableName = Objects.requireNonNull(dispatch.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "missing required TABLE_NAME");
        this.idColumn = Objects.requireNonNull(dispatch.getMetaData(JanitorOrm.MetaData.ID_FIELD), "missing required ID_FIELD");

        final List<String> databaseBackedFields = new ArrayList<>();
        final List<String> allFields = dispatch.streamAttributeNames().toList();
        for (final String field : allFields) {
            @Nullable final String columnName = dispatch.getMetaData(field, JanitorOrm.MetaData.COLUMN_NAME);
            if (columnName != null && !columnName.isBlank()) {
                columnForField.put(field, columnName);
                fieldForColumn.put(columnName, field);
                databaseBackedFields.add(columnName);
            }
        }
        this.columns = List.copyOf(databaseBackedFields);

    }

    @Override
    public List<J> getRightJoins(final L left) {
        return List.of();
    }

    @Override
    public List<J> getLeftJoins(final R right) {
        return List.of();
    }
}
