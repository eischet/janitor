// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection options for {@link MsGraphClient}. Scripts pass them as a map, e.g.
 * {@code msgraph.connect(clientId, secret, tenant, {proxyHost: "proxy", proxyPort: 3128})}.
 */
public class MsGraphOptions extends JanitorComposed<MsGraphOptions> {

    public static final String DEFAULT_BASE_URL = "https://graph.microsoft.com/v1.0";

    public static final DispatchTable<MsGraphOptions> DISPATCH = new DispatchTable<>(MsGraphOptions::new);

    static {
        DISPATCH.addStringProperty("baseUrl", MsGraphOptions::getBaseUrl, MsGraphOptions::setBaseUrl);
        DISPATCH.addStringProperty("proxyHost", MsGraphOptions::getProxyHost, MsGraphOptions::setProxyHost);
        DISPATCH.addIntegerProperty("proxyPort", MsGraphOptions::getProxyPort, MsGraphOptions::setProxyPort);
        DISPATCH.addBooleanProperty("tokenCache", MsGraphOptions::isTokenCache, MsGraphOptions::setTokenCache);
        DISPATCH.addIntegerProperty("timeoutSeconds", MsGraphOptions::getTimeoutSeconds, MsGraphOptions::setTimeoutSeconds);
        DISPATCH.addIntegerProperty("maxRetries", MsGraphOptions::getMaxRetries, MsGraphOptions::setMaxRetries);
    }

    private @NotNull String baseUrl = DEFAULT_BASE_URL;
    private @Nullable String proxyHost;
    private int proxyPort;
    private boolean tokenCache;
    private int timeoutSeconds = 60;
    private int maxRetries = 4;

    public MsGraphOptions() {
        super(DISPATCH);
    }

    /**
     * @return the Graph endpoint including the API version, without a trailing slash
     */
    public @NotNull String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final @Nullable String baseUrl) {
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : stripTrailingSlash(baseUrl.trim());
    }

    public @Nullable String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final @Nullable String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @return true if a proxy is configured; a blank host means "no proxy"
     */
    public boolean hasProxy() {
        return proxyHost != null && !proxyHost.isBlank();
    }

    /**
     * The persistent token cache stores tokens on disk (Windows: DPAPI-encrypted file, macOS: keychain,
     * Linux: keyring via libsecret), so that they survive process restarts. Tokens are always cached in memory anyway,
     * as long as the client lives. Linux servers usually have no keyring, so this is off by default.
     * @return true if the persistent token cache is used
     */
    public boolean isTokenCache() {
        return tokenCache;
    }

    public void setTokenCache(final boolean tokenCache) {
        this.tokenCache = tokenCache;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(final int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * @return how often a throttled (429) or unavailable (503, 504) request is retried
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    private static String stripTrailingSlash(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
