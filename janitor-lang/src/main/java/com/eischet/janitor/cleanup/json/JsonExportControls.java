package com.eischet.janitor.cleanup.json;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JsonExportControls {

    // omitCommonFields: war vom Typ "CommonField", der hier aber nicht sichtbar ist.
    // Da ist der Code zu sehr miteinander verzahnt gewesen!
    // In meiner Not habe ich das erstmal ersetzt durch Object

    // LATER: wieso fehlt hier die Option, leere Felder nicht zu exportieren!?
    // LATER: CommonField heraausnehmen, damit ich die JSON-Sachen in ein eigenes Package packen kann!

    public static JsonExportControls omitting(final boolean pretty, final Object... fields) {
        final JsonExportControls controls = new JsonExportControls(pretty);
        controls.omitCommonFields.addAll(Arrays.asList(fields));
        return controls;
    }

    public static JsonExportControls pretty() {
        return new JsonExportControls(true);
    }

    private final boolean pretty;
    private final Set<Object> omitCommonFields = new HashSet<>(4);

    public static JsonExportControls standard() {
        return new JsonExportControls(false);
    }

    public boolean isOmitting(final Object commonField) {
        return omitCommonFields.contains(commonField);
    }

    public JsonExportControls(final boolean pretty) {
        this.pretty = pretty;
    }

    public boolean isPretty() {
        return pretty;
    }
}
