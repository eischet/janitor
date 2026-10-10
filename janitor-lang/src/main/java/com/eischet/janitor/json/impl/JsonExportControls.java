// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.impl;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Controls how objects are exported to JSON, e.g. whether the output is indented and which fields are left out. */
public class JsonExportControls {

    // omitCommonFields: used to be of type "CommonField", which is not visible here.
    // The code was too tightly coupled!
    // As a stopgap, this has been replaced by Object for now.

    // LATER: why is there no option to skip exporting empty fields!?
    // LATER: remove CommonField so that the JSON classes can be moved into a package of their own!

    /**
     * Creates export controls that leave out some fields.
     * @param pretty whether to indent the output
     * @param fields the fields to leave out
     * @return the controls
     */
    public static JsonExportControls omitting(final boolean pretty, final Object... fields) {
        final JsonExportControls controls = new JsonExportControls(pretty);
        controls.omitCommonFields.addAll(Arrays.asList(fields));
        return controls;
    }

    /**
     * @return export controls that produce indented JSON
     */
    public static JsonExportControls pretty() {
        return new JsonExportControls(true);
    }

    private final boolean pretty;
    private final Set<Object> omitCommonFields = new HashSet<>(4);

    /**
     * @return export controls that produce compact JSON
     */
    public static JsonExportControls standard() {
        return new JsonExportControls(false);
    }

    /**
     * Checks whether a field is left out of the export.
     * @param commonField the field
     * @return true if the field is omitted
     */
    public boolean isOmitting(final Object commonField) {
        return omitCommonFields.contains(commonField);
    }

    public JsonExportControls(final boolean pretty) {
        this.pretty = pretty;
    }

    /**
     * @return true if the output is indented
     */
    public boolean isPretty() {
        return pretty;
    }
}
