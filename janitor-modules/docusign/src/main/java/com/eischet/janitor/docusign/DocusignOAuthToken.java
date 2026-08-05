package com.eischet.janitor.docusign;

import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignOAuthToken extends JanitorComposed<DocusignOAuthToken> {

    public static final DispatchTable<DocusignOAuthToken> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addStringProperty("accessToken", self -> self.wrapped.getAccessToken(), (self, value) -> self.wrapped.setAccessToken(value));
        DISPATCHER.addStringProperty("tokenType", self -> self.wrapped.getTokenType(), (self, value) -> self.wrapped.setTokenType(value));
        DISPATCHER.addStringProperty("refreshToken", self -> self.wrapped.getRefreshToken(), (self, value) -> self.wrapped.setRefreshToken(value));
        DISPATCHER.addNullableLongProperty("expiresIn", self -> self.wrapped.getExpiresIn(), (self, value) -> self.wrapped.setExpiresIn(value != null ? value : 0));
        DISPATCHER.addStringProperty("scope", self -> self.wrapped.getScope(), (self, value) -> self.wrapped.setScope(value));
    }

    private final OAuth.OAuthToken wrapped;

    public DocusignOAuthToken(final OAuth.OAuthToken wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    public OAuth.OAuthToken getWrapped() {
        return wrapped;
    }
}
