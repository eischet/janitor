package com.eischet.janitor.runtime;

import com.eischet.janitor.api.types.JanitorObject;

public class JanitorComparison<LEFT extends JanitorObject, RIGHT extends JanitorObject> {

    private final Class<LEFT> leftClass;
    private final Class<RIGHT> rightClass;
    private final JanitorSemantics.Comparer<LEFT, RIGHT> comparer;

    JanitorComparison(final Class<LEFT> leftClass, final Class<RIGHT> rightClass, JanitorSemantics.Comparer<LEFT, RIGHT> comparer) {
        this.leftClass = leftClass;
        this.rightClass = rightClass;
        this.comparer = comparer;
    }

    public ComparisonResult compare(JanitorObject left, JanitorObject right) {
        if (leftClass.isAssignableFrom(left.getClass()) && rightClass.isAssignableFrom(right.getClass())) {
            return comparer.compare(leftClass.cast(left), rightClass.cast(right));
        } else {
            return null;
        }
    }

}
