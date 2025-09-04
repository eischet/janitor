package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This is the main entry point for working with the Janitor language in Java.
 *
 * You'll use this class to:
 * <ul>
 * <li>Access the basic built-in types NULL, TRUE and FALSE.</li>
 * <li>Create instances of Janitor classes from Java classes, e.g. Janitor.int(17).</li>
 * <li>Compile scripts into executable code.</li>
 * </ul>
 */
public final class Janitor {

    /**
     * 'null' in the Janitor language, designed to act just like null in Java and JavaScript, among others.
     */
    public static final JNull NULL = JNull.NULL;

    /**
     * The boolean value 'true'.
     */
    public static final JBool TRUE = JBool.TRUE;

    /**
     * The boolean value 'false'.
     */
    public static final JBool FALSE = JBool.FALSE;

    /**
     * Customisation of the scripting environment is mostly done in an Environment, and these can be
     * discovered using the ServiceLoader mechanism. The idea here is that you either implement
     * an environment provider yourself or choose an existing library that comes with one.
     * This alles your own code to automatically discover your environment, which could also be done
     * using a global variable. However, it also allows foreign / library code that you're using to
     * discover <b>your</b> provider, which is nice to have.
     * There's also a userProvider setting that takes precedence.
     */
    private static final JanitorEnvironmentProvider automaticProvider =
            ServiceLoader.load(JanitorEnvironmentProvider.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .max(Comparator.comparingInt(JanitorEnvironmentProvider::priority))
                    .orElse(null);

    /**
     * The userProvider takes precedence over discovered providers. If you want to simple use a single environment
     * in your app, use this setting to avoid all ceremony required to use the automaticProvider ans simply set
     * the user provider.
     */
    private static @Nullable JanitorEnvironmentProvider userProvider = null;

    /**
     * Compiles a script (source code) to a runnable script (an executable AST in the default implementation).
     *
     * @param runtime a runtime to use [TODO: this is actually only used to retrieve an environment, so this could and should be dropped soon!]
     * @param moduleName the name of the script module; this will show up in stack traces, so it's a good idea to pick an informative name here if you run lots of scripts
     * @param source the actual script source code
     * @return the script in executable form
     * @throws JanitorCompilerException on compiler errors
     */
    public RunnableScript compile(JanitorRuntime runtime, String moduleName, @Language("Janitor") String source) throws JanitorCompilerException {
        return runtime.compile(moduleName, source);
    }

    // TODO: properly document this, and when you're at it make this more useful.
    public RunnableScript checkCompile(JanitorRuntime runtime, String moduleName, @Language("Janitor") String source) throws JanitorCompilerException {
        return runtime.checkCompile(moduleName, source);
    }

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

    /**
     * Return a new JInt object, or NULL if the value is null.
     * @param value a Java Long or null
     * @return a JInt from the Java Long, or NULL
     */
    @NotNull
    public static JanitorObject nullableLong(final Long value) {
        if (value == null) {
            return Janitor.NULL;
        }
        return Janitor.integer(value);
    }

    /**
     * Return a new JInt object, or NULL if the value is null.
     * @param value a Java Integer or null
     * @return a JInt from the Java Long, or NULL
     */
    @NotNull
    public static JanitorObject nullableInteger(final Integer value) {
        if (value == null) {
            return Janitor.NULL;
        }
        return Janitor.integer(value);
    }

    /**
     * Return a new JMap object.
     * @return a new JMap object
     */
    @NotNull
    public static JMap map() {
        return getBuiltins().map();
    }

    /**
     * Return a new empty JList.
     * @return an empty JList
     */
    @NotNull
    public static JList list() {
        return getBuiltins().list();
    }

    /**
     * Returns a new empty list, preallocated for the specified number of elements.
     * Even though this is probably a negligible optimization, if you know the required list size before creating the list,
     * you can tell us about it here. If you don't have the size, do not bother and use the plain list() method.
     * @param initialSize the initial size of the list
     * @return an empty JList
     */
    @NotNull
    public static JList list(int initialSize) {
        return getBuiltins().list(initialSize);
    }

    /**
     * Returns a new JList as a copy of a java.util.List of JanitorObject elements.
     * @param list a list of JanitorObject elements
     * @return a JList containing all elements of the list argument
     */
    @NotNull
    public static JList list(@NotNull @Unmodifiable List<? extends JanitorObject> list) {
        return getBuiltins().list(list);
    }

    /**
     * Returns a new JList with all elements from the Stream of JanitorObject elements.
     * @param stream a stram of JanitorObject elements
     * @return a JList containing all elements of the stream argument
     */
    @NotNull
    public static JList list(@NotNull Stream<? extends JanitorObject> stream) {
        return getBuiltins().list(stream);
    }

    /**
     * Returns a new JList with all elements from the Stream of JanitorObject elements.
     * Use this when returning a writable list for an object property.
     * @param stream a stream of JanitorObject elements
     * @param onUpdate will be called whenever the JList object is changed
     * @return a JList containing all elements of the stream argument
     */
    @NotNull
    public static JList responsiveList(final DispatchTable<?> elementDispatchTable, @NotNull Stream<? extends JanitorObject> stream, @NotNull Consumer<JList> onUpdate) {
        return getBuiltins().responsiveList(elementDispatchTable, stream, onUpdate);
    }

    /**
     * Returns a new empty JSet.
     * @return a new empty JSet
     */
    @NotNull
    public static JSet set() {
        return getBuiltins().set();
    }

    /**
     * Returns a new JSet containing all elements of the provided collection.
     * @param collection a collection of JanitorObject elements
     * @return a JSet containing all elements of the collection
     */
    public static @NotNull JSet set(@NotNull Collection<? extends JanitorObject> collection) {
        return getBuiltins().set(collection);
    }

    /**
     * Returns a new JSet containing all elements of the provided stream.
     * @param stream a stream of JanitorObject elements
     * @return a JSet containing all elements of the stream
     */
    public static @NotNull JSet set(@NotNull Stream<? extends JanitorObject> stream) {
        return getBuiltins().set(stream);
    }

    /**
     * Returns a new JInt from the long argument.
     * @param value a number
     * @return a JInt representing the number
     */
    public static @NotNull JInt integer(long value) {
        return getBuiltins().integer(value);
    }

    /**
     * Returns a new JInt from the int argument.
     * @param value a number
     * @return a JInt representing the number
     */
    public static @NotNull JInt integer(int value) {
        return getBuiltins().integer(value);
    }

    /**
     * Returns a new JInt from the Number, or NULL in case Java null is passed in.
     * @param value a number or null
     * @return a JInt representing the number or NULL
     */
    public static @NotNull JanitorObject nullableInteger(@Nullable Number value) {
        return getBuiltins().nullableInteger(value);
    }

    /**
     * Returns a new JBinary object from the byte array.
     * @param arr a byte array
     * @return a JBinary object
     */
    public static @NotNull JBinary binary(byte @NotNull [] arr) {
        return getBuiltins().binary(arr);
    }

    /**
     * Returns a new JFloat from the passed in value, or NULL if Java null is passed.
     * @param value Java null or a Double
     * @return NULL or a JFloat representing the argument
     */
    public static @NotNull JanitorObject nullableFloatingPoint(final Double value) {
        return getBuiltins().nullableFloatingPoint(value);
    }

    /**
     * Returns a new JFloat from the passed in value.
     * @param value a double
     * @return a JFloat representing the argument
     */
    public static @NotNull JFloat floatingPoint(double value) {
        return getBuiltins().floatingPoint(value);
    }

    /**
     * Returns a new JFloat from the passed in value.
     * @param value a long
     * @return a JFloat representing the argument
     */
    public static @NotNull JFloat floatingPoint(long value) {
        return getBuiltins().floatingPoint(value);
    }

    /**
     * Returns a new JFloat from the passed in value.
     * @param value an int
     * @return a JFloat representing the argument
     */
    public static @NotNull JFloat floatingPoint(int value) {
        return getBuiltins().floatingPoint(value);
    }


    /**
     * Returns a new JDuration from the arguments.
     * @param value the number of e.g. seconds, minutes, hours...
     * @param kind the unit, e.g. seconds, minutes, hours
     * @return the JDuration
     */
    public static @NotNull JDuration duration(long value, JDuration.JDurationKind kind) {
        return getBuiltins().duration(value, kind);
    }

    /**
     * Returns a new JRegex for the Java regular expression pattern.
     * @param pattern a Java Pattern
     * @return a JRegex instance
     */
    public static @NotNull JRegex regex(@NotNull Pattern pattern) {
        return getBuiltins().regex(pattern);
    }

    /**
     * Returns a new JDateTime or NULL.
     * @param dateTime a Java LocalDateTime or Java null
     * @return a new JDateTime or NULL
     */
    public static @NotNull JanitorObject nullableDateTime(@Nullable LocalDateTime dateTime) {
        return getBuiltins().nullableDateTime(dateTime);
    }

    /**
     * Returns a new JDateTime.
     * @param dateTime a Java LocalDateTime
     * @return a new JDateTime
     */
    public static @NotNull JDateTime dateTime(@NotNull LocalDateTime dateTime) {
        return getBuiltins().dateTime(dateTime);
    }

    /**
     * Returns a new JDateTime from the datetime literal, or NULL.
     * @param text a datetime literal, or Java null
     * @return a new JDateTime from the datetime literal, or NULL
     */
    public static @NotNull JanitorObject nullableDateTimeFromLiteral(@Nullable String text) {
        return getBuiltins().nullableDateTimeFromLiteral(text);
    }

    /**
     * Returns a new JDate from the date literal, or NULL.
     * @param text a date literal, or Java null
     * @return a new JDate from the date literal, or NULL
     */
    public static @NotNull JanitorObject nullableDateFromLiteral(@Nullable String text) {
        return getBuiltins().nullableDateFromLiteral(text);
    }

    /**
     * Returns the current date and time as a JDateTime.
     * @return the current date and time as a JDateTime
     */
    public static @NotNull JDateTime now() {
        return getBuiltins().now();
    }

    /**
     * Returns the current date as a JDate.
     * @return the current date as a JDate.
     */
    public static @NotNull JDate today() {
        return getBuiltins().today();
    }

    /**
     * Returns a JDate from the LocalDate argument
     * @param date a LocalDate
     * @return a JDate
     */
    public static @NotNull JDate date(final @NotNull LocalDate date) {
        return getBuiltins().date(date);
    }

    /**
     * Returns a JDate from the LocalDate argument, or NULL when null.
     * @param date a LocalDate or Java null
     * @return a JDate or NULL
     */
    public static @NotNull JanitorObject nullableDate(@Nullable LocalDate date) {
        return getBuiltins().nullableDate(date);
    }

    /**
     * Returns a JDate from the given year, month and day.
     * The rules for Java's LocalDate::of method apply for the Java implementation!
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month the month-of-year to represent, from 1 (January) to 12 (December)
     * @param day the day-of-month to represent, from 1 to 31
     * @return that JDate you asked for
     * @throws DateTimeException in case Java's LocalDateTime does not accept the parameters.
     */
    public static @NotNull JDate date(long year, long month, long day) throws DateTimeException {
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

    /**
     * Turns a Java Boolean into a Janitor JBool, or returns NULL for Java null.
     * @param value a Boolean or null
     * @return a JBool or NULL
     */
    public static JanitorObject nullableBooleanOf(final Boolean value) {
        return value == null ? JNull.NULL : toBool(value);
    }

    /**
     * Gets the user defined environment provider
     * @return the user defined environment provider
     */
    public static @Nullable JanitorEnvironmentProvider getUserProvider() {
        return userProvider;
    }

    /**
     * Sets the user defined environment provider.
     * This method should only ever be used by application code! Library code should <b>never</b> call this.
     * @param userProvider the user defined environment provider
     */
    public static void setUserProvider(final @NotNull JanitorEnvironmentProvider userProvider) {
        Janitor.userProvider = userProvider;
    }

    /**
     * Gets the selected automatic environment provider
     * @return the selected automatic environment provider
     */
    public static @NotNull JanitorEnvironmentProvider getAutomaticProvider() {
        return automaticProvider;
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
     * Retrieves the best applicable enviornment from either the userProvider or the highest priority automatic provider.
     * @return an environment
     * @throws IllegalStateException when neither a user env nor an automatically provided env are available
     */
    public static @NotNull JanitorEnvironment current() throws IllegalStateException {
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
     * Map a nullable java Boolean to a JBool. Null yields FALSE.
     * @param value the value
     * @return the JBool
     */
    public static JBool toBool(final Boolean value) {
        return value == Boolean.TRUE ? JBool.TRUE : JBool.FALSE;
    }

    /**
     * Private constructor, to keep you from creating instances of this singleton / "namespace class".
     */
    private Janitor() {
    }

    /**
     * Nested namespace class for script meta-data, which is available with Dispatcher instances, which help
     * bridging between Janitor code and Java code.
     */
    public static final class MetaData {

        /**
         * The class name for an object.
         */
        public static MetaDataKey<String> CLASS = new MetaDataKey<>("class", String.class);

        /**
         * Gives Java code an optional hint what the type of a property is supposed to be.
         * Turns out that knowing the type of an object is very convenient.
         * These hints are automatically populated by most Dispatcher methods.
         */
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


        /**
         * Meta-Data: mark a property as required.
         * <p>
         * Janitor does currently not use this itself, but for additional tooling, this can e.g. make the difference
         * between emitting "foo: string | undefined" or "foo: string", assuming you're generating an index.d.ts file
         * for some Janitor classes.
         * </p>
         */
        public static final MetaDataKey<Boolean> REQUIRED = new MetaDataKey<>("required", Boolean.class);

        /**
         * Helper for emitting TS defs for Janutor objects: A property points to a class of this name.
         * To be used where type hints are not possible, because they refer to builtin types only.
         */
        public static MetaDataKey<String> REF = new MetaDataKey<>("ref", String.class);

        /**
         * Marks fields that are nullable on the Java side, without specifying anything about script-side nullability.
         */
        public static MetaDataKey<Boolean> HOST_NULLABLE = new MetaDataKey<>("host_nullable", Boolean.class);


        /**
         * The column name for an object property, in case you're working with a database.
         */
        public static MetaDataKey<String> COLUMN_NAME = new MetaDataKey<>("column_name", String.class);

        /**
         * The database table name for an object.
         */
        public static MetaDataKey<String> TABLE_NAME = new MetaDataKey<>("table_name", String.class);

        /**
         * The column name of an ID field for an object, e.g. "person_id".
         */
        public static MetaDataKey<String> ID_FIELD = new MetaDataKey<>("id_field", String.class);

        /**
         * The column name of a KEY field for an object, e.g. "person_key".
         */
        public static MetaDataKey<String> KEY_FIELD = new MetaDataKey<>("key_field", String.class);


        /**
         * The maximum length for an object property.
         */
        public static MetaDataKey<Integer> MAX_LENGTH = new MetaDataKey<>("max_length", Integer.class);



        /**
         * Private constructor, to keep you from creating instances of this singleton / "namespace class".
         */
        private MetaData() {}

    }

    public static final class Semantics {
        /**
         * Check two objects for equality.
         *
         * @param leftValue  the left value
         * @param rightValue the right value
         * @return TRUE if the values are equals, or FALSE if not.
         */
        public static @NotNull JBool areEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
            return Janitor.toBool(leftValue == rightValue
                                  || leftValue.janitorGetHostValue() == rightValue.janitorGetHostValue()
                                  || Objects.equals(leftValue.janitorGetHostValue(), rightValue.janitorGetHostValue())
                                  || (leftValue instanceof JNumber leftNumber && rightValue instanceof JNumber rightNumber && 0 == compareNumbers(leftNumber, rightNumber))

            );
        }

        private static int compareNumbers(final JNumber leftNumber, final JNumber rightNumber) {
            return Double.compare(leftNumber.toDouble(), rightNumber.toDouble());
        }

        /**
         * Check two objects for equality.
         *
         * @param leftValue  the left value
         * @param rightValue the right value
         * @return FALSE if the values are equals, or TRUE if not.
         */
        public static @NotNull JBool areNotEquals(final JanitorObject leftValue, final JanitorObject rightValue) {
            return areEquals(leftValue, rightValue).opposite();
        }

    }

}
