// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.filter.FilterLogic;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Turns an {@link OrmApiModel} into an OpenAPI 3.1 description, the way the {@code TypeScriptEmitter} turns dispatch tables into type
 * definitions. Of the operations, only those that an entity offers (see {@link RestOperations}) are described: by default, listing,
 * searching and getting; creating, changing and deleting if the entity opted in.
 */
public final class OpenApiEmitter {

    private static final String JSON = "application/json";
    private static final String PROBLEM_JSON = "application/problem+json";

    private final @NotNull OrmApiModel model;
    private final @NotNull OpenApiOptions options;
    private final @NotNull RestSecurityDescription security;
    private boolean usesUnauthorized;

    /**
     * @param model    the entities to describe
     * @param options  title, version, authentication etc. of the API
     * @param security what it takes to call the operations, where that differs from what the options say; the policy of the API is one
     */
    public OpenApiEmitter(final @NotNull OrmApiModel model, final @NotNull OpenApiOptions options, final @NotNull RestSecurityDescription security) {
        this.model = model;
        this.options = options;
        this.security = security;
    }

    /**
     * @param model   the entities to describe
     * @param options title, version, authentication etc. of the API
     */
    public OpenApiEmitter(final @NotNull OrmApiModel model, final @NotNull OpenApiOptions options) {
        this(model, options, RestSecurityDescription.none());
    }

    /**
     * @param model the entities to describe
     * @return an emitter with {@link OpenApiOptions#defaults()}
     */
    public static @NotNull OpenApiEmitter of(final @NotNull OrmApiModel model) {
        return new OpenApiEmitter(model, OpenApiOptions.defaults());
    }

    /**
     * @return the description as JSON text
     * @throws JsonException if it cannot be written
     */
    public @NotNull String toJson() throws JsonException {
        return JsonTree.toJson(build());
    }

    /**
     * @return the description as a tree of maps and lists
     */
    public synchronized @NotNull Map<String, Object> build() {
        usesUnauthorized = false;
        final String restrictions = "Rows and properties can be restricted depending on who is calling; this description shows all of them.";
        final Map<String, Object> info = JsonTree.object("title", options.title(), "version", options.version(),
                "description", options.description() == null ? restrictions : options.description() + " " + restrictions);
        final Map<String, Object> document = JsonTree.object("openapi", "3.1.0", "info", info);
        if (options.serverUrl() != null) {
            document.put("servers", List.of(JsonTree.object("url", options.serverUrl())));
        }
        final Map<String, Object> paths = JsonTree.object();
        final Map<String, Object> schemas = JsonTree.object();
        addCommonSchemas(schemas);
        for (final ApiEntity entity : model.getEntities()) {
            schemas.put(entity.className(), OrmSchemaBuilder.entitySchema(entity));
            schemas.put(entity.className() + "Page", pageSchema(entity.className()));
            addPaths(paths, entity);
        }
        for (final ApiSubresource sub : model.getSubresources()) {
            addSubresourcePaths(paths, sub);
        }
        for (final ApiLink link : model.getLinks()) {
            if (!schemas.containsKey(link.className())) {
                schemas.put(link.className(), OrmSchemaBuilder.entitySchema(link));
                schemas.put(link.className() + "Page", pageSchema(link.className()));
            }
            addLinkPaths(paths, link);
        }
        document.put("paths", paths);
        final Map<String, Object> responses = commonResponses();
        if (usesUnauthorized) {
            responses.put("Unauthorized", JsonTree.object(
                    "description", "The caller is not authenticated: the request needs credentials, see the security schemes.",
                    "headers", JsonTree.object("WWW-Authenticate", JsonTree.object("description", "What the caller is expected to authenticate with.", "schema", JsonTree.object("type", "string"))),
                    "content", JsonTree.object(PROBLEM_JSON, JsonTree.object("schema", ref("Problem")))));
        }
        final Map<String, Object> components = JsonTree.object("schemas", schemas, "responses", responses);
        if (!options.securitySchemes().isEmpty()) {
            final Map<String, Object> schemes = JsonTree.object();
            options.securitySchemes().forEach((name, scheme) -> schemes.put(name, scheme.toTree()));
            components.put("securitySchemes", schemes);
        }
        document.put("components", components);
        if (!options.security().isEmpty()) {
            document.put("security", options.security().stream().map(ApiSecurityRequirement::toTree).toList());
        }
        return document;
    }

    private void addCommonSchemas(final Map<String, Object> schemas) {
        final List<Object> operators = FilterOperator.OPERATORS.stream().map(FilterOperator::getCode).collect(Collectors.toList());
        final List<Object> logics = new ArrayList<>();
        for (final FilterLogic logic : FilterLogic.values()) {
            logics.add(logic.getCode());
        }
        final Map<String, Object> properties = JsonTree.object();
        properties.put("logic", JsonTree.object("type", "string", "enum", logics, "description", "Combines the filters of a group."));
        properties.put("filters", JsonTree.object("type", "array", "items", JsonTree.object("$ref", "#/components/schemas/FilterExpression")));
        properties.put("field", JsonTree.object("type", "string", "description", "The property to compare, which must be filterable."));
        properties.put("operator", JsonTree.object("type", "string", "enum", operators));
        properties.put("ignoreCase", JsonTree.object("type", List.of("boolean", "null")));
        properties.put("valueString", JsonTree.object("type", "string"));
        properties.put("valueBoolean", JsonTree.object("type", "boolean"));
        properties.put("valueDouble", JsonTree.object("type", "number"));
        properties.put("valueLong", JsonTree.object("type", "integer", "format", "int64"));
        properties.put("valueDate", JsonTree.object("type", "string", "format", "date"));
        properties.put("valueDateTime", JsonTree.object("type", "string", "format", "date-time"));
        schemas.put("FilterExpression", JsonTree.object(
                "type", "object",
                "description", "Either a group (logic and filters) or a comparison (field, operator and, for most operators, one value).",
                "properties", properties));
        schemas.put("SearchRequest", JsonTree.object(
                "type", "object",
                "properties", JsonTree.object(
                        "filter", JsonTree.object("$ref", "#/components/schemas/FilterExpression"),
                        "sort", JsonTree.object("type", "array", "items", JsonTree.object("type", "string"),
                                "description", "Property names; a leading minus sorts descending."),
                        "page", JsonTree.object("type", "integer", "minimum", 0L),
                        "size", JsonTree.object("type", "integer", "minimum", 1L))));
        schemas.put("Problem", JsonTree.object(
                "type", "object",
                "description", "An error, as in RFC 9457.",
                "properties", JsonTree.object(
                        "type", JsonTree.object("type", "string"),
                        "title", JsonTree.object("type", "string"),
                        "status", JsonTree.object("type", "integer"),
                        "detail", JsonTree.object("type", "string"))));
    }

    private Map<String, Object> commonResponses() {
        return JsonTree.object(
                "BadRequest", problemResponse("The request is malformed, e.g. an unknown or non-filterable property in a filter."),
                "Forbidden", problemResponse("The request is not permitted."),
                "NotFound", problemResponse("There is no such entity."),
                "Conflict", problemResponse("The request conflicts with existing data, e.g. a duplicate key."),
                "PreconditionFailed", problemResponse("The entity was changed by someone else: the version in If-Match is outdated."),
                "PreconditionRequired", problemResponse("The entity insists on an If-Match header (see RestOrm.requireIfMatch), and the request has none."));
    }

    private static Map<String, Object> problemResponse(final String description) {
        return JsonTree.object("description", description,
                "content", JsonTree.object(PROBLEM_JSON, JsonTree.object("schema", ref("Problem"))));
    }

    private static Map<String, Object> ref(final String schema) {
        return JsonTree.object("$ref", "#/components/schemas/" + schema);
    }

    private static Map<String, Object> responseRef(final String response) {
        return JsonTree.object("$ref", "#/components/responses/" + response);
    }

    private Map<String, Object> pageSchema(final String className) {
        return JsonTree.object(
                "type", "object",
                "required", List.of("items", "total", "page", "size"),
                "properties", JsonTree.object(
                        "items", JsonTree.object("type", "array", "items", ref(className)),
                        "total", JsonTree.object("type", "integer", "format", "int64", "description", "The number of entities that match the filter, over all pages; "
                                + "for entities that are checked one by one, the number of visible ones among the candidates that were checked."),
                        "truncated", JsonTree.object("type", "boolean", "description", "Only for entities that are checked one by one: true if there were more "
                                + "candidates than were checked, so that the pages end early; a narrower filter finds the rest."),
                        "page", JsonTree.object("type", "integer", "description", "The number of this page, starting at 0."),
                        "size", JsonTree.object("type", "integer", "description", "The page size that was used.")));
    }

    private static List<Object> listParameters(final ApiEntity entity) {
        final String filterable = fieldNames(entity, true);
        final String sortable = fieldNames(entity, false);
        final List<Object> listParameters = new ArrayList<>();
        listParameters.add(JsonTree.object(
                "name", "filter", "in", "query", "required", Boolean.FALSE,
                "description", "A filter expression as JSON. Filterable properties: " + filterable + ".",
                "content", JsonTree.object(JSON, JsonTree.object("schema", ref("FilterExpression")))));
        listParameters.add(JsonTree.object(
                "name", "sort", "in", "query", "required", Boolean.FALSE,
                "description", "Comma-separated property names; a leading minus sorts descending. Sortable properties: " + sortable + "."
                        + (entity.defaultSort() == null ? "" : " Default: " + entity.defaultSort() + "."),
                "schema", JsonTree.object("type", "string")));
        listParameters.add(JsonTree.object(
                "name", "page", "in", "query", "required", Boolean.FALSE,
                "description", "The number of the page, starting at 0.",
                "schema", JsonTree.object("type", "integer", "minimum", 0L, "default", 0L)));
        listParameters.add(JsonTree.object(
                "name", "size", "in", "query", "required", Boolean.FALSE,
                "description", "The page size; at most " + entity.maxPageSize() + ".",
                "schema", JsonTree.object("type", "integer", "minimum", 1L, "maximum", (long) entity.maxPageSize(), "default", (long) OrmApiModel.DEFAULT_PAGE_SIZE)));
        return listParameters;
    }

    private void addPaths(final Map<String, Object> paths, final ApiEntity entity) {
        final String base = "/" + entity.path();
        final String name = entity.className();
        final List<Object> listParameters = listParameters(entity);

        final Map<String, Object> pageResponse = JsonTree.object("description", "One page of " + name + " entities.",
                "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name + "Page"))));
        final Map<String, Object> collectionItem = JsonTree.object();
        if (entity.allows(RestOperation.LIST)) {
            collectionItem.put("get", JsonTree.object(
                    "operationId", "list" + name,
                    "summary", "Lists " + name + " entities",
                    "description", lazyNote(entity),
                    "tags", List.of(name),
                    "parameters", listParameters,
                    "responses", JsonTree.object("200", pageResponse, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"))));
        }
        if (entity.allows(RestOperation.CREATE)) {
            final Map<String, Object> created = JsonTree.object("description", "The new entity, as it was stored.",
                    "headers", writeHeaders(entity, true),
                    "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name))));
            collectionItem.put("post", JsonTree.object(
                    "operationId", "create" + name,
                    "summary", "Creates a " + name,
                    "description", createNote(entity),
                    "tags", List.of(name),
                    "requestBody", entityBody(name),
                    "responses", JsonTree.object("201", created, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"), "409", responseRef("Conflict"))));
        }
        if (!collectionItem.isEmpty()) {
            secure(collectionItem, method -> security.securityFor(entity, method.equals("get") ? RestOperation.LIST : RestOperation.CREATE));
            paths.put(base, collectionItem);
        }

        if (entity.allows(RestOperation.SEARCH)) {
            final Map<String, Object> searchItem = JsonTree.object("post", JsonTree.object(
                    "operationId", "search" + name,
                    "summary", "Like the list, but with the parameters in the body; for filters that are too long for a URL",
                    "tags", List.of(name),
                    "requestBody", JsonTree.object("required", Boolean.TRUE, "content", JsonTree.object(JSON, JsonTree.object("schema", ref("SearchRequest")))),
                    "responses", JsonTree.object("200", pageResponse, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"))));
            secure(searchItem, method -> security.securityFor(entity, RestOperation.SEARCH));
            paths.put(base + "/search", searchItem);
        }

        final Map<String, Object> idParameter = JsonTree.object("name", "id", "in", "path", "required", Boolean.TRUE, "schema", JsonTree.object("type", "integer", "format", "int64"));
        final Map<String, Object> itemPath = JsonTree.object();
        if (entity.allows(RestOperation.GET)) {
            itemPath.put("get", getOperation(entity, "get" + name, "Gets a " + name + " by id", idParameter));
        }
        if (entity.allows(RestOperation.UPDATE)) {
            itemPath.put("put", updateOperation(entity, "replace" + name, "Replaces all properties of a " + name, idParameter, true));
            itemPath.put("patch", updateOperation(entity, "update" + name, "Changes the given properties of a " + name, idParameter, false));
        }
        if (entity.allows(RestOperation.DELETE)) {
            final List<Object> parameters = new ArrayList<>(List.of(idParameter));
            if (entity.versionField() != null) {
                parameters.add(ifMatchParameter(entity));
            }
            final Map<String, Object> responses = JsonTree.object("204", JsonTree.object("description", "The entity was deleted."), "403", responseRef("Forbidden"), "404", responseRef("NotFound"), "409", responseRef("Conflict"));
            addPreconditionResponses(entity, responses);
            itemPath.put("delete", JsonTree.object("operationId", "delete" + name, "summary", "Deletes a " + name, "tags", List.of(name), "parameters", parameters, "responses", responses));
        }
        if (!itemPath.isEmpty()) {
            secure(itemPath, method -> security.securityFor(entity, switch (method) {
                case "put", "patch" -> RestOperation.UPDATE;
                case "delete" -> RestOperation.DELETE;
                default -> RestOperation.GET;
            }));
            paths.put(base + "/{id}", itemPath);
        }
        if (entity.hasKey() && entity.allows(RestOperation.GET)) {
            final Map<String, Object> byKeyItem = JsonTree.object("get", getOperation(entity, "get" + name + "ByKey", "Gets a " + name + " by key",
                    JsonTree.object("name", "key", "in", "path", "required", Boolean.TRUE, "schema", JsonTree.object("type", "string"))));
            secure(byKeyItem, method -> security.securityFor(entity, RestOperation.GET));
            paths.put(base + "/by-key/{key}", byKeyItem);
        }
    }

    /**
     * Adds to the operations of a path item what it takes to call them: their own requirement if the host describes one (otherwise the
     * document's default applies), and the {@code 401} response if the requirement that applies needs authentication.
     *
     * @param ownByMethod tells, for the name of an HTTP method (lower case), the host's requirement for that operation, or null
     */
    @SuppressWarnings("unchecked")
    private void secure(final Map<String, Object> pathItem, final java.util.function.Function<String, List<ApiSecurityRequirement>> ownByMethod) {
        for (final Map.Entry<String, Object> entry : pathItem.entrySet()) {
            final Map<String, Object> operation = (Map<String, Object>) entry.getValue();
            final List<ApiSecurityRequirement> own = ownByMethod.apply(entry.getKey());
            if (own != null) {
                operation.put("security", own.stream().map(ApiSecurityRequirement::toTree).toList());
            }
            if (ApiSecurityRequirement.requiresAuthentication(own != null ? own : options.security())) {
                ((Map<String, Object>) operation.get("responses")).put("401", responseRef("Unauthorized"));
                usesUnauthorized = true;
            }
        }
    }

    private static Map<String, Object> idParameter(final String name, final String description) {
        return JsonTree.object("name", name, "in", "path", "required", Boolean.TRUE, "description", description,
                "schema", JsonTree.object("type", "integer", "format", "int64"));
    }

    /** {@code /parents/{id}/children}: the entities below another entity, listed and created like the ones at their own path. */
    private void addSubresourcePaths(final Map<String, Object> paths, final ApiSubresource sub) {
        final ApiEntity child = sub.child();
        final ApiEntity parent = sub.parent();
        final String name = child.className();
        final Map<String, Object> item = JsonTree.object();
        final Map<String, Object> notFound = JsonTree.object("404", responseRef("NotFound"));
        if (child.allows(RestOperation.LIST)) {
            final List<Object> parameters = new ArrayList<>();
            parameters.add(idParameter("id", "The id of the " + parent.className() + "."));
            parameters.addAll(listParameters(child));
            final Map<String, Object> responses = JsonTree.object(
                    "200", JsonTree.object("description", "One page of the " + name + " entities of the " + parent.className() + ".",
                            "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name + "Page")))),
                    "400", responseRef("BadRequest"), "403", responseRef("Forbidden"));
            responses.putAll(notFound);
            item.put("get", JsonTree.object(
                    "operationId", "list" + name + "Of" + parent.className(),
                    "summary", "Lists the " + name + " entities of a " + parent.className(),
                    "tags", List.of(name),
                    "parameters", parameters,
                    "responses", responses));
        }
        if (child.allows(RestOperation.CREATE)) {
            final Map<String, Object> created = JsonTree.object("description", "The new entity, as it was stored.",
                    "headers", writeHeaders(child, true),
                    "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name))));
            final Map<String, Object> responses = JsonTree.object("201", created, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"), "409", responseRef("Conflict"));
            responses.putAll(notFound);
            item.put("post", JsonTree.object(
                    "operationId", "create" + name + "In" + parent.className(),
                    "summary", "Creates a " + name + " in a " + parent.className(),
                    "description", createNote(child) + " The property '" + sub.field().name() + "' is set from the path; if the body has it, it has to name the same " + parent.className() + ".",
                    "tags", List.of(name),
                    "parameters", List.of(idParameter("id", "The id of the " + parent.className() + ".")),
                    "requestBody", entityBody(name),
                    "responses", responses));
        }
        if (!item.isEmpty()) {
            secure(item, method -> security.securityFor(child, method.equals("get") ? RestOperation.LIST : RestOperation.CREATE));
            paths.put("/" + parent.path() + "/{id}/" + sub.path(), item);
        }
    }

    /** {@code /users/{id}/groups} and {@code /users/{id}/groups/{groupId}}: the records of a join table below the entity at one end. */
    private void addLinkPaths(final Map<String, Object> paths, final ApiLink link) {
        final String name = link.className();
        final String base = "/" + link.parent().path() + "/{id}/" + link.path();
        final String where = link.parent().className() + " to " + link.other().className();
        final Map<String, Object> parentId = idParameter("id", "The id of the " + link.parent().className() + ".");
        final Map<String, Object> otherId = idParameter("otherId", "The id of the " + link.other().className() + " at the other end.");
        final Map<String, Object> notFound = JsonTree.object("404", responseRef("NotFound"));
        if (link.allows(RestOperation.LIST)) {
            final List<Object> parameters = new ArrayList<>(List.of(parentId));
            parameters.add(JsonTree.object("name", "page", "in", "query", "required", Boolean.FALSE, "description", "The number of the page, starting at 0.",
                    "schema", JsonTree.object("type", "integer", "minimum", 0L, "default", 0L)));
            parameters.add(JsonTree.object("name", "size", "in", "query", "required", Boolean.FALSE, "description", "The page size; at most " + link.other().maxPageSize() + ".",
                    "schema", JsonTree.object("type", "integer", "minimum", 1L, "maximum", (long) link.other().maxPageSize(), "default", (long) OrmApiModel.DEFAULT_PAGE_SIZE)));
            final Map<String, Object> responses = JsonTree.object(
                    "200", JsonTree.object("description", "One page of the links, in the order of the keys of the entities at the other end.",
                            "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name + "Page")))),
                    "400", responseRef("BadRequest"), "403", responseRef("Forbidden"));
            responses.putAll(notFound);
            final Map<String, Object> listItem = JsonTree.object("get", JsonTree.object(
                    "operationId", "list" + name + "Of" + link.parent().className() + "As" + capitalized(link.path()),
                    "summary", "Lists the links from a " + where,
                    "description", "Links cannot be filtered or sorted.",
                    "tags", List.of(name),
                    "parameters", parameters,
                    "responses", responses));
            secure(listItem, method -> security.securityForLink(link, RestOperation.LIST));
            paths.put(base, listItem);
        }
        final Map<String, Object> one = JsonTree.object();
        final List<Object> parameters = List.of(parentId, otherId);
        final String suffix = link.parent().className() + "As" + capitalized(link.path());
        if (link.allows(RestOperation.GET)) {
            final Map<String, Object> responses = JsonTree.object(
                    "200", JsonTree.object("description", "The link.", "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name)))),
                    "403", responseRef("Forbidden"));
            responses.putAll(notFound);
            one.put("get", JsonTree.object("operationId", "get" + name + "Of" + suffix, "summary", "Gets the link from a " + where, "tags", List.of(name), "parameters", parameters, "responses", responses));
        }
        if (link.allows(RestOperation.CREATE)) {
            final Map<String, Object> link200 = JsonTree.object("description", "The link existed and was changed.", "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name))));
            final Map<String, Object> link201 = JsonTree.object("description", "The link was created.", "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name))));
            final Map<String, Object> responses = JsonTree.object("200", link200, "201", link201, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"));
            responses.putAll(notFound);
            one.put("put", JsonTree.object(
                    "operationId", "put" + name + "Of" + suffix,
                    "summary", "Creates or changes the link from a " + where,
                    "description", "Both ends have to exist. The body has the extra properties of the link, and is optional; the two references come from the path, and are ignored in the body. Properties that are missing are set to null.",
                    "tags", List.of(name),
                    "parameters", parameters,
                    "requestBody", JsonTree.object("required", Boolean.FALSE, "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name)))),
                    "responses", responses));
        }
        if (link.allows(RestOperation.DELETE)) {
            final Map<String, Object> responses = JsonTree.object("204", JsonTree.object("description", "The link was deleted."), "403", responseRef("Forbidden"));
            responses.putAll(notFound);
            one.put("delete", JsonTree.object("operationId", "delete" + name + "Of" + suffix, "summary", "Deletes the link from a " + where, "tags", List.of(name), "parameters", parameters, "responses", responses));
        }
        if (!one.isEmpty()) {
            secure(one, method -> security.securityForLink(link, switch (method) {
                case "put" -> RestOperation.CREATE;
                case "delete" -> RestOperation.DELETE;
                default -> RestOperation.GET;
            }));
            paths.put(base + "/{otherId}", one);
        }
    }

    private static String capitalized(final String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static Map<String, Object> entityBody(final String name) {
        return JsonTree.object("required", Boolean.TRUE, "content", JsonTree.object(JSON, JsonTree.object("schema", ref(name))));
    }

    private static Map<String, Object> ifMatchParameter(final ApiEntity entity) {
        return JsonTree.object("name", "If-Match", "in", "header", "required", entity.requiresIfMatch(),
                "description", "The ETag of the version that the caller has seen; the request is refused if the entity has changed since."
                        + (entity.requiresIfMatch() ? "" : " Without it, the last write wins."),
                "schema", JsonTree.object("type", "string"));
    }

    private static void addPreconditionResponses(final ApiEntity entity, final Map<String, Object> responses) {
        if (entity.versionField() != null) {
            responses.put("412", responseRef("PreconditionFailed"));
            if (entity.requiresIfMatch()) {
                responses.put("428", responseRef("PreconditionRequired"));
            }
        }
    }

    /** The headers of a response that carries an entity that was written: the new ETag and Last-Modified. */
    private static Map<String, Object> writeHeaders(final ApiEntity entity, final boolean created) {
        final Map<String, Object> headers = JsonTree.object();
        if (created) {
            headers.put("Location", JsonTree.object("description", "Where the new entity is, relative to this request.", "schema", JsonTree.object("type", "string")));
        }
        if (entity.versionField() != null) {
            headers.put("ETag", JsonTree.object("description", "The version of the entity as stored.", "schema", JsonTree.object("type", "string")));
        }
        if (entity.modifiedField() != null) {
            headers.put("Last-Modified", JsonTree.object("description", "When the entity was last modified.", "schema", JsonTree.object("type", "string")));
        }
        return headers;
    }

    /** Explains which properties of the body count, since the schema is the one of reading. */
    private static String createNote(final ApiEntity entity) {
        return writeNote(entity) + " Properties that are missing keep the values of a new " + entity.className() + ".";
    }

    private static String writeNote(final ApiEntity entity) {
        return "The body has the properties of " + entity.className() + ". Read-only properties are ignored, so an entity that was read can be sent back. "
                + "Properties that are not part of the API are refused.";
    }

    private Map<String, Object> updateOperation(final ApiEntity entity, final String operationId, final String summary, final Map<String, Object> idParameter, final boolean replace) {
        final List<Object> parameters = new ArrayList<>(List.of(idParameter));
        if (entity.versionField() != null) {
            parameters.add(ifMatchParameter(entity));
        }
        final Map<String, Object> ok = JsonTree.object("description", "The entity, as it was stored.",
                "headers", writeHeaders(entity, false),
                "content", JsonTree.object(JSON, JsonTree.object("schema", ref(entity.className()))));
        final Map<String, Object> responses = JsonTree.object("200", ok, "400", responseRef("BadRequest"), "403", responseRef("Forbidden"), "404", responseRef("NotFound"), "409", responseRef("Conflict"));
        addPreconditionResponses(entity, responses);
        return JsonTree.object(
                "operationId", operationId,
                "summary", summary,
                "description", writeNote(entity) + (replace ? " All required properties must be given; nullable ones that are missing are set to null." : " Properties that are missing are left as they are."),
                "tags", List.of(entity.className()),
                "parameters", parameters,
                "requestBody", entityBody(entity.className()),
                "responses", responses);
    }

    private Map<String, Object> getOperation(final ApiEntity entity, final String operationId, final String summary, final Map<String, Object> parameter) {
        final Map<String, Object> headers = JsonTree.object();
        if (entity.versionField() != null) {
            headers.put("ETag", JsonTree.object("description", "The version of the entity; send it as If-None-Match to get 304 if unchanged.", "schema", JsonTree.object("type", "string")));
        }
        if (entity.modifiedField() != null) {
            headers.put("Last-Modified", JsonTree.object("description", "When the entity was last modified.", "schema", JsonTree.object("type", "string")));
        }
        final Map<String, Object> ok = JsonTree.object("description", "The entity.",
                "content", JsonTree.object(JSON, JsonTree.object("schema", ref(entity.className()))));
        if (!headers.isEmpty()) {
            ok.put("headers", headers);
        }
        final Map<String, Object> responses = JsonTree.object("200", ok);
        if (!headers.isEmpty()) {
            responses.put("304", JsonTree.object("description", "The entity has not changed."));
        }
        responses.put("403", responseRef("Forbidden"));
        responses.put("404", responseRef("NotFound"));
        return JsonTree.object(
                "operationId", operationId,
                "summary", summary,
                "tags", List.of(entity.className()),
                "parameters", List.of(parameter),
                "responses", responses);
    }

    /** Lazily loaded long texts are not part of lists; says so, or returns null if there are none. */
    private static String lazyNote(final ApiEntity entity) {
        final String lazy = entity.fields().stream()
                .filter(field -> Boolean.TRUE.equals(entity.table().getMetaData(field.name(), JanitorOrm.MetaData.LAZY_LOAD)))
                .map(ApiField::name)
                .collect(Collectors.joining(", "));
        return lazy.isEmpty() ? null : "The properties " + lazy + " are only included when getting a single entity, not in lists.";
    }

    private static String fieldNames(final ApiEntity entity, final boolean filter) {
        final String names = entity.fields().stream()
                .filter(field -> filter ? field.filterable() : field.sortable())
                .map(ApiField::name)
                .collect(Collectors.joining(", "));
        return names.isEmpty() ? "none" : names;
    }
}
