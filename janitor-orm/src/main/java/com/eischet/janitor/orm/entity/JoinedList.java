// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorAware;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.ref.ForeignKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A lazily loaded list of the join records that connect a parent entity to other entities, i.e. a many-to-many association.
 * @param <T> the type of the join records
 * @param <U> the type of the uplink
 * @param <V> the type of the DAO for the join records
 * @param <W> the type of the entities on the other side of the join
 */
public class JoinedList<T extends OrmJoined, U extends Uplink, V extends JoinDao<T>, W extends OrmEntity> implements JanitorAware {

    protected final @NotNull JoinedList.JanitorJoinedList companion = new JanitorJoinedList(this);
    protected final Class<T> entityClass;
    protected final OrmEntity parent;
    private final Supplier<U> uplinkSupplier;
    private final Function<U, V> daoRetriever;
    private final JoinLoader<T, V> loader;
    private final Function<T, ForeignKey<W>> plucker;
    protected @Nullable List<T> list;
    protected boolean loaded = false;

    public JoinedList(final OrmEntity parent,
                      final Class<T> entityClass,
                      final Supplier<U> uplinkSupplier,
                      final Function<U, V> daoRetriever,
                      final JoinLoader<T, V> loader,
                      final Function<T, ForeignKey<W>> plucker) {
        this.parent = parent;
        this.entityClass = entityClass;
        this.uplinkSupplier = uplinkSupplier;
        this.daoRetriever = daoRetriever;
        this.loader = loader;
        this.plucker = plucker;
    }


    /**
     * @return true if the join records were loaded from the database
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return a stream of a copy of the join records that are in the list now; this does not load them from the database
     */
    public Stream<T> stream() {
        return readList().stream();
    }

    /**
     * Loads the join records from the database, unless that has already happened.
     * @return this list
     */
    public JoinedList<T, U, V, W> lazyLoad() {
        if (!loaded) {
            final U uplink = uplinkSupplier.get();
            final V dao = daoRetriever.apply(uplink);
            dao.callLazyTransaction(conn -> load(conn, dao));
        }
        return this;
    }

    /**
     * Loads this join collection through an already active connection. This is useful for callers
     * that need several lazy values to participate in one surrounding transaction.
     */
    public JoinedList<T, U, V, W> load(final @NotNull DatabaseConnection connection) throws DatabaseError {
        if (!loaded) {
            final U uplink = uplinkSupplier.get();
            load(connection, daoRetriever.apply(uplink));
        }
        return this;
    }

    private Void load(final @NotNull DatabaseConnection connection, final @NotNull V dao) throws DatabaseError {
        @NotNull @Unmodifiable final List<T> items = loader.load(connection, dao);
        ensureList().addAll(items);
        loaded = true;
        return null;
    }

    /** Replaces the complete in-memory join collection without triggering a database load. */
    public void replaceAll(final @NotNull Collection<? extends T> entities) {
        ensureList().clear();
        ensureList().addAll(entities);
        loaded = true;
    }



    private void scriptAdd(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final U uplink = uplinkSupplier.get();
        final V dao = daoRetriever.apply(uplink);
        final T entity = dao.convertToEntity(process, args, parent);
        dao.insertForScript(process, JCallArgs.ofSingleArgument(process, entity));
        loaded = false; // simply lazy-load again if needed
    }


    /**
     * @return the list of join records, which is created if it does not exist yet
     */
    protected @NotNull List<T> ensureList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * @return a copy of the list of join records, to protect against concurrent modification
     */
    protected @NotNull @Unmodifiable List<T> readList() {
        // copy the existing list to prevent concurrent modification exceptions
        return list == null ? Collections.emptyList() : List.copyOf(list);
    }

    /**
     * Adds a join record to the list in memory; this does not save it in the database.
     * @param entity the join record
     */
    public void add(T entity) {
        ensureList().add(entity);
    }

    /** Removes all join records from the list in memory; this does not delete them in the database. */
    public void clear() {
        if (list != null) {
            list.clear();
        }
    }

    /**
     * Adds a join record of unknown type to the list.
     * @param entity the join record, which must be of the join record class of this list
     */
    protected void addGeneric(JanitorObject entity) {
        add(entityClass.cast(entity));
    }

    /**
     * @return the number of join records that are in the list now
     */
    public int size() {
        return list == null ? 0 : list.size();
    }

    @Override
    public JanitorObject asJanitorObject() {
        return companion;
    }

    /**
     * @return true if there are no join records in the list now
     */
    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    /**
     * @return the join records
     */
    public Stream<T> getFullJoinedObjects() {
        return list == null ? Stream.empty() : list.stream();
    }

    /**
     * @return the references to the entities on the other side of the join, one for each join record
     */
    public Stream<ForeignKey<W>> getMainJoinedObjects() {
        return getFullJoinedObjects().map(plucker);
    }

    /**
     * @return the class of the join records
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }


    /** The script view of a {@link JoinedList}, which offers the methods size and add. */
    protected static class JanitorJoinedList extends JanitorComposed<JanitorJoinedList> implements JIterable {
        public static DispatchTable<JanitorJoinedList> DISPATCH = new DispatchTable<>();

        static {
            //DISPATCH.addBuilderMethod("add", (self, process, args) -> self.parent.addGeneric(args.get(0)));
            DISPATCH.addMethod("size", (self, process, args) -> Janitor.integer(self.parent.size()));
            //DISPATCH.addBuilderMethod("clear", (self, process, args) -> self.parent.clear());
            DISPATCH.addBuilderMethod("add", (self, process, args) -> self.parent.scriptAdd(process, args));

        }

        protected final JoinedList<?, ?, ?, ?> parent;

        public JanitorJoinedList(final @NotNull JoinedList<?, ?, ?, ?> parent) {
            super(DISPATCH);
            this.parent = parent;
        }

        @Override
        public Iterator<? extends JanitorObject> getIterator() {
            return parent.readList().iterator();
        }

        @Override
        public String toString() {
            return parent.toString();
        }

        @Override
        public boolean janitorIsTrue() {
            return !parent.isEmpty();
        }
    }

    /**
     * @return a short description of this list, including whether it was loaded
     */
    public String toString() {
        return "JoinedList<" + entityClass.getSimpleName() + ">(" + (loaded && list != null ? list.size() : "not loaded yet") + ")";
    }



}
