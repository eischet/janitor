package com.eischet.janitor.orm.entity;

import com.eischet.janitor.orm.ref.ForeignKey;

/**
 * Represent a join between two ORM entities, which can optionally carry additional data, too.
 * The sides are rather arbitrary, but they both need to be OrmEntity instances.
 * @param <L> "left side" of the join
 * @param <R> "right side" of the join
 */
public interface OrmJoiner<
        L extends OrmEntity,
        R extends OrmEntity
    > extends OrmObject {

    /**
     * Get the left side of the join.
     * @return the left side of the join
     */
    ForeignKey<L> getLeft();

    /**
     * Set the left side of the join.
     * @param left the left side of the join
     */
    void setLeft(ForeignKey<L> left);

    /**
     * Get the right side of the join.
     * @return the right side of the join
     */
    ForeignKey<R> getRight();

    /**
     * Set the right side of the join.
     * @param right
     */
    void setRight(ForeignKey<R> right);

}
