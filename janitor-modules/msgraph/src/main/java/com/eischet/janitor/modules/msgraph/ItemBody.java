// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

/** A Graph "itemBody" resource, e.g. the body or unique body of a message: content plus content type ("html" or "text"). */
public class ItemBody extends JanitorComposed<ItemBody> {

    public static final DispatchTable<ItemBody> DISPATCH = new DispatchTable<>(ItemBody::new);

    static {
        DISPATCH.addStringProperty("contentType", ItemBody::getContentType, ItemBody::setContentType);
        DISPATCH.addStringProperty("content", ItemBody::getContent, ItemBody::setContent);
        DISPATCH.addBooleanProperty("html", ItemBody::isHtml);
    }

    private @Nullable String contentType;
    private @Nullable String content;

    public ItemBody() {
        super(DISPATCH);
    }

    public @Nullable String getContentType() {
        return contentType;
    }

    public void setContentType(final @Nullable String contentType) {
        this.contentType = contentType;
    }

    public @Nullable String getContent() {
        return content;
    }

    public void setContent(final @Nullable String content) {
        this.content = content;
    }

    /**
     * @return true if the content is HTML
     */
    public boolean isHtml() {
        return "html".equalsIgnoreCase(contentType);
    }

    /**
     * @return true if the content is plain text
     */
    public boolean isText() {
        return "text".equalsIgnoreCase(contentType);
    }
}
