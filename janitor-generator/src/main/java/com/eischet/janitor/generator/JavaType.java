package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.generator.writing.CodeOutputStream;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface JavaType extends JanitorObject {

    String getPackageName();
    String getName();

    default @Nullable Consumer<CodeOutputStream> defaultValueEmitter() {
        return null;
    }

    default boolean isPrimitive() {
        return false;
    }

}
