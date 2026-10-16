// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.exceptions;

import com.eischet.dbxs.SimplePreparedStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the message of a {@link DatabaseError} for a failed statement, short enough to be read in a log:
 * <pre>
 * conn: ORA-00001: unique constraint (SA.ITEM1_UX) violated
 *   statement: insert into item (117 columns)
 *   values: item_id=60304, item_sc='BI2442264210', modify_date=2026-10-16T10:46:56, ...
 *   null: item_rmk, item_keya, ...
 * </pre>
 * What the database said comes first. Long statements are shortened (an insert to its table and number of columns, an update to its table,
 * number of columns and condition), and the values are labeled with the column they belong to wherever the statement tells it. The full
 * statement and all arguments are still available from the error itself.
 */
final class StatementErrorFormat {

    /** Longer statements, that cannot be shortened by their structure, are cut off. */
    static final int MAX_STATEMENT_LENGTH = 300;
    /** Longer values are cut off. */
    static final int MAX_VALUE_LENGTH = 60;

    private static final Pattern INSERT = Pattern.compile("(?is)^\\s*insert\\s+into\\s+([^\\s(]+)\\s*\\(([^)]*)\\)\\s*values\\s*\\(([^)]*)\\)\\s*;?\\s*$");
    private static final Pattern UPDATE = Pattern.compile("(?is)^\\s*update\\s+(\\S+)\\s+set\\s+(.*?)(?:\\s+where\\s+(.*?))?\\s*;?\\s*$");
    private static final Pattern ASSIGNMENT = Pattern.compile("(?s)^\\s*([\\w.\"]+)\\s*=\\s*\\?\\s*$");
    private static final Pattern COMPARED_COLUMN = Pattern.compile("(?is)([\\w.\"]+)\\s*(?:=|<>|!=|<=|>=|<|>|\\s+like|\\s+in\\s*\\(?)\\s*$");

    private StatementErrorFormat() {
    }

    static @NotNull String format(final @Nullable String connectionName, final @NotNull String sql, final @NotNull List<SimplePreparedStatement.Arg> args, final @Nullable Throwable cause) {
        final StringBuilder out = new StringBuilder();
        if (connectionName != null) {
            out.append(connectionName).append(": ");
        }
        out.append(reason(cause));
        final List<String> names = new ArrayList<>();
        out.append("\n  statement: ").append(shorten(sql, names));
        if (!args.isEmpty()) {
            final List<String> values = new ArrayList<>();
            final List<String> nulls = new ArrayList<>();
            for (final SimplePreparedStatement.Arg arg : args) {
                final String name = arg.getCol() >= 1 && arg.getCol() <= names.size() && names.get(arg.getCol() - 1) != null
                        ? names.get(arg.getCol() - 1)
                        : "#" + arg.getCol();
                if (isNull(arg)) {
                    nulls.add(name);
                } else {
                    values.add(name + "=" + value(arg));
                }
            }
            if (!values.isEmpty()) {
                out.append("\n  values: ").append(String.join(", ", values));
            }
            if (!nulls.isEmpty()) {
                out.append("\n  null: ").append(String.join(", ", nulls));
            }
        }
        return out.toString();
    }

    /** The first line of what went wrong, usually the message of the driver's exception without its link to the documentation. */
    static @NotNull String reason(final @Nullable Throwable cause) {
        if (cause == null) {
            return "error in statement";
        }
        final String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            return cause.getClass().getSimpleName();
        }
        final String trimmed = message.strip();
        final int newline = trimmed.indexOf('\n');
        return newline < 0 ? trimmed : trimmed.substring(0, newline).strip();
    }

    /**
     * Shortens a statement, and collects the names of its parameters as far as it tells them (null where it doesn't).
     */
    static @NotNull String shorten(final @NotNull String sql, final @NotNull List<String> names) {
        final String oneLine = sql.strip().replaceAll("\\s+", " ");
        final Matcher insert = INSERT.matcher(oneLine);
        if (insert.matches()) {
            final String[] columns = insert.group(2).split(",");
            final String[] values = insert.group(3).split(",");
            if (columns.length == values.length && allPlaceholders(values)) {
                for (final String column : columns) {
                    names.add(column.strip());
                }
                return "insert into " + insert.group(1) + " (" + columns.length + " columns)";
            }
        }
        final Matcher update = UPDATE.matcher(oneLine);
        if (update.matches()) {
            final String[] assignments = update.group(2).split(",");
            final List<String> assigned = new ArrayList<>();
            for (final String assignment : assignments) {
                final Matcher column = ASSIGNMENT.matcher(assignment);
                if (!column.matches()) {
                    assigned.clear();
                    break;
                }
                assigned.add(column.group(1));
            }
            if (!assigned.isEmpty()) {
                names.addAll(assigned);
                final String condition = update.group(3);
                if (condition != null) {
                    names.addAll(placeholderNames(condition));
                }
                return "update " + update.group(1) + " set (" + assigned.size() + " columns)" + (condition == null ? "" : " where " + cut(condition, MAX_STATEMENT_LENGTH));
            }
        }
        names.addAll(placeholderNames(oneLine));
        return cut(oneLine, MAX_STATEMENT_LENGTH);
    }

    /** For each {@code ?}: the column it is compared with, e.g. {@code id} for {@code id = ?}, or null if there is none. */
    private static @NotNull List<String> placeholderNames(final @NotNull String sql) {
        final List<String> names = new ArrayList<>();
        boolean quoted = false;
        for (int i = 0; i < sql.length(); i++) {
            final char c = sql.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (c == '?' && !quoted) {
                final Matcher column = COMPARED_COLUMN.matcher(sql.substring(0, i));
                names.add(column.find() ? column.group(1) : null);
            }
        }
        return names;
    }

    private static boolean allPlaceholders(final String[] values) {
        for (final String value : values) {
            if (!"?".equals(value.strip())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNull(final SimplePreparedStatement.Arg arg) {
        return arg.getValue() == null || "null".equals(arg.getType());
    }

    private static @NotNull String value(final SimplePreparedStatement.Arg arg) {
        final Object value = arg.getValue();
        if (value instanceof CharSequence text) {
            return "'" + cut(text.toString(), MAX_VALUE_LENGTH) + "'";
        }
        return cut(String.valueOf(value), MAX_VALUE_LENGTH);
    }

    private static @NotNull String cut(final @NotNull String text, final int max) {
        final String oneLine = text.replace("\r", "").replace("\n", " ");
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max) + "... (" + oneLine.length() + " chars)";
    }
}
