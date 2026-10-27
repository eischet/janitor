// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.AttributeLookupHandler;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OptimisticLockError;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.entity.OrmObject;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.MalformedExpression;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyIdentity;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.ref.ForeignKeySearchResult;
import com.eischet.janitor.orm.ref.ForeignKeyString;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Answers the requests of the REST API for the entities of an {@link OrmDaoCollection}, independent of any HTTP framework: an HTTP
 * binding turns its requests into {@link RestRequest}s and writes the {@link RestResponse}s back.
 * <p>
 * Routes (relative to wherever the binding mounts the handler), for each exposed entity with the path {@code /x}:
 * </p>
 * <ul>
 *     <li>{@code GET /x?filter=...&sort=...&page=...&size=...}: one page of entities and the total count</li>
 *     <li>{@code POST /x/search}: the same, with the parameters in a JSON body</li>
 *     <li>{@code GET /x/{id}} and {@code GET /x/by-key/{key}}: one entity, with {@code ETag} and {@code Last-Modified} if the entity has a
 *     version or modification date, and {@code 304} for conditional requests</li>
 *     <li>{@code POST /x}, {@code PUT /x/{id}}, {@code PATCH /x/{id}} and {@code DELETE /x/{id}} for entities that offer {@link RestOperation#CREATE},
 *     {@link RestOperation#UPDATE} and {@link RestOperation#DELETE} (see {@link RestOperations}): they answer with the stored entity (201 and 200) or
 *     204. If a request for an entity with a version column has an {@code If-Match} header, it is only carried out if it matches (else 412);
     *     entities can insist on the header (428 without it).</li>
 *     <li>{@code GET /openapi.json}: the API description</li>
 * </ul>
 * <p>
 * Every request runs in one database transaction. Long texts that are loaded lazily are only part of the answer for a single entity, not for
 * lists. All properties of the API are always written, so the output matches the schema of the API description.
 * </p>
 */
public final class OrmRestHandler<U> {

    private static final JanitorLogger log = JanitorLogger.getLogger(OrmRestHandler.class);

    private static final String JSON = "application/json";
    private static final String PROBLEM_JSON = "application/problem+json";

    private final @NotNull OrmDaoCollection<?> collection;
    private final @NotNull OrmApiModel model;
    private final @NotNull RestPolicy<? super U> policy;
    private final @NotNull OpenApiOptions options;
    private volatile @Nullable String openApiJson;

    /**
     * @param collection the DAOs to serve
     * @param model      the entities to expose, see {@link OrmApiModel#of}
     * @param policy     decides who may do what
     * @param options    for the API description
     */
    public OrmRestHandler(final @NotNull OrmDaoCollection<?> collection, final @NotNull OrmApiModel model, final @NotNull RestPolicy<? super U> policy, final @NotNull OpenApiOptions options) {
        this.collection = collection;
        this.model = model;
        this.policy = policy;
        this.options = options;
    }

    /**
     * @param collection the DAOs to serve
     * @param policy     decides who may do what
     * @return a handler for all exposed entities of the collection, with default options
     */
    public static <U> @NotNull OrmRestHandler<U> of(final @NotNull OrmDaoCollection<?> collection, final @NotNull RestPolicy<? super U> policy) {
        return new OrmRestHandler<>(collection, OrmApiModel.of(collection), policy, OpenApiOptions.defaults());
    }

    /** @return the entities that this handler serves */
    public @NotNull OrmApiModel getModel() {
        return model;
    }

    /**
     * Answers a request. This never throws: problems become responses with the matching status.
     * @param request the request
     * @return the response
     */
    public @NotNull RestResponse handle(final @NotNull RestRequest<U> request) {
        try {
            return route(request);
        } catch (RestException e) {
            return problem(e.status, e.getMessage(), e.headers);
        } catch (Rollback e) {
            return problem(e.problem.status, e.problem.getMessage(), e.problem.headers);
        } catch (MalformedExpression | JsonException e) {
            return problem(400, String.valueOf(e.getMessage()), Map.of());
        } catch (OptimisticLockError e) {
            return problem(412, "The entity was changed or deleted by someone else.", Map.of());
        } catch (DatabaseError e) {
            if (isConstraintViolation(e)) {
                return problem(409, "The request conflicts with existing data.", Map.of());
            }
            log.warn("database error while handling {} {}: {}", request.method(), request.path(), e.getMessage());
            return problem(500, "A database error occurred.", Map.of());
        } catch (RuntimeException e) {
            log.warn("error while handling {} {}: {}", request.method(), request.path(), e);
            return problem(500, "An internal error occurred.", Map.of());
        }
    }

    // --- routing ---

    private RestResponse route(final RestRequest<U> request) throws DatabaseError, JsonException {
        final List<String> segments = new ArrayList<>();
        for (final String segment : request.path().split("/")) {
            if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        if (segments.isEmpty()) {
            throw new RestException(404, "There is nothing here.");
        }
        if (segments.size() == 1 && segments.get(0).equals("openapi.json")) {
            requireMethod(request, List.of("GET"));
            if (!policy.permitsApiDescription(request.user())) {
                throw denial(request, null);
            }
            return new RestResponse(200, JSON, Map.of(), openApi());
        }
        final ApiEntity entity = model.findByPath(segments.get(0));
        if (entity == null) {
            throw new RestException(404, "There is no such resource: " + segments.get(0));
        }
        if (segments.size() == 1) {
            requireMethod(request, allowed(entity::allows, RestOperation.LIST, "GET", RestOperation.CREATE, "POST"));
            return request.method().equals("POST") ? create(request, entity, null) : list(request, entity, RestOperation.LIST, listParametersFromQuery(request), null);
        }
        if (segments.size() == 2 && segments.get(1).equals("search")) {
            requireMethod(request, allowed(entity::allows, RestOperation.SEARCH, "POST"));
            return list(request, entity, RestOperation.SEARCH, listParametersFromBody(request), null);
        }
        if (segments.size() == 2) {
            requireMethod(request, allowed(entity::allows, RestOperation.GET, "GET", RestOperation.UPDATE, "PUT", RestOperation.UPDATE, "PATCH", RestOperation.DELETE, "DELETE"));
            final long id = parseId(segments.get(1));
            return switch (request.method()) {
                case "PUT" -> update(request, entity, id, true);
                case "PATCH" -> update(request, entity, id, false);
                case "DELETE" -> delete(request, entity, id);
                default -> get(request, entity, entity.idField(), id);
            };
        }
        if (segments.size() == 3 && segments.get(1).equals("by-key") && entity.hasKey()) {
            requireMethod(request, allowed(entity::allows, RestOperation.GET, "GET"));
            return get(request, entity, entity.keyField(), segments.get(2));
        }
        if (segments.size() == 3 && !segments.get(1).equals("by-key")) {
            final ApiSubresource sub = model.findSubresource(entity, segments.get(2));
            if (sub != null) {
                requireMethod(request, allowed(sub.child()::allows, RestOperation.LIST, "GET", RestOperation.CREATE, "POST"));
                final long parentId = parseId(segments.get(1));
                return request.method().equals("POST")
                        ? create(request, sub.child(), new Scope(sub, parentId))
                        : list(request, sub.child(), RestOperation.LIST, listParametersFromQuery(request), new Scope(sub, parentId));
            }
            final ApiLink link = model.findLink(entity, segments.get(2));
            if (link != null) {
                requireMethod(request, allowed(link::allows, RestOperation.LIST, "GET"));
                return listLinks(request, link, parseId(segments.get(1)));
            }
        }
        if (segments.size() == 4 && !segments.get(1).equals("by-key")) {
            final ApiLink link = model.findLink(entity, segments.get(2));
            if (link != null) {
                requireMethod(request, allowed(link::allows, RestOperation.GET, "GET", RestOperation.CREATE, "PUT", RestOperation.DELETE, "DELETE"));
                final long parentId = parseId(segments.get(1));
                final long otherId = parseId(segments.get(3));
                return switch (request.method()) {
                    case "PUT" -> putLink(request, link, parentId, otherId);
                    case "DELETE" -> deleteLink(request, link, parentId, otherId);
                    default -> getLink(request, link, parentId, otherId);
                };
            }
        }
        throw new RestException(404, "There is no such resource.");
    }

    /** The parent that a request for the entities below it is about. */
    private record Scope(ApiSubresource subresource, long parentId) {
    }

    /**
     * Checks what a request below a parent needs of the parent: the caller may get it, and it has to be visible (inside of the restriction),
     * else it is "not found".
     */
    private void requireParent(final RestRequest<U> request, final DatabaseConnection conn, final Scope scope) throws DatabaseError {
        final ApiEntity parent = scope.subresource().parent();
        if (loadVisible(request, conn, parent, scope.parentId()) == null) {
            throw rollback(404, "There is no such " + parent.className() + ".", Map.of());
        }
    }

    /** The {@code Location} of an entity that was created, relative to the request: the entity is always at its own path, whatever the request was. */
    private static String locationOf(final RestRequest<?> request, final ApiEntity entity, final long id) {
        int depth = 0;
        for (final String segment : request.path().split("/")) {
            if (!segment.isEmpty()) {
                depth++;
            }
        }
        return "../".repeat(Math.max(0, depth - 1)) + entity.path() + "/" + id;
    }

    /** The methods of a route that the entity offers: operation and method alternate in the arguments. */
    private static List<String> allowed(final java.util.function.Predicate<RestOperation> offers, final Object... operationsAndMethods) {
        final List<String> methods = new ArrayList<>();
        for (int i = 0; i < operationsAndMethods.length; i += 2) {
            if (offers.test((RestOperation) operationsAndMethods[i])) {
                methods.add((String) operationsAndMethods[i + 1]);
            }
        }
        return methods;
    }

    private static void requireMethod(final RestRequest<?> request, final List<String> allowed) {
        if (allowed.isEmpty()) {
            throw new RestException(404, "There is no such resource.");
        }
        if (!allowed.contains(request.method())) {
            throw new RestException(405, "Method " + request.method() + " is not allowed here.", Map.of("Allow", String.join(", ", allowed)));
        }
    }

    private static long parseId(final String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new RestException(400, "'" + text + "' is not a valid id.");
        }
    }

    private String openApi() throws JsonException {
        String json = openApiJson;
        if (json == null) {
            json = new OpenApiEmitter(model, options, policy).toJson();
            openApiJson = json;
        }
        return json;
    }

    private void authorize(final RestRequest<U> request, final ApiEntity entity, final RestOperation operation) {
        if (!policy.permits(request.user(), entity, operation)) {
            throw denial(request, policy.securityFor(entity, operation));
        }
    }

    /**
     * What a request that is not permitted gets: 401 if nobody is authenticated, so that the caller knows that authenticating might help,
     * with the challenges of the security schemes that the operation needs; 403 if somebody is, and still may not.
     *
     * @param needed what the description says that the operation takes; null for the default of the description
     */
    private RestException denial(final RestRequest<U> request, final @Nullable List<ApiSecurityRequirement> needed) {
        if (request.user() != null) {
            return new RestException(403, "This request is not permitted.");
        }
        final String challenge = challengeFor(needed);
        return new RestException(401, "Authentication is required.", challenge == null ? Map.of() : Map.of("WWW-Authenticate", challenge));
    }

    /** The value of {@code WWW-Authenticate} for the schemes that an operation needs (or, if none are named, all that are declared), or null if there is none. */
    private @Nullable String challengeFor(final @Nullable List<ApiSecurityRequirement> needed) {
        final List<ApiSecurityRequirement> effective = needed != null && !needed.isEmpty() ? needed : options.security();
        final Set<String> named = new HashSet<>();
        for (final ApiSecurityRequirement requirement : effective) {
            named.addAll(requirement.schemes().keySet());
        }
        final Set<String> challenges = new LinkedHashSet<>();
        options.securitySchemes().forEach((name, scheme) -> {
            if ((named.isEmpty() || named.contains(name)) && scheme.challenge() != null) {
                challenges.add(scheme.challenge());
            }
        });
        return challenges.isEmpty() ? null : String.join(", ", challenges);
    }

    // --- lists ---

    private record ListParameters(@Nullable String filterJson, @Nullable FilterExpression filter, @Nullable String sort, @Nullable List<String> sortKeys, int page, @Nullable Integer size) {
    }

    private ListParameters listParametersFromQuery(final RestRequest<U> request) {
        return new ListParameters(request.param("filter"), null, request.param("sort"), null, parseInt(request.param("page"), "page", 0), parseOptionalInt(request.param("size"), "size"));
    }

    private ListParameters listParametersFromBody(final RestRequest<U> request) throws JsonException {
        final String body = request.body();
        if (body == null || body.isBlank()) {
            return new ListParameters(null, null, null, null, 0, null);
        }
        final JMap map = Janitor.current().parseJsonToMap(body);
        String filterJson = null;
        final JanitorObject filter = map.get(Janitor.string("filter"));
        if (filter instanceof JMap filterMap) {
            filterJson = Janitor.current().writeJson(filterMap::writeJson);
        } else if (filter != null && filter != Janitor.NULL) {
            throw new RestException(400, "'filter' must be an object.");
        }
        List<String> sortKeys = null;
        final JanitorObject sort = map.get(Janitor.string("sort"));
        if (sort instanceof JList list) {
            sortKeys = new ArrayList<>();
            for (final JanitorObject element : list) {
                if (!(element instanceof JString text)) {
                    throw new RestException(400, "'sort' must be a list of property names.");
                }
                sortKeys.add(text.janitorGetHostValue());
            }
        } else if (sort != null && sort != Janitor.NULL) {
            throw new RestException(400, "'sort' must be a list of property names.");
        }
        return new ListParameters(filterJson, null, null, sortKeys, parseInt(numberText(map, "page"), "page", 0), parseOptionalInt(numberText(map, "size"), "size"));
    }

    private static @Nullable String numberText(final JMap map, final String key) {
        final JanitorObject value = map.get(Janitor.string(key));
        if (value == null || value == Janitor.NULL) {
            return null;
        }
        if (value instanceof JNumber number) {
            return Long.toString(number.toLong());
        }
        throw new RestException(400, "'" + key + "' must be a number.");
    }

    private static int parseInt(final @Nullable String text, final String name, final int defaultValue) {
        final Integer value = parseOptionalInt(text, name);
        return value == null ? defaultValue : value;
    }

    private static @Nullable Integer parseOptionalInt(final @Nullable String text, final String name) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            throw new RestException(400, "'" + name + "' must be a number.");
        }
    }

    private RestResponse list(final RestRequest<U> request, final ApiEntity entity, final RestOperation operation, final ListParameters parameters, final @Nullable Scope scope) throws DatabaseError, JsonException {
        authorize(request, entity, operation);
        if (scope != null) {
            authorize(request, scope.subresource().parent(), RestOperation.GET);
        }
        final Set<String> denied = policy.deniedFields(request.user(), entity);
        final int size = pageSize(entity, parameters.size());
        if (parameters.page() < 0) {
            throw new RestException(400, "'page' must not be negative.");
        }
        FilterExpression expression = combine(entity, clientFilter(entity, parameters, denied), policy.restriction(request.user(), entity));
        if (scope != null) {
            // not subject to FILTERABLE: it is the API's own condition
            expression = FilterExpression.and(List.of(expression, FilterExpression.from(scope.subresource().field().name(), FilterOperator.EQ, scope.parentId())));
        }
        final FilterExpression finalExpression = expression;
        final FilterQuery query = new FilterQuery(expression).withPage(parameters.page(), size);
        applySort(entity, parameters, denied, query);
        if (policy.checksEachEntity(request.user(), entity)) {
            return scan(request, entity, parameters, denied, size, finalExpression, scope);
        }
        final Dao<OrmEntity> dao = daoOf(entity);
        final String json = collection.getDataManager().callTransaction(conn -> {
            if (scope != null) {
                requireParent(request, conn, scope);
            }
            final int total = dao.countByFilter(conn, finalExpression);
            final List<OrmEntity> found = dao.findByFilter(conn, query);
            final List<Object> items = new ArrayList<>(toTrees(conn, entity, found, denied, false));
            return JsonTree.toJson(JsonTree.object("items", items, "total", (long) total, "page", (long) parameters.page(), "size", (long) size));
        });
        return new RestResponse(200, JSON, Map.of(), json);
    }

    /**
     * A list of entities that the policy checks one by one: loads at most its scan limit of candidates, keeps those it reveals, and returns the
     * requested page of them. The total counts the visible candidates; {@code truncated} says that there were more candidates than were checked.
     */
    private RestResponse scan(final RestRequest<U> request, final ApiEntity entity, final ListParameters parameters, final Set<String> denied, final int size,
                              final FilterExpression expression, final @Nullable Scope scope) throws DatabaseError {
        final int limit = Math.max(1, policy.scanLimit(request.user(), entity));
        final FilterQuery query = new FilterQuery(expression).withMaxRows(limit + 1);
        applySort(entity, parameters, denied, query);
        final Dao<OrmEntity> dao = daoOf(entity);
        final String json = collection.getDataManager().callTransaction(conn -> {
            if (scope != null) {
                requireParent(request, conn, scope);
            }
            final List<OrmEntity> candidates = dao.findByFilter(conn, query);
            final boolean truncated = candidates.size() > limit;
            final List<OrmEntity> visible = new ArrayList<>();
            for (final OrmEntity candidate : truncated ? candidates.subList(0, limit) : candidates) {
                final OrmEntity shown = policy.reveal(request.user(), entity, candidate, conn);
                if (shown != null) {
                    visible.add(shown);
                }
            }
            final long start = Math.min((long) parameters.page() * size, visible.size());
            final List<OrmEntity> slice = visible.subList((int) start, (int) Math.min(start + size, visible.size()));
            try {
                return JsonTree.toJson(JsonTree.object("items", new ArrayList<Object>(toTrees(conn, entity, slice, denied, false)), "total", (long) visible.size(),
                        "page", (long) parameters.page(), "size", (long) size, "truncated", truncated));
            } catch (JsonException e) {
                throw new DatabaseError("cannot write " + entity.className() + " as JSON", e);
            }
        });
        return new RestResponse(200, JSON, Map.of(), json);
    }

    private static int pageSize(final ApiEntity entity, final @Nullable Integer requested) {
        if (requested == null) {
            return Math.min(OrmApiModel.DEFAULT_PAGE_SIZE, entity.maxPageSize());
        }
        if (requested < 1) {
            throw new RestException(400, "'size' must be at least 1.");
        }
        return Math.min(requested, entity.maxPageSize());
    }

    private @Nullable FilterExpression clientFilter(final ApiEntity entity, final ListParameters parameters, final Set<String> denied) {
        if (parameters.filterJson() == null || parameters.filterJson().isBlank()) {
            return null;
        }
        final FilterExpression filter = FilterExpression.fromJson(parameters.filterJson());
        validateFilter(entity, filter, denied);
        return filter;
    }

    private static void validateFilter(final ApiEntity entity, final FilterExpression filter, final Set<String> denied) {
        if (filter.isIncomplete()) {
            throw new RestException(400, "The filter is incomplete: " + filter);
        }
        if (filter.isGroup()) {
            for (final FilterExpression child : filter.getFilters()) {
                validateFilter(entity, child, denied);
            }
            return;
        }
        final ApiField field = filter.getField() == null ? null : entity.field(filter.getField());
        if (field == null || !field.filterable() || denied.contains(field.name())) {
            throw new RestException(400, "Cannot filter by '" + filter.getField() + "'.");
        }
    }

    private static FilterExpression combine(final ApiEntity entity, final @Nullable FilterExpression filter, final @Nullable FilterExpression restriction) {
        if (filter != null && restriction != null) {
            return FilterExpression.and(List.of(filter, restriction));
        }
        if (filter != null) {
            return filter;
        }
        if (restriction != null) {
            return restriction;
        }
        return new FilterExpression(entity.idField(), FilterOperator.ISNOTNULL); // the DAO always needs a condition: this one matches everything
    }

    private static void applySort(final ApiEntity entity, final ListParameters parameters, final Set<String> denied, final FilterQuery query) {
        final List<String> keys = new ArrayList<>();
        boolean fromClient = false;
        if (parameters.sortKeys() != null && !parameters.sortKeys().isEmpty()) {
            keys.addAll(parameters.sortKeys());
            fromClient = true;
        } else if (parameters.sort() != null && !parameters.sort().isBlank()) {
            for (final String key : parameters.sort().split(",")) {
                keys.add(key);
            }
            fromClient = true;
        } else if (entity.defaultSort() != null) {
            for (final String key : entity.defaultSort().split(",")) {
                keys.add(key);
            }
        }
        for (String key : keys) {
            key = key.trim();
            if (key.isEmpty()) {
                continue;
            }
            final boolean descending = key.startsWith("-");
            final String name = descending || key.startsWith("+") ? key.substring(1).trim() : key;
            final ApiField field = entity.field(name);
            if (field == null || !field.sortable() || denied.contains(name)) {
                if (fromClient) {
                    throw new RestException(400, "Cannot sort by '" + name + "'.");
                }
                continue; // a default sort that the caller may not use is simply not applied
            }
            if (descending) {
                query.orderByDescending(name);
            } else {
                query.orderBy(name);
            }
        }
    }

    // --- single entities ---

    private RestResponse get(final RestRequest<U> request, final ApiEntity entity, final String lookupField, final Object lookupValue) throws DatabaseError, JsonException {
        authorize(request, entity, RestOperation.GET);
        final Set<String> denied = policy.deniedFields(request.user(), entity);
        final FilterExpression restriction = policy.restriction(request.user(), entity);
        final Dao<OrmEntity> dao = daoOf(entity);
        final OrmEntity found = collection.getDataManager().callTransaction(conn -> {
            final OrmEntity candidate;
            if (restriction == null) {
                candidate = lookupValue instanceof Long id ? dao.findById(conn, id) : dao.findByKey(conn, (String) lookupValue);
            } else {
                final FilterExpression match = lookupValue instanceof Long id
                        ? FilterExpression.from(lookupField, FilterOperator.EQ, id)
                        : FilterExpression.from(lookupField, FilterOperator.EQ, (String) lookupValue);
                final List<OrmEntity> matches = dao.findByFilter(conn, new FilterQuery(FilterExpression.and(List.of(match, restriction))).withMaxRows(1));
                candidate = matches.isEmpty() ? null : matches.get(0);
            }
            return candidate == null ? null : reveal(request, conn, entity, candidate);
        });
        if (found == null) {
            throw new RestException(404, "There is no such " + entity.className() + ".");
        }
        final Map<String, String> headers = validatorHeaders(entity, found);
        if (notModified(request, versionOf(entity, found), modifiedOf(entity, found))) {
            return RestResponse.empty(304, headers);
        }
        return collection.getDataManager().callTransaction(conn -> entityResponse(conn, entity, found, denied, 200, headers));
    }

    private @Nullable Long versionOf(final ApiEntity entity, final OrmEntity found) {
        return entity.versionField() == null ? null : longOf(valueOf(entity, entity.versionField(), found));
    }

    private @Nullable LocalDateTime modifiedOf(final ApiEntity entity, final OrmEntity found) {
        return entity.modifiedField() == null ? null : valueOf(entity, entity.modifiedField(), found) instanceof LocalDateTime m ? m : null;
    }

    /** The {@code ETag} (from the version) and {@code Last-Modified} (from the modification date) headers of an entity, as far as it has them. */
    private Map<String, String> validatorHeaders(final ApiEntity entity, final OrmEntity found) {
        final Map<String, String> headers = new HashMap<>();
        final Long version = versionOf(entity, found);
        if (version != null) {
            headers.put("ETag", "\"" + version + "\"");
        }
        final LocalDateTime modified = modifiedOf(entity, found);
        if (modified != null) {
            headers.put("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(modified.atZone(collection.getClock().getZone()).withZoneSameInstant(ZoneOffset.UTC)));
        }
        return headers;
    }

    private RestResponse entityResponse(final DatabaseConnection conn, final ApiEntity entity, final OrmEntity found, final Set<String> denied, final int status, final Map<String, String> headers) throws DatabaseError {
        try {
            return new RestResponse(status, JSON, headers, JsonTree.toJson(toTrees(conn, entity, List.of(found), denied, true).get(0)));
        } catch (JsonException e) {
            throw new DatabaseError("cannot write " + entity.className() + " as JSON", e);
        }
    }

    // --- links ---

    private void authorizeLink(final RestRequest<U> request, final ApiLink link, final RestOperation operation) {
        if (!policy.permitsLink(request.user(), link, operation)) {
            throw denial(request, policy.securityForLink(link, operation));
        }
    }

    /** The parent that the links belong to has to be visible (inside of its restriction), else it is "not found". */
    private void requireLinkParent(final RestRequest<U> request, final DatabaseConnection conn, final ApiLink link, final long parentId) throws DatabaseError {
        final ApiEntity parent = link.parent();
        if (loadVisible(request, conn, parent, parentId) == null) {
            throw rollback(404, "There is no such " + parent.className() + ".", Map.of());
        }
    }

    @SuppressWarnings("unchecked")
    private JoinDao<OrmJoined> joinDaoOf(final ApiLink link) {
        final JoinDao<? extends OrmJoined> dao = collection.getJoinDao(link.className());
        if (dao == null) {
            throw new IllegalStateException("no DAO for " + link.className());
        }
        return (JoinDao<OrmJoined>) dao;
    }

    private long otherIdOf(final ApiLink link, final OrmJoined row) {
        return valueOf(link, link.toField(), row) instanceof ForeignKey<?> reference ? reference.getId() : 0;
    }

    /**
     * Loads those of the entities at the other end of a link that the caller may see (inside of their restriction): without a restriction
     * with one query per few hundred, with one with queries for blocks of ids, since filters have no "in".
     */
    private Map<Long, OrmEntity> visibleOthers(final RestRequest<U> request, final DatabaseConnection conn, final ApiLink link, final Collection<Long> ids) throws DatabaseError {
        final Map<Long, OrmEntity> result = loadOthers(conn, link, ids, policy.restriction(request.user(), link.other()));
        if (policy.checksEachEntity(request.user(), link.other())) {
            for (final Iterator<Map.Entry<Long, OrmEntity>> it = result.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry<Long, OrmEntity> entry = it.next();
                final OrmEntity shown = policy.reveal(request.user(), link.other(), entry.getValue(), conn);
                if (shown == null) {
                    it.remove();
                } else {
                    entry.setValue(shown);
                }
            }
        }
        return result;
    }

    private Map<Long, OrmEntity> loadOthers(final DatabaseConnection conn, final ApiLink link, final Collection<Long> ids, final @Nullable FilterExpression restriction) throws DatabaseError {
        final Dao<OrmEntity> dao = daoOf(link.other());
        final Map<Long, OrmEntity> result = new java.util.LinkedHashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        if (restriction == null) {
            for (final OrmEntity other : dao.findByIds(conn, ids)) {
                result.put(other.getId(), other);
            }
            return result;
        }
        final List<Long> remaining = new ArrayList<>(new java.util.LinkedHashSet<>(ids));
        for (int from = 0; from < remaining.size(); from += 100) {
            final List<FilterExpression> alternatives = new ArrayList<>();
            for (final Long id : remaining.subList(from, Math.min(from + 100, remaining.size()))) {
                alternatives.add(FilterExpression.from(link.other().idField(), FilterOperator.EQ, id));
            }
            final FilterExpression expression = FilterExpression.and(List.of(FilterExpression.or(alternatives), restriction));
            for (final OrmEntity other : dao.findByFilter(conn, new FilterQuery(expression))) {
                result.put(other.getId(), other);
            }
        }
        return result;
    }

    /** The join record between a parent and another entity, if there is one and the other entity is visible to the caller. */
    private @Nullable OrmJoined findLink(final RestRequest<U> request, final DatabaseConnection conn, final ApiLink link, final long parentId, final long otherId) throws DatabaseError {
        if (visibleOthers(request, conn, link, List.of(otherId)).isEmpty()) {
            return null;
        }
        for (final OrmJoined row : joinDaoOf(link).findByColumnValue(conn, link.fromColumn(), parentId)) {
            if (otherIdOf(link, row) == otherId) {
                return row;
            }
        }
        return null;
    }

    private RestResponse listLinks(final RestRequest<U> request, final ApiLink link, final long parentId) throws DatabaseError, JsonException {
        authorizeLink(request, link, RestOperation.LIST);
        if (request.param("filter") != null || request.param("sort") != null) {
            throw new RestException(400, "Links cannot be filtered or sorted; they come in the order of the keys of the entities at the other end.");
        }
        final int page = parseInt(request.param("page"), "page", 0);
        if (page < 0) {
            throw new RestException(400, "'page' must not be negative.");
        }
        final int size = pageSize(link.other(), parseOptionalInt(request.param("size"), "size"));
        final String json = collection.getDataManager().callTransaction(conn -> {
            requireLinkParent(request, conn, link, parentId);
            final List<OrmJoined> rows = joinDaoOf(link).findByColumnValue(conn, link.fromColumn(), parentId);
            final Map<Long, OrmEntity> others = visibleOthers(request, conn, link, rows.stream().map(row -> otherIdOf(link, row)).toList());
            final List<OrmJoined> visible = new ArrayList<>(rows.stream().filter(row -> others.containsKey(otherIdOf(link, row))).toList());
            visible.sort(Comparator.<OrmJoined, String>comparing(row -> others.get(otherIdOf(link, row)).coalesceKey()).thenComparingLong(row -> otherIdOf(link, row)));
            final long start = Math.min((long) page * size, visible.size());
            final List<OrmJoined> slice = visible.subList((int) start, (int) Math.min(start + size, visible.size()));
            return JsonTree.toJson(JsonTree.object("items", new ArrayList<Object>(toTrees(conn, link, slice, Set.of(), true)), "total", (long) visible.size(), "page", (long) page, "size", (long) size));
        });
        return new RestResponse(200, JSON, Map.of(), json);
    }

    private RestResponse getLink(final RestRequest<U> request, final ApiLink link, final long parentId, final long otherId) throws DatabaseError {
        authorizeLink(request, link, RestOperation.GET);
        return collection.getDataManager().callTransaction(conn -> {
            requireLinkParent(request, conn, link, parentId);
            final OrmJoined row = findLink(request, conn, link, parentId, otherId);
            if (row == null) {
                throw rollback(404, "There is no such link.", Map.of());
            }
            return linkResponse(conn, link, row, 200);
        });
    }

    private RestResponse putLink(final RestRequest<U> request, final ApiLink link, final long parentId, final long otherId) throws DatabaseError, JsonException {
        authorizeLink(request, link, RestOperation.CREATE);
        final String text = request.body() == null || request.body().isBlank() ? "{}" : request.body();
        final WriteBody body = parseBodyText(text, link, Set.of(), true, Map.of(link.fromField(), parentId, link.toField(), otherId));
        return collection.getDataManager().callTransaction(conn -> {
            requireLinkParent(request, conn, link, parentId);
            if (visibleOthers(request, conn, link, List.of(otherId)).isEmpty()) {
                throw rollback(404, "There is no such " + link.other().className() + ".", Map.of());
            }
            final OrmJoined existing = findLink(request, conn, link, parentId, otherId);
            final OrmJoined row = existing != null ? existing : newJoin(link);
            try {
                apply(link, row, body);
            } catch (JsonException e) {
                throw rollback(400, String.valueOf(e.getMessage()), Map.of());
            } catch (RestException e) {
                throw new Rollback(e);
            }
            checkReferences(conn, link, row, body.written());
            runHook(() -> policy.beforeLinkWrite(request.user(), link, RestOperation.CREATE, row, conn));
            if (existing != null) {
                joinDaoOf(link).update(conn, row);
            } else {
                joinDaoOf(link).insert(conn, row);
            }
            final OrmJoined stored = findLink(request, conn, link, parentId, otherId);
            return linkResponse(conn, link, stored == null ? row : stored, existing != null ? 200 : 201);
        });
    }

    private RestResponse deleteLink(final RestRequest<U> request, final ApiLink link, final long parentId, final long otherId) throws DatabaseError {
        authorizeLink(request, link, RestOperation.DELETE);
        return collection.getDataManager().callTransaction(conn -> {
            requireLinkParent(request, conn, link, parentId);
            final OrmJoined row = findLink(request, conn, link, parentId, otherId);
            if (row == null) {
                throw rollback(404, "There is no such link.", Map.of());
            }
            runHook(() -> policy.beforeLinkWrite(request.user(), link, RestOperation.DELETE, row, conn));
            joinDaoOf(link).delete(conn, row);
            return RestResponse.empty(204, Map.of());
        });
    }

    @SuppressWarnings("unchecked")
    private OrmJoined newJoin(final ApiLink link) {
        return ((OrmDispatchTable<OrmJoined, Uplink>) link.table()).createNewInstance((Uplink) collection);
    }

    private RestResponse linkResponse(final DatabaseConnection conn, final ApiLink link, final OrmJoined row, final int status) throws DatabaseError {
        try {
            return new RestResponse(status, JSON, Map.of(), JsonTree.toJson(toTrees(conn, link, List.of(row), Set.of(), true).get(0)));
        } catch (JsonException e) {
            throw new DatabaseError("cannot write " + link.className() + " as JSON", e);
        }
    }

    // --- writing ---

    /**
     * What a request wants to write: the JSON with the values that are applied to the entity, the properties that are set to null (the readers
     * of plain properties do not know null, so those are assigned directly), and the names of all properties that are written.
     */
    private record WriteBody(String json, Set<String> nulls, Set<String> written, @Nullable Object claimed) {
    }

    /**
     * For entities created below a parent: what the body said about the property that refers to the parent (an id or a key), if anything. It
     * has to be the parent, since the path says what it is.
     */
    private void checkClaim(final DatabaseConnection conn, final Scope scope, final @Nullable Object claimed) throws DatabaseError {
        if (claimed == null) {
            return;
        }
        final ApiEntity parent = scope.subresource().parent();
        final OrmEntity found = load(conn, daoOf(parent), parent.idField(), scope.parentId(), null);
        final boolean matches = claimed instanceof Number number
                ? number.longValue() == scope.parentId()
                : found != null && String.valueOf(claimed).equals(found.getKey());
        if (!matches) {
            throw rollback(400, "'" + scope.subresource().field().name() + "' must be the " + parent.className() + " that the path is about.", Map.of());
        }
    }

    /**
     * Checks the body of a write request against the API's idea of the entity and keeps what may be written: unknown properties and
     * properties that the caller may not see are refused, read-only ones (id, version, ...) are ignored, so that a changed answer of the
     * API can be sent back as it is.
     *
     * @param complete true if the body has to describe the whole entity (POST and PUT): all required properties must be given, and
     *                 nullable ones that are not given are set to null
     */
    private WriteBody parseBody(final RestRequest<U> request, final ApiFields entity, final Set<String> denied, final boolean complete, final Map<String, Long> forced) throws JsonException {
        final String text = request.body();
        if (text == null || text.isBlank()) {
            throw new RestException(400, "The request needs a JSON object as its body.");
        }
        return parseBodyText(text, entity, denied, complete, forced);
    }

    /** @param forced properties that are set from the path, and what they are set to */
    private WriteBody parseBodyText(final String text, final ApiFields entity, final Set<String> denied, final boolean complete, final Map<String, Long> forced) throws JsonException {
        Object claimed = null;
        final JMap map = Janitor.current().parseJsonToMap(text);
        final JMap kept = Janitor.map();
        final Set<String> written = new LinkedHashSet<>();
        final Set<String> nulls = new LinkedHashSet<>();
        for (final JanitorObject keyObject : map.keySet()) {
            final String key = String.valueOf(keyObject.janitorGetHostValue());
            final ApiField field = entity.field(key);
            if (field == null) {
                throw new RestException(400, "Unknown property '" + key + "'.");
            }
            if (denied.contains(key)) {
                throw new RestException(400, "Cannot write '" + key + "'.");
            }
            if (field.readOnly()) {
                continue;
            }
            if (forced.containsKey(key)) {
                claimed = map.get(keyObject) == Janitor.NULL ? null : map.get(keyObject).janitorGetHostValue();
                continue; // set from the path, below
            }
            final JanitorObject value = map.get(keyObject);
            if (value == null || value == Janitor.NULL) {
                nulls.add(key);
            } else if (field.columnType() == ColumnTypeHint.DATETIME && !field.isReference() && value instanceof JString dateText) {
                kept.put(key, Janitor.string(toLocalDateTime(dateText.janitorGetHostValue())));
            } else {
                kept.put(key, value);
            }
            written.add(key);
        }
        for (final Map.Entry<String, Long> entry : forced.entrySet()) {
            kept.put(entry.getKey(), Janitor.integer(entry.getValue()));
            written.add(entry.getKey());
        }
        if (complete) {
            for (final ApiField field : entity.fields()) {
                if (field.readOnly() || denied.contains(field.name()) || written.contains(field.name())) {
                    continue;
                }
                if (field.required()) {
                    throw new RestException(400, "Missing property '" + field.name() + "'.");
                }
                nulls.add(field.name());
                written.add(field.name());
            }
        }
        return new WriteBody(Janitor.current().writeJson(kept::writeJson), nulls, written, claimed);
    }

    /**
     * The API writes date-times with an offset (see the schema: {@code date-time}), while the JSON readers of the properties take local ones.
     * An offset is therefore converted to the time zone of the collection's clock; a local date-time is passed on as it is.
     */
    private String toLocalDateTime(final String text) {
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(collection.getClock().getZone()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return text;
        }
    }

    @SuppressWarnings("unchecked")
    private void apply(final ApiFields entity, final OrmObject instance, final WriteBody body) throws JsonException {
        ((DispatchTable<OrmObject>) entity.table()).readFromJson(() -> instance, body.json());
        for (final String name : body.nulls()) {
            try {
                final JanitorObject property = ((AttributeLookupHandler<OrmObject>) entity.table().get(name)).lookupAttribute(instance);
                if (!(property instanceof JAssignable assignable)) {
                    throw new RestException(400, "'" + name + "' cannot be set.");
                }
                assignable.assign(Janitor.NULL);
            } catch (RestException e) {
                throw e;
            } catch (Exception e) {
                throw new RestException(400, "'" + name + "' must not be null.");
            }
        }
        for (final String name : body.written()) {
            final ApiField field = entity.field(name);
            if (field != null && field.maxLength() != null && !field.isReference() && valueOf(entity, name, instance) instanceof String text && text.length() > field.maxLength()) {
                throw new RestException(400, "'" + name + "' is longer than " + field.maxLength() + " characters.");
            }
        }
    }

    /** Refuses references to something that does not exist, which would otherwise end in a constraint violation. */
    private void checkReferences(final DatabaseConnection conn, final ApiFields entity, final OrmObject instance, final Set<String> written) throws DatabaseError {
        for (final String name : written) {
            final ApiField field = entity.field(name);
            if (field != null && field.isReference() && valueOf(entity, name, instance) instanceof ForeignKey<?> reference && !reference.isNull() && reference.resolveOrNull(conn) == null) {
                throw rollback(400, "'" + name + "' refers to a " + field.referencedEntity() + " that does not exist.", Map.of());
            }
        }
    }

    /**
     * Loads an entity as the caller may see it: inside of the restriction and revealed by the policy (see {@link RestPolicy#reveal}); null if
     * there is none, or it is hidden.
     */
    private @Nullable OrmEntity loadVisible(final RestRequest<U> request, final DatabaseConnection conn, final ApiEntity entity, final long id) throws DatabaseError {
        final OrmEntity found = load(conn, daoOf(entity), entity.idField(), id, policy.restriction(request.user(), entity));
        return found == null ? null : reveal(request, conn, entity, found);
    }

    /**
     * The entity as the policy shows it, if it checks its kind one by one; else the entity itself.
     */
    private @Nullable OrmEntity reveal(final RestRequest<U> request, final DatabaseConnection conn, final ApiEntity entity, final OrmEntity instance) throws DatabaseError {
        return policy.checksEachEntity(request.user(), entity) ? policy.reveal(request.user(), entity, instance, conn) : instance;
    }

    /** Loads an entity from the database, never from a cache, so that it can be changed without harm; null if there is none, or none that the restriction lets through. */
    private @Nullable OrmEntity load(final DatabaseConnection conn, final Dao<OrmEntity> dao, final String field, final Object value, final @Nullable FilterExpression restriction) throws DatabaseError {
        final FilterExpression match = value instanceof Long id ? FilterExpression.from(field, FilterOperator.EQ, id) : FilterExpression.from(field, FilterOperator.EQ, (String) value);
        final FilterExpression expression = restriction == null ? match : FilterExpression.and(List.of(match, restriction));
        final List<OrmEntity> found = dao.findByFilter(conn, new FilterQuery(expression).withMaxRows(1));
        return found.isEmpty() ? null : found.get(0);
    }

    /**
     * If the caller says which version of a versioned entity it has seen (the {@code If-Match} header), the entity is only changed or deleted if
     * that is still the current one. Entities can also insist on the header, see {@link RestMetaData#REQUIRE_IF_MATCH}.
     */
    private void checkPrecondition(final RestRequest<U> request, final ApiEntity entity, final OrmEntity found) throws DatabaseError {
        if (entity.versionField() == null) {
            return;
        }
        final Long version = versionOf(entity, found);
        final Map<String, String> headers = validatorHeaders(entity, found);
        final String ifMatch = request.header("If-Match");
        if (ifMatch == null) {
            if (entity.requiresIfMatch()) {
                throw rollback(428, "This entity is versioned: send the version that you have seen in the If-Match header.", headers);
            }
            return;
        }
        for (String tag : ifMatch.split(",")) {
            tag = tag.trim();
            if (tag.startsWith("W/")) {
                tag = tag.substring(2);
            }
            if (tag.equals("*") || tag.equals("\"" + version + "\"")) {
                return;
            }
        }
        throw rollback(412, "The entity has changed: it is now at version " + version + ".", headers);
    }

    private RestResponse create(final RestRequest<U> request, final ApiEntity entity, final @Nullable Scope scope) throws DatabaseError, JsonException {
        authorize(request, entity, RestOperation.CREATE);
        if (scope != null) {
            authorize(request, scope.subresource().parent(), RestOperation.GET);
        }
        final Set<String> denied = policy.deniedFields(request.user(), entity);
        final WriteBody body = parseBody(request, entity, denied, true, scope == null ? Map.of() : Map.of(scope.subresource().field().name(), scope.parentId()));
        final Dao<OrmEntity> dao = daoOf(entity);
        final OrmEntity instance = newInstance(entity);
        apply(entity, instance, body);
        return collection.getDataManager().callTransaction(conn -> {
            if (scope != null) {
                requireParent(request, conn, scope);
                checkClaim(conn, scope, body.claimed());
            }
            checkReferences(conn, entity, instance, body.written());
            runHook(() -> policy.beforeWrite(request.user(), entity, RestOperation.CREATE, instance, conn));
            dao.insert(conn, instance);
            final OrmEntity result = loadVisible(request, conn, entity, instance.getId());
            if (result == null) {
                throw new Rollback(denial(request, policy.securityFor(entity, RestOperation.CREATE))); // the new entity would be outside of what the caller may see
            }
            final Map<String, String> headers = validatorHeaders(entity, result);
            headers.put("Location", locationOf(request, entity, result.getId()));
            return entityResponse(conn, entity, result, denied, 201, headers);
        });
    }

    private RestResponse update(final RestRequest<U> request, final ApiEntity entity, final long id, final boolean replace) throws DatabaseError, JsonException {
        authorize(request, entity, RestOperation.UPDATE);
        final Set<String> denied = policy.deniedFields(request.user(), entity);
        final WriteBody body = parseBody(request, entity, denied, replace, Map.of());
        final FilterExpression restriction = policy.restriction(request.user(), entity);
        final Dao<OrmEntity> dao = daoOf(entity);
        return collection.getDataManager().callTransaction(conn -> {
            final OrmEntity found = load(conn, dao, entity.idField(), id, restriction);
            if (found == null || reveal(request, conn, entity, found) == null) {
                throw rollback(404, "There is no such " + entity.className() + ".", Map.of());
            }
            checkPrecondition(request, entity, found);
            try {
                apply(entity, found, body);
            } catch (JsonException e) {
                throw rollback(400, String.valueOf(e.getMessage()), Map.of());
            } catch (RestException e) {
                throw new Rollback(e);
            }
            checkReferences(conn, entity, found, body.written());
            runHook(() -> policy.beforeWrite(request.user(), entity, RestOperation.UPDATE, found, conn));
            dao.update(conn, found);
            final OrmEntity result = loadVisible(request, conn, entity, id);
            if (result == null) {
                throw new Rollback(denial(request, policy.securityFor(entity, RestOperation.UPDATE))); // the change would take the entity out of what the caller may see
            }
            return entityResponse(conn, entity, result, denied, 200, validatorHeaders(entity, result));
        });
    }

    private RestResponse delete(final RestRequest<U> request, final ApiEntity entity, final long id) throws DatabaseError {
        authorize(request, entity, RestOperation.DELETE);
        final FilterExpression restriction = policy.restriction(request.user(), entity);
        final Dao<OrmEntity> dao = daoOf(entity);
        return collection.getDataManager().callTransaction(conn -> {
            final OrmEntity found = load(conn, dao, entity.idField(), id, restriction);
            if (found == null || reveal(request, conn, entity, found) == null) {
                throw rollback(404, "There is no such " + entity.className() + ".", Map.of());
            }
            checkPrecondition(request, entity, found);
            runHook(() -> policy.beforeWrite(request.user(), entity, RestOperation.DELETE, found, conn));
            dao.delete(conn, found);
            return RestResponse.empty(204, Map.of());
        });
    }

    @SuppressWarnings("unchecked")
    private OrmEntity newInstance(final ApiEntity entity) {
        return ((EntityDispatchTable<OrmEntity, Uplink>) entity.table()).createNewInstance((Uplink) collection);
    }

    private static boolean isConstraintViolation(final Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null && sql.getSQLState().startsWith("23")) {
                return true; // SQLSTATE class 23: integrity constraint violation, e.g. a duplicate key
            }
            if (cause.getCause() == cause) {
                break;
            }
        }
        return false;
    }

    /** Something that the policy is asked to do while something is written; it may refuse with a {@link RestRejection}. */
    @FunctionalInterface
    private interface Hook {
        void run() throws DatabaseError;
    }

    /** Runs a hook of the policy, and turns its refusal into one that rolls the transaction back. */
    private static void runHook(final Hook hook) throws DatabaseError {
        try {
            hook.run();
        } catch (RestRejection e) {
            throw rollback(e.status(), e.getMessage(), Map.of());
        }
    }

    private static Rollback rollback(final int status, final String detail, final Map<String, String> headers) {
        return new Rollback(new RestException(status, detail, headers));
    }

    private boolean notModified(final RestRequest<U> request, final @Nullable Long version, final @Nullable LocalDateTime modified) {
        final String noneMatch = request.header("If-None-Match");
        if (noneMatch != null && version != null) {
            for (String tag : noneMatch.split(",")) {
                tag = tag.trim();
                if (tag.startsWith("W/")) {
                    tag = tag.substring(2);
                }
                if (tag.equals("*") || tag.equals("\"" + version + "\"")) {
                    return true;
                }
            }
            return false; // If-None-Match takes precedence over If-Modified-Since
        }
        final String modifiedSince = request.header("If-Modified-Since");
        if (modifiedSince != null && modified != null) {
            try {
                final ZonedDateTime since = ZonedDateTime.parse(modifiedSince.trim(), DateTimeFormatter.RFC_1123_DATE_TIME);
                final ZonedDateTime current = modified.atZone(collection.getClock().getZone()).withNano(0);
                return !current.toInstant().isAfter(since.toInstant());
            } catch (DateTimeParseException e) {
                return false; // an invalid date is ignored, as HTTP says
            }
        }
        return false;
    }

    // --- output ---

    @SuppressWarnings("unchecked")
    private Dao<OrmEntity> daoOf(final ApiEntity entity) {
        final Dao<? extends OrmEntity> dao = collection.getDao(entity.className());
        if (dao == null) {
            throw new IllegalStateException("no DAO for " + entity.className());
        }
        return (Dao<OrmEntity>) dao;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Object valueOf(final ApiFields entity, final String fieldName, final OrmObject instance) {
        final AttributeLookupHandler<OrmObject> handler = (AttributeLookupHandler<OrmObject>) entity.table().get(fieldName);
        try {
            return unwrap(handler.lookupAttribute(instance));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("cannot read '" + fieldName + "' of " + instance, e);
        }
    }

    /** Unpacks what the dispatch table returns for a property: the plain host value, or a reference. */
    private static @Nullable Object unwrap(@Nullable JanitorObject object) {
        if (object instanceof TemporaryAssignable assignable) {
            object = assignable.getValue();
        }
        if (object instanceof ForeignKey<?>) {
            return object;
        }
        return object == null ? null : object.janitorGetHostValue();
    }

    private static @Nullable Long longOf(final @Nullable Object value) {
        return value instanceof Number number ? Long.valueOf(number.longValue()) : null;
    }

    private @NotNull List<Map<String, Object>> toTrees(final DatabaseConnection conn, final ApiFields entity, final List<? extends OrmObject> instances, final Set<String> denied, final boolean includeLazy) throws DatabaseError {
        final List<ApiField> fields = new ArrayList<>();
        for (final ApiField field : entity.fields()) {
            final boolean lazy = Boolean.TRUE.equals(entity.table().getMetaData(field.name(), JanitorOrm.MetaData.LAZY_LOAD));
            if (!denied.contains(field.name()) && (includeLazy || !lazy)) {
                fields.add(field);
            }
        }
        final Map<String, Map<Long, String>> keysOfTargets = prefetchKeys(conn, entity, fields, instances);
        final List<Map<String, Object>> trees = new ArrayList<>(instances.size());
        for (final OrmObject instance : instances) {
            final Map<String, Object> tree = JsonTree.object();
            for (final ApiField field : fields) {
                final Object value = valueOf(entity, field.name(), instance);
                tree.put(field.name(), field.isReference() ? referenceValue(conn, field, value, keysOfTargets) : plainValue(value));
            }
            trees.add(tree);
        }
        return trees;
    }

    /** Loads the keys of all targets that the references by key of a page point to, with one query per target type. */
    private Map<String, Map<Long, String>> prefetchKeys(final DatabaseConnection conn, final ApiFields entity, final List<ApiField> fields, final List<? extends OrmObject> instances) throws DatabaseError {
        final Map<String, Set<Long>> idsOfTargets = new HashMap<>();
        for (final ApiField field : fields) {
            if (!field.isReference() || !field.referenceByKey()) {
                continue;
            }
            for (final OrmObject instance : instances) {
                final Object value = valueOf(entity, field.name(), instance);
                if ((value instanceof ForeignKeyInteger<?> || value instanceof ForeignKeySearchResult<?>) && !((ForeignKey<?>) value).isNull()) {
                    idsOfTargets.computeIfAbsent(field.referencedEntity(), k -> new HashSet<>()).add(((ForeignKey<?>) value).getId());
                }
            }
        }
        final Map<String, Map<Long, String>> result = new HashMap<>();
        for (final Map.Entry<String, Set<Long>> entry : idsOfTargets.entrySet()) {
            final Dao<? extends OrmEntity> dao = collection.getDao(entry.getKey());
            if (dao == null) {
                continue;
            }
            final Map<Long, String> keys = new HashMap<>();
            for (final OrmEntity target : dao.findByIds(conn, entry.getValue())) {
                if (target.getKey() != null && !target.getKey().isBlank()) {
                    keys.put(target.getId(), target.getKey());
                }
            }
            result.put(entry.getKey(), keys);
        }
        return result;
    }

    private static @Nullable Object referenceValue(final DatabaseConnection conn, final ApiField field, final @Nullable Object value, final Map<String, Map<Long, String>> keysOfTargets) throws DatabaseError {
        if (!(value instanceof ForeignKey<?> reference) || reference.isNull()) {
            return null;
        }
        if (!field.referenceByKey()) {
            return reference.getId();
        }
        if (reference instanceof ForeignKeyString<?> byKey) {
            return byKey.getKey();
        }
        if (reference instanceof ForeignKeyIdentity<?> identity && identity.getIdentity().getKey() != null) {
            return identity.getIdentity().getKey();
        }
        final String prefetched = keysOfTargets.getOrDefault(field.referencedEntity(), Map.of()).get(reference.getId());
        if (prefetched != null) {
            return prefetched;
        }
        final OrmEntity target = reference.resolveOrNull(conn);
        if (target == null) {
            return null; // points to nothing (any more)
        }
        return target.getKey() == null || target.getKey().isBlank() ? (Object) target.getId() : target.getKey();
    }

    private @Nullable Object plainValue(final @Nullable Object value) {
        return switch (value) {
            case null -> null;
            case LocalDate date -> date.toString();
            case LocalDateTime dateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime.atZone(collection.getClock().getZone()));
            case ZonedDateTime dateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
            case OffsetDateTime dateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
            case String string -> string;
            case Boolean bool -> bool;
            case Number number -> number;
            default -> value.toString();
        };
    }

    // --- errors ---

    private static RestResponse problem(final int status, final String detail, final Map<String, String> headers) {
        final String title = switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 412 -> "Precondition Failed";
            case 422 -> "Unprocessable Content";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            default -> status < 500 ? "Client Error" : "Internal Server Error";
        };
        try {
            final String body = JsonTree.toJson(JsonTree.object("type", "about:blank", "title", title, "status", (long) status, "detail", detail));
            return new RestResponse(status, PROBLEM_JSON, headers, body);
        } catch (JsonException e) {
            return new RestResponse(status, null, headers, null);
        }
    }

    /**
     * A {@link RestException} that is thrown inside a database transaction: as a {@link DatabaseError}, it makes the transaction roll back, and it
     * is turned into the problem response outside of it.
     */
    private static final class Rollback extends DatabaseError {
        private final RestException problem;

        Rollback(final RestException problem) {
            super(problem.getMessage());
            this.problem = problem;
        }
    }

    /** A problem that is the caller's to fix, or simply the answer: it ends the request with its status. */
    private static final class RestException extends RuntimeException {
        private final int status;
        private final Map<String, String> headers;

        RestException(final int status, final String message) {
            this(status, message, Map.of());
        }

        RestException(final int status, final String message, final Map<String, String> headers) {
            super(message, null, false, false);
            this.status = status;
            this.headers = headers;
        }
    }
}
