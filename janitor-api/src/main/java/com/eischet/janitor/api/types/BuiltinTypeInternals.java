package com.eischet.janitor.api.types;

import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JDuration;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface BuiltinTypeInternals {
    DispatchTable<JanitorObject> getBaseDispatcher();

    WrapperDispatchTable<Map<JanitorObject, JanitorObject>> getMapDispatcher();

    DispatchTable<JString> getStringDispatcher();

    DispatchTable<JList> getListDispatcher();

    WrapperDispatchTable<Set<JanitorObject>> getSetDispatcher();

    WrapperDispatchTable<Long> getIntDispatcher();

    WrapperDispatchTable<byte[]> getBinaryDispatcher();

    WrapperDispatchTable<Double> getFloatDispatcher();

    WrapperDispatchTable<Pattern> getRegexDispatcher();

    DispatchTable<JDuration> getDurationDispatch();

    DispatchTable<JDateTime> getDateTimeDispatch();
}
