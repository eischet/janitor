package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.RegularDispatchTable;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.JanitorWrapperDispatchTable;
import com.eischet.janitor.runtime.DateTimeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JanitorDefaultBuiltins implements JanitorBuiltins {

    protected final JanitorWrapperDispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<String> stringDispatcher = new JanitorWrapperDispatchTable<>();

    // TODO: figure out why I cannot write Dispatcher<JMap> here. I keep forgetting the subleties of the Java generics system...
    // I'm sure it's something with blah super foo extends lalala that everybody but me knows about. ;-)
    protected final JanitorWrapperDispatchTable<List<JanitorObject>> listDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<Set<JanitorObject>> setDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<Long> intDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<byte[]> binaryDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<Double> floatDispatcher = new JanitorWrapperDispatchTable<>();
    protected final JanitorWrapperDispatchTable<Pattern> regexDispatcher = new JanitorWrapperDispatchTable<>();
    protected final RegularDispatchTable<JDuration> durationDispatch = new RegularDispatchTable<>();
    private final JString emptyString;
    private final JInt zero;

    public JanitorDefaultBuiltins() {
        emptyString = JString.newInstance(stringDispatcher, "");
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
        stringDispatcher.addMethod("__get__", JStringClass::__get); // das lassen wir auch so: keine Zuweisung per Index an String-Teile, die sind ja immutable
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
        mapDispatcher.addMethod("__get__", JMapClass::__getIndexed);
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
        listDispatcher.addMethod("__get__", JListClass::__getSliced);

        setDispatcher.addMethod("add", JSetClass::__add);
        setDispatcher.addMethod("remove", JSetClass::__remove);
        setDispatcher.addMethod("contains", JSetClass::__contains);
        setDispatcher.addMethod("toList", JSetClass::__toList);
        setDispatcher.addMethod("size", JSetClass::__size);
        setDispatcher.addMethod("isEmpty", JSetClass::__isEmpty);

        intDispatcher.addLongProperty("int", JanitorWrapper::janitorGetHostValue);
        intDispatcher.addObjectProperty("epoch", wrapper -> JDateTime.ofNullable(DateTimeUtilities.localFromEpochSeconds(wrapper.janitorGetHostValue())));

        floatDispatcher.addLongProperty("int", doubleJanitorWrapper -> doubleJanitorWrapper.janitorGetHostValue().longValue());

        binaryDispatcher.addMethod("encodeBase64", JBinaryClass::__encodeBase64);
        binaryDispatcher.addMethod("toString", JBinaryClass::__toString);
        binaryDispatcher.addMethod("size", JBinaryClass::__size);
        binaryDispatcher.addStringProperty("string", wrapper -> wrapper.janitorIsTrue() ? new String(wrapper.janitorGetHostValue()) : "");
        binaryDispatcher.addIntegerProperty("length", wrapper -> wrapper.janitorGetHostValue() == null ? 0 : wrapper.janitorGetHostValue().length);

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


    }


    @Override
    public @NotNull JString emptyString() {
        return emptyString;
    }

    @Override
    public @NotNull JString string(final @Nullable String value) {
        return JString.newInstance(stringDispatcher, value == null ? "" : value);
    }

    @Override
    public @NotNull JanitorObject nullableString(final @Nullable String value) {
        return value == null ? JNull.NULL : JString.newInstance(stringDispatcher, value);
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
    public Internals internals() {
        return new Internals();
    }

    /**
     * Internals class for JanitorDefaultBuiltins.
     * <p>Use an instance of this class, obtained via {@link JanitorDefaultBuiltins#internals()}, to access the internal dispatch tables.
     * This is useful for extending the scripting runtime.</p>
     * <p>This class is separate from the JanitorDefaultBuiltins class only to avoid polluting its public API.</p>
     */
    public class Internals {

        public JanitorWrapperDispatchTable<Map<JanitorObject, JanitorObject>> getMapDispatcher() {
            return mapDispatcher;
        }

        public JanitorWrapperDispatchTable<String> getStringDispatcher() {
            return stringDispatcher;
        }

        public JanitorWrapperDispatchTable<List<JanitorObject>> getListDispatcher() {
            return listDispatcher;
        }

        public JanitorWrapperDispatchTable<Set<JanitorObject>> getSetDispatcher() {
            return setDispatcher;
        }

        public JanitorWrapperDispatchTable<Long> getIntDispatcher() {
            return intDispatcher;
        }

        public JanitorWrapperDispatchTable<byte[]> getBinaryDispatcher() {
            return binaryDispatcher;
        }

        public JanitorWrapperDispatchTable<Double> getFloatDispatcher() {
            return floatDispatcher;
        }

        public JanitorWrapperDispatchTable<Pattern> getRegexDispatcher() {
            return regexDispatcher;
        }

        public RegularDispatchTable<JDuration> getDurationDispatch() {
            return durationDispatch;
        }
    }
}
