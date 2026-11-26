// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import org.apache.poi.ss.util.CellReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FileRow extends JanitorObject, HasDispatcher<FileRow> {

    DispatchTable<FileRow> DISPATCH = dispatchTable();

    private static DispatchTable<FileRow> dispatchTable() {
        final DispatchTable<FileRow> dispatch = new DispatchTable<>();
        dispatch.setMetaData(Janitor.MetaData.CLASS, "FileRow");
        dispatch.addMethod("asMap", (self, process, arguments) -> {
            @NotNull final JMap map = Janitor.map();
            self.getHeaders().forEach(header -> map.put(header, Janitor.nullableString(self.getStringByHeader(header))));
            return map;
        }).setMetaData(Janitor.MetaData.HELP, "asMap(): Returns the row as a map from column header to value.")
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.MAP);
        dispatch.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, (self, process, arguments) -> {
            final JanitorObject index = arguments.get(0);
            if (index instanceof JInt integerIndex) {
                return Janitor.nullableString(self.getString(integerIndex.getAsInt()));
            }
            if (index instanceof JString stringIndex) {
                return Janitor.nullableString(self.getStringByHeader(stringIndex.janitorGetHostValue()));
            }
            return Janitor.NULL;
        }).setMetaData(Janitor.MetaData.HELP, "row[index]: The value of the column with the number (from 0) or the header, as a string.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("index", ScriptType.ANY)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.STRING);
        return dispatch;
    }

    long getRowNum();
    String getString(int index);
    String getStringByHeader(String header);
    Long getLong(int index);
    LocalDateTime getDate(int index);
    @NotNull
    @Unmodifiable List<String> getHeaders();

    default LocalDate getLocalDate(int index) {
        final LocalDateTime input = getDate(index);
        if (input == null) {
            return null;
        }
        return input.toLocalDate();
    }

    @Override
    default FileRow janitorGetHostValue() {
        return this;
    }

    @Override
    @Nullable
    default JanitorObject janitorGetAttribute(JanitorScriptProcess runningScript, String name, final boolean required) throws JanitorRuntimeException {
        final JanitorObject attribute = DISPATCH.dispatch(this, runningScript, name);
        if (attribute != null) {
            return attribute;
        }
        // the columns by their Excel names, e.g. row.B or row.dateB
        if (name != null && name.startsWith("date") && isColumnName(name.substring(4))) {
            final int index = CellReference.convertColStringToIndex(name.substring(4));
            final LocalDate ldt = getLocalDate(index);
            return runningScript.getBuiltins().nullableDateTime(ldt == null ? null : ldt.atStartOfDay());
        }
        if (isColumnName(name)) {
            final int index = CellReference.convertColStringToIndex(name);
            return Janitor.nullableString(getString(index));
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    /**
     * Tells whether a name can be the name of an Excel column: one to three letters, as Excel's last column is XFD. Longer names
     * would make the column number overflow.
     *
     * @param name the name used in the script
     * @return true if the name is a column name
     */
    private static boolean isColumnName(final String name) {
        if (name == null || name.isEmpty() || name.length() > 3) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                return false;
            }
        }
        return true;
    }

    @Override
    default Dispatcher<FileRow> getDispatcher() {
        return DISPATCH;
    }
}
