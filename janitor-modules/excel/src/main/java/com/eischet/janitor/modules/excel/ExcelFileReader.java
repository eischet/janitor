// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelFileReader extends TabularFileReader implements AutoCloseable {

    private Sheet sheet;
    private final Workbook workbook;

    @Override
    public ExcelFileReader selectSheet(final int index) {
        if (index < 0 || index >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("there is no sheet number " + index + ", the file has these sheets, counted from 0: " + sheetNames());
        }
        sheet = workbook.getSheetAt(index);
        return this;
    }

    @Override
    public ExcelFileReader selectSheet(final @NotNull String name) {
        final Sheet found = workbook.getSheet(name);
        if (found == null) {
            throw new IllegalArgumentException("there is no sheet named '" + name + "', the file has these sheets: " + sheetNames());
        }
        sheet = found;
        return this;
    }

    private List<String> sheetNames() {
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            names.add(workbook.getSheetName(i));
        }
        return names;
    }

    public ExcelFileReader(final String sourceFile) throws IOException {
        final File sourceFileFile = new File(sourceFile);
        // read into memory: POI changes a file that it opens directly
        final byte[] bytes = Files.readAllBytes(sourceFileFile.toPath());
        workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes));
        sheet = workbook.getSheetAt(0);
    }

    public ExcelFileReader(final byte[] data) throws IOException {
        workbook = WorkbookFactory.create(new ByteArrayInputStream(data));
        sheet = workbook.getSheetAt(0);
    }


    public static String clean(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    public static LocalDateTime extractDate(final Row row, final int index) {
        @SuppressWarnings("Duplicates")
        final Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        final CellType ct = cell.getCellType();
        if (ct == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue();
        }
        return null;
    }

    public static Long extractLong(final Row row, final int index) {
        final Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        final CellType ct = cell.getCellType();
        if (ct == CellType.STRING) {
            return Long.parseLong(clean(cell.getStringCellValue()), 10);
        } else if (ct == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else {
            return null;
        }
    }

    public static String extractString(final Row row, final int index) {
        final Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        final CellType ct = cell.getCellType();
        if (ct == CellType.STRING) {
            return clean(cell.getStringCellValue());
        } else if (ct == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        } else {
            return null;
        }
    }

    @Override
    public ExcelFileReader useHeaders() {
        this.useHeaders = true;
        return this;
    }


    @NotNull
    @Override
    public Iterator<FileRow> iterator() {
        final Iterator<Row> original = sheet.iterator();
        if (start > 0) {
            for (long i = 0; i < start; i++) {
                original.next();
            }
        }
        if (useHeaders) {
            final Row headerRow = original.next();
            for (final Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
        }
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public FileRow next() {
                return new ExcelFileRow(original.next(), headers);
            }
        };
    }

    boolean wasClosed = false;

    @Override
    public void close() throws IOException {
        if (!wasClosed) {
            workbook.close();
            wasClosed = true;
        }
    }

    @Override
    public @NotNull String janitorClassName() {
        return "excel-file-reader";
    }
}
