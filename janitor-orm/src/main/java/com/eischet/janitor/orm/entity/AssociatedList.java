// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorAware;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.meta.EntityWrangler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A lazily loaded list of the entities that refer to a parent entity through a foreign key column, i.e. a one-to-many association.
 * The entities are loaded from the database when they are first needed.
 * @param <T> the type of the associated entities
 * @param <U> the type of the uplink
 */
public class AssociatedList<T extends OrmEntity, U extends Uplink> implements Associated<T>, JanitorAware {

    protected final @NotNull JanitorAssociatedList companion = new JanitorAssociatedList(this);
    protected final Class<T> entityClass;
    protected final EntityWrangler<T, U> wrangler;
    protected final Supplier<U> uplinkSupplier;
    protected final String foreignKeyColumn;
    protected final OrmEntity parent;
    protected volatile @Nullable List<T> list;
    protected volatile boolean loaded = false;
    protected @Nullable Comparator<? super T> sorter;

    public AssociatedList(final OrmEntity parent,
                          final Class<T> entityClass,
                          final String foreignKeyColumn,
                          final EntityWrangler<T, U> wrangler,
                          final Supplier<U> uplinkSupplier,
                          final Comparator<? super T> sorter) {
        this.parent = parent;
        this.foreignKeyColumn = foreignKeyColumn;
        this.entityClass = entityClass;
        this.wrangler = wrangler;
        this.uplinkSupplier = uplinkSupplier;
        this.sorter = sorter;
    }

    public AssociatedList(final OrmEntity parent,
                          final Class<T> entityClass,
                          final String foreignKeyColumn,
                          final EntityWrangler<T, U> wrangler,
                          final Supplier<U> uplinkSupplier) {
        this(parent, entityClass, foreignKeyColumn, wrangler, uplinkSupplier, null);
    }

    /**
     * @return true if the entities were loaded from the database
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return a stream of a copy of the entities that are in the list now; this does not load them from the database
     */
    public Stream<T> stream() {
        return readList().stream();
    }

    /**
     * Loads the entities from the database, unless that has already happened.
     * Safe to call from several threads at once, e.g. on a parent entity that lives in a cache: only one of them loads, and the new list
     * is built aside and then replaces the old one, so that readers on other threads never see it half filled.
     * @return this list
     */
    public AssociatedList<T, U> lazyLoad() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    final U uplink = uplinkSupplier.get();
                    @NotNull final Dao<T> dao = wrangler.retrieveDao(uplink);
                    final List<T> items = new ArrayList<>(dao.lazyLoadByAssociation(foreignKeyColumn, parent));
                    if (sorter != null) {
                        items.sort(sorter);
                    }
                    final List<T> loadedList = new ArrayList<>(readList());
                    loadedList.addAll(items);
                    list = loadedList;
                    loaded = true;
                }
            }
        }
        return this;
    }

    /**
     * @return the list of entities, which is created if it does not exist yet
     */
    protected @NotNull List<T> ensureList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * @return a copy of the list of entities, to protect against concurrent modification
     */
    protected @NotNull @Unmodifiable List<T> readList() {
        // copy the existing list to prevent concurrent modification exceptions
        final List<T> current = list;
        return current == null ? Collections.emptyList() : List.copyOf(current);
    }

    /**
     * Adds an entity to the list in memory; this does not save it in the database.
     * @param entity the entity
     */
    public void add(T entity) {
        ensureList().add(entity);
    }

    /** Removes all entities from the list in memory; this does not delete them in the database. */
    public void clear() {
        if (list != null) {
            list.clear();
        }
    }

    /**
     * Adds an entity of unknown type to the list, as called from scripts.
     * @param entity the entity, which must be of the entity class of this list
     */
    protected void addGeneric(JanitorObject entity) {
        add(entityClass.cast(entity));
    }

    /**
     * @return the number of entities that are in the list now
     */
    public int size() {
        return list == null ? 0 : list.size();
    }

    @Override
    public JanitorObject asJanitorObject() {
        return companion;
    }

    /**
     * @return the class of the associated entities
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * @return the wrangler of the associated entities
     */
    public EntityWrangler<T, ?> getWrangler() {
        return wrangler;
    }

    /** The script view of an {@link AssociatedList}, which offers methods such as add, size and clear. */
    protected static class JanitorAssociatedList extends JanitorComposed<JanitorAssociatedList> implements JIterable {
        public static DispatchTable<JanitorAssociatedList> DISPATCH = new DispatchTable<>();
        static {
            DISPATCH.addBuilderMethod("add", (self, process, args) -> self.parent.addGeneric(args.get(0)))
                    .setMetaData(Janitor.MetaData.HELP, "add(entity): Adds the entity to the list in memory without saving it in the database; returns the list.");
            DISPATCH.addMethod("size", (self, process, args) -> Janitor.integer(self.parent.size()))
                    .setMetaData(Janitor.MetaData.HELP, "size(): Returns the number of entities currently in the list.");
            DISPATCH.addBuilderMethod("clear", (self, process, args) -> self.parent.clear())
                    .setMetaData(Janitor.MetaData.HELP, "clear(): Removes all entities from the list in memory without deleting them in the database; returns the list.");
            DISPATCH.addMethod("asList", (self, process, args) -> Janitor.list(self.parent.readList()))
                    .setMetaData(Janitor.MetaData.HELP, "asList(): Returns a copy of the entities currently in the list as a plain list.");
        }
        protected final @NotNull AssociatedList<?, ?> parent;

        public JanitorAssociatedList(final @NotNull AssociatedList<?, ?> parent) {
            super(DISPATCH);
            this.parent = parent;
        }

        @Override
        public Iterator<? extends JanitorObject> getIterator() {
            return parent.readList().iterator();
        }
    }



}
