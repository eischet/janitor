// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.word;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import com.eischet.janitor.logging.JanitorLogger;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.jetbrains.annotations.Nullable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Word document that scripts fill in, extend and read, and then write as binary data or to a file. It is closed when the
 * script ends.
 */
public class WordDocument extends JanitorComposed<WordDocument> implements JanitorCleanupRequired {

    private static final JanitorLogger log = JanitorLogger.getLogger(WordDocument.class);

    public static final DispatchTable<WordDocument> DISPATCH = new DispatchTable<>(false);

    private static final ScriptType BINARY = new ScriptType.Builtin("binary");
    private static final QName XML_SPACE = new QName("http://www.w3.org/XML/1998/namespace", "space");

    private static final Signature REPLACE = Signature.method("replace")
            .param("placeholder", ScriptType.STRING)
            .param("value", ScriptType.ANY)
            .returns(ScriptType.INT)
            .help("Replaces every occurrence of the placeholder text, e.g. \"{{customer}}\", by the value: in the text, in "
                  + "tables, and in headers and footers. The formatting of the placeholder's first character is kept; line breaks "
                  + "in the value become line breaks in the document, null an empty text. Returns the number of replacements.");
    private static final Signature REPLACE_ALL = Signature.method("replaceAll")
            .param("values", ScriptType.MAP)
            .returns(ScriptType.INT)
            .help("Replaces several placeholders at once: the keys of the map are the placeholder texts, its values their "
                  + "replacements, as with replace(). Returns the number of replacements.");
    private static final Signature REPEAT_ROW = Signature.method("repeatRow")
            .param("marker", ScriptType.STRING)
            .param("items", ScriptType.LIST)
            .returns(ScriptType.INT)
            .help("Fills a table from a list: finds the first table row that contains the marker text, usually one of its "
                  + "placeholders, and replaces it by one copy per item. Each item is a map from placeholder text to value, which "
                  + "is replaced in its copy as with replace(). An empty list removes the row. Returns the number of rows written.");
    private static final Signature HEADING = Signature.method("heading")
            .param("text", ScriptType.STRING)
            .optional("level", ScriptType.INT, 1)
            .returns(ScriptType.SELF)
            .help("Adds a heading of level 1 to 9 at the end of the document, in the document's heading style, which is created "
                  + "if the document has none. Returns the document.");
    private static final Signature PARAGRAPH = Signature.method("paragraph")
            .param("text", ScriptType.STRING)
            .optional("bold", ScriptType.BOOL, false)
            .optional("italic", ScriptType.BOOL, false)
            .optional("size", ScriptType.FLOAT, null)
            .optional("align", ScriptType.STRING, null)
            .returns(ScriptType.SELF)
            .help("Adds a paragraph at the end of the document; line breaks in the text become line breaks in the paragraph. "
                  + "size is the font size in points, align one of \"left\", \"center\", \"right\" or \"justify\". Returns the document.");
    private static final Signature TABLE = Signature.method("table")
            .param("rows", ScriptType.listOf(ScriptType.LIST))
            .optional("header", ScriptType.BOOL, true)
            .returns(ScriptType.SELF)
            .help("Adds a table at the end of the document from a list of rows, each a list of cell values; with header, the "
                  + "first row is written in bold. Returns the document.");
    private static final Signature IMAGE = Signature.method("image")
            .param("source", ScriptType.ANY)
            .optional("width", ScriptType.FLOAT, 16.0)
            .returns(ScriptType.SELF)
            .help("Adds an image (PNG, JPEG or GIF) at the end of the document, from a file path or binary data, with the given "
                  + "width in centimetres; the height follows the proportions of the image. Returns the document.");
    private static final Signature PAGE_BREAK = Signature.method("pageBreak")
            .returns(ScriptType.SELF)
            .help("Adds a page break at the end of the document. Returns the document.");
    private static final Signature TO_BINARY = Signature.method("toBinary")
            .returns(BINARY)
            .help("Returns the document as binary data, the contents of a .docx file.");
    private static final Signature SAVE = Signature.method("save")
            .param("path", ScriptType.STRING)
            .help("Writes the document to a file, replacing it if it exists.");
    private static final Signature CLOSE = Signature.method("close")
            .help("Closes the document and frees its resources; it cannot be used afterwards. Calling it twice is harmless.");

    static {
        DISPATCH.setMetaData(Janitor.MetaData.HELP, "A Word document (.docx).");
        DISPATCH.addMethod(REPLACE, (self, process, args) -> {
            self.requireOpen(process, "replace");
            return Janitor.integer(self.replace(process, "replace", self.allParagraphs(), args.requiredString("placeholder"), args.get("value")));
        });
        DISPATCH.addMethod(REPLACE_ALL, (self, process, args) -> {
            self.requireOpen(process, "replaceAll");
            final List<XWPFParagraph> paragraphs = self.allParagraphs();
            int count = 0;
            for (final Map.Entry<JanitorObject, JanitorObject> entry : args.required("values", JMap.class).janitorGetHostValue().entrySet()) {
                count += self.replace(process, "replaceAll", paragraphs, text(entry.getKey()), entry.getValue());
            }
            return Janitor.integer(count);
        });
        DISPATCH.addMethod(REPEAT_ROW, WordDocument::repeatRow);
        DISPATCH.addMethod(HEADING, WordDocument::heading);
        DISPATCH.addMethod(PARAGRAPH, WordDocument::paragraph);
        DISPATCH.addMethod(TABLE, WordDocument::table);
        DISPATCH.addMethod(IMAGE, WordDocument::image);
        DISPATCH.addMethod(PAGE_BREAK, (self, process, args) -> {
            self.requireOpen(process, "pageBreak");
            self.document.createParagraph().createRun().addBreak(BreakType.PAGE);
            return self;
        });
        DISPATCH.addMethod(TO_BINARY, (self, process, args) -> process.getBuiltins().binary(self.write(process, "toBinary")));
        DISPATCH.addVoidMethod(SAVE, (self, process, args) -> {
            final String path = args.requiredString("path");
            final byte[] bytes = self.write(process, "save");
            try {
                Files.write(Path.of(path), bytes);
            } catch (IOException e) {
                throw new JanitorNativeException(process, "save: error writing file '" + path + "'", e);
            }
        });
        DISPATCH.addVoidMethod(CLOSE, (self, process, args) -> self.close());
        DISPATCH.addStringProperty("text", self -> {
            final StringBuilder text = new StringBuilder();
            if (!self.closed) {
                for (final XWPFParagraph paragraph : paragraphsOf(self.document, new ArrayList<>())) {
                    text.append(paragraph.getText()).append('\n');
                }
            }
            return text.toString();
        }).setMetaData(Janitor.MetaData.HELP, "text: The text of the document, one line per paragraph, including the text in tables but not headers and footers; read-only.");
        DISPATCH.addListProperty("paragraphs", self -> {
            final JList paragraphs = Janitor.list();
            if (!self.closed) {
                for (final XWPFParagraph paragraph : self.document.getParagraphs()) {
                    paragraphs.add(Janitor.string(paragraph.getText()));
                }
            }
            return paragraphs;
        }).setMetaData(Janitor.MetaData.HELP, "paragraphs: The texts of the paragraphs outside of tables, in their order; read-only.");
        DISPATCH.addListProperty("tables", self -> {
            final JList tables = Janitor.list();
            if (!self.closed) {
                for (final XWPFTable table : self.document.getTables()) {
                    final JList rows = Janitor.list();
                    for (final XWPFTableRow row : table.getRows()) {
                        final JList cells = Janitor.list();
                        for (final XWPFTableCell cell : row.getTableCells()) {
                            cells.add(Janitor.string(cell.getText()));
                        }
                        rows.add(cells);
                    }
                    tables.add(rows);
                }
            }
            return tables;
        }).setMetaData(Janitor.MetaData.HELP, "tables: The tables of the document, each a list of rows, each row a list of the texts of its cells; read-only.");
    }

    private final XWPFDocument document;
    private boolean closed;

    /**
     * Wraps a document of POI.
     *
     * @param document the document
     */
    public WordDocument(final XWPFDocument document) {
        super(DISPATCH);
        this.document = document;
    }

    /**
     * @return the document of POI, for hosts that need more than scripts get
     */
    public XWPFDocument getDocument() {
        return document;
    }

    private void requireOpen(final JanitorScriptProcess process, final String method) throws JanitorRuntimeException {
        if (closed) {
            throw new JanitorArgumentException(process, method + ": the document is closed");
        }
    }

    /**
     * The text that a value is written as: strings as they are, null as nothing, anything else as Janitor prints it.
     *
     * @param value the value
     * @return its text, with line breaks as {@code \n}
     */
    private static String text(final JanitorObject value) {
        final String text = switch (value) {
            case JNull ignored -> "";
            case JString string -> string.janitorGetHostValue();
            default -> value.janitorToString();
        };
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static List<XWPFParagraph> paragraphsOf(final IBody body, final List<XWPFParagraph> into) {
        into.addAll(body.getParagraphs());
        for (final XWPFTable table : body.getTables()) {
            for (final XWPFTableRow row : table.getRows()) {
                paragraphsOf(row, into);
            }
        }
        return into;
    }

    private static List<XWPFParagraph> paragraphsOf(final XWPFTableRow row, final List<XWPFParagraph> into) {
        for (final XWPFTableCell cell : row.getTableCells()) {
            paragraphsOf(cell, into);
        }
        return into;
    }

    private static void tablesOf(final IBody body, final List<XWPFTable> into) {
        for (final XWPFTable table : body.getTables()) {
            into.add(table);
            for (final XWPFTableRow row : table.getRows()) {
                for (final XWPFTableCell cell : row.getTableCells()) {
                    tablesOf(cell, into);
                }
            }
        }
    }

    /**
     * @return the paragraphs of the text, of all tables, and of the headers and footers
     */
    private List<XWPFParagraph> allParagraphs() {
        final List<XWPFParagraph> paragraphs = paragraphsOf(document, new ArrayList<>());
        document.getHeaderList().forEach(header -> paragraphsOf(header, paragraphs));
        document.getFooterList().forEach(footer -> paragraphsOf(footer, paragraphs));
        return paragraphs;
    }

    private int replace(final JanitorScriptProcess process, final String method, final List<XWPFParagraph> paragraphs,
                        final String placeholder, final JanitorObject value) throws JanitorRuntimeException {
        if (placeholder.isEmpty()) {
            throw new JanitorArgumentException(process, method + ": the placeholder must not be empty");
        }
        final String replacement = text(value);
        int count = 0;
        for (final XWPFParagraph paragraph : paragraphs) {
            count += replaceIn(paragraph, placeholder, replacement);
        }
        return count;
    }

    /**
     * A piece of text inside a run. Word splits the text of a paragraph into runs wherever it likes, e.g. where the spell
     * checker stopped, so a placeholder can be spread over several of these.
     *
     * @param run  the run
     * @param node the text node inside the run
     */
    private record Piece(XWPFRun run, CTText node) {
    }

    private static int replaceIn(final XWPFParagraph paragraph, final String placeholder, final String replacement) {
        int count = 0;
        int from = 0;
        while (true) {
            final List<Piece> pieces = new ArrayList<>();
            final List<Integer> starts = new ArrayList<>();
            final StringBuilder whole = new StringBuilder();
            for (final XWPFRun run : paragraph.getRuns()) {
                for (final CTText node : run.getCTR().getTList()) {
                    pieces.add(new Piece(run, node));
                    starts.add(whole.length());
                    whole.append(node.getStringValue());
                }
            }
            final int found = whole.indexOf(placeholder, from);
            if (found < 0) {
                return count;
            }
            final int end = found + placeholder.length();
            int first = 0;
            int last = 0;
            for (int i = 0; i < pieces.size(); i++) {
                if (starts.get(i) <= found) {
                    first = i;
                }
                if (starts.get(i) < end) {
                    last = i;
                }
            }
            final String before = pieces.get(first).node().getStringValue().substring(0, found - starts.get(first));
            final String after = pieces.get(last).node().getStringValue().substring(end - starts.get(last));
            if (first == last) {
                write(pieces.get(first), before + replacement + after);
            } else {
                write(pieces.get(first), before + replacement);
                for (int i = first + 1; i < last; i++) {
                    write(pieces.get(i), "");
                }
                write(pieces.get(last), after);
            }
            // continue behind the replacement, so that a value containing the placeholder is not replaced again;
            // line breaks are no text in the document
            from = found + replacement.replace("\n", "").length();
            count++;
        }
    }

    /**
     * Sets the text of a piece. Lines after the first are added to the end of its run, each behind a line break.
     */
    private static void write(final Piece piece, final String text) {
        final String[] lines = text.split("\n", -1);
        piece.node().setStringValue(lines[0]);
        try (XmlCursor cursor = piece.node().newCursor()) {
            cursor.setAttributeText(XML_SPACE, "preserve"); // or Word drops leading and trailing blanks
        }
        for (int i = 1; i < lines.length; i++) {
            piece.run().addBreak();
            piece.run().setText(lines[i]);
        }
    }

    private JanitorObject repeatRow(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "repeatRow");
        final String marker = args.requiredString("marker");
        final JList items = args.required("items", JList.class);
        final List<XWPFTable> tables = new ArrayList<>();
        tablesOf(document, tables);
        for (final XWPFTable table : tables) {
            final List<XWPFTableRow> rows = table.getRows();
            for (int index = 0; index < rows.size(); index++) {
                final XWPFTableRow template = rows.get(index);
                final StringBuilder rowText = new StringBuilder();
                paragraphsOf(template, new ArrayList<>()).forEach(paragraph -> rowText.append(paragraph.getText()).append('\n'));
                if (rowText.indexOf(marker) < 0) {
                    continue;
                }
                int written = 0;
                for (final JanitorObject item : items) {
                    if (!(item instanceof JMap values)) {
                        throw new JanitorArgumentException(process, "repeatRow: every item must be a map from placeholder to value, but got: " + item.janitorToString());
                    }
                    // fill the copy before it joins the table: the table keeps its own copy of the row from then on
                    final XWPFTableRow copy = new XWPFTableRow((CTRow) template.getCtRow().copy(), table);
                    final List<XWPFParagraph> paragraphs = paragraphsOf(copy, new ArrayList<>());
                    for (final Map.Entry<JanitorObject, JanitorObject> entry : values.janitorGetHostValue().entrySet()) {
                        replace(process, "repeatRow", paragraphs, text(entry.getKey()), entry.getValue());
                    }
                    table.addRow(copy, index + written);
                    written++;
                }
                table.removeRow(index + written);
                return Janitor.integer(written);
            }
        }
        throw new JanitorArgumentException(process, "repeatRow: no table row contains '" + marker + "'");
    }

    private JanitorObject heading(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "heading");
        final long level = args.integer("level");
        if (level < 1 || level > 9) {
            throw new JanitorArgumentException(process, "heading: level must be between 1 and 9, but is " + level);
        }
        final XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle(headingStyle((int) level));
        writeText(paragraph.createRun(), text(args.get("text")));
        return this;
    }

    /**
     * Finds the heading style of a level by its built-in name, which is the same in every language of Word while the id of
     * the style is not, and creates one if the document has none, as new documents do.
     *
     * @param level the level, from 1
     * @return the id of the style
     */
    private String headingStyle(final int level) {
        final XWPFStyles styles = document.getStyles() != null ? document.getStyles() : document.createStyles();
        final XWPFStyle existing = styles.getStyleWithName("heading " + level);
        if (existing != null) {
            return existing.getStyleId();
        }
        final CTStyle style = CTStyle.Factory.newInstance();
        style.setStyleId("Heading" + level);
        style.addNewName().setVal("heading " + level);
        style.setType(STStyleType.PARAGRAPH);
        style.addNewQFormat();
        style.addNewPPr().addNewOutlineLvl().setVal(BigInteger.valueOf(level - 1));
        style.getPPr().addNewKeepNext();
        final CTRPr font = style.addNewRPr();
        font.addNewB();
        final int points = switch (level) {
            case 1 -> 16;
            case 2 -> 14;
            case 3 -> 13;
            default -> 12;
        };
        font.addNewSz().setVal(BigInteger.valueOf(points * 2L));
        font.addNewSzCs().setVal(BigInteger.valueOf(points * 2L));
        styles.addStyle(new XWPFStyle(style, styles));
        return style.getStyleId();
    }

    private static void writeText(final XWPFRun run, final String text) {
        final String[] lines = text.split("\n", -1);
        run.setText(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            run.addBreak();
            run.setText(lines[i]);
        }
    }

    private JanitorObject paragraph(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "paragraph");
        final ParagraphAlignment alignment = alignment(process, args.string("align"));
        final XWPFParagraph paragraph = document.createParagraph();
        if (alignment != null) {
            paragraph.setAlignment(alignment);
        }
        final XWPFRun run = paragraph.createRun();
        run.setBold(args.bool("bold"));
        run.setItalic(args.bool("italic"));
        if (args.get("size") != JNull.NULL) {
            if (!(args.get("size") instanceof com.eischet.janitor.api.types.builtin.JNumber size) || size.toDouble() <= 0) {
                throw new JanitorArgumentException(process, "paragraph: size must be a font size in points, but is: " + args.get("size").janitorToString());
            }
            run.setFontSize(size.toDouble());
        }
        writeText(run, text(args.get("text")));
        return this;
    }

    private static @Nullable ParagraphAlignment alignment(final JanitorScriptProcess process, final @Nullable String name) throws JanitorArgumentException {
        if (name == null) {
            return null;
        }
        return switch (name) {
            case "left" -> ParagraphAlignment.LEFT;
            case "center" -> ParagraphAlignment.CENTER;
            case "right" -> ParagraphAlignment.RIGHT;
            case "justify" -> ParagraphAlignment.BOTH;
            default -> throw new JanitorArgumentException(process, "paragraph: align must be \"left\", \"center\", \"right\" or \"justify\", but is: " + name);
        };
    }

    private JanitorObject table(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "table");
        final JList rows = args.required("rows", JList.class);
        final boolean header = args.bool("header");
        int columns = 0;
        for (final JanitorObject row : rows) {
            if (!(row instanceof JList cells)) {
                throw new JanitorArgumentException(process, "table: every row must be a list of cell values, but got: " + row.janitorToString());
            }
            columns = Math.max(columns, cells.size());
        }
        if (rows.size() == 0 || columns == 0) {
            throw new JanitorArgumentException(process, "table: there are no rows with cells");
        }
        final XWPFTable table = document.createTable(rows.size(), columns);
        table.setWidth("100%");
        for (int r = 0; r < rows.size(); r++) {
            final JList cells = (JList) rows.get(r);
            for (int c = 0; c < cells.size(); c++) {
                final XWPFParagraph paragraph = table.getRow(r).getCell(c).getParagraphs().get(0);
                final XWPFRun run = paragraph.createRun();
                run.setBold(header && r == 0);
                writeText(run, text(cells.get(c)));
            }
        }
        return this;
    }

    private JanitorObject image(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        requireOpen(process, "image");
        final byte[] bytes = WordModule.bytesOf(process, "image", args.get("source"));
        if (!(args.get("width") instanceof com.eischet.janitor.api.types.builtin.JNumber width) || width.toDouble() <= 0) {
            throw new JanitorArgumentException(process, "image: width must be a width in centimetres, but is: " + args.get("width").janitorToString());
        }
        final PictureType type = pictureType(bytes);
        if (type == null) {
            throw new JanitorArgumentException(process, "image: only PNG, JPEG and GIF images can be added");
        }
        try {
            final BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new JanitorArgumentException(process, "image: the image cannot be read");
            }
            final double widthPoints = width.toDouble() / 2.54 * 72;
            final double heightPoints = widthPoints * image.getHeight() / image.getWidth();
            document.createParagraph().createRun().addPicture(new ByteArrayInputStream(bytes), type, "image",
                    Units.toEMU(widthPoints), Units.toEMU(heightPoints));
        } catch (JanitorRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JanitorNativeException(process, "image: error adding the image", e);
        }
        return this;
    }

    private static @Nullable PictureType pictureType(final byte[] bytes) {
        if (bytes.length > 4 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return PictureType.PNG;
        }
        if (bytes.length > 2 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8) {
            return PictureType.JPEG;
        }
        if (bytes.length > 4 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
            return PictureType.GIF;
        }
        return null;
    }

    private byte[] write(final JanitorScriptProcess process, final String method) throws JanitorRuntimeException {
        requireOpen(process, method);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            document.write(out);
        } catch (IOException | RuntimeException e) {
            throw new JanitorNativeException(process, method + ": error writing the document", e);
        }
        return out.toByteArray();
    }

    /**
     * Closes the document. Safe to call more than once.
     */
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            document.close();
        } catch (IOException | RuntimeException e) {
            log.warn("error closing word document", e);
        }
    }

    @Override
    public void janitorCleanup() {
        close();
    }

}
