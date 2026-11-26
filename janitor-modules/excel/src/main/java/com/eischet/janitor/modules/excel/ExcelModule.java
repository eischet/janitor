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
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The "excel" module: creates and changes Excel workbooks, on top of Apache POI.
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
 * {@code open} and {@code save} read and write arbitrary paths of the file system, so a host registers this module only
 * for scripts that may do that.
 * </p>
 */
public class ExcelModule extends JanitorComposed<ExcelModule> implements JanitorModule {

    public static final DispatchTable<ExcelModule> dispatcher = new DispatchTable<>(ExcelModule::new, false);

    static {
        dispatcher.setMetaData(Janitor.MetaData.HELP, "Creates and changes Excel workbooks, based on the Apache POI library.");
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
