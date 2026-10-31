// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign user information, which includes the accounts of the user. */
public class DocusignUserInfo extends JanitorComposed<DocusignUserInfo> {

    public static final DispatchTable<DocusignUserInfo> DISPATCH =  new DispatchTable();

    static {
        DISPATCH.addStringProperty("sub", self -> self.userInfo.getSub())
                .setMetaData(Janitor.MetaData.HELP, "sub: The user's ID (the OAuth subject); read-only.");
        DISPATCH.addStringProperty("email", self -> self.userInfo.getEmail())
                .setMetaData(Janitor.MetaData.HELP, "email: The user's email address; read-only.");
        DISPATCH.addStringProperty("name", self -> self.userInfo.getName())
                .setMetaData(Janitor.MetaData.HELP, "name: The user's full name; read-only.");
        DISPATCH.addStringProperty("givenName", self -> self.userInfo.getGivenName())
                .setMetaData(Janitor.MetaData.HELP, "givenName: The user's given name; read-only.");
        DISPATCH.addStringProperty("familyName", self -> self.userInfo.getFamilyName())
                .setMetaData(Janitor.MetaData.HELP, "familyName: The user's family name; read-only.");
        DISPATCH.addListProperty("accounts", self -> Janitor.list(self.userInfo.getAccounts().stream().map(DocusignAccount::new)))
                .setMetaData(Janitor.MetaData.HELP, "accounts: List of the DocuSign accounts the user can access; read-only.");
    }

    protected final OAuth.UserInfo userInfo;

    public DocusignUserInfo(final OAuth.UserInfo userInfo) {
        super(DISPATCH);
        this.userInfo = userInfo;
    }
}
