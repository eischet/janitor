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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AssociatedList<T extends OrmEntity, U extends Uplink> implements Associated<T>, JanitorAware {

    protected final @NotNull JanitorAssociatedList companion = new JanitorAssociatedList(this);
    protected final Class<T> entityClass;
    protected final EntityWrangler<T, U> wrangler;
    protected final Supplier<U> uplinkSupplier;
    protected final String foreignKeyColumn;
    protected final OrmEntity parent;
    protected @Nullable List<T> list;
    protected boolean loaded = false;

    public AssociatedList(final OrmEntity parent, final Class<T> entityClass, final String foreignKeyColumn, final EntityWrangler<T, U> wrangler, final Supplier<U> uplinkSupplier) {
        this.parent = parent;
        this.foreignKeyColumn = foreignKeyColumn;
        this.entityClass = entityClass;
        this.wrangler = wrangler;
        this.uplinkSupplier = uplinkSupplier;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Stream<T> stream() {
        return readList().stream();
    }

    public AssociatedList<T, U> lazyLoad() {
        if (!loaded) {
            final U uplink = uplinkSupplier.get();
            @NotNull final Dao<T> dao = wrangler.retrieveDao(uplink);
            @NotNull @Unmodifiable final List<T> items = dao.lazyLoadByAssociation(foreignKeyColumn, parent);
            ensureList().addAll(items);
            loaded = true;
        }
        return this;
    }


    protected @NotNull List<T> ensureList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    protected @NotNull @Unmodifiable List<T> readList() {
        // copy the existing list to prevent concurrent modification exceptions
        return list == null ? Collections.emptyList() : List.copyOf(list);
    }

    public void add(T entity) {
        ensureList().add(entity);
    }

    public void clear() {
        if (list != null) {
            list.clear();
        }
    }

    protected void addGeneric(JanitorObject entity) {
        add(entityClass.cast(entity));
    }

    public int size() {
        return list == null ? 0 : list.size();
    }

    @Override
    public JanitorObject asJanitorObject() {
        return companion;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public EntityWrangler<T, ?> getWrangler() {
        return wrangler;
    }

    protected static class JanitorAssociatedList extends JanitorComposed<JanitorAssociatedList> implements JIterable {
        public static DispatchTable<JanitorAssociatedList> DISPATCH = new DispatchTable<>();
        static {
            DISPATCH.addBuilderMethod("add", (self, process, args) -> self.parent.addGeneric(args.get(0)));
            DISPATCH.addMethod("size", (self, process, args) -> Janitor.integer(self.parent.size()));
            DISPATCH.addBuilderMethod("clear", (self, process, args) -> self.parent.clear());
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
