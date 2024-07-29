package com.eischet.janitor.toolbox.json.api;


import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public interface JsonExportable {

    boolean isList();
    boolean isObject();
    boolean isValue();
    boolean isDefaultOrEmpty();

    void writeJson(JsonOutputStream producer) throws JsonException;

    default @Language("JSON") String exportToJson(final @NotNull JsonOutputSupport environment) throws JsonException {
        return environment.writeJson(this::writeJson);
    }

    interface JsonOutputMapped {
        void writeAsJsonValue(JsonOutputStream producer) throws JsonException;
        default boolean isOmittedInJson() {
            return false;
        }
    }

}
