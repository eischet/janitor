// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

/** A Graph "mailFolder" resource, reduced to the properties this module uses. */
public class MailFolder extends JanitorComposed<MailFolder> {

    public static final DispatchTable<MailFolder> DISPATCH = new DispatchTable<>(MailFolder::new);

    static {
        DISPATCH.addStringProperty("id", MailFolder::getId, MailFolder::setId)
                .setMetaData(Janitor.MetaData.HELP, "id: The Graph ID of the folder; writable.");
        DISPATCH.addStringProperty("displayName", MailFolder::getDisplayName, MailFolder::setDisplayName)
                .setMetaData(Janitor.MetaData.HELP, "displayName: The folder's display name; writable.");
        DISPATCH.addStringProperty("parentFolderId", MailFolder::getParentFolderId, MailFolder::setParentFolderId)
                .setMetaData(Janitor.MetaData.HELP, "parentFolderId: The Graph ID of the parent folder; writable.");
        DISPATCH.addNullableIntegerProperty("childFolderCount", MailFolder::getChildFolderCount, MailFolder::setChildFolderCount)
                .setMetaData(Janitor.MetaData.HELP, "childFolderCount: The number of direct subfolders, or null if not loaded; writable.");
        DISPATCH.addNullableIntegerProperty("totalItemCount", MailFolder::getTotalItemCount, MailFolder::setTotalItemCount)
                .setMetaData(Janitor.MetaData.HELP, "totalItemCount: The number of items in the folder, or null if not loaded; writable.");
        DISPATCH.addNullableIntegerProperty("unreadItemCount", MailFolder::getUnreadItemCount, MailFolder::setUnreadItemCount)
                .setMetaData(Janitor.MetaData.HELP, "unreadItemCount: The number of unread items in the folder, or null if not loaded; writable.");
        DISPATCH.addNullableBooleanProperty("isHidden", MailFolder::getIsHidden, MailFolder::setIsHidden)
                .setMetaData(Janitor.MetaData.HELP, "isHidden: True if the folder is hidden, or null if not loaded; writable.");
    }

    private @Nullable String id;
    private @Nullable String displayName;
    private @Nullable String parentFolderId;
    private @Nullable Integer childFolderCount;
    private @Nullable Integer totalItemCount;
    private @Nullable Integer unreadItemCount;
    private @Nullable Boolean isHidden;

    public MailFolder() {
        super(DISPATCH);
    }

    public @Nullable String getId() {
        return id;
    }

    public void setId(final @Nullable String id) {
        this.id = id;
    }

    public @Nullable String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final @Nullable String displayName) {
        this.displayName = displayName;
    }

    public @Nullable String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(final @Nullable String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public @Nullable Integer getChildFolderCount() {
        return childFolderCount;
    }

    public void setChildFolderCount(final @Nullable Integer childFolderCount) {
        this.childFolderCount = childFolderCount;
    }

    public @Nullable Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(final @Nullable Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public @Nullable Integer getUnreadItemCount() {
        return unreadItemCount;
    }

    public void setUnreadItemCount(final @Nullable Integer unreadItemCount) {
        this.unreadItemCount = unreadItemCount;
    }

    public @Nullable Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(final @Nullable Boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public String toString() {
        return "MailFolder[" + id + ", " + displayName + "]";
    }
}
