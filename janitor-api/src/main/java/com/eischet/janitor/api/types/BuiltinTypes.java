package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
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
    JList responsiveList(final DispatchTable<?> elementDispatchTable, @NotNull Stream<? extends JanitorObject> stream, @NotNull Consumer<JList> onChange);


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

    /**
     * Returns a numeric object that best represents the double value.
     * This method tries to return an int when there is no loss of precision.
     *
     * @param v double value
     * @return an appropriate numeric object (may not be 'null')
     */
    @NotNull JanitorObject numeric(double v);
    /*
     * This is mainly useful for parsing JSON, were otherwise any ints would be returned as doubles, which some APIs do not like at all.
     * (E.g. you get a list of things from an APi as 1.0, 2.0, ... and send those back to the same API and *boom* it doesn't like it.
     * This should not really be our problem, but turns out the one who cares needs to act because the other side will not do anything.)
     */

    /**
     * Returns a numeric object that best represents the double value.
     * This method tries to return an int when there is no loss of precision.
     *
     * @param v double value
     * @return an appropriate numeric object (might be 'null')
     */
    @NotNull JanitorObject nullableNumeric(Double v);


}
