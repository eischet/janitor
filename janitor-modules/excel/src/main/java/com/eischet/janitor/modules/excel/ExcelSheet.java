// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A sheet of an {@link ExcelWorkbook}. Rows and columns are counted from 0, so the cell A1 is row 0, column 0.
 */
public class ExcelSheet extends JanitorComposed<ExcelSheet> {

    public static final DispatchTable<ExcelSheet> DISPATCH = new DispatchTable<>(false);

    /**
     * Excel's built-in date format, which Excel shows in the way of the user's country.
     */
    private static final String DATE_FORMAT = "m/d/yy";
    /**
     * Excel's built-in format for a date with a time, which Excel shows in the way of the user's country.
     */
    private static final String DATE_TIME_FORMAT = "m/d/yy h:mm";

    private static final Signature APPEND = Signature.method("append")
            .param("values", ScriptType.LIST)
            .optional("bold", ScriptType.BOOL, false)
            .returns(ScriptType.SELF)
            .help("Adds a row below the last one and writes the values of the list into its cells, from column 0; "
                  + "see set() for how values are written. Returns the sheet.");
    private static final Signature HEADER = Signature.method("header")
            .param("values", ScriptType.LIST)
            .returns(ScriptType.SELF)
            .help("Adds a row with column titles in bold, like append(values, bold = true). Returns the sheet.");
    private static final Signature SET = Signature.method("set")
            .param("row", ScriptType.INT)
            .param("column", ScriptType.INT)
            .param("value", ScriptType.ANY)
            .optional("bold", ScriptType.BOOL, false)
            .optional("format", ScriptType.STRING, null)
            .returns(ScriptType.SELF)
            .help("Writes a value into a cell; rows and columns are counted from 0. Strings, numbers, booleans, dates and "
                  + "date-times become cells of that type, null an empty cell, anything else its text. Dates and date-times are "
                  + "shown in Excel's date format, text with line breaks is wrapped. format is a number format in Excel's "
                  + "notation, e.g. \"0.00\" or \"dd.mm.yyyy\"; without it, the format set with columnFormat() applies. "
                  + "Returns the sheet.");
    private static final Signature FORMULA = Signature.method("formula")
            .param("row", ScriptType.INT)
            .param("column", ScriptType.INT)
            .param("formula", ScriptType.STRING)
            .optional("bold", ScriptType.BOOL, false)
            .optional("format", ScriptType.STRING, null)
            .returns(ScriptType.SELF)
            .help("Writes a formula into a cell, in Excel's English notation and without the leading \"=\", e.g. "
                  + "\"SUM(B2:B10)\"; Excel calculates it when the file is opened. Returns the sheet.");
    private static final Signature COLUMN_FORMAT = Signature.method("columnFormat")
            .param("column", ScriptType.INT)
            .param("format", ScriptType.STRING)
            .returns(ScriptType.SELF)
            .help("Sets the number format, in Excel's notation, for the values written into this column from now on; null "
                  + "removes it. Returns the sheet.");
    private static final Signature COLUMN_WIDTH = Signature.method("columnWidth")
            .param("column", ScriptType.INT)
            .param("characters", ScriptType.INT)
            .returns(ScriptType.SELF)
            .help("Sets the width of a column in characters (up to 255). Returns the sheet.");
    private static final Signature AUTO_SIZE = Signature.method("autoSize")
            .returns(ScriptType.SELF)
            .help("Fits the width of all columns written so far to their contents; call it after the last row. Not available "
                  + "in streaming workbooks, use columnWidth() there. Returns the sheet.");
    private static final Signature FREEZE = Signature.method("freeze")
            .optional("rows", ScriptType.INT, 1)
            .optional("columns", ScriptType.INT, 0)
            .returns(ScriptType.SELF)
            .help("Keeps the first rows and columns in place while the rest of the sheet scrolls; by default the first row. "
                  + "Returns the sheet.");
    private static final Signature AUTO_FILTER = Signature.method("autoFilter")
            .returns(ScriptType.SELF)
            .help("Turns the first row into filter buttons for all rows and columns written so far; call it after the last "
                  + "row. Returns the sheet.");

    static {
        DISPATCH.setMetaData(Janitor.MetaData.HELP, "A sheet of an Excel workbook; rows and columns are counted from 0.");
        DISPATCH.addMethod(APPEND, (self, process, args) -> self.append(process, "append", args.required("values", JList.class), args.bool("bold")));
        DISPATCH.addMethod(HEADER, (self, process, args) -> self.append(process, "header", args.required("values", JList.class), true));
        DISPATCH.addMethod(SET, (self, process, args) -> {
            final Cell cell = self.cell(process, "set", args.integer("row"), args.integer("column"));
            self.write(process, "set", cell, args.get("value"), args.bool("bold"), args.string("format"));
            return self;
        });
        DISPATCH.addMethod(FORMULA, ExcelSheet::formula);
        DISPATCH.addMethod(COLUMN_FORMAT, (self, process, args) -> {
            self.workbook.requireOpen(process, "columnFormat");
            final int column = self.index(process, "columnFormat", "column", args.integer("column"), self.maxColumns());
            final String format = args.string("format");
            if (format == null) {
                self.columnFormats.remove(column);
            } else {
                self.columnFormats.put(column, format);
            }
            return self;
        });
        DISPATCH.addMethod(COLUMN_WIDTH, (self, process, args) -> {
            self.workbook.requireOpen(process, "columnWidth");
            final int column = self.index(process, "columnWidth", "column", args.integer("column"), self.maxColumns());
            final int characters = self.index(process, "columnWidth", "characters", args.integer("characters"), 256);
            self.sheet.setColumnWidth(column, characters * 256);
            return self;
        });
        DISPATCH.addMethod(AUTO_SIZE, ExcelSheet::autoSize);
        DISPATCH.addMethod(FREEZE, (self, process, args) -> {
            self.workbook.requireOpen(process, "freeze");
            final int rows = self.index(process, "freeze", "rows", args.integer("rows"), self.maxRows());
            final int columns = self.index(process, "freeze", "columns", args.integer("columns"), self.maxColumns());
            self.sheet.createFreezePane(columns, rows);
            return self;
        });
        DISPATCH.addMethod(AUTO_FILTER, (self, process, args) -> {
            self.workbook.requireOpen(process, "autoFilter");
            if (self.nextRow > 0 && self.columnCount > 0) {
                self.sheet.setAutoFilter(new CellRangeAddress(0, self.nextRow - 1, 0, self.columnCount - 1));
            }
            return self;
        });
        DISPATCH.addStringProperty("name", self -> self.sheet.getSheetName())
                .setMetaData(Janitor.MetaData.HELP, "name: The name of the sheet; read-only.");
        DISPATCH.addIntegerProperty("rowCount", self -> self.nextRow)
                .setMetaData(Janitor.MetaData.HELP, "rowCount: The number of rows up to the last one written, which is the row that append() writes next; read-only.");
    }

    private final ExcelWorkbook workbook;
    private final Sheet sheet;
    private final Map<Integer, String> columnFormats = new HashMap<>();
    private int nextRow;
    private int columnCount;

    ExcelSheet(final ExcelWorkbook workbook, final Sheet sheet) {
        super(DISPATCH);
        this.workbook = workbook;
        this.sheet = sheet;
        this.nextRow = sheet.getLastRowNum() + 1;
        for (final Row row : sheet) {
            columnCount = Math.max(columnCount, row.getLastCellNum());
        }
    }

    /**
     * @return the sheet of POI, for hosts that need more than scripts get
     */
    public Sheet getSheet() {
        return sheet;
    }

    private int maxRows() {
        return workbook.getWorkbook().getSpreadsheetVersion().getMaxRows();
    }

    private int maxColumns() {
        final SpreadsheetVersion version = workbook.getWorkbook().getSpreadsheetVersion();
        return version.getMaxColumns();
    }

    private int index(final JanitorScriptProcess process, final String method, final String what, final long value, final int limit) throws JanitorArgumentException {
        if (value < 0 || value >= limit) {
            throw new JanitorArgumentException(process, "%s: %s must be between 0 and %s, but is %s".formatted(method, what, limit - 1, value));
        }
        return (int) value;
    }

    private Cell cell(final JanitorScriptProcess process, final String method, final long rowNumber, final long columnNumber) throws JanitorRuntimeException {
        workbook.requireOpen(process, method);
        final int rowIndex = index(process, method, "row", rowNumber, maxRows());
        final int columnIndex = index(process, method, "column", columnNumber, maxColumns());
        try {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            nextRow = Math.max(nextRow, rowIndex + 1);
            columnCount = Math.max(columnCount, columnIndex + 1);
            final Cell cell = row.getCell(columnIndex);
            return cell != null ? cell : row.createCell(columnIndex);
        } catch (RuntimeException e) {
            // a streaming workbook has written earlier rows to disk already
            throw new JanitorNativeException(process, "%s: cannot write to row %s, column %s".formatted(method, rowIndex, columnIndex), e);
        }
    }

    private ExcelSheet append(final JanitorScriptProcess process, final String method, final JList values, final boolean bold) throws JanitorRuntimeException {
        final int rowIndex = nextRow;
        int column = 0;
        if (values.size() == 0) {
            cell(process, method, rowIndex, 0);
        }
        for (final JanitorObject value : values) {
            write(process, method, cell(process, method, rowIndex, column++), value, bold, null);
        }
        return this;
    }

    private JanitorObject formula(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final Cell cell = cell(process, "formula", args.integer("row"), args.integer("column"));
        final String formula = args.requiredString("formula");
        try {
            cell.setCellFormula(formula.startsWith("=") ? formula.substring(1) : formula);
        } catch (RuntimeException e) {
            throw new JanitorNativeException(process, "formula: Excel cannot read the formula '" + formula + "'", e);
        }
        final String format = args.string("format");
        applyStyle(cell, args.bool("bold"), false, format != null ? format : columnFormats.get(cell.getColumnIndex()));
        return this;
    }

    private JanitorObject autoSize(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        workbook.requireOpen(process, "autoSize");
        if (workbook.isStreaming()) {
            throw new JanitorArgumentException(process, "autoSize: not available in a streaming workbook, use columnWidth()");
        }
        try {
            for (int column = 0; column < columnCount; column++) {
                sheet.autoSizeColumn(column);
            }
        } catch (RuntimeException e) {
            throw new JanitorNativeException(process, "autoSize: error measuring the columns", e);
        }
        return this;
    }

    private void write(final JanitorScriptProcess process, final String method, final Cell cell, final JanitorObject value,
                       final boolean bold, final @Nullable String explicitFormat) throws JanitorRuntimeException {
        String format = explicitFormat != null ? explicitFormat : columnFormats.get(cell.getColumnIndex());
        boolean wrap = false;
        try {
            switch (value) {
                case JNull ignored -> cell.setBlank();
                case JString text -> {
                    cell.setCellValue(text.janitorGetHostValue());
                    wrap = text.janitorGetHostValue().indexOf('\n') >= 0;
                }
                case JBool flag -> cell.setCellValue(flag.janitorIsTrue());
                case JNumber number -> cell.setCellValue(number.toDouble());
                case JDate date -> {
                    cell.setCellValue(date.janitorGetHostValue());
                    format = format != null || hasNumberFormat(cell) ? format : DATE_FORMAT;
                }
                case JDateTime dateTime -> {
                    cell.setCellValue(dateTime.janitorGetHostValue());
                    format = format != null || hasNumberFormat(cell) ? format : DATE_TIME_FORMAT;
                }
                default -> {
                    final String text = value.janitorToString();
                    cell.setCellValue(text);
                    wrap = text.indexOf('\n') >= 0;
                }
            }
        } catch (RuntimeException e) {
            // e.g. a text beyond Excel's limit of 32767 characters per cell
            throw new JanitorNativeException(process, "%s: cannot write the value into row %s, column %s"
                    .formatted(method, cell.getRowIndex(), cell.getColumnIndex()), e);
        }
        applyStyle(cell, bold, wrap, format);
    }

    /**
     * A cell of an opened workbook can bring its own number format, e.g. a date format in a template; it is kept then.
     *
     * @param cell the cell
     * @return true if the cell has a number format other than Excel's "General"
     */
    private static boolean hasNumberFormat(final Cell cell) {
        return cell.getCellStyle().getDataFormat() != 0;
    }

    private void applyStyle(final Cell cell, final boolean bold, final boolean wrap, final @Nullable String format) {
        final CellStyle style = workbook.style(bold, wrap, format);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

}
