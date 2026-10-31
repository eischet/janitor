// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

/** A Graph "emailAddress" resource: an address and an optional display name. */
public class EmailAddress extends JanitorComposed<EmailAddress> {

    public static final DispatchTable<EmailAddress> DISPATCH = new DispatchTable<>(EmailAddress::new);

    static {
        DISPATCH.addStringProperty("address", EmailAddress::getAddress, EmailAddress::setAddress)
                .setMetaData(Janitor.MetaData.HELP, "address: The email address; writable.");
        DISPATCH.addStringProperty("name", EmailAddress::getName, EmailAddress::setName)
                .setMetaData(Janitor.MetaData.HELP, "name: The display name, or null; writable.");
    }

    private @Nullable String address;
    private @Nullable String name;

    public EmailAddress() {
        super(DISPATCH);
    }

    public @Nullable String getAddress() {
        return address;
    }

    public void setAddress(final @Nullable String address) {
        this.address = address;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(final @Nullable String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name == null ? String.valueOf(address) : name + " <" + address + ">";
    }
}
