# Pull-up candidates: Cockpit → janitor-orm

Notes from a review of `janitor-orm` against its main client, the Cockpit backend
(`D:\projects\cockpit\server\cockpit-backend`), looking for code that is generic ORM
infrastructure but currently lives in the client instead of the library.

## 1. `getValueExpander` for foreign keys — pull up and simplify

### The duplication

`MisoEntityBase.getValueExpander(EntityWrangler<U, MisoDaoCollection>)`
([MisoEntityBase.java:280](../cockpit/server/cockpit-backend/src/main/java/miso/entity/MisoEntityBase.java))
and `Cockpit2DaoCollection.getValueExpander(EntityWrangler<U, Cockpit2DaoCollection>)`
([Cockpit2DaoCollection.java:66](../cockpit/server/cockpit-backend/src/main/java/cockpit/data2/Cockpit2DaoCollection.java))
are **the same ~35 lines of code, copy-pasted** into two unrelated entity hierarchies
inside Cockpit itself. Nothing in the body is Cockpit-specific — it only touches types
that already live in `janitor-orm`/`janitor-api`: `EntityWrangler`, `ForeignKey`,
`ForeignKeyInteger`, `ForeignKeyString`, `ForeignKeyIdentity`, `Janitor.NULL`, `JNumber`,
`JString`. The only "custom" bit is `instance.getSource()`, which is just how each
hierarchy exposes its `Uplink`.

A third, narrower overload,
`MisoEntityBase.getValueExpander(Class<U>, ForeignKeyNull<U>, Function<MisoDaoCollection, Dao<U>>)`,
is really the same logic again, hand-rolled without an `EntityWrangler` — it has exactly
**one** call site (the `modifiedBy` property in `MisoEntityBase.setup`). Since every
entity already has an `EntityWrangler` (its `DISPATCH`, an `EntityDispatchTable`), this
overload is redundant, not a genuine variant.

This is used constantly: ~85 call sites across `miso/entity/*` and `miso/join/*` alone,
always in the same shape:

```java
MisoActionType.DISPATCH.addReference(DISPATCH, "actionType", "act_type_id",
    MisoAction::getActionType, MisoAction::setActionType,
    getValueExpander(MisoActionType.DISPATCH));
```

### Proposal

Move the logic into `EntityWrangler` in `janitor-orm` (`janitor-orm/src/main/java/com/eischet/janitor/orm/meta/EntityWrangler.java`) as a default method, parameterized only by how to get from the owning instance to *this* wrangler's uplink type `U`:

```java
default <S extends OrmObject> @NotNull ValueExpander<S, ForeignKey<T>> getValueExpander(
        final @NotNull Function<S, U> uplinkOf) {
    return (instance, value) -> {
        if (value instanceof ForeignKey<?> fk && fk.getReferencedEntityClass() == getWrangledClass()) {
            //noinspection unchecked
            return (ForeignKey<T>) fk;
        }
        if (getWrangledClass().isInstance(value) && value instanceof ForeignKeyIdentity<?>) {
            //noinspection unchecked
            return (ForeignKeyIdentity<T>) value; // every entity is its own ForeignKeyIdentity
        }
        if (value == Janitor.NULL) {
            return getNullReference();
        }
        if (value instanceof JNumber idPointer) {
            return new ForeignKeyInteger<>(idPointer.toLong(), retrieveDao(uplinkOf.apply(instance)));
        }
        if (value instanceof JString keyPointer) {
            return new ForeignKeyString<>(keyPointer.janitorGetHostValue(), retrieveDao(uplinkOf.apply(instance)));
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to a foreign key referencing " + getSimpleClassName());
    };
}
```

(Two redundant `instanceof ForeignKey<?>` checks and the log-warning-then-cast-anyway branch
from the original are dropped as dead weight — see "simplifications" below.)

Then simplify `addReference` itself to take the uplink accessor directly instead of
making every call site build a `ValueExpander`:

```java
default <V extends OrmObject> void addReference(final DispatchTable<V> dispatch,
                                                  final String propertyName,
                                                  final String columnName,
                                                  final NotNullGetter<V, ForeignKey<T>> getter,
                                                  final NotNullSetter<V, ForeignKey<T>> setter,
                                                  final Function<V, U> uplinkOf) {
    addReference(dispatch, propertyName, columnName, getter, setter, getValueExpander(uplinkOf));
}
```

Every one of the ~85 Cockpit call sites then shrinks from this:

```java
MisoActionType.DISPATCH.addReference(DISPATCH, "actionType", "act_type_id",
    MisoAction::getActionType, MisoAction::setActionType,
    getValueExpander(MisoActionType.DISPATCH));
```

to this:

```java
MisoActionType.DISPATCH.addReference(DISPATCH, "actionType", "act_type_id",
    MisoAction::getActionType, MisoAction::setActionType, MisoBase::getSource);
```

`Cockpit2*` gets the exact same benefit for free, via `Cockpit2Entity::getSource`, once
its entities migrate — no second implementation needed. Both `MisoEntityBase` and
`Cockpit2DaoCollection` can then delete their private copies entirely.

### Simplifications spotted in the original while comparing the two copies

- `if (value instanceof ForeignKey<?> fk) { ...warn...; return (ForeignKey<U>) value; }` is
  immediately followed by two more `instanceof ForeignKey<?>` checks that can never be
  reached (the first one already matches any `ForeignKey`). The "warn but cast anyway"
  branch silently accepts a foreign key pointing at the wrong entity type — worth
  deciding explicitly whether that should become a thrown `IllegalArgumentException`
  instead of a warning + unchecked cast (looks like a latent correctness bug, not just
  redundant code).
- `ForeignKeyInteger<?> fk && ...` and `ForeignKeyIdentity<?> id` bindings introduce
  names (`fk`, `id`) that go unused after the pattern match in some branches — minor,
  but the rewrite above drops them.
- The single-callsite `Class`/`ForeignKeyNull`/`Function<Uplink, Dao<U>>` overload can be
  deleted outright once `modifiedBy` in `MisoEntityBase.setup` uses
  `MisoPerson.DISPATCH.getValueExpander(...)` like everything else.

### Migration note

This touches ~85 call sites in Cockpit plus the one in `Cockpit2EntityBase`. Recommend
doing it in two steps: (1) add the new default methods to `EntityWrangler` in
`janitor-orm` and cut a release, (2) do a mechanical find/replace pass in Cockpit
(`getValueExpander(X.DISPATCH)` → `MisoBase::getSource`, or keep the old cockpit-local
`getValueExpander` methods as thin `@Deprecated` wrappers delegating to the new one for a
transition period).

## 2. Duplicated property-builder helpers — delete, don't pull up (already exist)

`MisoEntityBase` defines its own `addIntProperty`, `addLongProperty`,
`addNullableLongProperty`, `addDateProperty`, `addBooleanProperty` static helpers
([MisoEntityBase.java:362-401](../cockpit/server/cockpit-backend/src/main/java/miso/entity/MisoEntityBase.java)).
These are **byte-for-byte equivalent** (same signature, same body) to helpers that
already exist in `janitor-orm`'s `OrmObject`
(`janitor-orm/src/main/java/com/eischet/janitor/orm/entity/OrmObject.java`):
`addLongProperty`, `addNullableLongProperty`, `addDateProperty`, `addBooleanProperty` are
identical; `addIntProperty` corresponds to `OrmObject.addIntegerProperty` (only the name
differs).

Nothing needs to move — this is the opposite situation from §1: the library already has
it, Cockpit just doesn't know. Recommend deleting Cockpit's copies and calling
`OrmObject.addLongProperty(...)` etc. directly (or adding a same-named `addIntProperty`
alias to `OrmObject` if you'd rather not touch the few call sites that use that name).

## 3. Bigger, riskier candidates (worth a separate discussion, not proposed here)

These also look generic, but pulling them up would require a design decision first, so
they're listed for awareness rather than as ready-to-do items:

- **`MisoEntityBase.setup(...)`** builds the common entity columns (`id`, `key`, `name`,
  `modifiedBy`/`modifiedDate`/`createdDate` change tracking, `discontinuedDate`/`active`
  soft-delete). The shape is generic, but it's built directly against `MisoEntity`
  getters/setters and `MisoDaoCollection`. Pulling this up would mean introducing
  something like a `Trackable`/`Discontinuable` marker interface in `janitor-orm` that
  entities opt into, similar to how `ChangeTrackedOrmEntity` already works for
  field-level change tracking — that's a real API addition, not a refactor.
- **`MisoEntityBase.addDatabaseMethods(...)`** wires up the `update`/`insert`/`merge`/
  `reload` script methods from a `Dao<T>` retriever. Most of it is generic, but `insert`
  has hard-coded special cases for `MisoTicket`/`MisoAction`, and the whole thing relies
  on `MisoDaoCollection.executeTransaction(...)`/`call(...)`, which aren't part of the
  `Uplink` contract in `janitor-orm` (currently just a marker interface). Generalizing
  this would mean deciding whether `Uplink` should grow a standard
  transaction-execution API — worth doing, but a separate decision from the
  `getValueExpander` cleanup.
- **Phase 2 for §1**: `OrmDaoCollection` already maintains a registry of
  `EntityDispatchTable` by class name (`getEntityDispatchTable(Class<T>)`). Once §1 is
  in place, `getValueExpander` could look up the target wrangler from that registry via
  the referenced entity's class instead of requiring the caller to pass `X.DISPATCH`
  explicitly — shrinking call sites further. Left out of the proposal above to keep it a
  minimal, mechanical change first.
