// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.BuiltinTypeInternals;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.DateTimeException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.eischet.janitor.api.types.builtin.JDate.DATE_FORMAT;
import static com.eischet.janitor.api.types.builtin.JDateTime.DATE_FORMAT_LONG;
import static com.eischet.janitor.api.types.builtin.JDateTime.DATE_FORMAT_SHORT;

/** The default implementation of {@link BuiltinTypes}: it owns the dispatch tables of the built-in types, and creates the built-in values. */
public class DefaultBuiltinTypes implements BuiltinTypes {

    /**
     * String interning: maximum length for automatically interned strings.
     */
    private static final int MAX_INTERNED_LENGTH = 10;

    protected final DispatchTable<JanitorObject> baseDispatcher = new DispatchTable<>(null);

    protected final WrapperDispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher = new WrapperDispatchTable<>(Janitor::map);
    protected final DispatchTable<JString> stringDispatcher = new DispatchTable<>(baseDispatcher, it -> it, false);

    // TODO: figure out why I cannot write Dispatcher<JMap> here. I keep forgetting the subleties of the Java generics system...
    // I'm sure it's something with blah super foo extends lalala that everybody but me knows about. ;-)

    protected final DispatchTable<JList> listDispatcher = new DispatchTable<>(baseDispatcher, it -> it, false);
    protected final WrapperDispatchTable<Set<JanitorObject>> setDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it, false);
    protected final WrapperDispatchTable<Long> intDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it, false);
    protected final WrapperDispatchTable<byte[]> binaryDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it, false);
    protected final WrapperDispatchTable<Double> floatDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it, false);
    protected final WrapperDispatchTable<Pattern> regexDispatcher = new WrapperDispatchTable<>(baseDispatcher, it -> it, false);
    protected final DispatchTable<JDuration> durationDispatch = new DispatchTable<>(baseDispatcher, it -> it, false);
    protected final DispatchTable<JDateTime> dateTimeDispatch = new DispatchTable<>(baseDispatcher, it -> it, false);
    protected final DispatchTable<JDate> dateDispatch = new DispatchTable<>(baseDispatcher, it -> it, false);


    private final JString emptyString;
    private final JInt zero;

    public DefaultBuiltinTypes() {
        // Every HELP text starts with the call form, "name(arguments): " or "name: " for properties, which the language reference relies on.
        baseDispatcher.addStringProperty("class", JanitorObject::janitorClassName)
                .setMetaData(Janitor.MetaData.HELP, "class: The name of the value's type, e.g. \"string\", \"int\" or \"list\".");

        emptyString = JString.newInstance(stringDispatcher, "", it -> it); // cannot pass this::intern here in a constructor, and "" is already interned anyway
        zero = JInt.newInstance(intDispatcher, 0);

        JStringClass.applyDefaults(stringDispatcher);
        JMapClass.applyDefaults(mapDispatcher);
        JListClass.applyDefaults(listDispatcher);
        JSetClass.applyDefaults(setDispatcher);
        JRegexClass.applyDefaults(regexDispatcher);
        JDateTimeClass.applyDefaults(dateTimeDispatch);
        JDateClass.applyDefaults(dateDispatch);
        JBinaryClass.applyDefaults(binaryDispatcher);

        intDispatcher.addLongProperty("int", JanitorWrapper::janitorGetHostValue)
                .setMetaData(Janitor.MetaData.HELP, "int: The number itself.");
        // intDispatcher.addDateTimeProperty("epoch", wrapper -> DateTimeUtils.fromEpochSeconds(wrapper.janitorGetHostValue()));
        intDispatcher.addObjectProperty("epoch", wrapper -> dateTime(DateTimeUtils.fromEpochSeconds(wrapper.janitorGetHostValue())))
                .setMetaData(Janitor.MetaData.HELP, "epoch: Reads the number as seconds since 1970-01-01 00:00 UTC and returns that point in time as a datetime, in the host's time zone.");

        floatDispatcher.addLongProperty("int", doubleJanitorWrapper -> doubleJanitorWrapper.janitorGetHostValue().longValue())
                .setMetaData(Janitor.MetaData.HELP, "int: The number as an integer, with the fraction cut off (towards zero).");

        durationDispatch.addLongProperty("seconds", JDuration::toSeconds)
                .setMetaData(Janitor.MetaData.HELP, "seconds: The length of the duration in whole seconds.");
        durationDispatch.addLongProperty("minutes", self -> self.toSeconds() / 60)
                .setMetaData(Janitor.MetaData.HELP, "minutes: The length of the duration in whole minutes, rounded down.");
        durationDispatch.addLongProperty("hours", self -> self.toSeconds() / 3600)
                .setMetaData(Janitor.MetaData.HELP, "hours: The length of the duration in whole hours, rounded down.");
        durationDispatch.addLongProperty("days", self -> self.toSeconds() / 86400)
                .setMetaData(Janitor.MetaData.HELP, "days: The length of the duration in whole days, rounded down.");
        durationDispatch.addLongProperty("weeks", self -> self.toSeconds() / 604800)
                .setMetaData(Janitor.MetaData.HELP, "weeks: The length of the duration in whole weeks, rounded down.");

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
        return JMap.newInstance(mapDispatcher);
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
    public @NotNull JList responsiveList(final DispatchTable<?> elementDispatchTable, @NotNull final Stream<? extends JanitorObject> stream, @NotNull final Consumer<JList> onUpdate) {
        return JList.newInstance(listDispatcher, new ArrayList<>(stream.toList())).withElementDispatchTable(elementDispatchTable).onUpdate(onUpdate);
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
        public DispatchTable<JList> getListDispatcher() {
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
        } else {
            final LocalDateTime parsed = LocalDateTime.parse(text, text.lastIndexOf(':') != text.indexOf(':') ? LITERAL_DATE_TIME_LONG : LITERAL_DATE_TIME_SHORT);
            requirePositiveYear(parsed.getYear(), text);
            return dateTime(parsed);
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
        if (text == null || text.isBlank()) {
            return Janitor.NULL;
        }
        if ("today".equals(text)) {
            return date(LocalDate.now());
        } else {
            final LocalDate parsed = LocalDate.parse(text, LITERAL_DATE);
            requirePositiveYear(parsed.getYear(), text);
            return date(parsed);
        }
    }

    /*
     * Literals are read strictly: a date that does not exist, like 2024-02-30, or a time like 24:00, is an error. The default
     * ResolverStyle.SMART would silently turn them into 2024-02-29 and midnight of the next day. STRICT needs "uuuu" (the
     * proleptic year) instead of "yyyy" (the year of an era), and would accept the year 0, which is excluded separately.
     */
    private static final DateTimeFormatter LITERAL_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter LITERAL_DATE_TIME_SHORT = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter LITERAL_DATE_TIME_LONG = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private static void requirePositiveYear(final int year, final String text) {
        if (year < 1) {
            throw new DateTimeException("year " + year + " is not supported: " + text);
        }
    }

    @Override
    public @NotNull JanitorObject nullableDateTimeFromJsonString(@Nullable final String jsonString) throws JsonException {
        if (jsonString == null || jsonString.isBlank()) {
            return Janitor.NULL;
        }
        try {
            final LocalDateTime parsed = LocalDateTime.parse(jsonString, JDateTime.JSON_FORMAT);
            return dateTime(parsed);
        } catch (DateTimeParseException e) {
            throw new JsonException("invalid datetime: '%s'".formatted(jsonString), e);
        }
    }

    @Override
    public @NotNull JanitorObject nullableDateFromJsonString(@Nullable final String jsonString) throws JsonException {
        if (jsonString == null || jsonString.isBlank()) {
            return Janitor.NULL;
        }
        try {
            final LocalDate parsed = LocalDate.parse(jsonString, DATE_FORMAT);
            return date(parsed);
        } catch (DateTimeParseException e) {
            throw new JsonException("invalid date: '%s'".formatted(jsonString), e);
        }
    }

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

    @Override
    public @NotNull JanitorObject numeric(final double v) {
        boolean isAnInteger = v == Math.floor(v) && !Double.isInfinite(v);
        if (isAnInteger) {
            return integer((long) v);
        } else {
            return floatingPoint(v);
        }
    }

    @Override
    public @NotNull JanitorObject nullableNumeric(final Double v) {
        return v == null ? JNull.NULL : numeric(v);
    }

    @Override
    public @NotNull JanitorObject nullableLegacyDate(@Nullable final Date legacyDate) {
        return legacyDate == null ? Janitor.NULL : date(DateTimeUtils.convert(legacyDate).toLocalDate());
    }

    @Override
    public @NotNull JanitorObject nullableLegacyDateTime(@Nullable final Date legacyDateTime) {
        return legacyDateTime == null ? Janitor.NULL : dateTime(DateTimeUtils.convert(legacyDateTime));
    }

}
