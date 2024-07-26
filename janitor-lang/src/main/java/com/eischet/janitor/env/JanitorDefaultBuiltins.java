package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.scripting.DispatchTable;
import com.eischet.janitor.api.scripting.Dispatcher;
import com.eischet.janitor.api.scripting.JanitorWrapper;
import com.eischet.janitor.api.types.JMap;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class JanitorDefaultBuiltins implements JanitorBuiltins {

    private final JString emptyString;

    // TODO: figure out why I cannot write Dispatcher<JMap> here. I keep forgetting the subleties of the Java generics system...
    private DispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher = new DispatchTable<>();
    private DispatchTable<String> stringDispatcher = new DispatchTable<>();


    public JanitorDefaultBuiltins() {
        emptyString = JString.newInstance(stringDispatcher, "");

        // OLD: addStringMethod("length", JStringClass::__length);
        stringDispatcher.addMethod("length", JStringClass::__length);
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
}
