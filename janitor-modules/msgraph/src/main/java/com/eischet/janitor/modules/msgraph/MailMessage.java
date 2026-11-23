// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Graph "message" resource, reduced to the properties this module uses.
 * Graph only returns the properties named in "$select"; everything else stays null.
 * The received date is kept as the ISO 8601 string Graph sends (in UTC, e.g. "2026-11-23T09:15:00Z"), so that no time zone
 * is decided here.
 */
public class MailMessage extends JanitorComposed<MailMessage> {

    public static final DispatchTable<MailMessage> DISPATCH = new DispatchTable<>(MailMessage::new);

    static {
        DISPATCH.addStringProperty("id", MailMessage::getId, MailMessage::setId)
                .setMetaData(Janitor.MetaData.HELP, "id: The Graph ID of the message; writable.");
        DISPATCH.addStringProperty("subject", MailMessage::getSubject, MailMessage::setSubject)
                .setMetaData(Janitor.MetaData.HELP, "subject: The subject of the message, or null if not selected; writable.");
        DISPATCH.addObjectProperty("sender", MailMessage::getSender, MailMessage::setSender, Recipient::new)
                .setMetaData(Janitor.MetaData.HELP, "sender: The sender as a recipient with an emailAddress, or null if not selected; writable.");
        DISPATCH.addListOfStringsProperty("categories", MailMessage::getCategories, MailMessage::setCategories)
                .setMetaData(Janitor.MetaData.HELP, "categories: List of the category names of the message, or null if not selected; writable.");
        DISPATCH.addNullableBooleanProperty("isRead", MailMessage::getIsRead, MailMessage::setIsRead)
                .setMetaData(Janitor.MetaData.HELP, "isRead: True if the message has been read, or null if not selected; writable.");
        DISPATCH.addStringProperty("internetMessageId", MailMessage::getInternetMessageId, MailMessage::setInternetMessageId)
                .setMetaData(Janitor.MetaData.HELP, "internetMessageId: The Message-ID header of the message, or null if not selected; writable.");
        DISPATCH.addStringProperty("parentFolderId", MailMessage::getParentFolderId, MailMessage::setParentFolderId)
                .setMetaData(Janitor.MetaData.HELP, "parentFolderId: The Graph ID of the folder that contains the message, or null if not selected; writable.");
        DISPATCH.addObjectProperty("uniqueBody", MailMessage::getUniqueBody, MailMessage::setUniqueBody, ItemBody::new)
                .setMetaData(Janitor.MetaData.HELP, "uniqueBody: The part of the body that is unique to this message, without quoted earlier messages, or null if not selected; writable.");
        DISPATCH.addObjectProperty("body", MailMessage::getBody, MailMessage::setBody, ItemBody::new)
                .setMetaData(Janitor.MetaData.HELP, "body: The body of the message, or null if not selected; writable.");
        DISPATCH.addObjectProperty("from", MailMessage::getFrom, MailMessage::setFrom, Recipient::new)
                .setMetaData(Janitor.MetaData.HELP, "from: The author as a recipient with an emailAddress, which may differ from the sender, or null if not selected; writable.");
        DISPATCH.addStringProperty("receivedDateTime", MailMessage::getReceivedDateTime, MailMessage::setReceivedDateTime)
                .setMetaData(Janitor.MetaData.HELP, "receivedDateTime: When the message arrived, as the ISO 8601 string in UTC that Graph sends, or null if not selected; writable.");
        DISPATCH.addNullableBooleanProperty("hasAttachments", MailMessage::getHasAttachments, MailMessage::setHasAttachments)
                .setMetaData(Janitor.MetaData.HELP, "hasAttachments: True if the message has attachments that are not inline, or null if not selected; writable.");
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
    private @Nullable Recipient from;
    private @Nullable String receivedDateTime;
    private @Nullable Boolean hasAttachments;

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

    public @Nullable Recipient getFrom() {
        return from;
    }

    public void setFrom(final @Nullable Recipient from) {
        this.from = from;
    }

    /**
     * @return when the message arrived, as the ISO 8601 string in UTC that Graph sends
     */
    public @Nullable String getReceivedDateTime() {
        return receivedDateTime;
    }

    public void setReceivedDateTime(final @Nullable String receivedDateTime) {
        this.receivedDateTime = receivedDateTime;
    }

    /**
     * @return true if the message has attachments that are not inline (Graph does not count inline images)
     */
    public @Nullable Boolean getHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(final @Nullable Boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    @Override
    public String toString() {
        return "MailMessage[" + id + ", " + subject + "]";
    }
}
