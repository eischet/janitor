// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign o auth token. */
public class DocusignOAuthToken extends JanitorComposed<DocusignOAuthToken> {

    public static final DispatchTable<DocusignOAuthToken> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addStringProperty("accessToken", self -> self.wrapped.getAccessToken(), (self, value) -> self.wrapped.setAccessToken(value))
                .setMetaData(Janitor.MetaData.HELP, "accessToken: The access token for authenticating API requests; writable.");
        DISPATCHER.addStringProperty("tokenType", self -> self.wrapped.getTokenType(), (self, value) -> self.wrapped.setTokenType(value))
                .setMetaData(Janitor.MetaData.HELP, "tokenType: The type of the token, usually \"Bearer\"; writable.");
        DISPATCHER.addStringProperty("refreshToken", self -> self.wrapped.getRefreshToken(), (self, value) -> self.wrapped.setRefreshToken(value))
                .setMetaData(Janitor.MetaData.HELP, "refreshToken: The token for getting a new access token, if any; writable.");
        DISPATCHER.addNullableLongProperty("expiresIn", self -> self.wrapped.getExpiresIn(), (self, value) -> self.wrapped.setExpiresIn(value != null ? value : 0))
                .setMetaData(Janitor.MetaData.HELP, "expiresIn: The number of seconds the access token is valid; writable, null is stored as 0.");
        DISPATCHER.addStringProperty("scope", self -> self.wrapped.getScope(), (self, value) -> self.wrapped.setScope(value))
                .setMetaData(Janitor.MetaData.HELP, "scope: The scopes granted to the token; writable.");
    }

    private final OAuth.OAuthToken wrapped;

    public DocusignOAuthToken(final OAuth.OAuthToken wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    /**
     * @return the wrapped DocuSign object
     */
    public OAuth.OAuthToken getWrapped() {
        return wrapped;
    }
}
