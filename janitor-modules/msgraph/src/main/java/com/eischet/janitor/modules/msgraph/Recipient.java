// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

/** A Graph "recipient" resource, e.g. the sender of a message; it only wraps an email address. */
public class Recipient extends JanitorComposed<Recipient> {

    public static final DispatchTable<Recipient> DISPATCH = new DispatchTable<>(Recipient::new);

    static {
        DISPATCH.addObjectProperty("emailAddress", Recipient::getEmailAddress, Recipient::setEmailAddress, EmailAddress::new)
                .setMetaData(Janitor.MetaData.HELP, "emailAddress: The recipient's email address, with address and name; writable.");
    }

    private @Nullable EmailAddress emailAddress;

    public Recipient() {
        super(DISPATCH);
    }

    public @Nullable EmailAddress getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final @Nullable EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the plain address, or null if there is none
     */
    public @Nullable String getAddress() {
        return emailAddress == null ? null : emailAddress.getAddress();
    }

    @Override
    public String toString() {
        return String.valueOf(emailAddress);
    }
}
