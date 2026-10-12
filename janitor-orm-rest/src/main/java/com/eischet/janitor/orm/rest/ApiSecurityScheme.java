// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How callers authenticate, as an entry of {@code components.securitySchemes} in the API description. The API does not authenticate anybody
 * itself (the host's resolver does that); this only describes it, so that tools such as Swagger UI and client generators know how to
 * authenticate, and so that a {@code 401} can tell callers what is expected ({@code WWW-Authenticate}, see {@link #challenge()}).
 * <p>
 * Create schemes with the factory methods, e.g. {@link #bearer(String)}, and name them in {@link OpenApiOptions#withSecurityScheme}.
 * </p>
 *
 * @param type             what kind of scheme this is
 * @param description      what to tell people about it, or null
 * @param scheme           for {@link Type#HTTP}: the name of the HTTP authentication scheme, e.g. "bearer" or "basic"
 * @param bearerFormat     for bearer tokens: a hint what they look like, e.g. "JWT"; or null
 * @param in               for {@link Type#API_KEY}: where the key is, "header", "query" or "cookie"
 * @param name             for {@link Type#API_KEY}: the name of the header, query parameter or cookie
 * @param flows            for {@link Type#OAUTH2}: the flows, by their names in OpenAPI ("authorizationCode", "clientCredentials", ...)
 * @param openIdConnectUrl for {@link Type#OPEN_ID_CONNECT}: the URL of the discovery document
 * @param realm            for basic authentication: the realm that the challenge names, or null
 * @param challengeText    what {@code WWW-Authenticate} says, if not the default for this kind of scheme; or null
 */
public record ApiSecurityScheme(@NotNull Type type,
                                @Nullable String description,
                                @Nullable String scheme,
                                @Nullable String bearerFormat,
                                @Nullable String in,
                                @Nullable String name,
                                @NotNull Map<String, OAuthFlow> flows,
                                @Nullable String openIdConnectUrl,
                                @Nullable String realm,
                                @Nullable String challengeText) {

    /** The kinds of schemes that OpenAPI knows. */
    public enum Type {
        /** HTTP authentication, e.g. bearer tokens or basic. */
        HTTP,
        /** A key in a header, a query parameter or a cookie. */
        API_KEY,
        /** OAuth 2. */
        OAUTH2,
        /** OpenID Connect. */
        OPEN_ID_CONNECT,
        /** Certificates of the client. */
        MUTUAL_TLS
    }

    /**
     * One flow of an OAuth 2 scheme.
     *
     * @param authorizationUrl where users authorize, for flows that have it; or null
     * @param tokenUrl         where tokens are issued, for flows that have it; or null
     * @param refreshUrl       where tokens are refreshed, or null
     * @param scopes           the scopes and what they mean
     */
    public record OAuthFlow(@Nullable String authorizationUrl, @Nullable String tokenUrl, @Nullable String refreshUrl, @NotNull Map<String, String> scopes) {
    }

    private static ApiSecurityScheme of(final Type type, final String scheme, final String bearerFormat, final String in, final String name,
                                        final Map<String, OAuthFlow> flows, final String openIdConnectUrl, final String realm) {
        return new ApiSecurityScheme(type, null, scheme, bearerFormat, in, name, Map.copyOf(flows), openIdConnectUrl, realm, null);
    }

    /**
     * @param bearerFormat a hint what the tokens look like, e.g. "JWT"; or null
     * @return bearer tokens in the {@code Authorization} header
     */
    public static @NotNull ApiSecurityScheme bearer(final @Nullable String bearerFormat) {
        return of(Type.HTTP, "bearer", bearerFormat, null, null, Map.of(), null, null);
    }

    /**
     * @param realm the realm for the challenge, or null
     * @return basic authentication
     */
    public static @NotNull ApiSecurityScheme basic(final @Nullable String realm) {
        return of(Type.HTTP, "basic", null, null, null, Map.of(), null, realm);
    }

    /**
     * @param in   where the key is: "header", "query" or "cookie"
     * @param name the name of the header, query parameter or cookie
     * @return an API key
     */
    public static @NotNull ApiSecurityScheme apiKey(final @NotNull String in, final @NotNull String name) {
        if (!in.equals("header") && !in.equals("query") && !in.equals("cookie")) {
            throw new IllegalArgumentException("an API key is in the header, the query or a cookie, not '" + in + "'");
        }
        return of(Type.API_KEY, null, null, in, name, Map.of(), null, null);
    }

    /**
     * @param authorizationUrl where users authorize
     * @param tokenUrl         where tokens are issued
     * @param scopes           the scopes and what they mean
     * @return OAuth 2 with the authorization code flow
     */
    public static @NotNull ApiSecurityScheme oauth2AuthorizationCode(final @NotNull String authorizationUrl, final @NotNull String tokenUrl, final @NotNull Map<String, String> scopes) {
        return of(Type.OAUTH2, null, null, null, null, Map.of("authorizationCode", new OAuthFlow(authorizationUrl, tokenUrl, null, scopes)), null, null);
    }

    /**
     * @param tokenUrl where tokens are issued
     * @param scopes   the scopes and what they mean
     * @return OAuth 2 with the client credentials flow
     */
    public static @NotNull ApiSecurityScheme oauth2ClientCredentials(final @NotNull String tokenUrl, final @NotNull Map<String, String> scopes) {
        return of(Type.OAUTH2, null, null, null, null, Map.of("clientCredentials", new OAuthFlow(null, tokenUrl, null, scopes)), null, null);
    }

    /**
     * @param url the URL of the discovery document
     * @return OpenID Connect
     */
    public static @NotNull ApiSecurityScheme openIdConnect(final @NotNull String url) {
        return of(Type.OPEN_ID_CONNECT, null, null, null, null, Map.of(), url, null);
    }

    /** @return authentication with a certificate of the client */
    public static @NotNull ApiSecurityScheme mutualTls() {
        return of(Type.MUTUAL_TLS, null, null, null, null, Map.of(), null, null);
    }

    /**
     * @param description what to tell people about the scheme
     * @return a copy with that description
     */
    public @NotNull ApiSecurityScheme withDescription(final @Nullable String description) {
        return new ApiSecurityScheme(type, description, scheme, bearerFormat, in, name, flows, openIdConnectUrl, realm, challengeText);
    }

    /**
     * @param challengeText what {@code WWW-Authenticate} should say, e.g. {@code Bearer realm="api"}
     * @return a copy with that challenge, replacing the default
     */
    public @NotNull ApiSecurityScheme withChallenge(final @Nullable String challengeText) {
        return new ApiSecurityScheme(type, description, scheme, bearerFormat, in, name, flows, openIdConnectUrl, realm, challengeText);
    }

    /**
     * What a {@code 401} tells callers in the {@code WWW-Authenticate} header: {@code Bearer} for bearer tokens, OAuth 2 and OpenID Connect,
     * {@code Basic realm="..."} for basic authentication; nothing for schemes that have no HTTP challenge (API keys, certificates) unless
     * one was given with {@link #withChallenge}.
     *
     * @return the challenge, or null if there is none
     */
    public @Nullable String challenge() {
        if (challengeText != null) {
            return challengeText;
        }
        return switch (type) {
            case HTTP -> "basic".equalsIgnoreCase(scheme)
                    ? (realm == null ? "Basic" : "Basic realm=\"" + realm.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                    : "Bearer";
            case OAUTH2, OPEN_ID_CONNECT -> "Bearer";
            case API_KEY, MUTUAL_TLS -> null;
        };
    }

    /** @return the entry for {@code components.securitySchemes}, as a tree of maps */
    public @NotNull Map<String, Object> toTree() {
        final Map<String, Object> tree = new LinkedHashMap<>();
        switch (type) {
            case HTTP -> {
                tree.put("type", "http");
                tree.put("scheme", scheme);
                if (bearerFormat != null) {
                    tree.put("bearerFormat", bearerFormat);
                }
            }
            case API_KEY -> {
                tree.put("type", "apiKey");
                tree.put("in", in);
                tree.put("name", name);
            }
            case OAUTH2 -> {
                tree.put("type", "oauth2");
                final Map<String, Object> flowTrees = new LinkedHashMap<>();
                flows.forEach((flowName, flow) -> {
                    final Map<String, Object> flowTree = new LinkedHashMap<>();
                    if (flow.authorizationUrl() != null) {
                        flowTree.put("authorizationUrl", flow.authorizationUrl());
                    }
                    if (flow.tokenUrl() != null) {
                        flowTree.put("tokenUrl", flow.tokenUrl());
                    }
                    if (flow.refreshUrl() != null) {
                        flowTree.put("refreshUrl", flow.refreshUrl());
                    }
                    flowTree.put("scopes", new LinkedHashMap<>(flow.scopes()));
                    flowTrees.put(flowName, flowTree);
                });
                tree.put("flows", flowTrees);
            }
            case OPEN_ID_CONNECT -> {
                tree.put("type", "openIdConnect");
                tree.put("openIdConnectUrl", openIdConnectUrl);
            }
            case MUTUAL_TLS -> tree.put("type", "mutualTLS");
        }
        if (description != null) {
            tree.put("description", description);
        }
        return tree;
    }
}
