package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignUserInfo extends JanitorComposed<DocusignUserInfo> {

    public static final DispatchTable<DocusignUserInfo> DISPATCH =  new DispatchTable();

    static {
        DISPATCH.addStringProperty("sub", self -> self.userInfo.getSub());
        DISPATCH.addStringProperty("email", self -> self.userInfo.getEmail());
        DISPATCH.addStringProperty("name", self -> self.userInfo.getName());
        DISPATCH.addStringProperty("givenName", self -> self.userInfo.getGivenName());
        DISPATCH.addStringProperty("familyName", self -> self.userInfo.getFamilyName());
        DISPATCH.addListProperty("accounts", self -> Janitor.list(self.userInfo.getAccounts().stream().map(DocusignAccount::new)));
    }

    protected final OAuth.UserInfo userInfo;

    public DocusignUserInfo(final OAuth.UserInfo userInfo) {
        super(DISPATCH);
        this.userInfo = userInfo;
    }
}
