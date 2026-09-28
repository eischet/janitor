package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.orm.dao.GenericDao;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * A single lazily-loaded text field, typically backed by a large (CLOB/NCLOB) column. Registered via
 * {@link OrmObject#addLazyTextProperty}, this is what an entity class field holds instead of a plain
 * {@code String} for a property that's expensive to load and often not actually needed — e.g. a
 * {@code configJson} blob that only a handful of call sites ever read.
 * <p>
 * An entity using this declares the field once, at construction time, e.g.:
 * <pre>{@code
 * protected final LazyLoadedString configJson = new LazyLoadedString(
 *         () -> getSource().getFooDao(), this::getId, "config_json");
 *
 * public String getConfigJson() { return configJson.getValue(); }
 * public void setConfigJson(final String value) { configJson.setValue(value); }
 * }</pre>
 * The entity's own getter/setter keep their ordinary {@code String} signature — callers on the Java side
 * and on the script side (via {@link OrmObject#addLazyTextProperty}) never see this class directly.
 * <p>
 * {@link GenericDao} excludes {@link com.eischet.janitor.orm.JanitorOrm.MetaData#LAZY_LOAD}-marked columns
 * from its default SELECT column list, so a freshly loaded entity's {@code LazyLoadedString} fields start
 * out unresolved; the first {@link #getValue()} call fetches the column with its own, separate query (see
 * {@link GenericDao#fetchLazyColumn}), the same way {@code TransmissionAttachment.getData()} already
 * lazily fetches its BLOB via a dedicated DAO query. From then on, the value is cached for the lifetime of
 * this instance. This only benefits read paths that don't touch the field: {@code update()} still writes
 * whatever the entity's getter currently returns, which resolves this if it hasn't been already — a lazy
 * field that's never read stays unread, but one that's about to be written back gets loaded first, same as
 * it would without laziness, just deferred to write time instead of read time.
 */
public final class LazyLoadedString {

    private final Supplier<GenericDao<?, ?>> daoSupplier;
    private final LongSupplier idSupplier;
    private final String column;

    private @Nullable String value;
    private boolean loaded;

    /**
     * @param daoSupplier retrieves the DAO to query when this field needs to be fetched (typically
     *                    {@code () -> getSource().getFooDao()} in the entity class)
     * @param idSupplier  retrieves the owning entity's ID at fetch time (typically {@code this::getId})
     * @param column      the database column this field is lazily backed by
     */
    public LazyLoadedString(final Supplier<GenericDao<?, ?>> daoSupplier, final LongSupplier idSupplier, final String column) {
        this.daoSupplier = daoSupplier;
        this.idSupplier = idSupplier;
        this.column = column;
    }

    /**
     * Returns the current value, fetching it from the database first if it hasn't been loaded or set yet.
     * An entity that hasn't been persisted yet (ID {@code 0}, the sentinel every new, not-yet-inserted
     * {@link OrmEntity} carries) is never queried — there's nothing to fetch, so this simply returns
     * whatever's already held in memory (typically {@code null} until explicitly set). A negative ID is
     * NOT treated as "not yet persisted" — some existing data legitimately uses negative IDs, and those
     * rows are fetchable like any other.
     */
    public @Nullable String getValue() {
        ensureLoaded();
        return value;
    }

    /**
     * Sets the value directly, without ever fetching the old one from the database (e.g. an entity built
     * from a script/JSON payload that's about to be inserted, not loaded, doesn't need a round trip just
     * to overwrite the field).
     */
    public void setValue(final @Nullable String value) {
        this.value = value;
        this.loaded = true;
    }

    /**
     * @return whether this field's value is already available in memory, without triggering a fetch.
     */
    public boolean isLoaded() {
        return loaded;
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        final long id = idSupplier.getAsLong();
        if (id == 0) {
            loaded = true;
            return;
        }
        try {
            value = daoSupplier.get().fetchLazyColumn(id, column);
        } catch (DatabaseError e) {
            throw new JanitorError("error lazily loading column '" + column + "' for id " + id, e);
        }
        loaded = true;
    }

}
