// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Changes to a message, sent as PATCH. Only the properties that are set are sent; an empty category list removes all
 * categories. Scripts pass them as a map, e.g. {@code client.update(user, id, {subject: "[1234] Re: ...", categories: ["Done"]})}.
 */
public class MessageUpdate extends JanitorComposed<MessageUpdate> {

    public static final DispatchTable<MessageUpdate> DISPATCH = new DispatchTable<>(MessageUpdate::new);

    static {
        DISPATCH.addStringProperty("subject", MessageUpdate::getSubject, MessageUpdate::setSubject);
        DISPATCH.addListOfStringsProperty("categories", MessageUpdate::getCategories, MessageUpdate::setCategories);
        DISPATCH.addNullableBooleanProperty("isRead", MessageUpdate::getIsRead, MessageUpdate::setIsRead);
    }

    private @Nullable String subject;
    private @Nullable List<String> categories;
    private @Nullable Boolean isRead;

    public MessageUpdate() {
        super(DISPATCH);
    }

    public @Nullable String getSubject() {
        return subject;
    }

    public MessageUpdate setSubject(final @Nullable String subject) {
        this.subject = subject;
        return this;
    }

    public @Nullable List<String> getCategories() {
        return categories;
    }

    public MessageUpdate setCategories(final @Nullable List<String> categories) {
        this.categories = categories == null ? null : List.copyOf(categories);
        return this;
    }

    public @Nullable Boolean getIsRead() {
        return isRead;
    }

    public MessageUpdate setIsRead(final @Nullable Boolean isRead) {
        this.isRead = isRead;
        return this;
    }

    /**
     * @return true if nothing would be changed
     */
    public boolean isEmpty() {
        return subject == null && categories == null && isRead == null;
    }

    /**
     * Writes the PATCH body. This is done by hand instead of via the dispatch table, because an empty category list
     * must be sent (it clears the categories), while the generic JSON writer leaves empty values out.
     * @param stream target
     * @throws JsonException on errors
     */
    void writePatchBody(final JsonOutputStream stream) throws JsonException {
        stream.beginObject();
        if (subject != null) {
            stream.key("subject").value(subject);
        }
        if (categories != null) {
            stream.key("categories").beginArray();
            for (final String category : categories) {
                stream.value(category);
            }
            stream.endArray();
        }
        if (isRead != null) {
            stream.key("isRead").value(isRead.booleanValue());
        }
        stream.endObject();
    }
}
