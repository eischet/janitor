package com.eischet.janitor.env;

import com.eischet.janitor.api.types.BuiltinTypeInternals;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.runtime.DateTimeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.eischet.janitor.api.types.builtin.JDate.DATE_FORMAT;
import static com.eischet.janitor.api.types.builtin.JDateTime.DATE_FORMAT_LONG;
import static com.eischet.janitor.api.types.builtin.JDateTime.DATE_FORMAT_SHORT;

public class DefaultBuiltinTypes implements BuiltinTypes {

    /**
     * String interning: maximum length for automatically interned strings.
     */
    private static final int MAX_INTERNED_LENGTH = 10;

    protected final DispatchTable<JanitorObject> baseDispatcher = new DispatchTable<>();

    protected final WrapperDispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher = new WrapperDispatchTable<>();
    protected final DispatchTable<JString> stringDispatcher = new DispatchTable<>(baseDispatcher, it -> it);

    // TODO: figure out why I cannot write Dispatcher<JMap> here. I keep forgetting the subleties of the Java generics system...
    // I'm sure it's something with blah super foo extends lalala that everybody but me knows about. ;-)



    protected final WrapperDispatchTable<List<JanitorObject>> listDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final WrapperDispatchTable<Set<JanitorObject>> setDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final WrapperDispatchTable<Long> intDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final WrapperDispatchTable<byte[]> binaryDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final WrapperDispatchTable<Double> floatDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final WrapperDispatchTable<Pattern> regexDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it);
    protected final DispatchTable<JDuration> durationDispatch = new DispatchTable<>(baseDispatcher, it -> it);
    protected final DispatchTable<JDateTime> dateTimeDispatch = new DispatchTable<>(baseDispatcher, it -> it);
    protected final DispatchTable<JDate> dateDispatch = new DispatchTable<>(baseDispatcher, it -> it);


    private final JString emptyString;
    private final JInt zero;

    public DefaultBuiltinTypes() {
        baseDispatcher.addStringProperty("class", JanitorObject::janitorClassName);

        emptyString = JString.newInstance(stringDispatcher, "", it -> it); // cannot pass this::intern here in a constructor, and "" is already interned anyway
        zero = JInt.newInstance(intDispatcher, 0);

        // OLD: addStringMethod("length", JStringClass::__length);
        stringDispatcher.addMethod("length", JStringClass::__length);
        stringDispatcher.addMethod("trim", JStringClass::__trim);
        stringDispatcher.addMethod("contains", JStringClass::__contains);
        stringDispatcher.addMethod("containsIgnoreCase", JStringClass::__containsIgnoreCase);
        stringDispatcher.addMethod("splitLines", JStringClass::__splitLines);
        stringDispatcher.addMethod("indexOf", JStringClass::__indexOf);
        stringDispatcher.addMethod("empty", JStringClass::__empty);
        stringDispatcher.addMethod("startsWith", JStringClass::__startsWith);
        stringDispatcher.addMethod("endsWith", JStringClass::__endsWith);
        stringDispatcher.addMethod("removeLeadingZeros", JStringClass::__removeLeadingZeros);
        stringDispatcher.addMethod("substring", JStringClass::__substring);
        stringDispatcher.addMethod("replaceAll", JStringClass::__replaceAll);
        stringDispatcher.addMethod("replace", JStringClass::__replace);
        stringDispatcher.addMethod("replaceFirst", JStringClass::__replaceFirst);
        stringDispatcher.addMethod("toUpperCase", JStringClass::__toUpperCase);
        stringDispatcher.addMethod("toLowerCase", JStringClass::__toLowerCase);
        stringDispatcher.addMethod("count", JStringClass::__count);
        stringDispatcher.addMethod("format", JStringClass::__format);
        stringDispatcher.addMethod("expand", JStringClass::__expand);
        stringDispatcher.addMethod("toBinaryUtf8", JStringClass::__toBinaryUtf8);
        stringDispatcher.addMethod("int", JStringClass::__toInt);
        stringDispatcher.addMethod("toInt", JStringClass::__toInt);
        stringDispatcher.addMethod("toFloat", JStringClass::__toFloat);
        stringDispatcher.addMethod("get", JStringClass::__get);
        stringDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JStringClass::__get); // das lassen wir auch so: keine Zuweisung per Index an String-Teile, die sind ja immutable
        stringDispatcher.addMethod("isNumeric", JStringClass::__isNumeric);
        stringDispatcher.addMethod("startsWithNumbers", JStringClass::__startsWithNumbers);
        stringDispatcher.addMethod("parseDate", JStringClass::__parseDate);
        stringDispatcher.addMethod("parseDateTime", JStringClass::__parseDateTime);
        stringDispatcher.addMethod("cutFilename", JStringClass::__cutFilename);
        stringDispatcher.addMethod("urlEncode", JStringClass::__urlEncode);
        stringDispatcher.addMethod("urlDecode", JStringClass::__urlDecode);
        stringDispatcher.addMethod("decodeBase64", JStringClass::__decodeBase64);

        mapDispatcher.addMethod("toJson", JMapClass::__toJson);
        mapDispatcher.addMethod("parseJson", JMapClass::__parseJson);
        mapDispatcher.addMethod("get", JMapClass::__get);
        mapDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JMapClass::__getIndexed);
        mapDispatcher.addMethod("put", JMapClass::__put);
        mapDispatcher.addMethod("size", JMapClass::__size);
        mapDispatcher.addMethod("isEmpty", JMapClass::__isEmpty);
        mapDispatcher.addMethod("keys", JMapClass::__keys);
        mapDispatcher.addMethod("values", JMapClass::__values);

        listDispatcher.addMethod("toJson", JListClass::__toJson);
        listDispatcher.addMethod("parseJson", JListClass::__parseJson);
        listDispatcher.addMethod("count", JListClass::__count);
        listDispatcher.addMethod("filter", JListClass::__filter);
        listDispatcher.addMethod("map", JListClass::__map);
        listDispatcher.addMethod("join", JListClass::__join);
        listDispatcher.addMethod("toSet", JListClass::__toSet);
        listDispatcher.addMethod("size", JListClass::__size);
        listDispatcher.addMethod("isEmpty", JListClass::__isEmpty);
        listDispatcher.addMethod("contains", JListClass::__contains);
        listDispatcher.addMethod("randomSublist", JListClass::__randomSublist);
        listDispatcher.addMethod("addAll", JListClass::__addAll);
        listDispatcher.addMethod("put", JListClass::__put);
        listDispatcher.addMethod("add", JListClass::__add);
        listDispatcher.addMethod("get", JListClass::__get);
        listDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JListClass::__getSliced);

        setDispatcher.addMethod("add", JSetClass::__add);
        setDispatcher.addMethod("remove", JSetClass::__remove);
        setDispatcher.addMethod("contains", JSetClass::__contains);
        setDispatcher.addMethod("toList", JSetClass::__toList);
        setDispatcher.addMethod("size", JSetClass::__size);
        setDispatcher.addMethod("isEmpty", JSetClass::__isEmpty);

        intDispatcher.addLongProperty("int", JanitorWrapper::janitorGetHostValue);
        intDispatcher.addObjectProperty("epoch", wrapper -> dateTime(DateTimeUtilities.localFromEpochSeconds(wrapper.janitorGetHostValue())));

        floatDispatcher.addLongProperty("int", doubleJanitorWrapper -> doubleJanitorWrapper.janitorGetHostValue().longValue());

        binaryDispatcher.addMethod("encodeBase64", JBinaryClass::__encodeBase64);
        binaryDispatcher.addMethod("toString", JBinaryClass::__toString);
        binaryDispatcher.addMethod("size", JBinaryClass::__size);
        binaryDispatcher.addStringProperty("string", wrapper -> wrapper.janitorIsTrue() ? new String(wrapper.janitorGetHostValue()) : "");
        binaryDispatcher.addIntegerProperty("length", wrapper -> wrapper.janitorGetHostValue() == null ? 0 : wrapper.janitorGetHostValue().length);

        binaryDispatcher.addMethod("sha256", JBinaryClass::__sha256);

        durationDispatch.addLongProperty("seconds", JDuration::toSeconds);
        durationDispatch.addLongProperty("minutes", self -> self.toSeconds() / 60);
        durationDispatch.addLongProperty("hours", self -> self.toSeconds() / 3600);
        durationDispatch.addLongProperty("days", self -> self.toSeconds() / 86400);
        durationDispatch.addLongProperty("weeks", self -> self.toSeconds() / 604800);

        regexDispatcher.addMethod("extract", JRegexClass::extract);
        regexDispatcher.addMethod("extractAll", JRegexClass::extractAll);
        regexDispatcher.addMethod("replaceAll", JRegexClass::replaceAll);
        regexDispatcher.addMethod("replaceFirst", JRegexClass::replaceFirst);
        regexDispatcher.addMethod("split", JRegexClass::split);


        dateTimeDispatch.addLongProperty("epoch", JDateTimeClass::__epochAsAttribute);
        dateTimeDispatch.addMethod("toEpoch", JDateTimeClass::__epoch);
        dateTimeDispatch.addMethod("date", JDateTimeClass::__date);
        dateTimeDispatch.addMethod("time", JDateTimeClass::__time);
        dateTimeDispatch.addMethod("string", JDateTimeClass::__string);
        dateTimeDispatch.addMethod("format", JDateTimeClass::__string);
        dateTimeDispatch.addMethod("formatAtTimezone", JDateTimeClass::__formatAtTimezone);
        dateTimeDispatch.addMethod("kw", JDateTimeClass::__kw);
        dateTimeDispatch.addMethod("year", JDateTimeClass::__year);

        dateDispatch.addLongProperty("year", JDate::getYear);
        dateDispatch.addLongProperty("month", JDate::getMonth);
        dateDispatch.addLongProperty("day", JDate::getDayOfMonth);

    }


    @Override
    public @NotNull JString emptyString() {
        return emptyString;
    }

    @Override
    public @NotNull JString string(final @Nullable String value) {
        return JString.newInstance(stringDispatcher, value == null ? "" : value, this::intern);
    }

    @Override
    public @NotNull JanitorObject nullableString(final @Nullable String value) {
        return value == null ? JNull.NULL : JString.newInstance(stringDispatcher, value, this::intern);
    }

    @Override
    public @NotNull JMap map() {
        return JMap.newInstance(mapDispatcher, this);
    }

    @Override
    public @NotNull JList list() {
        return JList.newInstance(listDispatcher, new ArrayList<>());
    }

    @Override
    public @NotNull JList list(final int initialSize) {
        return JList.newInstance(listDispatcher, new ArrayList<>(initialSize));
    }

    @Override
    public @NotNull JList list(@NotNull final List<? extends JanitorObject> list) {
        return JList.newInstance(listDispatcher, new ArrayList<>(list));
    }

    @Override
    public @NotNull JList list(@NotNull final Stream<? extends JanitorObject> stream) {
        return JList.newInstance(listDispatcher, new ArrayList<>(stream.toList()));
    }

    @Override
    public @NotNull JSet set() {
        return JSet.newInstance(setDispatcher, new HashSet<>());
    }

    @Override
    public @NotNull JSet set(@NotNull final Collection<? extends JanitorObject> collection) {
        return JSet.newInstance(setDispatcher, new HashSet<>(collection));
    }

    @Override
    public @NotNull JSet set(@NotNull final Stream<? extends JanitorObject> stream) {
        return JSet.newInstance(setDispatcher, new HashSet<>(stream.toList()));
    }

    @Override
    public @NotNull JInt integer(final long value) {
        if (value == 0) {
            return zero;
        }
        return JInt.newInstance(intDispatcher, value);
    }

    @Override
    public @NotNull JInt integer(final int value) {
        if (value == 0) {
            return zero;
        }
        return JInt.newInstance(intDispatcher, value);
    }

    @Override
    public @NotNull JanitorObject nullableInteger(@Nullable final Number value) {
        if (value == null) {
            return JNull.NULL;
        }
        if (value.longValue() == 0) {
            return zero;
        }
        return JInt.newInstance(intDispatcher, value.longValue());
    }

    @Override
    public @NotNull JBinary binary(final byte @NotNull [] arr) {
        return JBinary.newInstance(binaryDispatcher, arr);
    }

    /**
     * Create a new JFloat.
     *
     * @param value the number
     * @return the number, or NULL if the input is null
     */
    @Override
    public @NotNull JanitorObject nullableFloatingPoint(final Double value) {
        if (value == null) {
            return JNull.NULL;
        } else {
            return JFloat.newInstance(floatDispatcher, value);
        }
    }

    /**
     * Create a new JFloat.
     *
     * @param value the number
     * @return the number
     */
    @Override
    public @NotNull JFloat floatingPoint(final double value) {
        return JFloat.newInstance(floatDispatcher, value);
    }

    /**
     * Create a new JFloat.
     *
     * @param value the number
     * @return the number
     */
    @Override
    public @NotNull JFloat floatingPoint(final long value) {
        return JFloat.newInstance(floatDispatcher, value);
    }

    /**
     * Create a new JFloat.
     *
     * @param value the number
     * @return the number
     */
    @Override
    public @NotNull JFloat floatingPoint(final int value) {
        return JFloat.newInstance(floatDispatcher, value);
    }

    @Override
    public @NotNull JDuration duration(final long value, final JDuration.JDurationKind kind) {
        return JDuration.newInstance(durationDispatch, value, kind);
    }

    @Override
    public @NotNull JRegex regex(@NotNull final Pattern pattern) {
        return JRegex.newInstance(regexDispatcher, pattern);
    }

    /**
     * Retrieve an Internals object, which then gives access to the internal dispatch tables.
     * Frowned upon in most situations, for embedding and extending the scripting runtime, this is a key feature.
     *
     * @return internals
     */
    @Override
    public BuiltinTypeInternals internals() {
        return new Internals();
    }

    /**
     * Internals class for JanitorDefaultBuiltins.
     * <p>Use an instance of this class, obtained via {@link DefaultBuiltinTypes#internals()}, to access the internal dispatch tables.
     * This is useful for extending the scripting runtime.</p>
     * <p>This class is separate from the JanitorDefaultBuiltins class only to avoid polluting its public API.</p>
     */
    public class Internals implements BuiltinTypeInternals {

        @Override
        public DispatchTable<JanitorObject> getBaseDispatcher() {
            return baseDispatcher;
        }

        @Override
        public WrapperDispatchTable<Map<JanitorObject, JanitorObject>> getMapDispatcher() {
            return mapDispatcher;
        }

        @Override
        public DispatchTable<JString> getStringDispatcher() {
            return stringDispatcher;
        }

        @Override
        public WrapperDispatchTable<List<JanitorObject>> getListDispatcher() {
            return listDispatcher;
        }

        @Override
        public WrapperDispatchTable<Set<JanitorObject>> getSetDispatcher() {
            return setDispatcher;
        }

        @Override
        public WrapperDispatchTable<Long> getIntDispatcher() {
            return intDispatcher;
        }

        @Override
        public WrapperDispatchTable<byte[]> getBinaryDispatcher() {
            return binaryDispatcher;
        }

        @Override
        public WrapperDispatchTable<Double> getFloatDispatcher() {
            return floatDispatcher;
        }

        @Override
        public WrapperDispatchTable<Pattern> getRegexDispatcher() {
            return regexDispatcher;
        }

        @Override
        public DispatchTable<JDuration> getDurationDispatch() {
            return durationDispatch;
        }

        @Override
        public DispatchTable<JDateTime> getDateTimeDispatch() {
            return dateTimeDispatch;
        }

    }

    /**
     * Create a new JDateTime.
     *
     * @param text the date and time as a string
     */
    @Override
    public @NotNull JanitorObject nullableDateTimeFromLiteral(@Nullable final String text) {
        if ("now".equals(text)) {
            return dateTime(LocalDateTime.now());
        } else if (text == null) {
            return JNull.NULL;
        } else if (text.lastIndexOf(':') != text.indexOf(':')) {
            return dateTime(LocalDateTime.parse(text, DATE_FORMAT_LONG));
        } else {
            return dateTime((LocalDateTime.parse(text, DATE_FORMAT_SHORT)));
        }
    }



    @Override
    public @NotNull JDateTime dateTime(@NotNull final LocalDateTime dateTime) {
        return JDateTime.newInstance(dateTimeDispatch, dateTime);
    }

    /**
     * Create a new JDateTime.
     * @param dateTime the date and time
     * @return the date and time, or NULL if the input is null
     */
    @Override
    public @NotNull JanitorObject nullableDateTime(@Nullable final LocalDateTime dateTime) {
        return dateTime == null ? JNull.NULL : JDateTime.newInstance(dateTimeDispatch, dateTime);
    }

    /**
     * Create a new JDateTime.
     * @return the current date and time
     */
    @Override
    public @NotNull JDateTime now() {
        return JDateTime.newInstance(dateTimeDispatch, LocalDateTime.now());
    }


    @Override
    public @NotNull JDate date(final @NotNull LocalDate date) {
        return JDate.newInstance(dateDispatch, JDate.packLocalDate(date));
    }

    @Override
    public @NotNull JanitorObject nullableDate(@Nullable final LocalDate date) {
        return date == null ? JNull.NULL : date(date);
    }

    @Override
    public @NotNull JDate today() {
        return date(LocalDate.now());
    }

    @Override
    public @NotNull JanitorObject nullableDateFromLiteral(@Nullable final String text) {
        if ("today".equals(text)) {
            return date(LocalDate.now());
        } else {
            return date(LocalDate.parse(text, DATE_FORMAT));
        }
    }





    /**
     * Create a new JDate.
     * @param localDate the date
     * @return the date
    public static JDate of(final LocalDate localDate) {
        return new JDate(localDate);
    }
     */

    /**
     * Create a new JDate.
     * @param year the year
     * @param month the month
     * @param day the day
     * @return the date
    public static JDate of(final int year, final int month, final int day) {
        return new JDate(year, month, day);
    }
     */

    /**
     * Create a new JDate.
     * @param year the year
     * @param month the month
     * @param day the day
     * @return the date
     */
    @Override
    public @NotNull JDate date(final long year, final long month, final long day) {
        return date(LocalDate.of((int) year, (int) month, (int) day));
    }

    /**
     * Parse a date from a string.
     * @param process the running script
     * @param string the string
     * @param format the format
     * @return the date
     */
    @Override
    public @NotNull JanitorObject parseNullableDate(final JanitorScriptProcess process, final String string, final String format) {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            final LocalDate d = LocalDate.parse(string, formatter);
            return date(d);
        } catch (DateTimeParseException e) {
            process.warn("error parsing date '%s' with format '%s': %s".formatted(string, format, e.getMessage()));
            return JNull.NULL;
        }
    }

    /**
     * Interns a string if it is short enough.
     * @param string the string to intern
     * @return the interned string, and/or the original string if it is too long
     */
    @Override
    public @Nullable String intern(@Nullable String string) {
        if (string != null) {
            if (string.length() <= MAX_INTERNED_LENGTH) {
                return string.intern();
            } else {
                return string;
            }
        } else {
            return null;
        }
    }

}
