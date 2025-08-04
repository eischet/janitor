package com.eischet.janitor.api.util;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The TypeScriptEmitter takes a set of dispatch tables and converts them a TypeScript type definitions,
 * The Janitor objects need to have appropriate Type Hints available and, for non-primitive fields, should supply a REF entry.
 * This is helpful for people who want to use Janitor and/or Java on the server side, and TS/JS on the client.
 */
public class TypeScriptEmitter {

    private String header;
    private String footer;
    private final List<DispatchTable<?>> dispatchTables = new ArrayList<>();

    public TypeScriptEmitter() {
    }

    public String getHeader() {
        return header;
    }

    public TypeScriptEmitter setHeader(final String header) {
        this.header = header;
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public TypeScriptEmitter setFooter(final String footer) {
        this.footer = footer;
        return this;
    }

    public TypeScriptEmitter addDispatchTable(DispatchTable<?> dispatchTable) {
        dispatchTables.add(dispatchTable);
        return this;
    }

    public String emit() {
        final StringBuilder out = new StringBuilder();
        if (header != null) {
            out.append(header).append(NEWLINE);
        }
        for (final DispatchTable<?> dispatchTable : dispatchTables) {
            out.append(buildTypeScriptDefinition(dispatchTable)).append(NEWLINE);
        }
        if (footer != null) {
            out.append(footer).append(NEWLINE);
        }
        return out.toString();
    }



    private static final String NEWLINE = "\n";

    public static <T extends JanitorObject> String buildTypeScriptDefinition(DispatchTable<T> dispatch) {
        final @Nullable String className = Objects.requireNonNull(dispatch.getMetaData(Janitor.MetaData.CLASS), "CLASS metadata entry required, but missing");
        final List<String> attributes = dispatch.streamAttributeNames().toList();

        final StringBuilder out = new StringBuilder();
        out.append("interface ").append(className).append(" {").append(NEWLINE);
        for (final String attribute : attributes) {
            final @Nullable Janitor.MetaData.TypeHint typeHint = dispatch.getMetaData(attribute, Janitor.MetaData.TYPE_HINT);
            final @Nullable String typeRef = dispatch.getMetaData(attribute, Janitor.MetaData.REF);
            final boolean required = dispatch.getMetaData(attribute, Janitor.MetaData.REQUIRED) == Boolean.TRUE;
            if (typeHint == null) {
                if (typeRef != null) {
                    out.append("    ").append(attribute);
                    if (!required) {
                        out.append("?");
                    }
                    out.append(": ").append(typeRef).append(";").append(NEWLINE);
                } else {
                    out.append("    // unknown type: ").append(attribute).append(NEWLINE);
                }
            } else if (typeHint == Janitor.MetaData.TypeHint.METHOD) {
                out.append("    // not serializable: method ").append(attribute).append(NEWLINE);
            } else if (typeHint == Janitor.MetaData.TypeHint.LIST) {
                if (typeRef != null) {
                    out.append("    ").append(attribute);
                    if (!required) {
                        out.append("?");
                    }
                    out.append(": ").append(typeRef).append("[];").append(NEWLINE);
                } else {
                    out.append("    ").append(attribute);
                    if (!required) {
                        out.append("?");
                    }
                    out.append(": any[];").append(NEWLINE);
                }
            } else {
                String jsType = switch (typeHint) {
                    case NUMBER, INTEGER, FLOAT -> "number";
                    case STRING -> "string";
                    case BOOLEAN -> "boolean";
                    case DATE, DATETIME -> "Date";
                    default -> "any";
                    // case METHOD, LIST -> "any";
                };
                out.append("    ").append(attribute);
                if (!required) {
                    out.append("?");
                }
                out.append(": ").append(jsType).append(";").append(NEWLINE);
            }
        }

        out.append("}").append(NEWLINE);

        return out.toString();
    }


}
