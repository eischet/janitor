package com.eischet.janitor.json.impl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class FlexiDateParser implements JsonDeserializer<LocalDateTime> {

    private static final Logger log = LoggerFactory.getLogger(FlexiDateParser.class);

    private static final @Unmodifiable List<DateTimeFormatter> FORMATTERS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );


    @Override
    public LocalDateTime deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final String str = json.getAsString();
        if (str == null || str.isBlank()) {
            return null;
        }
        for (final DateTimeFormatter formatter : FORMATTERS) {
            try {
                return formatter.parse(str, LocalDateTime::from);
            } catch (DateTimeParseException ignore) {

            }
        }
        log.warn("not parseable: {}", str);
        return null;
    }
}
