// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.interop.NullableGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes the DAOs of a collection properties of its dispatch table, so that scripts reach them by name, e.g.
 * {@code shop.Order}, and editors can list them.
 * <p>
 * Declare them once, in the static initializer that sets up the dispatch table, through the collection's getters:
 * {@code DAOS.add("ShopOrder", ShopDaoCollection::getOrderDao)}. A name that starts with the prefix of the collection is
 * also offered without it ({@code Order}). Names that the table already has, e.g. methods of the collection, win. A
 * getter may return null, e.g. for a DAO that a version of the database does not have; scripts then see {@code null}.
 * </p>
 * <p>
 * Each property carries the dispatcher of its DAOs (see {@link GenericDao#dispatcherFor} and {@link JoinDao#dispatcherFor}),
 * which editors use to suggest their members. {@link #undeclared} finds registered DAOs that were forgotten here.
 * </p>
 *
 * @param <S> the collection
 */
public final class DaoProperties<S extends OrmDaoCollection<S>> {


    private final DispatchTable<S> dispatch;
    private final String prefix;
    private final Set<String> declared = new LinkedHashSet<>();

    /**
     * @param dispatch the dispatch table of the collection
     * @param prefix   the prefix of the entity names that may be left out, e.g. "Shop"; empty for none
     */
    public DaoProperties(final @NotNull DispatchTable<S> dispatch, final @NotNull String prefix) {
        this.dispatch = dispatch;
        this.prefix = prefix;
    }

    /**
     * Declares the DAO of an entity or join table.
     *
     * @param name       the entity's class name, under which the DAO registers itself, e.g. "ShopOrder"
     * @param getter     the collection's getter of the DAO
     * @param dispatcher the dispatcher of the DAO, e.g. {@code GenericDao.dispatcherFor(OrderDao.DISPATCH, Order.DISPATCH)}
     * @return this, for chained calls
     */
    @SuppressWarnings("unchecked")
    public @NotNull DaoProperties<S> add(final @NotNull String name, final @NotNull NullableGetter<S, ? extends JanitorObject> getter,
                                         final @NotNull Dispatcher<?> dispatcher) {
        final NullableGetter<S, JanitorObject> dao = (NullableGetter<S, JanitorObject>) getter;
        final Dispatcher<JanitorObject> type = (Dispatcher<JanitorObject>) dispatcher;
        final Set<String> existing = dispatch.streamAttributeNames().collect(Collectors.toSet());
        final List<String> names = !prefix.isEmpty() && name.startsWith(prefix) && name.length() > prefix.length()
                ? List.of(name, name.substring(prefix.length()))
                : List.of(name);
        for (final String scriptName : names) {
            if (!existing.contains(scriptName)) {
                dispatch.addObjectProperty(scriptName, dao, type)
                        .setMetaData(Janitor.MetaData.HELP, scriptName + ": The DAO of " + name + ".");
            }
        }
        declared.add(name);
        return this;
    }

    /**
     * Finds the DAOs that registered themselves in a collection but were not declared here; scripts cannot reach those.
     * Collections call this at the end of their constructor and log the result.
     *
     * @param collection a collection, after its DAOs were created
     * @return the names of their entities and join tables, sorted
     */
    public @NotNull @Unmodifiable List<String> undeclared(final @NotNull S collection) {
        return Stream.concat(collection.getEntityNames().stream(), collection.getJoinNames().stream())
                .filter(name -> !declared.contains(name))
                .sorted()
                .toList();
    }
}
