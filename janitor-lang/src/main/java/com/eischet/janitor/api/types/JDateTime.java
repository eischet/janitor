package com.eischet.janitor.api.types;


import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.tools.DateTimeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class JDateTime implements JConstant {

    private static final Logger log = LoggerFactory.getLogger(JDateTime.class);

    private static final JDateTimeClass myClass = new JDateTimeClass();

    private static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    protected final long dateTime;

    public JDateTime(final LocalDateTime dateTime) {
        this.dateTime = DateTimeUtilities.packLocalDateTime(dateTime);
    }

    public JDateTime(final String text) {
        if ("now".equals(text)) {
            dateTime = DateTimeUtilities.packLocalDateTime(LocalDateTime.now());
        } else if (text.lastIndexOf(':') != text.indexOf(':')) {
            dateTime = DateTimeUtilities.packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_LONG));
        } else {
            dateTime = DateTimeUtilities.packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_SHORT));
        }
    }

    public static JDateTime now() {
        return new JDateTime("now");
    }

    public static JanitorObject ofNullable(final LocalDateTime dateTime) {
        if (dateTime != null) {
            return new JDateTime(dateTime);
        } else {
            return JNull.NULL;
        }
    }

    @Override
    public LocalDateTime janitorGetHostValue() {
        return DateTimeUtilities.unpackLocalDateTime(dateTime);
    }

    public long getInternalRepresentation() {
        return dateTime;
    }


    @Override
    public String janitorToString() {
        return "@" + DATE_FORMAT_LONG.format(janitorGetHostValue());
    }

    @Override
    public boolean janitorIsTrue() {
        return dateTime != 0;
    }

    @Override
    public String toString() {
        return janitorToString();
    }


    public static JanitorObject parse(final String string, final String format) {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            final LocalDateTime d = LocalDateTime.parse(string, formatter);
            return ofNullable(d);
        } catch (DateTimeParseException e) {
            log.warn("error parsing date '{}' with format '{}'", string, format, e);
            return JNull.NULL;
        }
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject boundMethod = myClass.getBoundMethod(name, this);
        if (boundMethod != null) {
            return boundMethod;
        }
        if (Objects.equals("epoch", name)) {
            return JInt.of(janitorGetHostValue().toEpochSecond(ZoneId.systemDefault().getRules().getOffset(janitorGetHostValue())));
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    public JDate toDate() {
        return JDate.of(janitorGetHostValue().toLocalDate());
    }

    @Override
    public @NotNull String janitorClassName() {
        return "datetime";
    }

}