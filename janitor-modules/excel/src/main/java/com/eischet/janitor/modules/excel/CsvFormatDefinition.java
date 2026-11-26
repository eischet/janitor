// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.apache.commons.csv.QuoteMode;
import org.jetbrains.annotations.Nullable;

public class CsvFormatDefinition extends JanitorComposed<CsvFormatDefinition> {

    public static final DispatchTable<CsvFormatDefinition> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.setMetaData(Janitor.MetaData.CLASS, "CsvFormatDefinition");
        DISPATCH.addNullableBooleanProperty("allowMissingColumnNames", self -> fail(), (self, state) -> self.builder.setAllowMissingColumnNames(state))
                .setMetaData(Janitor.MetaData.HELP, "allowMissingColumnNames: CSV option: allows missing column names in the header; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("autoFlush", self -> fail(), (self, state) -> self.builder.setAutoFlush(state))
                .setMetaData(Janitor.MetaData.HELP, "autoFlush: CSV option: flushes automatically when writing; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("allowMissingColumnNames", self -> fail(), (self, state) -> self.builder.setAllowMissingColumnNames(state))
                .setMetaData(Janitor.MetaData.HELP, "allowMissingColumnNames: CSV option: allows missing column names in the header; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("ignoreEmptyLines", self -> fail(), (self, state) -> self.builder.setIgnoreEmptyLines(state))
                .setMetaData(Janitor.MetaData.HELP, "ignoreEmptyLines: CSV option: skips empty lines (default true); write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("ignoreHeaderCase", self -> fail(), (self, state) -> self.builder.setIgnoreHeaderCase(state))
                .setMetaData(Janitor.MetaData.HELP, "ignoreHeaderCase: CSV option: ignores the case of the header names; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("ignoreSurroundingSpaces", self -> fail(), (self, state) -> self.builder.setIgnoreSurroundingSpaces(state))
                .setMetaData(Janitor.MetaData.HELP, "ignoreSurroundingSpaces: CSV option: ignores spaces around values; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("skipHeaderRecord", self -> fail(), (self, state) -> self.builder.setSkipHeaderRecord(state))
                .setMetaData(Janitor.MetaData.HELP, "skipHeaderRecord: CSV option: skips the header record; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("lenientEof", self -> fail(), (self, state) -> self.builder.setLenientEof(state))
                .setMetaData(Janitor.MetaData.HELP, "lenientEof: CSV option: tolerates an end of file inside a quoted value; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("trailingData", self -> fail(), (self, state) -> self.builder.setTrailingData(state))
                .setMetaData(Janitor.MetaData.HELP, "trailingData: CSV option: tolerates data after the closing quote of a value; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("trailingDelimiter", self -> fail(), (self, state) -> self.builder.setTrailingDelimiter(state))
                .setMetaData(Janitor.MetaData.HELP, "trailingDelimiter: CSV option: allows a delimiter at the end of each record; write-only, reads as null.");
        DISPATCH.addNullableBooleanProperty("trim", self -> fail(), (self, state) -> self.builder.setTrim(state))
                .setMetaData(Janitor.MetaData.HELP, "trim: CSV option: trims leading and trailing blanks of values; write-only, reads as null.");

        DISPATCH.addStringProperty("delimiter", self -> fail(), (self, state) -> self.builder.setDelimiter(state))
                .setMetaData(Janitor.MetaData.HELP, "delimiter: CSV option: the delimiter between values (default semicolon); write-only, reads as null.");
        DISPATCH.addStringProperty("nullString", self -> fail(), (self, state) -> self.builder.setNullString(state))
                .setMetaData(Janitor.MetaData.HELP, "nullString: CSV option: the string that stands for null; write-only, reads as null.");
        DISPATCH.addStringProperty("recordSeparator", self -> fail(), (self, state) -> self.builder.setRecordSeparator(state))
                .setMetaData(Janitor.MetaData.HELP, "recordSeparator: CSV option: the separator between records; write-only, reads as null.");
        DISPATCH.addStringProperty("commentMarker", self -> fail(), (self, state) -> self.builder.setCommentMarker(requireCharacter(state)))
                .setMetaData(Janitor.MetaData.HELP, "commentMarker: CSV option: the character that starts a comment line, exactly one character; write-only, reads as null.");
        DISPATCH.addStringProperty("escapeCharacter", self -> fail(), (self, state) -> self.builder.setEscape(requireCharacter(state)))
                .setMetaData(Janitor.MetaData.HELP, "escapeCharacter: CSV option: the escape character, exactly one character; write-only, reads as null.");
        DISPATCH.addStringProperty("quoteCharacter", self -> fail(), (self, state) -> self.builder.setQuote(requireCharacter(state)))
                .setMetaData(Janitor.MetaData.HELP, "quoteCharacter: CSV option: the quote character, exactly one character (default double quote); write-only, reads as null.");

        DISPATCH.addNullableLongProperty("maxRows", self -> 0L, (self, state) -> self.builder.setMaxRows(state))
                .setMetaData(Janitor.MetaData.HELP, "maxRows: CSV option: the maximum number of rows to read; write-only, reads as 0.");
        /* not mapped yet:
        private DuplicateHeaderMode duplicateHeaderMode;
        private String[] headerComments;
        private String[] headers;
        private QuoteMode quoteMode;
         */

    }

    private static Character requireCharacter(final @Nullable String state) throws IllegalArgumentException {
        if (state == null) {
            return null;
        }
        if (state.length() != 1) {
            throw new IllegalArgumentException("character must be exactly one character long");
        }
        return state.charAt(0);
    }

    private final CSVFormat.Builder builder;


    public CsvFormatDefinition() {
        super(DISPATCH);
        builder = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL)
                .setIgnoreEmptyLines(true)
                .setQuote('"')
                .setQuoteMode(QuoteMode.MINIMAL);
    }

    private static <T> T fail() throws JanitorError {
        return null;
        // throw new JanitorError("this property is not readable, thanks to the Apache CSV developers who wrongly thought that was a good idea");
    }

    public CSVFormat getFormat() {
        return builder.get();
    }


}
