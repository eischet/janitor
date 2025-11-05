package com.eischet.janitor.orm.entity;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    public boolean isLoaded() {
        return loaded;
    }

    public Stream<T> stream() {
        return readList().stream();
    }

    public JoinedList<T, U, V, W> lazyLoad() {
        if (!loaded) {
            final U uplink = uplinkSupplier.get();
            final V dao = daoRetriever.apply(uplink);
            @NotNull @Unmodifiable final List<T> items = dao.callLazyTransaction(conn -> loader.load(conn, dao));
            ensureList().addAll(items);
            loaded = true;
        }
        return this;
    }

    private void scriptAdd(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final U uplink = uplinkSupplier.get();
        final V dao = daoRetriever.apply(uplink);
        final T entity = dao.convertToEntity(process, args, parent);

        loaded = false;

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

    public Stream<T> getFullJoinedObjects() {
        return list == null ? Stream.empty() : list.stream();
    }

    public Stream<ForeignKey<W>> getMainJoinedObjects() {
        return getFullJoinedObjects().map(plucker);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }


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
    }



}
