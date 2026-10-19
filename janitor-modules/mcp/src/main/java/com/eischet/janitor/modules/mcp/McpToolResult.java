// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mcp;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import io.modelcontextprotocol.spec.McpSchema;
import org.jetbrains.annotations.NotNull;

/**
 * The result of a tool call, as seen by scripts: {@code isError}, {@code text} (all text parts, joined by newlines),
 * {@code content} (all parts as maps, e.g. {@code {"type": "text", "text": "..."}}) and {@code structured} (the
 * structured content as a Janitor value, or null).
 */
public class McpToolResult extends JanitorComposed<McpToolResult> {

    public static final DispatchTable<McpToolResult> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addBooleanProperty("isError", McpToolResult::isError);
        DISPATCH.addStringProperty("text", McpToolResult::getText);
        DISPATCH.addObjectProperty("content", McpToolResult::getContent);
        DISPATCH.addObjectProperty("structured", McpToolResult::getStructured);
    }

    private final @NotNull McpSchema.CallToolResult result;
    private final @NotNull String text;
    private final @NotNull JList content;
    private final @NotNull JanitorObject structured;

    McpToolResult(final @NotNull McpSchema.CallToolResult result, final @NotNull JList content, final @NotNull JanitorObject structured) {
        super(DISPATCH);
        this.result = result;
        this.text = textOf(result);
        this.content = content;
        this.structured = structured;
    }

    /**
     * @return the result as received from the SDK
     */
    public @NotNull McpSchema.CallToolResult getResult() {
        return result;
    }

    /**
     * @return true if the server flagged the result as an error, e.g. because the tool failed
     */
    public boolean isError() {
        return Boolean.TRUE.equals(result.isError());
    }

    /**
     * @return all text parts of the content, joined by newlines; empty if there are none
     */
    public @NotNull String getText() {
        return text;
    }

    /**
     * @return all parts of the content as maps
     */
    public @NotNull JList getContent() {
        return content;
    }

    /**
     * @return the structured content, or Janitor's null if the tool did not return any
     */
    public @NotNull JanitorObject getStructured() {
        return structured;
    }

    static @NotNull String textOf(final @NotNull McpSchema.CallToolResult result) {
        if (result.content() == null) {
            return "";
        }
        final StringBuilder text = new StringBuilder();
        for (final McpSchema.Content part : result.content()) {
            if (part instanceof McpSchema.TextContent textContent && textContent.text() != null) {
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(textContent.text());
            }
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return "McpToolResult[isError=" + isError() + ", text=" + text + "]";
    }
}
