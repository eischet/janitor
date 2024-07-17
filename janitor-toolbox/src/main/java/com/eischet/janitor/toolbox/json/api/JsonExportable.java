package com.eischet.janitor.toolbox.json.api;


import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface JsonExportable {

    boolean isList();
    boolean isObject();
    boolean isValue();
    boolean isDefaultOrEmpty();

    void writeJson(JsonOutputStream producer) throws JsonException;

    default @Language("JSON") String exportToJson(final JsonOutputSupport environment) throws JsonException {
        return environment.writeJson(this::writeJson);
    }

    static void writeOptionalBooleanKeyValue(@NotNull final JsonOutputStream producer, final @Nullable Boolean value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }

    static void writeOptionalStringKeyValue(@NotNull final JsonOutputStream producer, final @Nullable String value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }

    static void writeOptionalNumberKeyValue(@NotNull final JsonOutputStream producer, final @Nullable Number value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }


    static void writeOptional(@NotNull JsonOutputStream producer, @Nullable JsonExportable object, final @NotNull String key) throws JsonException {
        if (object != null && !object.isDefaultOrEmpty()) {
            producer.key(key);
            object.writeJson(producer);
        }
    }

    static void writeOptionalList(@NotNull JsonOutputStream producer, @Nullable List<? extends JsonExportable> list, @NotNull String key) throws JsonException {
        if (list != null && !list.isEmpty()) {
            producer.key(key);
            producer.beginArray();
            for (final JsonExportable item : list) {
                item.writeJson(producer);
            }
            producer.endArray();
        }
    }


    static void writeOptionalMappedKeyValue(final @NotNull JsonOutputStream producer, final @Nullable JsonOutputMapped value, final @NotNull String key) throws JsonException {
        if (value != null && !value.isOmittedInJson()) {
            producer.key(key);
            value.writeAsJsonValue(producer);
        }
    }

    interface JsonOutputMapped {
        void writeAsJsonValue(JsonOutputStream producer) throws JsonException;
        default boolean isOmittedInJson() {
            return false;
        }
    }

}
