// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import com.eischet.janitor.logging.JanitorLogger;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The "excel" module: reads Excel and CSV files row by row, and creates and changes Excel workbooks, on top of Apache POI
 * and Apache Commons CSV.
 * <pre>
 *   import excel;
 *   for (row in excel.read("tickets.xlsx").useHeaders()) {
 *       print(row["Number"], row.B);
 *   }
 * </pre>
 * <pre>
 *   import excel;
 *   wb = excel.create();
 *   sheet = wb.sheet("Tickets");
 *   sheet.header(["Number", "Title", "Logged"]);
 *   sheet.append([4711, "Printer on fire", @today]);
 *   sheet.autoSize();
 *   data = wb.toBinary();
 * </pre>
 * The module offers a small API of its own instead of mapping the classes of POI: a workbook, its sheets, and cells that
 * are addressed by row and column. Cell types and the formats of dates follow the Janitor values that a script writes.
 * <p>
 * {@code read}, {@code open} and {@code save} read and write arbitrary paths of the file system, so a host registers this module only
 * for scripts that may do that.
 * </p>
 */
public class ExcelModule extends JanitorComposed<ExcelModule> implements JanitorModule {

    public static final DispatchTable<ExcelModule> dispatcher = new DispatchTable<>(ExcelModule::new, false);

    static {
        dispatcher.setMetaData(Janitor.MetaData.HELP, "Reads Excel and CSV files row by row, and creates and changes Excel workbooks.");
    }

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("excel", ExcelModule::new).withDispatcher(() -> ExcelModule.dispatcher);

    private static final Signature CREATE = Signature.method("create")
            .optional("streaming", ScriptType.BOOL, false)
            .returns(ScriptType.of(ExcelWorkbook.DISPATCH))
            .help("Creates a new, empty workbook (.xlsx). With streaming, rows are written to a temporary file as they are added, "
                  + "for sheets with very many rows: rows can only be appended then, autoSize() is not available, and the workbook "
                  + "is closed once it has been written with toBinary() or save().");
    private static final Signature OPEN = Signature.method("open")
            .param("source", ScriptType.ANY)
            .returns(ScriptType.of(ExcelWorkbook.DISPATCH))
            .help("Opens an existing workbook (.xlsx or .xls) from a file path or from binary data, e.g. a template to fill in. "
                  + "The source is read completely and is not changed; write the result with toBinary() or save().");

    static {
        dispatcher.addMethod(CREATE, ExcelModule::create);
        dispatcher.addMethod(OPEN, ExcelModule::open);
    }

    private static final JanitorLogger log = JanitorLogger.getLogger(ExcelModule.class);

    /**
     * The named argument that chooses the dialect; it is not a setting of the CSV format.
     */
    private static final Set<String> DIALECT = Set.of("dialect");

    static {
        dispatcher.addMethod("read", (self, process, args) -> {
            final String filename = args.getRequired(0, JString.class).janitorGetHostValue();
            final TabularFileType type = guessDialect(filename, dialectOf(process, args));
            final CsvFormatDefinition format = formatOf(process, args, DIALECT);
            return reading(process, "read", () -> TabularFileReader.build(type, filename, format));
        })
                .setMetaData(Janitor.MetaData.HELP, "read(filename, dialect = null, **format): Opens an Excel or CSV file for reading row by row; without a dialect (EXCEL, CSV_UTF8, ...) it is guessed from the file name. The keyword arguments set the CSV format.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("filename", ScriptType.STRING), ScriptType.parameter("dialect", ScriptType.STRING), ScriptType.parameter("**format", ScriptType.MAP)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
        dispatcher.addMethod("fromString", (self, process, args) -> {
            final String contents = args.getRequired(0, JString.class).janitorGetHostValue();
            final CsvFormatDefinition format = formatOf(process, args, Collections.emptySet());
            return reading(process, "fromString", () -> TabularFileReader.build(TabularFileType.CSV_UTF8, contents.getBytes(StandardCharsets.UTF_8), format));
        })
                .setMetaData(Janitor.MetaData.HELP, "fromString(contents, **format): Reads CSV text row by row. The keyword arguments set the CSV format.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("contents", ScriptType.STRING), ScriptType.parameter("**format", ScriptType.MAP)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
        dispatcher.addMethod("fromBinary", (self, process, args) -> {
            final byte[] data = args.getRequired(0, JBinary.class).janitorGetHostValue();
            final TabularFileType type = guessDialect(null, dialectOf(process, args));
            final CsvFormatDefinition format = formatOf(process, args, DIALECT);
            return reading(process, "fromBinary", () -> TabularFileReader.build(type, data, format));
        })
                .setMetaData(Janitor.MetaData.HELP, "fromBinary(data, dialect = null, **format): Reads an Excel or CSV file from binary data, row by row; the dialect defaults to EXCEL. The keyword arguments set the CSV format.")
                .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(ScriptType.parameter("data", ScriptType.ANY), ScriptType.parameter("dialect", ScriptType.STRING), ScriptType.parameter("**format", ScriptType.MAP)))
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.of(TabularFileReader.DISPATCH));
    }

    /**
     * Opens a reader and reports what goes wrong as an error of the script. For hosts that add reading functions for their
     * own sources.
     *
     * @param process the running script
     * @param method  the name of the function, for the error message
     * @param opener  opens the reader
     * @return the reader
     * @throws JanitorRuntimeException if the source cannot be read
     */
    public static TabularFileReader reading(final JanitorScriptProcess process, final String method, final Callable<TabularFileReader> opener) throws JanitorRuntimeException {
        try {
            return opener.call();
        } catch (JanitorRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JanitorNativeException(process, method + ": error reading the file", e);
        }
    }

    /**
     * Reads the dialect of a reading function, its second argument, which scripts may also pass by name
     * ({@code dialect = "CSV_UTF8"}). For hosts that add reading functions for their own sources.
     *
     * @param process the running script
     * @param args    the arguments of the call
     * @return the dialect, or null for the default
     * @throws JanitorRuntimeException if the dialect is given both ways, or is not a string
     */
    public static @Nullable String dialectOf(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final JanitorObject named = args.getByName("dialect");
        if (named == null) {
            return args.getOptionalStringValue(1, null);
        }
        if (args.size() > 1) {
            throw new JanitorArgumentException(process, args.getFunctionName() + ": the dialect is given twice, as the second argument and as dialect");
        }
        if (named == Janitor.NULL) {
            return null;
        }
        if (named instanceof JString string) {
            return string.janitorGetHostValue();
        }
        throw new JanitorArgumentException(process, args.getFunctionName() + ": dialect must be a string, but is: " + named.janitorToString());
    }

    /**
     * Builds the CSV format from the keyword arguments of a reading function.
     *
     * @param process the running script
     * @param args    the arguments of the call
     * @param others  the names of keyword arguments that are no settings of the CSV format
     * @return the format
     * @throws JanitorRuntimeException if a keyword argument is no setting of the CSV format
     */
    public static CsvFormatDefinition formatOf(final JanitorScriptProcess process, final JCallArgs args, final Set<String> others) throws JanitorRuntimeException {
        final CsvFormatDefinition format = new CsvFormatDefinition();
        args.asKwargs(others).applyTo(process, format);
        return format;
    }

    /**
     * Chooses the file type: the dialect if it names one, else CSV in UTF-8 for a file name ending in {@code .csv}, else Excel.
     *
     * @param filename the name of the file, or null if there is none
     * @param dialect  the dialect that the script gave, or null
     * @return the file type
     */
    public static TabularFileType guessDialect(final @Nullable String filename, final @Nullable String dialect) {
        if (dialect != null && !dialect.isBlank()) {
            try {
                return TabularFileType.valueOf(dialect);
            } catch (IllegalArgumentException e) {
                log.warn("ignoring invalid dialect {}: we know {}", dialect, TabularFileType.values());
            }
        }
        if (filename != null && filename.endsWith(".csv")) {
            return TabularFileType.CSV_UTF8;
        }
        return TabularFileType.EXCEL;
    }

    public ExcelModule() {
        super(dispatcher);
    }

    private JanitorObject create(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final ExcelWorkbook workbook = args.bool("streaming")
                ? new ExcelWorkbook(new SXSSFWorkbook(), true)
                : new ExcelWorkbook(new XSSFWorkbook(), false);
        process.registerCleanable(workbook);
        return workbook;
    }

    private JanitorObject open(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final JanitorObject source = args.get("source");
        final byte[] bytes;
        if (source instanceof JBinary binary) {
            bytes = binary.janitorGetHostValue();
        } else if (source instanceof JString path) {
            try {
                // read into memory: POI changes a file that it opens directly, and keeps it locked
                bytes = Files.readAllBytes(Path.of(path.janitorGetHostValue()));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "open: error reading file '" + path.janitorGetHostValue() + "'", e);
            }
        } else {
            throw new JanitorArgumentException(process, "open: expected a file path or binary data, but got: " + source.janitorToString());
        }
        try {
            final ExcelWorkbook workbook = new ExcelWorkbook(WorkbookFactory.create(new ByteArrayInputStream(bytes)), false);
            process.registerCleanable(workbook);
            return workbook;
        } catch (IOException | RuntimeException e) {
            throw new JanitorNativeException(process, "open: this is not an Excel workbook that can be read", e);
        }
    }

}
