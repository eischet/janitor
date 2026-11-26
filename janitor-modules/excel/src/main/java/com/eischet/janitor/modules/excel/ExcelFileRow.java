// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import org.apache.poi.ss.usermodel.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDateTime;
import java.util.List;

public class ExcelFileRow implements FileRow {

    private final Row row;
    private final List<String> headers;

    public ExcelFileRow(final Row row, final List<String> headers) {
        this.row = row;
        this.headers = headers;
    }

    @Override
    public @NotNull @Unmodifiable List<String> getHeaders() {
        return headers == null ? List.of() : List.copyOf(headers);
    }

    public long getRowNum() {
        return row.getRowNum();
    }

    public String getString(final int excelIndex) {
        return ExcelFileReader.extractString(row, excelIndex);
    }

    @Override
    public String getStringByHeader(final String header) {
        if (header == null) {
            return null;
        }
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        final int index = headers.indexOf(header);
        if (index < 0) {
            return null;
        }
        return getString(index);
    }

    public Long getLong(final int excelIndex) {
        return ExcelFileReader.extractLong(row, excelIndex);
    }

    public LocalDateTime getDate(final int excelIndex) {
        return ExcelFileReader.extractDate(row, excelIndex);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "file-row";
    }

    @Override
    public String toString() {
        return row.toString();
    }
}
