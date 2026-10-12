// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds JSON schemas (the dialect of OpenAPI 3.1) for the entities of an {@link OrmApiModel}, from the properties and meta-data that the
 * model collected. It works on the model only; it does not touch the dispatch tables or their API.
 * <p>
 * The schemas are plain trees of maps and lists, so that the OpenAPI emitter can embed them. See {@link #toJson(Map)} for the text.
 * </p>
 */
public final class OrmSchemaBuilder {

    private OrmSchemaBuilder() {
    }

    /**
     * The schema of an entity as the API returns it.
     * @param entity the entity
     * @return the schema
     */
    public static @NotNull Map<String, Object> entitySchema(final @NotNull ApiFields entity) {
        final Map<String, Object> properties = JsonTree.object();
        final List<Object> required = new ArrayList<>();
        for (final ApiField field : entity.fields()) {
            properties.put(field.name(), fieldSchema(field));
            if (field.required()) {
                required.add(field.name());
            }
        }
        final Map<String, Object> schema = JsonTree.object("type", "object", "properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        schema.put("additionalProperties", Boolean.FALSE);
        return schema;
    }

    /**
     * The schema of one property.
     * @param field the property
     * @return the schema
     */
    public static @NotNull Map<String, Object> fieldSchema(final @NotNull ApiField field) {
        final Map<String, Object> schema = JsonTree.object();
        if (field.nullable()) {
            schema.put("type", List.of(field.jsonType(), "null"));
        } else {
            schema.put("type", field.jsonType());
        }
        if (field.jsonFormat() != null) {
            schema.put("format", field.jsonFormat());
        }
        if (field.maxLength() != null && "string".equals(field.jsonType()) && !field.isReference()) {
            schema.put("maxLength", field.maxLength());
        }
        if (field.readOnly()) {
            schema.put("readOnly", Boolean.TRUE);
        }
        if (field.isReference()) {
            schema.put("description", referenceDescription(field));
            schema.put("x-references", field.referencedEntity());
        } else if (field.description() != null) {
            schema.put("description", field.description());
        }
        if (field.precision() != null) {
            schema.put("x-precision", field.precision());
        }
        if (field.scale() != null) {
            schema.put("x-scale", field.scale());
        }
        if (field.example() != null) {
            schema.put("example", ExampleParser.parse(field.example()));
        }
        return schema;
    }

    private static String referenceDescription(final ApiField field) {
        final String how = field.referenceByKey() ? "the key" : "the id";
        final String prefix = field.description() == null ? "" : field.description() + " ";
        return prefix + "Reference to a " + field.referencedEntity() + ", given as " + how + " of the target.";
    }

    /**
     * @param schema a schema built by this class
     * @return its JSON text
     * @throws JsonException if it cannot be written
     */
    public static @NotNull String toJson(final @NotNull Map<String, Object> schema) throws JsonException {
        return JsonTree.toJson(schema);
    }

    /** Turns the JSON text of an example value into a value of the tree: only strings, numbers and booleans are supported. */
    private static final class ExampleParser {
        static Object parse(final String json) {
            final String text = json.trim();
            if (text.equals("true") || text.equals("false")) {
                return Boolean.valueOf(text);
            }
            if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
                return text.substring(1, text.length() - 1);
            }
            try {
                return text.contains(".") ? (Object) Double.valueOf(text) : (Object) Long.valueOf(text);
            } catch (NumberFormatException e) {
                return text; // not JSON we understand: pass it on as a string rather than failing the whole description
            }
        }
    }
}
