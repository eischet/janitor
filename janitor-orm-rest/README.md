# janitor-orm-rest

A REST API and an OpenAPI 3.1 description for the entities of a Janitor ORM `OrmDaoCollection`, derived from the meta-data of the entities' dispatch tables. There is no code to write per entity.

* `janitor-orm-rest` (this module): the model of the API, the schema and OpenAPI generation and a framework-neutral request handler.
* `janitor-orm-rest-servlet`: the Jakarta Servlet binding, which is the only HTTP binding.

## Exposing entities

An entity is exposed if and only if its dispatch table has a path; without one, it is not part of the API.

```java
public static final EntityDispatchTable<Article, MyDaos> DISPATCH = new EntityDispatchTable<>(...);

static {
    DISPATCH.dbTable("demo_article", "article_id", "article_key", "article_name", "seq_demo_article_id");
    DISPATCH.addLongColumn("id", "article_id", Article::getId, Article::setId);
    DISPATCH.addStringColumn("name", "article_name", Article::getName, Article::setName, 100);
    RestOrm.hidden(DISPATCH.addStringColumn("internalNote", "note", Article::getNote, Article::setNote, 200));
    RestOrm.expose(DISPATCH, "articles");
}
```

Of the properties of an exposed entity, the API shows those that are backed by a database column, are not hidden (`RestOrm.hidden`) and exist in the schema version of the collection. Everything else is derived:

| Source | Becomes |
|---|---|
| column type (`ColumnTypeHint`) | JSON type and format (`int64`, `date`, `date-time`, ...) |
| `MAX_LENGTH`, `DECIMAL_PRECISION`/`DECIMAL_SCALE` | `maxLength`, `x-precision`, `x-scale` |
| `HOST_NULLABLE`, `REQUIRED` | `nullable` and `required` |
| no setter, id, version and modification date columns | `readOnly` |
| `Janitor.MetaData.HELP`, `RestMetaData.EXAMPLE` | `description`, `example` |
| `FILTERABLE`/`SORTABLE` (default: not for long texts and lazily loaded columns) | which properties filters and sort orders may use |

References to other entities are written as the **key** of the target; if the target has no key column, as its **id**. Reading accepts both.

Optional columns, declared on the entity's dispatch table with `versionColumn(...)` and `modifiedColumn(...)`: a version counter (the entity implements `Versioned`), which becomes the `ETag` and protects against lost updates (see "Writing"), and a modification timestamp (the entity implements `HasModifiedDate`, and the DAO sets it on every insert and update), which becomes `Last-Modified`.

Entities are read-only unless they opt in to more operations:

```java
RestOrm.allow(RestOrm.expose(DISPATCH, "articles"), RestOperation.CREATE, RestOperation.UPDATE, RestOperation.DELETE);
```

## Endpoints

For each exposed entity at the path `x`:

| Request | Meaning |
|---|---|
| `GET /x?filter=&sort=&page=&size=` | one page: `{"items": [...], "total": n, "page": p, "size": s}` |
| `POST /x/search` | the same, with `{"filter": {...}, "sort": [...], "page": 0, "size": 50}` as the body |
| `GET /x/{id}`, `GET /x/by-key/{key}` | one entity; `304` for `If-None-Match` / `If-Modified-Since` |
| `POST /x` | creates an entity (if the entity allows `CREATE`): `201`, with the stored entity and a `Location` |
| `PUT /x/{id}`, `PATCH /x/{id}` | replaces all / changes the given properties (`UPDATE`): `200` with the stored entity |
| `DELETE /x/{id}` | deletes an entity (`DELETE`): `204` |
| `GET /openapi.json` | the API description |

* `filter` is a `FilterExpression` as JSON, e.g. `{"field":"name","operator":"contains","valueString":"saw"}`, or a group with `"logic": "and"|"or"` and `"filters": [...]`. Only filterable properties are accepted; anything else is a `400`, so no SQL can be injected through field names.
* `sort` is a comma-separated list of property names, a leading `-` sorts descending.
* The page size is at most `RestMetaData.MAX_PAGE_SIZE` of the entity (default 500); larger requests are reduced to that.
* Long texts that are loaded lazily are only part of the answer for a single entity, not of lists.
* Errors are `application/problem+json`.

## Writing

Writing is something that an entity has to opt in to (`RestOrm.allow`), and the `RestPolicy` still decides about every request (`CREATE`, `UPDATE`, `DELETE`). The body is a JSON object with the properties of the entity, as the API returns them.

* Unknown properties, hidden properties and properties that the policy denies are refused (`400`). Read-only properties (id, version, modification date, properties without setter) are ignored, so that an entity that was read can be sent back.
* `POST` and `PUT` need all required properties; nullable properties that are missing are set to `null`. `PATCH` leaves missing properties as they are. `null` is refused for properties that cannot be null.
* Date-times are accepted with an offset (`2026-03-04T05:06:07+01:00`, converted to the time zone of the collection's clock) or without (taken as they are).
* Texts longer than their column (`400`) and references to something that does not exist (`400`) are refused before anything is written. Violations of database constraints, e.g. a duplicate key, are `409`.
* Every request is one transaction. With a `RestPolicy.restriction`, an entity that is created or changed to lie outside of it is rejected (`403`) and rolled back.
* **Versioned entities** (`versionColumn`): every update counts the version up, and the response carries the new `ETag`. A client can protect itself against lost updates by sending the `ETag` that it has seen in an `If-Match` header with `PUT`, `PATCH` and `DELETE` (`*` means "whatever it is"): if the entity has changed meanwhile, the answer is `412` and nothing is written. The header is optional; without it, the last write wins. An entity can insist on it with `RestOrm.requireIfMatch(table)` (`428` without it). The check is atomic (`update ... where version = ?`, throwing `OptimisticLockError`), so it holds for concurrent requests, and the DAO enforces it for scripts and other Java code, too.

## Sub-resources: the entities below an entity

An entity that refers to another one (the folder of a doc) can be listed and created below it. Declare it on the reference property of the child:

```java
RestOrm.subresource(DISPATCH.addReference("folder", "folder_id", Folder.DISPATCH, Doc::getFolder, Doc::setFolder, doc -> doc.source), "docs");
```

* `GET /folders/{id}/docs` lists the docs of the folder, with `filter`, `sort`, `page` and `size` as for `GET /docs`; `POST /folders/{id}/docs` creates a doc in it (if `Doc` allows `CREATE`). In the body, the `folder` property is optional; if it is given, it has to name that folder (by id or key).
* Individual docs stay at `/docs/{id}`; the `Location` of a created doc points there.
* Both entities have to be exposed. The caller needs `GET` on the folder and `LIST` / `CREATE` on the docs, the restrictions of both apply, and a folder that the caller cannot see is `404`.

## Links: the records of a join table

A join table connects two entities, and can have extra columns. Its records are exposed as *links* below each of the entities, in the directions that you declare on the dispatch table of the join records:

```java
RestOrm.link(Membership.DISPATCH, "user", "groups", "group");   // /users/{id}/groups
RestOrm.link(Membership.DISPATCH, "group", "users", "user");    // /groups/{id}/users
RestOrm.allow(Membership.DISPATCH, RestOperation.CREATE, RestOperation.DELETE);
```

The arguments name properties of the join records, so self-joins work, too. The primary key of the join table must be exactly the two columns of the references, and both entities have to be exposed; otherwise the model refuses to build.

* `GET /users/{id}/groups` lists the join records (both references, as keys, and the extra columns), in the order of the keys at the other end, with `page` and `size`. Links cannot be filtered or sorted.
* `GET /users/{id}/groups/{groupId}` is one of them (`404` if the group is not linked); `PUT` creates it (`201`) or changes it (`200`), with the extra columns in an optional body; `DELETE` removes it (`204`). `PUT` needs `CREATE` and `DELETE` needs `DELETE` on the join table's operations. The two references come from the path and are ignored in the body.
* Both ends are addressed by id. The restrictions of both entities apply: what the caller cannot see is `404`, and missing from lists.
* `RestPolicy.permitsLink` decides. By default, links are part of the entity that they are below: reading needs `GET` on it, changing needs `UPDATE` ("managing the groups of a user is changing the user"). Join tables are not versioned, so there is no `If-Match` for links.

## Access control

The API has no notion of users or roles. The host supplies two things, both typed with the host's own kind of user `U` (the class of the people that it knows, a name, a token):

* a `RestUserResolver<U>` that tells, for a servlet request, who is calling (`null` for an anonymous caller). The servlet does not look at the container's own security (`getUserPrincipal()`, `isUserInRole()`), unless the resolver does.
* a `RestPolicy<U>` that decides, given that user: whether an operation on an entity is allowed (`permits`, `403` otherwise), which rows are visible (`restriction`, a filter that is added to every request; outside of it an entity is "not found"), and which properties are hidden (`deniedFields`).

The handler takes a `RestPolicy<? super U>`, so a policy for a more general kind of user (`RestPolicy.<Object>allowAll()`) works with any.

A request that is not permitted is answered with **401** if there is no user (the caller can try again after authenticating, and is told how: see `WWW-Authenticate` below), and with **403** if there is one. This covers `permits` and `permitsLink`, and entities that would be created or changed to lie outside of a `restriction`. A `RestRejection` from a hook has the status that the hook chose.

### Authentication in the API description

The API description tells tools and clients how to authenticate, if you tell it. Declare the schemes and what the operations need in the options, and give the same options to the handler:

```java
OpenApiOptions options = OpenApiOptions.defaults()
        .withSecurityScheme("bearer", ApiSecurityScheme.bearer("JWT"))
        .withSecurity(ApiSecurityRequirement.of("bearer"));     // what every operation needs, unless said otherwise

new OrmRestHandler<>(daoCollection, OrmApiModel.of(daoCollection), myPolicy, options);
```

* Schemes: `bearer`, `basic`, `apiKey`, `oauth2AuthorizationCode`, `oauth2ClientCredentials`, `openIdConnect` and `mutualTls`, each with `withDescription(...)`. They become `components.securitySchemes`, which gives Swagger UI its "Authorize" button and tells client generators what to send.
* The requirements are alternatives, of which one is enough. `withAnonymousAllowed()` adds "or nothing": authentication is then optional, and operations are not documented with `401`.
* Operations that deviate (public ones, or ones that need other scopes) are described by the policy, which is also a `RestSecurityDescription`: override `securityFor(entity, operation)` (`null`: as above; an empty list: public) and, for links, `securityForLink` (by default as the entity that the link is below). Sub-resources use what their entities say.
* This is a description only. Every request is decided by the policy; the description never grants or refuses anything. It cannot show what depends on who is calling: restrictions and denied properties. The API description says so.
* A `401` carries `WWW-Authenticate` with the challenge of the schemes that the operation needs: `Bearer` for bearer tokens, OAuth 2 and OpenID Connect, `Basic realm="..."` for basic authentication, nothing for API keys and certificates (unless you give one with `withChallenge`). Without any declared scheme, the `401` has no challenge.
* `/openapi.json` is public by default; `RestPolicy.permitsApiDescription(user)` can restrict it.

### Taking part in what is written

The policy also has hooks that run in the transaction of a write request, after the body was applied and checked and immediately before the entity is inserted, updated or deleted (`beforeWrite`; for links `beforeLinkWrite`). They see the user, so a host that records who changed what can do it in one place; whatever the hook sets on the entity is written with it:

```java
public class MyPolicy implements RestPolicy<Person> {
    @Override public boolean permits(Person user, ApiEntity entity, RestOperation op) { return user != null; }

    @Override
    public void beforeWrite(Person user, ApiEntity entity, RestOperation op, OrmEntity instance, DatabaseConnection conn) {
        if (instance instanceof Tracked tracked && op != RestOperation.DELETE) {
            tracked.setLastModifiedBy(user.getId());
        }
        if (op == RestOperation.DELETE && !user.isAdmin()) {
            throw new RestRejection(403, "only admins delete");   // rolls the request back
        }
    }
}
```

The entity is never a cached one. A `RestRejection` (status 4xx) refuses the request: nothing is written, and the caller gets that status. The hooks only see writes that come through the API; writes through scripts or the DAOs do not pass through them.

`RestPolicy.denyAll()` is a good starting point for a policy that grants selectively. `RestPolicy.allowAll()` is for tests and for APIs that are protected otherwise.

Anything that is exposed can be read by everybody that the policy lets in: review what `RestMetaData.PATH` is set on, and hide properties that are not meant for clients.

## Servlet

```java
OrmRestServlet.register(servletContext, "api", "/api/*",
        () -> OrmRestHandler.of(daoCollection, myPolicy),   // myPolicy is a RestPolicy<Person>
        request -> sessionOf(request).person());             // a RestUserResolver<Person>
```

The pattern must end in `/*`; the API description is then at `/api/openapi.json`. The handler is created on the first request, so the DAOs need not exist when the servlet is registered. Request bodies are limited to one million characters (`setMaxBodyChars`).

## Not part of the API yet

Addressing parents and the other end of links by key (`by-key`), filtering and sorting links, and properties that are not backed by a column.
