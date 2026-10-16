// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

/** A Graph "mailFolder" resource, reduced to the properties this module uses. */
public class MailFolder extends JanitorComposed<MailFolder> {

    public static final DispatchTable<MailFolder> DISPATCH = new DispatchTable<>(MailFolder::new);

    static {
        DISPATCH.addStringProperty("id", MailFolder::getId, MailFolder::setId);
        DISPATCH.addStringProperty("displayName", MailFolder::getDisplayName, MailFolder::setDisplayName);
        DISPATCH.addStringProperty("parentFolderId", MailFolder::getParentFolderId, MailFolder::setParentFolderId);
        DISPATCH.addNullableIntegerProperty("childFolderCount", MailFolder::getChildFolderCount, MailFolder::setChildFolderCount);
        DISPATCH.addNullableIntegerProperty("totalItemCount", MailFolder::getTotalItemCount, MailFolder::setTotalItemCount);
        DISPATCH.addNullableIntegerProperty("unreadItemCount", MailFolder::getUnreadItemCount, MailFolder::setUnreadItemCount);
        DISPATCH.addNullableBooleanProperty("isHidden", MailFolder::getIsHidden, MailFolder::setIsHidden);
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
