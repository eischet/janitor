package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.generator.writing.CodeOutputStream;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** A Java type that can be used for generated fields. */
public interface JavaType extends JanitorObject {

    /**
     * @return the package of the type
     */
    String getPackageName();
    /**
     * @return the name of the type
     */
    String getName();

    /**
     * @return code that writes the initial value of fields of this type, or null if they have none
     */
    default @Nullable Consumer<CodeOutputStream> defaultValueEmitter() {
        return null;
    }

    /**
     * @return true if this is a Java primitive type, which cannot be null
     */
    default boolean isPrimitive() {
        return false;
    }

}
