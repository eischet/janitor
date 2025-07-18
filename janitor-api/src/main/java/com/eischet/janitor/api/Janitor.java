package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This is the main entry point for working with the Janitor language in Java.
 * TODO: add methods for compiling and running scripts, so that the previous sentence is actually true :o)
 */
public class Janitor {

    public static final JNull NULL = JNull.NULL;

    public static final JBool TRUE = JBool.TRUE;
    public static final JBool FALSE = JBool.FALSE;

    private static final JanitorEnvironmentProvider automaticProvider =
            ServiceLoader.load(JanitorEnvironmentProvider.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .max(Comparator.comparingInt(JanitorEnvironmentProvider::priority))
                    .orElse(null);
    public static @Nullable JanitorEnvironmentProvider userProvider = null;

    /**
     * Return an empty String.
     *
     * @return an empty string
     */
    @NotNull
    public static JString emptyString() {
        return getBuiltins().emptyString();
    }

    /**
     * Return a new String object.
     *
     * @param value a Java String, which may be null
     * @return a JString from the Java String, or the empty string when null
     */
    @NotNull
    public static JString string(final @Nullable String value) {
        return getBuiltins().string(value);
    }

    /**
     * Return a new String object, or NULL if the value is null.
     * "nullable" refers to our scripting NULL, not to Java null.
     * (Typescript might express this more concisely as "JString | JNull")
     *
     * @param value a Java String or null
     * @return a JString from the Java String, or NULL
     */
    @NotNull
    public static JanitorObject nullableString(final @Nullable String value) {
        return getBuiltins().nullableString(value);
    }


    @NotNull
    public static JMap map() {
        return getBuiltins().map();
    }

    @NotNull
    public static JList list() {
        return getBuiltins().list();
    }

    @NotNull
    public static JList list(int initialSize) {
        return getBuiltins().list(initialSize);
    }

    @NotNull
    public static JList list(@NotNull List<? extends JanitorObject> list) {
        return getBuiltins().list(list);
    }

    @NotNull
    public static JList list(@NotNull Stream<? extends JanitorObject> stream) {
        return getBuiltins().list(stream);
    }


    @NotNull
    public static JSet set() {
        return getBuiltins().set();
    }

    public static @NotNull JSet set(@NotNull Collection<? extends JanitorObject> list) {
        return getBuiltins().set(list);
    }

    public static @NotNull JSet set(@NotNull Stream<? extends JanitorObject> stream) {
        return getBuiltins().set(stream);
    }


    public static @NotNull JInt integer(long value) {
        return getBuiltins().integer(value);
    }

    public static @NotNull JInt integer(int value) {
        return getBuiltins().integer(value);
    }

    public static @NotNull JanitorObject nullableInteger(@Nullable Number value) {
        return getBuiltins().nullableInteger(value);
    }

    public static @NotNull JBinary binary(byte @NotNull [] arr) {
        return getBuiltins().binary(arr);
    }

    public static @NotNull JanitorObject nullableFloatingPoint(final Double value) {
        return getBuiltins().nullableFloatingPoint(value);
    }


    public static @NotNull JFloat floatingPoint(double value) {
        return getBuiltins().floatingPoint(value);
    }

    public static @NotNull JFloat floatingPoint(long value) {
        return getBuiltins().floatingPoint(value);
    }

    public static @NotNull JFloat floatingPoint(int value) {
        return getBuiltins().floatingPoint(value);
    }


    public static @NotNull JDuration duration(long value, JDuration.JDurationKind kind) {
        return getBuiltins().duration(value, kind);
    }

    public static @NotNull JRegex regex(@NotNull Pattern pattern) {
        return getBuiltins().regex(pattern);
    }


    public static @NotNull JanitorObject nullableDateTime(@Nullable LocalDateTime dateTime) {
        return getBuiltins().nullableDateTime(dateTime);
    }

    public static @NotNull JDateTime dateTime(@NotNull LocalDateTime dateTime) {
        return getBuiltins().dateTime(dateTime);
    }

    public static @NotNull JanitorObject nullableDateTimeFromLiteral(@Nullable String text) {
        return getBuiltins().nullableDateTimeFromLiteral(text);
    }

    public static @NotNull JanitorObject nullableDateFromLiteral(@Nullable String text) {
        return getBuiltins().nullableDateFromLiteral(text);
    }

    public static @NotNull JDateTime now() {
        return getBuiltins().now();
    }


    public static @NotNull JDate today() {
        return getBuiltins().today();
    }

    public static @NotNull JDate date(final @NotNull LocalDate date) {
        return getBuiltins().date(date);
    }


    public static @NotNull JanitorObject nullableDate(@Nullable LocalDate date) {
        return getBuiltins().nullableDate(date);
    }

    public static @NotNull JDate date(long year, long month, long day) {
        return getBuiltins().date(year, month, day);
    }

    /**
     * RAM saver: intern a string.
     * Janitor uses Strings in lots of places, and an environment can choose to intern these strings
     * to save space.
     *
     * @param string a string
     * @return the string, possible interned
     */
    public static @Nullable String intern(final @Nullable String string) {
        return getBuiltins().intern(string);
    }

    /**
     * Returns a numeric object that best represents the double value.
     * This method tries to return an int when there is no loss of precision.
     *
     * @param v double value
     * @return an appropriate numeric object (may not be 'null')
     */
    public static @NotNull JanitorObject numeric(double v) {
        return getBuiltins().numeric(v);
    }
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
    public static @NotNull JanitorObject nullableNumeric(Double v) {
        return getBuiltins().nullableNumeric(v);
    }


    /**
     * Require a boolean value.
     * @param value the value to check
     * @return the value, if it's a boolean
     * @throws JanitorGlueException if the value is not a boolean
     */
    public static JBool requireBool(final JanitorObject value) throws JanitorGlueException {
        if (value instanceof JBool ok) {
            return ok;
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a boolean value, but got " + value.janitorClassName() + " instead.");
    }

    private Janitor() {
    }

    /**
     * Require a date value.
     * @param value the value to check
     * @return the value, if it's a date
     * @throws JanitorGlueException if the value is not a date
     */
    public static JDate requireDate(final JanitorObject value) throws JanitorGlueException {
        if (value instanceof JDate ok) {
            return ok;
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a date value, but got " + value.janitorClassName() + " instead.");
    }

    /**
     * Require a value to be string.
     * @param value the value
     * @return the value as string
     * @throws JanitorGlueException if the value is not a string
     */
    public static JString requireString(final JanitorObject value) throws JanitorGlueException {
        if (value instanceof JString ok) {
            return ok;
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a string value, but got " + value.janitorClassName() + " instead.");
    }

    /**
     * Require a datetime value.
     * @param value the value to check
     * @return the value, if it's a datetime
     * @throws JanitorGlueException if the value is not a datetime
     */
    public static JDateTime requireDateTime(final JanitorObject value) throws JanitorGlueException {
        if (value instanceof JDateTime ok) {
            return ok;
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a datetime value, but got " + value.janitorClassName() + " instead.");
    }

    public static JanitorObject nullableBooleanOf(final Boolean value) {
        return value == null ? JNull.NULL : toBool(value);
    }

    public static @Nullable JanitorEnvironmentProvider getUserProvider() {
        return userProvider;
    }

    public static void setUserProvider(final @NotNull JanitorEnvironmentProvider userProvider) {
        Janitor.userProvider = userProvider;
    }

    public static @NotNull JanitorEnvironmentProvider getAutomaticProvider() {
        return automaticProvider;
    }

    public static @NotNull JanitorEnvironment current() {
        if (userProvider != null) {
            return userProvider.getCurrentEnvironment();
        }
        if (automaticProvider != null) {
            automaticProvider.getCurrentEnvironment();
        }
        throw new IllegalStateException("No JanitorEnvironmentProvider was found. Either use JanitorEnvironmentLocator.setUserProvider to define a global one, or provide a ServiceLoader-based implementation.");
    }

    /**
     * @return the current environment's builtin types.
     */
    public static @NotNull BuiltinTypes getBuiltins() {
        return current().getBuiltinTypes();
    }

    /**
     * Map a java boolean to a JBool.
     * @param value the value
     * @return the JBool
     */
    public static JBool toBool(final boolean value) {
        return value ? JBool.TRUE : JBool.FALSE;
    }

    /**
     * Map a nullable java Boolean to a JBool. Null yields FALSE.
     * @param value the value
     * @return the JBool
     */
    public static JBool toBool(final Boolean value) {
        return value == Boolean.TRUE ? JBool.TRUE : JBool.FALSE;
    }

    public static class MetaData {

        public enum TypeHint {
            NUMBER, INTEGER, FLOAT, STRING, BOOLEAN, METHOD, DATE, DATETIME, LIST
        }

        /**
         * This optional meta-data annotation can be used to tell host code (= Java) what a property's type is supposed to be.
         * This can be useful, for example, when host code wants to write an object into a database.
         * Note that these are <b>Janitor</b> types, not Java types, so e.g. a FLOAT could be a Double in Java or a Float...
         */
        public static final MetaDataKey<TypeHint> TYPE_HINT = new MetaDataKey<>("type_hint", TypeHint.class);

        /**
         * Meta-Data: HELP for an object or property.
         * <p>
         * In Python, they call this a "docstring". Janitor does not currently have syntax to attach such a docstring to
         * an object/property, but it can be provided by the runtime.
         * </p>
         */

        public static final MetaDataKey<String> HELP = new MetaDataKey<>("help", String.class);


        /**
         * Meta-Data: NAME for an object or property.
         * <p>
         * Objects that do have names include functions, methods and classes.
         * For example, "function foo() { ... }" will have the NAME = "foo".
         * </p>
         */
        public static final MetaDataKey<String> NAME = new MetaDataKey<>("name", String.class);

        private MetaData() {}


    }
}
