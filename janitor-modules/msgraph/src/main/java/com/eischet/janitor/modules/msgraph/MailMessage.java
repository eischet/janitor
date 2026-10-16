// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Graph "message" resource, reduced to the properties this module uses.
 * Graph only returns the properties named in "$select"; everything else stays null.
 * Date/time properties are deliberately left out for now, because Graph sends them in UTC and the time zone handling
 * has not been decided yet.
 */
public class MailMessage extends JanitorComposed<MailMessage> {

    public static final DispatchTable<MailMessage> DISPATCH = new DispatchTable<>(MailMessage::new);

    static {
        DISPATCH.addStringProperty("id", MailMessage::getId, MailMessage::setId);
        DISPATCH.addStringProperty("subject", MailMessage::getSubject, MailMessage::setSubject);
        DISPATCH.addObjectProperty("sender", MailMessage::getSender, MailMessage::setSender, Recipient::new);
        DISPATCH.addListOfStringsProperty("categories", MailMessage::getCategories, MailMessage::setCategories);
        DISPATCH.addNullableBooleanProperty("isRead", MailMessage::getIsRead, MailMessage::setIsRead);
        DISPATCH.addStringProperty("internetMessageId", MailMessage::getInternetMessageId, MailMessage::setInternetMessageId);
        DISPATCH.addStringProperty("parentFolderId", MailMessage::getParentFolderId, MailMessage::setParentFolderId);
        DISPATCH.addObjectProperty("uniqueBody", MailMessage::getUniqueBody, MailMessage::setUniqueBody, ItemBody::new);
        DISPATCH.addObjectProperty("body", MailMessage::getBody, MailMessage::setBody, ItemBody::new);
    }

    private @Nullable String id;
    private @Nullable String subject;
    private @Nullable Recipient sender;
    private @Nullable List<String> categories;
    private @Nullable Boolean isRead;
    private @Nullable String internetMessageId;
    private @Nullable String parentFolderId;
    private @Nullable ItemBody uniqueBody;
    private @Nullable ItemBody body;

    public MailMessage() {
        super(DISPATCH);
    }

    public @Nullable String getId() {
        return id;
    }

    public void setId(final @Nullable String id) {
        this.id = id;
    }

    public @Nullable String getSubject() {
        return subject;
    }

    public void setSubject(final @Nullable String subject) {
        this.subject = subject;
    }

    public @Nullable Recipient getSender() {
        return sender;
    }

    public void setSender(final @Nullable Recipient sender) {
        this.sender = sender;
    }

    public @Nullable List<String> getCategories() {
        return categories;
    }

    public void setCategories(final @Nullable List<String> categories) {
        this.categories = categories;
    }

    public @Nullable Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(final @Nullable Boolean isRead) {
        this.isRead = isRead;
    }

    public @Nullable String getInternetMessageId() {
        return internetMessageId;
    }

    public void setInternetMessageId(final @Nullable String internetMessageId) {
        this.internetMessageId = internetMessageId;
    }

    public @Nullable String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(final @Nullable String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public @Nullable ItemBody getUniqueBody() {
        return uniqueBody;
    }

    public void setUniqueBody(final @Nullable ItemBody uniqueBody) {
        this.uniqueBody = uniqueBody;
    }

    public @Nullable ItemBody getBody() {
        return body;
    }

    public void setBody(final @Nullable ItemBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MailMessage[" + id + ", " + subject + "]";
    }
}
