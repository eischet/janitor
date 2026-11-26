// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.types.JanitorScopeAware;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TabularFileReader extends JanitorComposed<TabularFileReader> implements Iterable<FileRow>, JIterable, JanitorScopeAware {

    public static final DispatchTable<TabularFileReader> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.setMetaData(Janitor.MetaData.CLASS, "TabularFileReader");
        DISPATCH.addMethod("useHeaders", (self, process, args) -> self.useHeaders())
                .setMetaData(Janitor.MetaData.HELP, "useHeaders(): Treats the first row read as the header row with the column names; returns the reader.")
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
        DISPATCH.addMethod("startAt", (self, process, args) -> self.startAt(args.getRequiredLongValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "startAt(row): Skips the given number of rows before reading; returns the reader.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("row", ScriptType.INT)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
        DISPATCH.addMethod("sheet", (self, process, args) -> {
            final JanitorObject sheet = args.require(1).get(0);
            try {
                if (sheet instanceof JInt number) {
                    return self.selectSheet(number.getAsInt());
                }
                if (sheet instanceof JString name) {
                    return self.selectSheet(name.janitorGetHostValue());
                }
            } catch (IllegalArgumentException e) {
                throw new JanitorArgumentException(process, "sheet: " + e.getMessage());
            }
            throw new JanitorArgumentException(process, "sheet: expected the number or the name of a sheet, but got: " + sheet.janitorToString());
        })
                .setMetaData(Janitor.MetaData.HELP, "sheet(sheet): Reads another sheet of an Excel file than the first one, given by its number (from 0) or its name; returns the reader.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("sheet", ScriptType.ANY)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
        DISPATCH.addMethod("close", (self, process, args) -> {
            try {
                self.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return self;
        })
                .setMetaData(Janitor.MetaData.HELP, "close(): Closes the file; returns the reader.")
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
    }

    @Override
    public void janitorLeaveScope() {
        try {
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected long start = 0;
    protected final List<String> headers = new ArrayList<>();
    protected boolean useHeaders;


    public TabularFileReader() {
        super(DISPATCH);
    }

    public TabularFileReader startAt(final long row) {
        start = row;
        return this;
    }

    public static TabularFileReader build(@NotNull TabularFileType fileType, @NotNull String fileName, @NotNull CsvFormatDefinition format) throws IOException {
        return switch (fileType) {
            case EXCEL -> new ExcelFileReader(fileName);
            case CSV_UTF8, CSV_UTF16LE, CSV -> new CsvFileReader(fileName, fileType.getCode(), format);
        };
    }

    public static TabularFileReader build(@NotNull TabularFileType fileType, byte @NotNull [] data, @NotNull CsvFormatDefinition format) throws IOException {
        return switch (fileType) {
            case EXCEL -> new ExcelFileReader(data);
            case CSV_UTF8, CSV_UTF16LE, CSV -> new CsvFileReader(data, fileType.getCode(), format);
        };
    }

    /**
     * Chooses the sheet to read by its number. Only files with sheets can do that.
     *
     * @param index the number of the sheet, from 0
     * @return the reader
     * @throws IllegalArgumentException if there is no such sheet, or the file has no sheets
     */
    public TabularFileReader selectSheet(final int index) {
        throw new IllegalArgumentException("this file has no sheets");
    }

    /**
     * Chooses the sheet to read by its name. Only files with sheets can do that.
     *
     * @param name the name of the sheet
     * @return the reader
     * @throws IllegalArgumentException if there is no such sheet, or the file has no sheets
     */
    public TabularFileReader selectSheet(final @NotNull String name) {
        throw new IllegalArgumentException("this file has no sheets");
    }

    public TabularFileReader useHeaders() {
        this.useHeaders = true;
        return this;
    }

    public abstract void close() throws IOException;

    @Override
    public Iterator<FileRow> getIterator() {
        return iterator();
    }

}
