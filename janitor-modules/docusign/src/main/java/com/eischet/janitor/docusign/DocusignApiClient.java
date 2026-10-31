// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/** Wrapper for the DocuSign API client: it handles authentication, e.g. via JWT, and provides access to the APIs. */
public class DocusignApiClient extends JanitorComposed<DocusignApiClient> {

    protected static final Logger log = LoggerFactory.getLogger(DocusignApiClient.class);

    public static DispatchTable<DocusignApiClient> DISPATCHER = new DispatchTable<DocusignApiClient>();

    static {
        DISPATCHER.addStringProperty("basePath", self -> self.wrapped.getBasePath(), (self, value) -> self.wrapped.setBasePath(value))
                .setMetaData(Janitor.MetaData.HELP, "basePath: The base path of the DocuSign REST API that the client calls; writable.");
        DISPATCHER.addStringProperty("oauthBasePath", self -> {
            log.debug("oAuthBasePath is write-only in the ApiClient."); // for whatever reason
            return "";
        }, (self, value) -> self.wrapped.setOAuthBasePath(value))
                .setMetaData(Janitor.MetaData.HELP, "oauthBasePath: The base path of the DocuSign authentication server; write-only, reading it returns an empty string.");

        DISPATCHER.addMethod("getUserInfo", (self, process, args) -> {
            try {
                final DocusignOAuthToken janitorAccessToken = args.getRequired(0, DocusignOAuthToken.class);
                OAuth.UserInfo userInfo = self.wrapped.getUserInfo(janitorAccessToken.getWrapped().getAccessToken());
                return new DocusignUserInfo(userInfo);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "error getting user information from token", e);
            }
            // String accountId = userInfo.getAccounts().get(0).getAccountId();
        })
                .setMetaData(Janitor.MetaData.HELP, "getUserInfo(token): Returns the information about the user that the OAuth token belongs to, including their accounts.");

        DISPATCHER.addMethod("requestJWTUserToken", (self, process, arguments) -> {
            arguments.require(4, 5);
            final String clientId = arguments.getRequiredStringValue(0);
            final String userId = arguments.getRequiredStringValue(1);
            final JList scopes = arguments.getRequired(2, JList.class);
            final String privateKey = arguments.getRequiredStringValue(3);
            final JNumber expiresIn = arguments.size() > 4 ? arguments.getRequired(4, JNumber.class) : Janitor.integer(3600);
            try {
                OAuth.OAuthToken oAuthToken = self.wrapped.requestJWTUserToken(
                        clientId,
                        userId,
                        scopes.stream().map(Object::toString).collect(Collectors.toList()),
                        privateKey.getBytes(StandardCharsets.UTF_8),
                        expiresIn.toLong());
                return new DocusignOAuthToken(oAuthToken);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "error getting oauth token", e);
            }
        })
                .setMetaData(Janitor.MetaData.HELP, "requestJWTUserToken(clientId, userId, scopes, privateKey, expiresIn): Requests an OAuth token for the user via JWT grant, with the scopes as a list and the private key as a string, and returns it; expiresIn is optional and defaults to 3600 seconds.");
        DISPATCHER.addVoidMethod("setAccessToken", (self, process, arguments) -> {
            final JanitorObject arg = arguments.get(0);
            if (arg instanceof JString string) {
                self.wrapped.addDefaultHeader("Authorization", "Bearer " + string);
            } else if (arg instanceof DocusignOAuthToken oAuthToken) {
                self.wrapped.addDefaultHeader("Authorization", "Bearer " + oAuthToken.getWrapped().getAccessToken());
            }
        })
                .setMetaData(Janitor.MetaData.HELP, "setAccessToken(token): Makes the client authenticate its requests with the token, given as an OAuth token or as an access token string; other values are ignored.");
        // Convenience method: simply log in instead of fetch+setAccessToken.
        DISPATCHER.addMethod("login", (self, process, arguments) -> {
            arguments.require(4, 5);
            final String clientId = arguments.getRequiredStringValue(0);
            final String userId = arguments.getRequiredStringValue(1);
            final JList scopes = arguments.getRequired(2, JList.class);
            final String privateKey = arguments.getRequiredStringValue(3);
            final JNumber expiresIn = arguments.size() > 4 ? arguments.getRequired(4, JNumber.class) : Janitor.integer(3600);
            try {
                OAuth.OAuthToken oAuthToken = self.wrapped.requestJWTUserToken(
                        clientId,
                        userId,
                        scopes.stream().map(Object::toString).collect(Collectors.toList()),
                        privateKey.getBytes(StandardCharsets.UTF_8),
                        expiresIn.toLong());
                self.wrapped.addDefaultHeader("Authorization", "Bearer " + oAuthToken.getAccessToken());
                return new DocusignOAuthToken(oAuthToken);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "error getting oauth token", e);
            }
        })
                .setMetaData(Janitor.MetaData.HELP, "login(clientId, userId, scopes, privateKey, expiresIn): Like requestJWTUserToken, but also makes the client use the new token; returns the OAuth token, expiresIn is optional and defaults to 3600 seconds.");
        DISPATCHER.addMethod("envelopes", (self, process, arguments) -> new DocusignEnvelopesApi(self.wrapped))
                .setMetaData(Janitor.MetaData.HELP, "envelopes(): Returns the envelopes API, which uses this client.");
    }

    protected final ApiClient wrapped = new ApiClient();

    public DocusignApiClient() {
        super(DISPATCHER);
    }
}
