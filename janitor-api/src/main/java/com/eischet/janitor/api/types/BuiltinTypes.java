package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.builtin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Interface / factory for working with built-in types.
 */
public interface BuiltinTypes {

    /**
     * Return an empty String.
     *
     * @return an empty string
     */
    @NotNull
    JString emptyString();

    /**
     * Return a new String object.
     *
     * @param value a Java String, which may be null
     * @return a JString from the Java String, or the empty string when null
     */
    @NotNull
    JString string(final @Nullable String value);

    /**
     * Return a new String object, or NULL if the value is null.
     * "nullable" refers to our scripting NULL, not to Java null.
     * (Typescript might express this more concisely as "JString | JNull")
     *
     * @param value a Java String or null
     * @return a JString from the Java String, or NULL
     */
    @NotNull
    JanitorObject nullableString(final @Nullable String value);

    @NotNull
    JMap map();

    @NotNull
    JList list();

    @NotNull
    JList list(int initialSize);

    @NotNull
    JList list(@NotNull List<? extends JanitorObject> list);

    @NotNull
    JList list(@NotNull Stream<? extends JanitorObject> stream);


    @NotNull
    JSet set();

    @NotNull JSet set(@NotNull Collection<? extends JanitorObject> list);

    @NotNull JSet set(@NotNull Stream<? extends JanitorObject> stream);


    @NotNull
    JInt integer(long value);

    @NotNull JInt integer(int value);

    @NotNull JanitorObject nullableInteger(@Nullable Number value);

    @NotNull
    JBinary binary(byte @NotNull [] arr);

    @NotNull JanitorObject nullableFloatingPoint(final Double value);


    @NotNull JFloat floatingPoint(double value);

    @NotNull JFloat floatingPoint(long value);

    @NotNull JFloat floatingPoint(int value);


    @NotNull JDuration duration(long value, JDuration.JDurationKind kind);

    @NotNull JRegex regex(@NotNull Pattern pattern);


    @NotNull JanitorObject nullableDateTime(@Nullable LocalDateTime dateTime);

    @NotNull JDateTime dateTime(@NotNull LocalDateTime dateTime);

    BuiltinTypeInternals internals();

    @NotNull JanitorObject nullableDateTimeFromLiteral(@Nullable String text);

    @NotNull JanitorObject nullableDateFromLiteral(@Nullable String text);

    @NotNull JDateTime now();


    @NotNull JDate today();

    @NotNull JDate date(final @NotNull LocalDate date);


    @NotNull JanitorObject nullableDate(@Nullable LocalDate date);

    @NotNull JDate date(long year, long month, long day);

    @NotNull JanitorObject parseNullableDate(JanitorScriptProcess process, String string, String format);

    /**
     * RAM saver: intern a string.
     * Janitor uses Strings in lots of places, and an environment can choose to intern these strings
     * to save space.
     * @param string a string
     * @return the string, possible interned
     */
    @Nullable String intern(final @Nullable String string);

}
