# janitor-orm — Architecture Overview

Working reference document, written while getting oriented in the module. Not
end-user documentation — meant as a map for future sessions/reviews to start
from, so it can go stale as the code changes; re-verify before relying on it
for anything precise.

## What this module is

A small ORM that generates SQL (insert/update/delete/select) from the
metadata already attached to a class's Janitor `DispatchTable` — the same
table that makes a Java object scriptable from Janitor. Rather than a
separate annotation/config layer, `janitor-orm` piggybacks on
`com.eischet.janitor.api.metadata.HasMetaData`/`MetaDataKey`/`MetaDataBuilder`
(from `janitor-api`), which already lets you attach arbitrary key/value
metadata to a `DispatchTable` and to individual properties when you register
them. `JanitorOrm.MetaData` (in
[JanitorOrm.java](src/main/java/com/eischet/janitor/orm/JanitorOrm.java))
defines the ORM-specific keys: `TABLE_NAME`, `COLUMN_NAME`, `COLUMN_TYPE`,
`ID_FIELD`, `KEY_FIELD`, `NAME_FIELD`, `ID_SEQUENCE`, `MAX_LENGTH`,
`JOIN_TABLE_PK`, `WRANGLER`.

SQL generation itself is deliberately simple (see `StatementCreator`) —
plain `select`/`insert`/`update`/`delete` with `?` placeholders, no query
builder DSL, no joins in the generated SQL. Execution goes through
`janitor-dbxs` (`DatabaseConnection`, `SimplePreparedStatement`,
`SimpleResultSet`).

## The convention the module leans on

`OrmEntity` ([entity/OrmEntity.java](src/main/java/com/eischet/janitor/orm/entity/OrmEntity.java))
is the base contract every entity table must satisfy:
- `id` (long) — unique, auto-generated via a DB sequence, never hand-set by
  client code.
- `key` (nullable String) — a short, unique, human-meaningful lookup handle
  (e.g. a login name). Optional per entity (`KEY_FIELD` metadata may be
  absent).
- `name` (nullable String) — a non-unique descriptive label.
- `softDeleted` (boolean) — soft-delete flag; the ORM itself does not filter
  on this automatically anywhere I found — that's left to the app (e.g. via
  filter expressions).

Every column an entity exposes to the ORM must be registered as a Janitor
property AND carry `COLUMN_NAME` + `COLUMN_TYPE` metadata.
[entity/OrmObject.java](src/main/java/com/eischet/janitor/orm/entity/OrmObject.java)
provides `addStringProperty`/`addLongProperty`/`addDateProperty`/etc. static
helpers that do both in one call (register the Janitor property *and* attach
the ORM metadata), so entity classes typically look like:

```java
DISPATCH.addLongProperty(dispatch, "id", "person_id", Person::getId, Person::setId);
```

`ColumnTypeHint` ([sql/ColumnTypeHint.java](src/main/java/com/eischet/janitor/orm/sql/ColumnTypeHint.java))
is the small, fixed vocabulary of column types the marshalling code
understands: `INT, VARCHAR, NVARCHAR, NCLOB, BIT, DATETIME, DATE, DECIMAL`.
Every property that should be persisted needs one of these as its
`COLUMN_TYPE`.

## Layers, top to bottom

1. **Entity classes** (app-defined, not in this module) — implement
   `OrmEntity`, register a `DispatchTable` using the `OrmObject.addXxxProperty`
   helpers (or `EntityWrangler.addReference` for foreign keys).
2. **Wrangler** ([meta/Wrangler.java](src/main/java/com/eischet/janitor/orm/meta/Wrangler.java),
   [meta/EntityWrangler.java](src/main/java/com/eischet/janitor/orm/meta/EntityWrangler.java),
   [meta/SimpleWrangler.java](src/main/java/com/eischet/janitor/orm/meta/SimpleWrangler.java)) —
   one per entity class. Bundles: the Java class, its `DispatchTable`, a
   `ForeignKeyNull<T>` singleton (see below), a constructor function, and a
   function to retrieve the entity's `Dao` from an app-defined `Uplink`.
   `EntityWrangler.addReference(...)` is how you declare a foreign-key
   property (a `ForeignKey<T>` typed field, backed by an `INT` column,
   tagged with `Janitor.MetaData.REF` = the referenced class's simple name
   and `JanitorOrm.MetaData.WRANGLER` = a lazy `WranglerSource` pointing back
   at `this`, so mutually-referencing entities can register in either
   order). `SimpleWrangler.duplicate()` gives you a generic "clone by
   copying every assignable scripting attribute" for free.
3. **Uplink** ([dao/Uplink.java](src/main/java/com/eischet/janitor/orm/dao/Uplink.java)) —
   empty tagging interface for "whatever object holds all your DAOs" in the
   host app. Exists purely so the `<U>` type parameter that shows up
   everywhere has a name to point at.
4. **EntityIndex** ([meta/EntityIndex.java](src/main/java/com/eischet/janitor/orm/meta/EntityIndex.java)) —
   a registry, keyed by simple class name, of `DispatchTable`s, `Dao`s and
   `JoinDao`s. Populated as each `GenericDao`/`JoinDao` is constructed. Used
   at read time to resolve `Janitor.MetaData.REF` (the class name a foreign
   key column points to) back to the right `Dao` for building a
   `ForeignKeyInteger`.
5. **Dao / GenericDao** ([dao/Dao.java](src/main/java/com/eischet/janitor/orm/dao/Dao.java),
   [dao/GenericDao.java](src/main/java/com/eischet/janitor/orm/dao/GenericDao.java), 794
   lines — the core engine) — one instance per entity type (app subclasses
   `GenericDao<T>` and implements `getDataManager()`). On construction it
   reads `TABLE_NAME`/`ID_FIELD`/`KEY_FIELD` off the entity's `DispatchTable`
   and walks every registered attribute to build `columnForField`/
   `fieldForColumn` maps and the ordered `columns` list — this is the "column
   list" every generated SQL statement uses. Provides: `findById`,
   `findByKey`, `findAll` (with optional dialect-aware LIMIT/OFFSET),
   `findByQuery` (raw SQL escape hatch), `findByAssociation` (one-to-many by
   FK column), `findByFilter`/`countByFilter` (see filter engine below),
   `insert`/`update`/`delete`, plus `lazyLoadById`/`lazyLoadByKey`/
   `lazyLoadByAssociation` (transaction-wrapping convenience wrappers used by
   `ForeignKey` proxies and `AssociatedList`). `GenericDao` is itself a
   `JanitorObject`/`JCallable`, so a Dao is directly usable from scripts
   (`dao.getById(5)`, `dao(map)` to construct+populate, etc.) — see its
   static `DISPATCH` block.
6. **JoinDao** ([dao/JoinDao.java](src/main/java/com/eischet/janitor/orm/dao/JoinDao.java), 493
   lines) — parallel engine for many-to-many join-table rows
   (`OrmJoiner<L,R>`: a row with a `left`/`right` FK pair, keyed by a
   *composite* primary key declared via `JOIN_TABLE_PK` metadata, not a
   single surrogate `id`). Adds `merge()` (upsert: try update-by-PK, insert
   if zero rows changed) on top of the usual insert/update/delete.
7. **StatementCreator** ([sql/StatementCreator.java](src/main/java/com/eischet/janitor/orm/sql/StatementCreator.java)) —
   pure string-building of `select`/`insert`/`update`/`delete`/`count` SQL
   from table/column name lists, quoting columns via the `dbxs`
   `DatabaseDialect`. No knowledge of entities or metadata.
8. **CommonDao** ([dao/CommonDao.java](src/main/java/com/eischet/janitor/orm/dao/CommonDao.java)) —
   the `ColumnTypeHint` ↔ `JanitorObject` ↔ JDBC marshalling switch
   statements (`readProperty`/`writeProperty`), shared by both `GenericDao`
   and `JoinDao`. This is where a `ForeignKey<T>` gets written as its
   `.getId()` (or resolved from a `ForeignKeyString`'s key first), and where
   a NULL-able `INT` column is read as `Janitor.nullableInteger(...)` only if
   `Janitor.MetaData.HOST_NULLABLE` is `true` on that field — otherwise it
   assumes the Java field is a non-nullable primitive and reads it as a
   plain (never-null) integer.

## Foreign keys are first-class values, not just IDs

`ForeignKey<T>` ([ref/ForeignKey.java](src/main/java/com/eischet/janitor/orm/ref/ForeignKey.java))
is a `sealed interface` with five implementations, each a lazy proxy that
resolves via its owning `Dao` on first access (cached after that):

- `ForeignKeyInteger<T>` — the common case: numeric FK column, resolves via
  `dao.lazyLoadById(id)`.
- `ForeignKeyString<T>` — FK by the referenced entity's `key` column instead
  of `id`.
- `ForeignKeyIdentity<T>` — wraps an already-fully-loaded entity (or an
  entity that *is* its own FK target); never touches the database.
- `ForeignKeySearchResult<T>` — a lightweight id/key/name/softDeleted tuple
  (e.g. for autocomplete/search results) that is *both* a `ForeignKey` and a
  minimal `OrmEntity`.
- `ForeignKeyNull<T>` — the typed "no reference" singleton. Database `NULL`
  is represented by this, never by Java `null` — the whole point being that
  FK-typed fields can stay `@NotNull` in entity classes.

`ForeignKey.matches(...)`/`matchesWithUnknownType(...)` exist because
`equals()` across the different implementations can't reasonably compare
(`ForeignKeyInteger` vs. an already-resolved `ForeignKeyIdentity` need
different comparison logic) — use these instead of `Objects.equals()` when
you're not sure which concrete type you have.

## One-to-many vs. many-to-many collections

- **One-to-many**: `AssociatedList<T,U>`
  ([entity/AssociatedList.java](src/main/java/com/eischet/janitor/orm/entity/AssociatedList.java)) —
  held by the "one" side, lazily loads children via
  `dao.lazyLoadByAssociation(foreignKeyColumn, parent)` on first `.stream()`/
  `.lazyLoad()`. Exposes an add/size/clear/asList surface to scripts via a
  `JanitorAware` companion object.
- **Many-to-many**: `JoinedList<T,U,V,W>`
  ([entity/JoinedList.java](src/main/java/com/eischet/janitor/orm/entity/JoinedList.java)) —
  same lazy-load shape, but backed by a `JoinDao` and a `JoinLoader`
  function instead of a plain FK-column query; `.add()` from a script goes
  through `JoinDao.convertToEntity`/`insertForScript`.
- `JoinManager`/`GenericJoinManager` ([dao/JoinManager.java](src/main/java/com/eischet/janitor/orm/dao/JoinManager.java),
  [dao/GenericJoinManager.java](src/main/java/com/eischet/janitor/orm/dao/GenericJoinManager.java))
  look like an *earlier or parallel* attempt at the many-to-many problem —
  see "Loose ends" below, they don't appear to be finished/wired up.

## The filter engine (dynamic WHERE clauses)

`FilterExpression` ([filter/FilterExpression.java](src/main/java/com/eischet/janitor/orm/filter/FilterExpression.java))
is a JSON-serializable expression tree (leaf = `field`/`operator`/one typed
value; group = `logic` (`and`/`or`) + child `filters`), matched by a
maintainer-owned UI built on top of this structure rather than a third-party
component. `FilterOperator`
([filter/FilterOperator.java](src/main/java/com/eischet/janitor/orm/filter/FilterOperator.java))
is the operator vocabulary (`eq`, `neq`, `lt`, ..., `startswith`,
`contains`, `isnull`, `isempty`, ...). `GenericDao.expressionToSql()` walks
this tree and, for each leaf, looks up the field's `COLUMN_NAME`/
`COLUMN_TYPE` off the entity `DispatchTable` to build a parameterized SQL
fragment plus a `Prepper` (a closure that binds the actual value onto a
`SimplePreparedStatement` later). `getCustomExpressionHandler()` is the
documented override point for filter fields that aren't a plain column
(computed/joined fields) — return an `ExpressionHandler` with a raw SQL
fragment and its own `Prepper`. `FilterQuery`
([dao/FilterQuery.java](src/main/java/com/eischet/janitor/orm/dao/FilterQuery.java))
wraps a `FilterExpression` with paging (`maxRows`), `orderByClause`,
`queryTimeout`, and an optional raw-SQL `queryRewriter` hook (their own
comment: mainly useful in tests, e.g. injecting an MS-SQL `WAITFOR DELAY` to
test timeout handling).

## Change tracking (partial-column UPDATE)

`ChangeTracker`/`ChangeTrackedOrmEntity`/`GenericChangeTrackedDao`
(`entity/` package) are meant to let an entity mark individual fields as
modified so `update()` only writes the changed columns instead of the whole
row. Confirmed with the maintainer (2026-08-28): this is a deliberate,
early-stage experiment, not a finished feature awaiting a bugfix — no
concept for it has been settled yet, so `update()` still writes every column
regardless of `isChangeTracked()`. Don't "complete" this without checking in
first; the design itself is still open.

## Scripting surface

Both `GenericDao` and `JoinDao` are `JanitorComposed` + `JCallable`, so from
a script a Dao is directly callable as a constructor (`Person()`,
`Person({name: "..."})`, `Person('{"name":"..."}')`) and exposes
`insert`/`update`/`getById`/`getByKey`/`getAll`/`findById`/`findByKey`/
`queryForEach`/`verbose`/`tableName`/`columns`/`jsonSchema` as script-visible
members. `SqlTypeJanitorMapper`
([jdbc/SqlTypeJanitorMapper.java](src/main/java/com/eischet/janitor/orm/jdbc/SqlTypeJanitorMapper.java))
is a separate, entity-agnostic path: turns an arbitrary JDBC `ResultSet` row
into a `JList` of `JanitorObject`s purely from `java.sql.Types`, for ad-hoc
queries that aren't backed by a registered entity at all (see
`GenericDao.scriptQueryForEach`, which is column-by-column-typed rather than
using this mapper, so it's not clear yet where this mapper is actually
called from within the module — worth checking call sites in host apps).

## Loose ends noticed while reading (not verified as bugs, just flagged for a future pass)

- **`GenericChangeTrackedDao`** ([entity/GenericChangeTrackedDao.java](src/main/java/com/eischet/janitor/orm/entity/GenericChangeTrackedDao.java)) —
  confirmed intentionally unfinished, see "Change tracking" above. Not a bug
  to fix opportunistically; the design needs to be settled first.
- **`GenericJoinManager`/`ListManager`** — confirmed with the maintainer
  (2026-08-28): unfinished attempts at simplifying the many-to-many
  problem. `JoinedList` ([entity/JoinedList.java](src/main/java/com/eischet/janitor/orm/entity/JoinedList.java))
  is the actual, working, currently-used mechanism — leave
  `GenericJoinManager`/`ListManager` alone for now, don't try to finish or
  remove them without checking in first.
- **`lazy/LazyProperty` and `lazy/LazyClobProperty`** are not referenced
  anywhere else in `janitor-orm` (`GenericDao.readAllProperties` reads every
  column eagerly on every load, unconditionally). Confirmed with the
  maintainer (2026-08-28): also in development, not dead code — leave alone
  for now, to be picked up in a later pass.
- **Test coverage**: still thin overall (tests live in `janitor-tests`, per
  this project's convention). As of this writing there's
  [FilterExpressionTestCase.java](../janitor-tests/src/test/java/com/eischet/janitor/orm/filter/FilterExpressionTestCase.java),
  [FilterOperatorTestCase.java](../janitor-tests/src/test/java/com/eischet/janitor/orm/filter/FilterOperatorTestCase.java)
  and [ForeignKeyHashCodeTestCase.java](../janitor-tests/src/test/java/com/eischet/janitor/orm/ref/ForeignKeyHashCodeTestCase.java) —
  but `GenericDao`/`JoinDao`/the filter-to-SQL translation/actual foreign
  key resolution against a database have no test coverage at all yet.

## File map

```
JanitorOrm.java              MetaDataKey constants (the ORM's metadata vocabulary)

entity/
  OrmObject.java              root interface + addXxxProperty helpers (property + metadata in one call)
  OrmEntity.java               id/key/name/softDeleted contract, beforeInsert/Update/Delete hooks
  OrmJoined.java                marker: "this is a join-table row" (empty)
  OrmJoiner.java                 left/right FK pair contract for join-table rows
  OrmListAttribute.java          marker: "this attribute is list-like" (empty)
  Associated.java                 marker for one-to-many collections (empty)
  AssociatedList.java              one-to-many lazy collection (working)
  JoinedList.java                  many-to-many lazy collection (working)
  JoinLoader.java                   functional interface: how to load a JoinedList's rows
  ChangeTracker.java                 modified-field tracking contract
  ChangeTrackedOrmEntity.java          entity addon for change tracking
  GenericChangeTrackedDao.java          DAO addon for change tracking (experimental, unfinished by design)

meta/
  Wrangler.java / EntityWrangler.java / SimpleWrangler.java     per-entity-class metadata bundle + factory
  JoinWrangler.java / SimpleJoinWrangler.java                     same, for join-table classes
  WranglerSource.java                                              lazy indirection for addReference()
  EntityIndex.java                                                   className -> DispatchTable/Dao/JoinDao registry

dao/
  Dao.java / GenericDao.java              the entity CRUD engine (see above)
  JoinDao.java                              the join-table CRUD engine (see above)
  CommonDao.java                             ColumnTypeHint <-> JanitorObject <-> JDBC marshalling
  Uplink.java                                 tagging interface, "your app's DAO container"
  FilterQuery.java                             paging/ordering/timeout wrapper around a FilterExpression
  ExpressionHandler.java / ExpressionPrepperBuilder.java / Prepper.java / NamedPrepper.java
                                                  custom-filter-field and value-binding plumbing
  JoinManager.java / GenericJoinManager.java       apparently unfinished/superseded (see Loose ends)
  ListManager.java                                   empty marker, unused
  DaoLogging.java                                      optional lazy-load logging hook
  EntityChangeListener.java                              insert/update/delete listener contract

ref/
  ForeignKey.java (sealed) + ForeignKeyInteger/String/Identity/SearchResult/Null
                                              the five FK proxy implementations (see above)

filter/
  FilterExpression.java / FilterOperator.java / FilterLogic.java / MalformedExpression.java
                                              the JSON-serializable filter expression tree

sql/
  StatementCreator.java     plain SQL string building
  ColumnTypeHint.java         INT/VARCHAR/NVARCHAR/NCLOB/BIT/DATETIME/DATE/DECIMAL

lazy/
  LazyProperty.java / LazyClobProperty.java     unused within this module (see Loose ends)

jdbc/
  SqlTypeJanitorMapper.java     entity-agnostic JDBC-row -> JanitorObject mapper
```
