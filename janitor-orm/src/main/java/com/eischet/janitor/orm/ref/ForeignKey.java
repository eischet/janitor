package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.eischet.janitor.api.Janitor.nullable;

public sealed interface ForeignKey<T extends OrmEntity> extends JanitorObject, JsonWriter permits ForeignKeyNull, ForeignKeyInteger, ForeignKeyString, ForeignKeyIdentity, ForeignKeySearchResult {

    long getId();

    @NotNull Optional<T> resolve();

    @NotNull Optional<T> resolve(@NotNull DatabaseConnection conn) throws DatabaseError;

    void preResolve(@NotNull DatabaseConnection conn) throws DatabaseError;

    default @Nullable T resolveOrNull() {
        return resolve().orElse(null);
    }

    default @Nullable T resolveOrNull(final @NotNull DatabaseConnection conn) throws DatabaseError {
        return resolve(conn).orElse(null);
    }

    default <R> @NotNull R map(@NotNull final Function<T, R> mapper, @NotNull R defaultValue) {
        return resolve().map(mapper).orElse(defaultValue);
    }

    default <R> @Nullable R map(@NotNull final Function<T, R> mapper) {
        return resolve().map(mapper).orElse(null);
    }


    /**
     * Return the class of the referenced entity.
     * @return the class of the referenced entity, or null if the foreign key is null.
     */
    @Nullable Class<?> getReferencedEntityClass();

    /**
     * Return the name of the referenced entity class.
     * @return the name of the referenced entity class, or "null" if the foreign key is null.
     */
    @NotNull String getReferencedEntityClassName();

    /**
     * Check if the foreign key is null or undefined.
     * This is better than comparing ForeignKey objects with == or equals.
     * <p><em>Note</em>: We apply some rules to simplify this, e.g. IDs below 1 count as null.</p>
     * @return true if the foreign key is null or undefined, false otherwise
     */
    boolean isNull();

    static <U extends OrmEntity> boolean matches(@NotNull ForeignKey<U> a, @NotNull ForeignKey<U> b) {
        if (a == b) {
            return true;
        }
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a instanceof ForeignKeyNull<U> || b instanceof ForeignKeyNull<U>) {
            return false; // null != null, just like in SQL
        }
        if (!Objects.equals(a.getReferencedEntityClass(), b.getReferencedEntityClass())) {
            return false;
        }
        if (a instanceof ForeignKeyInteger<?> aa && b instanceof ForeignKeyInteger<?> bb) {
            return aa.getId() == bb.getId();
        }
        if (a instanceof ForeignKeyString<?> aa && b instanceof ForeignKeyString<?> bb) {
            return aa.getKey().equals(bb.getKey());
        }
        if (a instanceof ForeignKeyIdentity<U> aa && b instanceof ForeignKeyIdentity<U> bb) {
            return Objects.equals(aa.getIdentity(), bb.getIdentity());
        }
        if (a instanceof ForeignKeyIdentity<U> aa) {
            return isPointingTo(b, aa);
        }
        if (b instanceof ForeignKeyIdentity<U> bb) {
            return isPointingTo(a, bb);
        }
        return false;
    }

    static <U extends OrmEntity> boolean isPointingTo(@NotNull ForeignKey<U> pointer, @NotNull ForeignKeyIdentity<U> target) {
        return (pointer instanceof ForeignKeyInteger<U> idPointer && target.getIdentity().getId() == idPointer.getId() ||
                pointer instanceof ForeignKeyString<U> keyPointer && keyPointer.getKey().equalsIgnoreCase(target.getIdentity().getKey()));
    }


    @Override
    default @NotNull JanitorObject janitorUnpack() {
        return nullable(resolve());
    }


}
