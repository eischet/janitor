// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CsvFileReader extends TabularFileReader {

    private final Reader in;
    private final CSVParser records;

    public CsvFileReader(final String filename, final String dialect) throws IOException {
        this(filename, dialect, new CsvFormatDefinition());
    }

    public CsvFileReader(final String filename, final String dialect, final CsvFormatDefinition formatDefinition) throws IOException {
        if ("csv".equalsIgnoreCase(dialect) || "csv-utf16le".equalsIgnoreCase(dialect)) {
            this.in = new InputStreamReader(new BOMInputStream(new FileInputStream(filename)), StandardCharsets.UTF_16LE);
            this.records = formatDefinition.getFormat().parse(in);
        } else if ("csv-utf8".equalsIgnoreCase(dialect)) {
            this.in = new InputStreamReader(new BOMInputStream(new FileInputStream(filename)), StandardCharsets.UTF_8);
            this.records = formatDefinition.getFormat().parse(in);
        } else {
            throw new IOException("invalid csv format: csv or csv-utf8 can be used");
        }
    }

    public CsvFileReader(final byte[] data, final String dialect, final CsvFormatDefinition formatDefinition) throws IOException {
        if ("csv".equalsIgnoreCase(dialect) || "csv-utf16le".equalsIgnoreCase(dialect)) {
            this.in = new InputStreamReader(new BOMInputStream(new ByteArrayInputStream(data)), StandardCharsets.UTF_16LE);
            this.records = formatDefinition.getFormat().parse(in);
        } else if ("csv-utf8".equalsIgnoreCase(dialect)) {
            this.in = new InputStreamReader(new BOMInputStream(new ByteArrayInputStream(data)), StandardCharsets.UTF_8);
            this.records = formatDefinition.getFormat().parse(in);
        } else {
            throw new IOException("invalid csv format: csv or csv-utf8 can be used");
        }
    }

    boolean wasClosed = false;

    @Override
    public void close() throws IOException {
        if (!wasClosed) {
            records.close();
            in.close();
            wasClosed = true;
        }
    }

    @NotNull
    @Override
    public Iterator<FileRow> iterator() {
        final Iterator<CSVRecord> original = records.iterator();
        if (start > 0) {
            for (long i = 0; i < start; i++) {
                original.next();
            }
        }

        if (useHeaders) {
            final CSVRecord headerRow = original.next();
            for (final String cell : headerRow) {
                headers.add(cell);
            }
        }


        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public FileRow next() {
                return new CsvFileRow(original.next(), headers);
            }
        };
    }


    @Override
    public @NotNull String janitorClassName() {
        return "csv-reader";
    }
}
