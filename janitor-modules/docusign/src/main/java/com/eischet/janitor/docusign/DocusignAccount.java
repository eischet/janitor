// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for a DocuSign account, as listed in the user information. */
public class DocusignAccount extends JanitorComposed<DocusignAccount> {

    public static final DispatchTable<DocusignAccount> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("accountId", self -> self.account.getAccountId())
                .setMetaData(Janitor.MetaData.HELP, "accountId: The ID of the DocuSign account; read-only.");
        DISPATCH.addStringProperty("isDefault", self -> self.account.getIsDefault()); // ??? "true" or what is going on here ???
        DISPATCH.addStringProperty("accountName", self -> self.account.getAccountName())
                .setMetaData(Janitor.MetaData.HELP, "accountName: The name of the DocuSign account; read-only.");
        DISPATCH.addStringProperty("baseUri", self -> self.account.getBaseUri())
                .setMetaData(Janitor.MetaData.HELP, "baseUri: The base URI of the API server that hosts the account; read-only.");
        // LATER: organization
    }

    private final OAuth.Account account;

    public DocusignAccount(final OAuth.Account account) {
        super(DISPATCH);
        this.account = account;
    }
}
