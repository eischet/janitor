// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDateTime;
import java.util.List;

public class CsvFileRow implements FileRow {

    private final CSVRecord row;
    private final List<String> headers;

    public CsvFileRow(final CSVRecord row, final List<String> headers) {
        this.row = row;
        this.headers = headers;
    }

    @Override
    public @NotNull @Unmodifiable List<String> getHeaders() {
        return headers == null ? List.of() : List.copyOf(headers);
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

    @Override
    public long getRowNum() {
        return row.getRecordNumber() - 1;
    }

    @Override
    public String getString(final int index) {
        final String val = row.get(index);
        if (val != null && val.startsWith("\"") && val.endsWith("\"")) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    @Override
    public Long getLong(final int index) {
        return Long.parseLong(getString(index));
    }

    @Override
    public LocalDateTime getDate(final int index) {
        return null; // LATER parse dates (if needed)
    }

    @Override
    public String toString() {
        return row.toString();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "csv-row";
    }
}
