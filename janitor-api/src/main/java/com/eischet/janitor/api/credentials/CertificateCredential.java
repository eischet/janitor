// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A certificate with its private key, e.g. for TLS client authentication.
 */
public interface CertificateCredential extends JanitorCredential {

    /**
     * The private key that belongs to the first certificate of the chain.
     *
     * @return the key
     */
    @NotNull PrivateKey privateKey();

    /**
     * The certificate, followed by the certificates of its chain, if any.
     *
     * @return at least one certificate
     */
    @NotNull List<X509Certificate> certificateChain();

    @Override
    default @NotNull String kind() {
        return "a certificate with its key";
    }
}
