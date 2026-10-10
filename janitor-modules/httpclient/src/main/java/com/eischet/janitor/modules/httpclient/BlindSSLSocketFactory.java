// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.httpclient;

import com.eischet.janitor.logging.JanitorLogger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * SSL Socket factory that disables certificate verification.
 * <p>Put together in trial and error (and with a little help from Google), this class
 * turns off SSL certificate verification when needed.</p>
 * <p><b>Be aware that using this class severely degrades security!</b></p>
 * <p>Upgraded via <a href="https://stackoverflow.com/questions/52600211/how-to-programmatically-disable-certificate-hostname-verification-in-java-ldap-j">this StackOverflow answer</a>
 * for Java 8 - 181+</p>
 */
public class BlindSSLSocketFactory extends SSLSocketFactory {

    private static final JanitorLogger log = JanitorLogger.getLogger(BlindSSLSocketFactory.class);

    public static final SSLSocketFactory defaultFactory = new BlindSSLSocketFactory();

    /**
     * @return the shared factory instance
     */
    public static SSLSocketFactory getDefault() {
        return defaultFactory;
    }

    private SSLSocketFactory proxiedFactory = null;

    /**
     * Creates an SSL context that trusts every certificate. This is insecure, and only meant for deliberately ignoring security issues, e.g. in tests.
     * @return the context
     * @throws NoSuchAlgorithmException if SSL is not available
     * @throws KeyManagementException if the context cannot be initialized
     */
    public static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        final X509TrustManager[] trumanShow = { getBlindTrustManager() };
        sslContext.init(null, trumanShow, new SecureRandom());
        return sslContext;
    }

    /**
     * @return a trust manager that accepts every certificate, which is insecure
     */
    public static X509ExtendedTrustManager getBlindTrustManager() {
        return new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {

            }

            /**
             * @return null, because there are no restrictions
             */
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            /**
             * Accepts every server certificate.
             * @param arg0 the certificate chain
             * @param arg1 the authentication type
             * @throws CertificateException never
             */
            public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                // never fails, which is the whole point
            }

            /**
             * Accepts every client certificate.
             * @param arg0 the certificate chain
             * @param arg1 the authentication type
             * @throws CertificateException never
             */
            public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                // never fails, which is the whole point
            }
        };
    }

    public BlindSSLSocketFactory() {
        log.info("BlindSSLSocketFactory()");
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            final X509TrustManager [] trumanShow = { getBlindTrustManager() };
            sslContext.init(null, trumanShow, new SecureRandom());
            proxiedFactory = sslContext.getSocketFactory();
        }
        catch (final NoSuchAlgorithmException e) {
            log.error("JVM does not speak SSL, we're screwed", e);
        }
        catch (final KeyManagementException e) {
            log.error("I don't know why, but we're screwed nevertheless", e);
        }
    }


    @Override
    public Socket createSocket(final Socket arg0, final String arg1, final int arg2, final boolean arg3) throws IOException {
        log.debug( String.format("createSocket(%s,%s,%s,%s)", arg0, arg1, arg2, arg3));
        return proxiedFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        log.debug( "getDefaultCipherSuites()");
        return proxiedFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        log.debug( "getSupportedCipherSuites()");
        return proxiedFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final String arg0, final int arg1) throws IOException {
        //log.fine( "createSocket(%s,%s)", arg0, arg1);
        return proxiedFactory.createSocket(arg0, arg1);
    }

    @Override
    public Socket createSocket(final InetAddress arg0, final int arg1) throws IOException {
        //log.fine( "createSocket(%s,%s)", arg0, arg1);
        return proxiedFactory.createSocket(arg0, arg1);
    }

    @Override
    public Socket createSocket(final String arg0, final int arg1, final InetAddress arg2, final int arg3) throws IOException {
        //log.fine( "createSocket(%s,%s,%s)", arg0, arg1, arg3);
        return proxiedFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public Socket createSocket(final InetAddress arg0, final int arg1, final InetAddress arg2, final int arg3) throws IOException {
        //log.fine( "createSocket(%s,%s,%s)", arg0, arg1, arg3);
        return proxiedFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * Creates an unconnected socket.
     * @return the socket
     * @throws IOException on network errors
     */
    public Socket createSocket() throws IOException {
        log.info("someone is calling the unspecified createSocket method");
        return proxiedFactory.createSocket();
    }

}
