package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignAccount extends JanitorComposed<DocusignAccount> {

    public static final DispatchTable<DocusignAccount> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("accountId", self -> self.account.getAccountId());
        DISPATCH.addStringProperty("isDefault", self -> self.account.getIsDefault()); // ??? "true" or what is going on here ???
        DISPATCH.addStringProperty("accountName", self -> self.account.getAccountName());
        DISPATCH.addStringProperty("baseUri", self -> self.account.getBaseUri());
        // LATER: organization
    }

    private final OAuth.Account account;

    public DocusignAccount(final OAuth.Account account) {
        super(DISPATCH);
        this.account = account;
    }
}
