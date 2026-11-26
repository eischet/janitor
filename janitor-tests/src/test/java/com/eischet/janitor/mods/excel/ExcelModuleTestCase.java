// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.excel;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.modules.excel.ExcelModule;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The "excel" module: what a script writes must arrive in the Excel file, with the cell types of its values.
 */
public class ExcelModuleTestCase extends JanitorTest {

    private Workbook run(final @Language("Janitor") String script) throws JanitorRuntimeException, JanitorCompilerException, IOException {
        final JanitorObject result = evaluate(script, env -> env.addModule(ExcelModule.REGISTRATION), null);
        final JBinary binary = assertInstanceOf(JBinary.class, result);
        return WorkbookFactory.create(new ByteArrayInputStream(binary.janitorGetHostValue()));
    }

    @Test
    void valuesKeepTheirTypes() throws Exception {
        try (Workbook workbook = run("""
                import excel;
                wb = excel.create();
                sheet = wb.sheet("Tickets");
                sheet.header(["Number", "Title", "Logged", "Done", "Hours", "Note"]);
                sheet.append([4711, "Printer on fire", @2026-11-26, true, 1.5, null]);
                sheet.append([4712, "two\\nlines", @2026-11-26-08:30, false, 0.25, "x"]);
                return wb.toBinary();
                """)) {
            final Sheet sheet = workbook.getSheet("Tickets");
            assertNotNull(sheet);
            assertEquals(2, sheet.getLastRowNum());

            final Cell title = sheet.getRow(0).getCell(1);
            assertEquals("Title", title.getStringCellValue());
            assertTrue(workbook.getFontAt(title.getCellStyle().getFontIndex()).getBold(), "the header row is bold");

            assertEquals(CellType.NUMERIC, sheet.getRow(1).getCell(0).getCellType());
            assertEquals(4711d, sheet.getRow(1).getCell(0).getNumericCellValue());
            assertEquals("Printer on fire", sheet.getRow(1).getCell(1).getStringCellValue());
            assertFalse(workbook.getFontAt(sheet.getRow(1).getCell(1).getCellStyle().getFontIndex()).getBold());
            assertTrue(DateUtil.isCellDateFormatted(sheet.getRow(1).getCell(2)), "a date is shown as a date");
            assertEquals(LocalDateTime.of(2026, 11, 26, 0, 0), sheet.getRow(1).getCell(2).getLocalDateTimeCellValue());
            assertEquals(CellType.BOOLEAN, sheet.getRow(1).getCell(3).getCellType());
            assertTrue(sheet.getRow(1).getCell(3).getBooleanCellValue());
            assertEquals(1.5d, sheet.getRow(1).getCell(4).getNumericCellValue());
            final Cell empty = sheet.getRow(1).getCell(5);
            assertTrue(empty == null || empty.getCellType() == CellType.BLANK, "null is an empty cell");

            assertTrue(sheet.getRow(2).getCell(1).getCellStyle().getWrapText(), "text with line breaks is wrapped");
            assertTrue(DateUtil.isCellDateFormatted(sheet.getRow(2).getCell(2)));
            assertEquals(LocalDateTime.of(2026, 11, 26, 8, 30), sheet.getRow(2).getCell(2).getLocalDateTimeCellValue());
        }
    }

    @Test
    void cellsFormulasAndLayout() throws Exception {
        try (Workbook workbook = run("""
                import excel;
                wb = excel.create();
                sheet = wb.sheet("Sums");
                sheet.header(["Item", "Price"]);
                sheet.columnFormat(1, "0.00");
                sheet.append(["a", 1.5]).append(["b", 2]);
                sheet.set(3, 0, "Sum", bold = true);
                sheet.formula(3, 1, "=SUM(B2:B3)");
                sheet.set(5, 4, 17, format = "0%");
                sheet.columnWidth(0, 30);
                sheet.freeze().autoFilter().autoSize();
                if (sheet.rowCount != 6) { throw "rowCount is " + sheet.rowCount; }
                wb.sheet("Second");
                if (wb.sheets != ["Sums", "Second"]) { throw "sheets are " + wb.sheets; }
                if (wb.sheet(0).name != "Sums") { throw "sheet 0 is " + wb.sheet(0).name; }
                return wb.toBinary();
                """)) {
            final Sheet sheet = workbook.getSheetAt(0);
            assertEquals("0.00", sheet.getRow(1).getCell(1).getCellStyle().getDataFormatString());
            assertEquals(CellType.FORMULA, sheet.getRow(3).getCell(1).getCellType());
            assertEquals("SUM(B2:B3)", sheet.getRow(3).getCell(1).getCellFormula());
            assertEquals("0.00", sheet.getRow(3).getCell(1).getCellStyle().getDataFormatString(), "the format of the column applies to formulas");
            assertEquals("0%", sheet.getRow(5).getCell(4).getCellStyle().getDataFormatString());
            assertNull(sheet.getRow(4), "rows that nothing was written to do not exist");
            assertEquals(1, sheet.getPaneInformation().getHorizontalSplitPosition(), "the first row is frozen");
            assertEquals(2, workbook.getNumberOfSheets());
        }
    }

    @Test
    void stylesAreSharedBetweenCells() throws Exception {
        try (Workbook workbook = run("""
                import excel;
                wb = excel.create();
                sheet = wb.sheet("Many");
                for (i from 0 to 4999) {
                    sheet.append([i, @2026-11-26, "text", i / 7.0], bold = i % 2 == 0);
                }
                return wb.toBinary();
                """)) {
            assertEquals(4999, workbook.getSheetAt(0).getLastRowNum());
            assertTrue(workbook.getNumCellStyles() < 10, "20000 cells must not create a style each, Excel allows 64000: " + workbook.getNumCellStyles());
        }
    }

    @Test
    void streamingWorkbook() throws Exception {
        try (Workbook workbook = run("""
                import excel;
                wb = excel.create(streaming = true);
                sheet = wb.sheet("Big");
                sheet.header(["n", "square"]);
                for (i from 1 to 20000) {
                    sheet.append([i, i * i]);
                }
                sheet.columnWidth(1, 20);
                return wb.toBinary();
                """)) {
            final Sheet sheet = workbook.getSheetAt(0);
            assertEquals(20000, sheet.getLastRowNum());
            assertEquals(20000d * 20000d, sheet.getRow(20000).getCell(1).getNumericCellValue());
        }
        final JanitorRuntimeException closed = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                wb = excel.create(streaming = true);
                wb.sheet("x").append([1]);
                wb.toBinary();
                return wb.toBinary();
                """));
        assertTrue(closed.getMessage().contains("closed"), closed.getMessage());
        final JanitorRuntimeException autoSize = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                wb = excel.create(streaming = true);
                wb.sheet("x").append([1]).autoSize();
                return wb.toBinary();
                """));
        assertTrue(autoSize.getMessage().contains("columnWidth"), autoSize.getMessage());
    }

    @Test
    void openFillsATemplateAndSaves(@TempDir Path tempDir) throws Exception {
        final Path template = tempDir.resolve("template.xlsx");
        final Path target = tempDir.resolve("filled.xlsx");
        final @Language("Janitor") String script = """
                import excel;
                first = excel.create();
                first.sheet("Report").header(["Customer", "Date"]);
                first.sheet("Report").set(1, 1, @2026-01-01, format = "yyyy-mm-dd");
                first.save(template);

                wb = excel.open(template);
                sheet = wb.sheet("Report");
                sheet.set(1, 0, "ACME");
                sheet.set(1, 1, @2026-11-26);
                sheet.append(["next", null]);
                wb.save(target);

                again = excel.open(wb.toBinary());
                return again.sheet(0).rowCount;
                """;
        final JanitorObject rows = evaluate(script, env -> env.addModule(ExcelModule.REGISTRATION), globals -> {
            globals.bind("template", template.toString());
            globals.bind("target", target.toString());
        });
        assertEquals("3", rows.janitorToString());
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(Files.readAllBytes(target)))) {
            final Sheet sheet = workbook.getSheet("Report");
            assertEquals("ACME", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("yyyy-mm-dd", sheet.getRow(1).getCell(1).getCellStyle().getDataFormatString(), "the template's date format is kept");
            assertEquals(LocalDateTime.of(2026, 11, 26, 0, 0), sheet.getRow(1).getCell(1).getLocalDateTimeCellValue());
            assertEquals("next", sheet.getRow(2).getCell(0).getStringCellValue());
        }
        try (Workbook untouched = WorkbookFactory.create(new ByteArrayInputStream(Files.readAllBytes(template)))) {
            assertNull(untouched.getSheet("Report").getRow(2), "opening a file does not change it");
        }
    }

    @Test
    void mistakesAreReported() {
        final JanitorRuntimeException row = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                wb = excel.create();
                wb.sheet("x").set(-1, 0, "a");
                return wb.toBinary();
                """));
        assertTrue(row.getMessage().contains("row must be between 0 and 1048575"), row.getMessage());
        final JanitorRuntimeException closed = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                wb = excel.create();
                sheet = wb.sheet("x");
                wb.close();
                sheet.append([1]);
                return null;
                """));
        assertTrue(closed.getMessage().contains("the workbook is closed"), closed.getMessage());
        final JanitorRuntimeException noSheet = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                return excel.create().sheet(3);
                """));
        assertTrue(noSheet.getMessage().contains("there is no sheet number 3"), noSheet.getMessage());
        final JanitorRuntimeException notExcel = assertThrows(JanitorRuntimeException.class, () -> run("""
                import excel;
                return excel.open("no such file.xlsx");
                """));
        assertTrue(notExcel.getMessage().contains("error reading file"), notExcel.getMessage());
    }

}
