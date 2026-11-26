// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.excel;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.modules.excel.ExcelModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The "excel" module reads Excel and CSV files row by row; a row gives its values by number, Excel column name or header.
 */
public class ExcelReadingTestCase extends JanitorTest {

    private String run(final @Language("Janitor") String script) throws Exception {
        return evaluate(script, env -> env.addModule(ExcelModule.REGISTRATION), null).janitorToString();
    }

    @Test
    void readsWhatItWrote() throws Exception {
        assertEquals("[4711|Printer on fire|4711|Printer on fire, 4712|Coffee machine|4712|Coffee machine]", run("""
                import excel;
                wb = excel.create();
                wb.sheet("Cover").append(["nothing to see"]);
                wb.sheet("Tickets").header(["Number", "Title"]).append([4711, "Printer on fire"]).append([4712, "Coffee machine"]);
                found = [];
                for (row in excel.fromBinary(wb.toBinary()).sheet("Tickets").useHeaders()) {
                    found.add(row["Number"] + "|" + row["Title"] + "|" + row.A + "|" + row[1]);
                }
                return found;
                """));
    }

    @Test
    void sheetsByNumberAndName() throws Exception {
        final @Language("Janitor") String book = """
                import excel;
                wb = excel.create();
                wb.sheet("Cover").append(["first"]);
                wb.sheet("Second sheet").append(["second"]);
                data = wb.toBinary();
                found = [];
                """;
        assertEquals("[first]", run(book + "for (row in excel.fromBinary(data)) { found.add(row.A); } return found;"));
        assertEquals("[second]", run(book + "for (row in excel.fromBinary(data).sheet(1)) { found.add(row.A); } return found;"));
        assertEquals("[second]", run(book + "for (row in excel.fromBinary(data).sheet('Second sheet')) { found.add(row.A); } return found;"));
        final JanitorRuntimeException missing = assertThrows(JanitorRuntimeException.class,
                () -> run(book + "for (row in excel.fromBinary(data).sheet('Missing')) { found.add(row.A); } return found;"));
        assertTrue(missing.getMessage().contains("Second sheet"), missing.getMessage());
    }

    @Test
    void csvFromTextBinaryAndFile(@TempDir Path tempDir) throws Exception {
        assertEquals("[4711 Printer on fire]", run("""
                import excel;
                found = [];
                for (row in excel.fromString("Number;Title\\n4711;Printer on fire").useHeaders()) { m = row.asMap(); found.add(m["Number"] + " " + m["Title"]); }
                return found;
                """));
        final @Language("Janitor") String binary = """
                import excel;
                data = "skip me\\na,b,c".toBinaryUtf8();
                found = [];
                for (row in excel.fromBinary(data, %s, delimiter = ",").startAt(1)) { found.add(row.A + row.B + row.C); }
                return found;
                """;
        assertEquals("[abc]", run(binary.formatted("'CSV_UTF8'")));
        assertEquals("[abc]", run(binary.formatted("dialect = 'CSV_UTF8'")));
        assertThrows(JanitorRuntimeException.class, () -> run(binary.formatted("'CSV_UTF8', dialect = 'EXCEL'")));

        final Path file = tempDir.resolve("list.csv");
        Files.writeString(file, "x;y\n", StandardCharsets.UTF_8);
        assertEquals("[xy]", evaluate("""
                import excel;
                found = [];
                reader = excel.read(path);
                for (row in reader) { found.add(row.A + row.B); }
                reader.close();
                return found;
                """, env -> env.addModule(ExcelModule.REGISTRATION), globals -> globals.bind("path", file.toString())).janitorToString());
    }

    @Test
    void onlyExcelColumnNamesAreColumns() {
        final @Language("Janitor") String rows = "import excel; for (row in excel.fromString('a;b;c')) { %s }";
        final JanitorRuntimeException error = assertThrows(JanitorRuntimeException.class, () -> run(rows.formatted("return row.toString;")));
        assertFalse(error.getMessage().contains("out of bounds"), error.getMessage());
        final JanitorRuntimeException dateError = assertThrows(JanitorRuntimeException.class, () -> run(rows.formatted("return row.dateOfBirth;")));
        assertFalse(dateError.getMessage().contains("out of bounds"), dateError.getMessage());
    }

}
