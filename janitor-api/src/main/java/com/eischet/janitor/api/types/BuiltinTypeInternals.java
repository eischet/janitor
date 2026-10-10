// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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

/**
 * Gives access to the dispatchers (the "dispatch tables") of the built-in types.
 * This is an internal interface that is used by the environment to wire up the built-in types.
 */
public interface BuiltinTypeInternals {
    /**
     * @return the dispatch table for the base type that all other types inherit from
     */
    DispatchTable<JanitorObject> getBaseDispatcher();

    /**
     * @return the dispatch table for maps
     */
    WrapperDispatchTable<Map<JanitorObject, JanitorObject>> getMapDispatcher();

    /**
     * @return the dispatch table for strings
     */
    DispatchTable<JString> getStringDispatcher();

    /**
     * @return the dispatch table for lists
     */
    DispatchTable<JList> getListDispatcher();

    /**
     * @return the dispatch table for sets
     */
    WrapperDispatchTable<Set<JanitorObject>> getSetDispatcher();

    /**
     * @return the dispatch table for integers
     */
    WrapperDispatchTable<Long> getIntDispatcher();

    /**
     * @return the dispatch table for binary data
     */
    WrapperDispatchTable<byte[]> getBinaryDispatcher();

    /**
     * @return the dispatch table for floating point numbers
     */
    WrapperDispatchTable<Double> getFloatDispatcher();

    /**
     * @return the dispatch table for regular expressions
     */
    WrapperDispatchTable<Pattern> getRegexDispatcher();

    /**
     * @return the dispatch table for durations
     */
    DispatchTable<JDuration> getDurationDispatch();

    /**
     * @return the dispatch table for datetimes
     */
    DispatchTable<JDateTime> getDateTimeDispatch();
}
