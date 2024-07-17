package com.eischet.janitor.api.util;

import com.eischet.janitor.api.types.JanitorObject;

/**
 * Class that compares two JanitorObjects.
 * @param <LEFT> the left object type
 * @param <RIGHT> the right object type
 */
public class JanitorComparison<LEFT extends JanitorObject, RIGHT extends JanitorObject> {

    private final Class<LEFT> leftClass;
    private final Class<RIGHT> rightClass;
    private final JanitorSemantics.Comparer<LEFT, RIGHT> comparer;

    /**
     * Create a new JanitorComparison.
     * @param leftClass the left class
     * @param rightClass the right class
     * @param comparer the comparer, which does the actual work, probably in the form of a lambda
     */
    JanitorComparison(final Class<LEFT> leftClass, final Class<RIGHT> rightClass, JanitorSemantics.Comparer<LEFT, RIGHT> comparer) {
        this.leftClass = leftClass;
        this.rightClass = rightClass;
        this.comparer = comparer;
    }

    /**
     * Perform the comparison.
     * @param left the left object
     * @param right the right object
     * @return the result of the comparison
     */
    public ComparisonResult compare(JanitorObject left, JanitorObject right) {
        if (leftClass.isAssignableFrom(left.getClass()) && rightClass.isAssignableFrom(right.getClass())) {
            return comparer.compare(leftClass.cast(left), rightClass.cast(right));
        } else {
            return null;
        }
    }

}
