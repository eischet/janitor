// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The hook through which the host decides who may do what, and through which it takes part in what is written. The API itself knows nothing
 * about users, roles or tokens: it hands the user of the request, whatever type the host uses for that (a person, a name, a token), to the
 * policy, which can be written in a few lines for whatever model the host uses.
 * <p>
 * The user comes from the HTTP binding's resolver; it is {@code null} for an anonymous caller, which the policy has to cope with.
 * Policies are consulted before the database is touched, except for the hooks that run when something is written ({@link #beforeWrite} and
 * {@link #beforeLinkWrite}). Implementations must be thread-safe.
 * </p>
 * <p>
 * As a {@link RestSecurityDescription}, a policy can also tell the API description what it takes to call an operation. That is only a
 * description for tools and clients; the decision is always made by {@link #permits}.
 * </p>
 *
 * @param <U> the type of the user, e.g. the class of the people that the host knows
 */
public interface RestPolicy<U> extends RestSecurityDescription {

    /**
     * Decides whether a request may do an operation on an entity at all.
     *
     * @param user      the user of the request, or null if there is none
     * @param entity    the entity
     * @param operation the operation
     * @return true to allow it; false makes the API answer with 401 if there is no user (the caller can try again after authenticating),
     * and with 403 if there is one
     */
    boolean permits(@Nullable U user, @NotNull ApiEntity entity, @NotNull RestOperation operation);

    /**
     * Decides whether a request may do an operation on the links of an entity, i.e. on the records of a join table below it: {@code LIST} and
     * {@code GET} read them, {@code CREATE} adds or changes one, {@code DELETE} removes one. The restrictions of both entities that a link
     * connects apply as usual.
     * <p>
     * By default, links are part of the entity that they are below: reading them needs {@code GET} on it, changing them {@code UPDATE}
     * ("managing the groups of a user is changing the user").
     * </p>
     *
     * @param user      the user of the request, or null if there is none
     * @param link      the link
     * @param operation the operation
     * @return true to allow it; false makes the API answer with 403
     */
    default boolean permitsLink(@Nullable U user, @NotNull ApiLink link, @NotNull RestOperation operation) {
        return permits(user, link.parent(), operation.isRead() ? RestOperation.GET : RestOperation.UPDATE);
    }

    /**
     * Decides whether a request may get the API description itself ({@code /openapi.json}), which lists everything that is exposed.
     *
     * @param user the user of the request, or null if there is none
     * @return true to allow it, which is the default; false makes the API answer with 401 or 403
     */
    default boolean permitsApiDescription(@Nullable U user) {
        return true;
    }

    /**
     * Restricts which rows a request can see, e.g. to those of its own tenant. The restriction is combined with the filter of the request
     * (and applies to getting a single entity, which then looks like "not found" if it is outside the restriction).
     *
     * @param user   the user of the request, or null if there is none
     * @param entity the entity
     * @return a filter that every visible row must match, or null for no restriction. It may only use properties that the API exposes.
     */
    default @Nullable FilterExpression restriction(@Nullable U user, @NotNull ApiEntity entity) {
        return null;
    }

    /**
     * Whether the policy checks the entities of a kind one by one ({@link #reveal}), for rules that a filter cannot express, e.g. a script that
     * decides per record. Lists then work differently: they check at most {@link #scanLimit} candidates (those that match the filter and the
     * {@link #restriction}, in the order of the request), so that their {@code total} counts only the visible ones among those, and
     * {@code truncated} says whether there were more candidates.
     *
     * @param user   the user of the request, or null if there is none
     * @param entity the entity
     * @return true to have every entity of the kind checked with {@link #reveal}; false (the default) to rely on {@link #restriction} alone
     */
    default boolean checksEachEntity(@Nullable U user, @NotNull ApiEntity entity) {
        return false;
    }

    /**
     * Checks one entity for a request, if {@link #checksEachEntity} says so: wherever the API would show it (getting it, in a list, as the
     * parent of a sub-resource or as an end of a link) and before it is changed or deleted. An entity that is hidden looks like "not found".
     * The policy can also show a changed copy instead, e.g. with some text blanked out; it is only shown, never written.
     *
     * @param user     the user of the request, or null if there is none
     * @param entity   the entity
     * @param instance the entity as loaded, inside of the {@link #restriction}
     * @param conn     the connection of the request
     * @return the entity to show (the instance itself, or a copy), or null to hide it
     * @throws DatabaseError on database errors
     */
    default @Nullable OrmEntity reveal(@Nullable U user, @NotNull ApiEntity entity, @NotNull OrmEntity instance, @NotNull DatabaseConnection conn) throws DatabaseError {
        return instance;
    }

    /**
     * How many candidates a list checks at most for entities that are checked one by one ({@link #checksEachEntity}).
     *
     * @param user   the user of the request, or null if there is none
     * @param entity the entity
     * @return the largest number of candidates to load and check for one request
     */
    default int scanLimit(@Nullable U user, @NotNull ApiEntity entity) {
        return 1000;
    }

    /**
     * Hides properties from a request: they are left out of the output, and filtering or sorting by them is rejected.
     *
     * @param user   the user of the request, or null if there is none
     * @param entity the entity
     * @return the names of the properties to hide
     */
    default @NotNull Set<String> deniedFields(@Nullable U user, @NotNull ApiEntity entity) {
        return Set.of();
    }

    /**
     * Called when an entity is about to be written because of a request, in the transaction of the request: after the body was applied to the
     * entity and checked, immediately before it is inserted, updated or deleted. This is the place to add what only the host knows, e.g. to
     * record the user who made the change: whatever this method changes on the entity is written with it. It is also the place to refuse a
     * change that the generic rules cannot judge, by throwing a {@link RestRejection}, which rolls the request back.
     * <p>
     * It is called for {@link RestOperation#CREATE} (also for entities that are created below a parent), {@link RestOperation#UPDATE}
     * (PUT and PATCH) and {@link RestOperation#DELETE}. The entity is never one that is shared with a cache: it is new, or was just loaded
     * for this request.
     * </p>
     *
     * @param user      the user of the request, or null if there is none
     * @param entity    the description of the entity, e.g. to tell which kind it is
     * @param operation what is about to happen
     * @param instance  the entity that will be written
     * @param conn      the connection of the transaction of the request, e.g. to look something up or to write an audit record
     * @throws RestRejection if the request is to be refused
     * @throws DatabaseError on database errors
     */
    default void beforeWrite(@Nullable U user, @NotNull ApiEntity entity, @NotNull RestOperation operation, @NotNull OrmEntity instance, @NotNull DatabaseConnection conn) throws DatabaseError {
    }

    /**
     * Like {@link #beforeWrite}, for the records of a join table that are written because of a request: {@link RestOperation#CREATE} when a link
     * is created or changed (PUT), {@link RestOperation#DELETE} when it is deleted.
     *
     * @param user      the user of the request, or null if there is none
     * @param link      the link
     * @param operation what is about to happen
     * @param row       the join record that will be written
     * @param conn      the connection of the transaction of the request
     * @throws RestRejection if the request is to be refused
     * @throws DatabaseError on database errors
     */
    default void beforeLinkWrite(@Nullable U user, @NotNull ApiLink link, @NotNull RestOperation operation, @NotNull OrmJoined row, @NotNull DatabaseConnection conn) throws DatabaseError {
    }

    /**
     * @param <U> the type of the user
     * @return a policy that permits everything; fine for tests and for APIs that are protected otherwise, e.g. by the network
     */
    static <U> @NotNull RestPolicy<U> allowAll() {
        return (user, entity, operation) -> true;
    }

    /**
     * @param <U> the type of the user
     * @return a policy that permits nothing; a starting point for policies that grant selectively
     */
    static <U> @NotNull RestPolicy<U> denyAll() {
        return (user, entity, operation) -> false;
    }
}
