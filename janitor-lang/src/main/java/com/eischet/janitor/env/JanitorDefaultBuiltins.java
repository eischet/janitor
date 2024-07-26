package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.types.wrapper.JanitorWrapperDispatchTable;
import com.eischet.janitor.api.types.wrapper.JanitorWrapper;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.util.DateTimeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class JanitorDefaultBuiltins implements JanitorBuiltins {

    private final JString emptyString;
    private final JInt zero;

    // TODO: figure out why I cannot write Dispatcher<JMap> here. I keep forgetting the subleties of the Java generics system...
    // I'm sure it's something with blah super foo extends lalala that everybody but me knows about. ;-)

    private JanitorWrapperDispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher = new JanitorWrapperDispatchTable<>();
    private JanitorWrapperDispatchTable<String> stringDispatcher = new JanitorWrapperDispatchTable<>();
    private JanitorWrapperDispatchTable<List<JanitorObject>> listDispatcher = new JanitorWrapperDispatchTable<>();
    private JanitorWrapperDispatchTable<Set<JanitorObject>> setDispatcher = new JanitorWrapperDispatchTable<>();
    private JanitorWrapperDispatchTable<Long> intDispatcher = new JanitorWrapperDispatchTable<>();
    private JanitorWrapperDispatchTable<byte[]> binaryDispatcher = new JanitorWrapperDispatchTable<>();


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

        listDispatcher.addMethod("toJson", JListOperations::__toJson);
        listDispatcher.addMethod("parseJson", JListOperations::__parseJson);
        listDispatcher.addMethod("count", JListOperations::__count);
        listDispatcher.addMethod("filter", JListOperations::__filter);
        listDispatcher.addMethod("map", JListOperations::__map);
        listDispatcher.addMethod("join", JListOperations::__join);
        listDispatcher.addMethod("toSet", JListOperations::__toSet);
        listDispatcher.addMethod("size", JListOperations::__size);
        listDispatcher.addMethod("isEmpty", JListOperations::__isEmpty);
        listDispatcher.addMethod("contains", JListOperations::__contains);
        listDispatcher.addMethod("randomSublist", JListOperations::__randomSublist);
        listDispatcher.addMethod("addAll", JListOperations::__addAll);
        listDispatcher.addMethod("put", JListOperations::__put);
        listDispatcher.addMethod("add", JListOperations::__add);
        listDispatcher.addMethod("get", JListOperations::__get);
        listDispatcher.addMethod("__get__", JListOperations::__getSliced);

        setDispatcher.addMethod("add", JSetClass::__add);
        setDispatcher.addMethod("remove", JSetClass::__remove);
        setDispatcher.addMethod("contains", JSetClass::__contains);
        setDispatcher.addMethod("toList", JSetClass::__toList);
        setDispatcher.addMethod("size", JSetClass::__size);
        setDispatcher.addMethod("isEmpty", JSetClass::__isEmpty);

        intDispatcher.addLongProperty("int", JanitorWrapper::janitorGetHostValue);
        intDispatcher.addObjectProperty("epoch", wrapper -> JDateTime.ofNullable(DateTimeUtilities.localFromEpochSeconds(wrapper.janitorGetHostValue())));


        binaryDispatcher.addMethod("encodeBase64", JBinaryClass::__encodeBase64);
        binaryDispatcher.addMethod("toString", JBinaryClass::__toString);
        binaryDispatcher.addMethod("size", JBinaryClass::__size);
        binaryDispatcher.addStringProperty("string", wrapper -> wrapper.janitorIsTrue() ? new String(wrapper.janitorGetHostValue()) : "");
        binaryDispatcher.addIntegerProperty("length", wrapper -> wrapper.janitorGetHostValue() == null ? 0 : wrapper.janitorGetHostValue().length);

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
        return new JMap(mapDispatcher, this);
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
    public @NotNull JList list(@NotNull final List<JanitorObject> list) {
        return JList.newInstance(listDispatcher, new ArrayList<>(list));
    }

    @Override
    public @NotNull JList list(@NotNull final Stream<JanitorObject> stream) {
        return JList.newInstance(listDispatcher, new ArrayList<>(stream.toList()));
    }

    @Override
    public @NotNull JSet set() {
        return JSet.newInstance(setDispatcher, new HashSet<>());
    }

    @Override
    public @NotNull JSet set(@NotNull final Collection<JanitorObject> collection) {
        return JSet.newInstance(setDispatcher, new HashSet<>(collection));
    }

    @Override
    public @NotNull JSet set(@NotNull final Stream<JanitorObject> stream) {
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
    public @NotNull JBinary binary(final byte[] arr) {
        return JBinary.newInstance(binaryDispatcher, arr);
    }


}
