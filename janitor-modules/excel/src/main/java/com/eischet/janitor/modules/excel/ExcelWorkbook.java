// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import com.eischet.janitor.logging.JanitorLogger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * An Excel workbook that scripts fill and then write as binary data or to a file. It is closed when the script ends.
 */
public class ExcelWorkbook extends JanitorComposed<ExcelWorkbook> implements JanitorCleanupRequired {

    private static final JanitorLogger log = JanitorLogger.getLogger(ExcelWorkbook.class);

    public static final DispatchTable<ExcelWorkbook> DISPATCH = new DispatchTable<>(false);

    private static final ScriptType BINARY = new ScriptType.Builtin("binary");

    private static final Signature SHEET = Signature.method("sheet")
            .param("sheet", ScriptType.ANY)
            .returns(ScriptType.of(ExcelSheet.DISPATCH))
            .help("Returns the sheet with the given name, creating it if the workbook has none of that name; "
                  + "a number returns the existing sheet at that position, counted from 0.");
    private static final Signature TO_BINARY = Signature.method("toBinary")
            .returns(BINARY)
            .help("Returns the workbook as binary data, the contents of an Excel file.");
    private static final Signature SAVE = Signature.method("save")
            .param("path", ScriptType.STRING)
            .help("Writes the workbook to a file, replacing it if it exists.");
    private static final Signature CLOSE = Signature.method("close")
            .help("Closes the workbook and frees its resources; it cannot be used afterwards. Calling it twice is harmless.");

    static {
        DISPATCH.setMetaData(Janitor.MetaData.HELP, "An Excel workbook: a file with one or more sheets.");
        DISPATCH.addMethod(SHEET, ExcelWorkbook::sheet);
        DISPATCH.addMethod(TO_BINARY, (self, process, args) -> process.getBuiltins().binary(self.write(process, "toBinary")));
        DISPATCH.addVoidMethod(SAVE, ExcelWorkbook::save);
        DISPATCH.addVoidMethod(CLOSE, (self, process, args) -> self.close());
        DISPATCH.addListProperty("sheets", self -> {
            final JList names = Janitor.list();
            if (!self.closed) {
                for (int i = 0; i < self.workbook.getNumberOfSheets(); i++) {
                    names.add(Janitor.string(self.workbook.getSheetName(i)));
                }
            }
            return names;
        }).setMetaData(Janitor.MetaData.HELP, "sheets: The names of the sheets, in their order; read-only.");
        DISPATCH.addBooleanProperty("streaming", self -> self.streaming)
                .setMetaData(Janitor.MetaData.HELP, "streaming: True if the workbook was created with streaming; read-only.");
    }

    private final Workbook workbook;
    private final boolean streaming;
    private final Map<Sheet, ExcelSheet> sheets = new IdentityHashMap<>();
    private final Map<String, CellStyle> styles = new HashMap<>();
    private @Nullable Font boldFont;
    private boolean closed;

    /**
     * Wraps a workbook of POI.
     *
     * @param workbook  the workbook
     * @param streaming true if it writes its rows to a temporary file as they are added
     */
    public ExcelWorkbook(final Workbook workbook, final boolean streaming) {
        super(DISPATCH);
        this.workbook = workbook;
        this.streaming = streaming;
    }

    /**
     * @return the workbook of POI, for hosts that need more than scripts get
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    boolean isStreaming() {
        return streaming;
    }

    void requireOpen(final JanitorScriptProcess process, final String method) throws JanitorRuntimeException {
        if (closed) {
            throw new JanitorArgumentException(process, method + ": the workbook is closed"
                                                        + (streaming ? "; a streaming workbook closes when it is written" : ""));
        }
    }

    private JanitorObject sheet(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "sheet");
        final JanitorObject which = args.get("sheet");
        final Sheet sheet;
        if (which instanceof JInt number) {
            final int index = number.getAsInt();
            if (index < 0 || index >= workbook.getNumberOfSheets()) {
                throw new JanitorArgumentException(process, "sheet: there is no sheet number " + index + ", the workbook has "
                                                            + workbook.getNumberOfSheets() + " sheets, counted from 0");
            }
            sheet = workbook.getSheetAt(index);
        } else if (which instanceof JString name) {
            final Sheet existing = workbook.getSheet(name.janitorGetHostValue());
            try {
                sheet = existing != null ? existing : workbook.createSheet(name.janitorGetHostValue());
            } catch (RuntimeException e) {
                throw new JanitorNativeException(process, "sheet: cannot create a sheet named '" + name.janitorGetHostValue() + "'", e);
            }
        } else {
            throw new JanitorArgumentException(process, "sheet: expected the name or the number of a sheet, but got: " + which.janitorToString());
        }
        return sheets.computeIfAbsent(sheet, it -> new ExcelSheet(this, it));
    }

    private void save(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final String path = args.requiredString("path");
        final byte[] bytes = write(process, "save");
        try {
            Files.write(Path.of(path), bytes);
        } catch (IOException e) {
            throw new JanitorNativeException(process, "save: error writing file '" + path + "'", e);
        }
    }

    private byte[] write(final JanitorScriptProcess process, final String method) throws JanitorRuntimeException {
        requireOpen(process, method);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
        } catch (IOException | RuntimeException e) {
            throw new JanitorNativeException(process, method + ": error writing the workbook", e);
        }
        if (streaming) {
            // POI can write a streaming workbook only once
            close();
        }
        return out.toByteArray();
    }

    /**
     * Closes the workbook; a streaming workbook also deletes its temporary files. Safe to call more than once.
     */
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            workbook.close();
        } catch (IOException | RuntimeException e) {
            log.warn("error closing excel workbook", e);
        }
        if (workbook instanceof SXSSFWorkbook streamed) {
            streamed.dispose();
        }
    }

    @Override
    public void janitorCleanup() {
        close();
    }

    /**
     * Returns the cell style for a combination of settings. Styles are shared, because a workbook can only hold a limited
     * number of them.
     *
     * @param bold   bold text
     * @param wrap   wrap long text and line breaks inside the cell
     * @param format a number format in Excel's notation, or null for none
     * @return the style, or null for a cell without any of these settings
     */
    @Nullable CellStyle style(final boolean bold, final boolean wrap, final @Nullable String format) {
        if (!bold && !wrap && format == null) {
            return null;
        }
        return styles.computeIfAbsent((bold ? "b" : "-") + (wrap ? "w" : "-") + (format == null ? "" : format), key -> {
            final CellStyle style = workbook.createCellStyle();
            if (bold) {
                if (boldFont == null) {
                    boldFont = workbook.createFont();
                    boldFont.setBold(true);
                }
                style.setFont(boldFont);
            }
            if (wrap) {
                style.setWrapText(true);
            }
            if (format != null) {
                style.setDataFormat(workbook.createDataFormat().getFormat(format));
            }
            return style;
        });
    }

}
