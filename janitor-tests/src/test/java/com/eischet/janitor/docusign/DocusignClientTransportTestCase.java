// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The DocuSign client must be able to send requests with the libraries this module brings along. Its HTTP layer is put
 * together from several artifacts at run time, so versions that do not fit only show when the first request is sent.
 */
public class DocusignClientTransportTestCase {

    @Test
    public void aRequestFailsAtTheConnectionNotInTheLibraries() {
        // nothing listens on port 1, so the request has to end with a connection error from the HTTP layer
        final ApiClient client = new ApiClient("https://127.0.0.1:1/restapi");
        final EnvelopesApi envelopes = new EnvelopesApi(client);
        // an Exception, not an Error: a linkage error would mean that the HTTP layer could not be set up at all
        assertThrows(Exception.class, () -> envelopes.getEnvelope("account", "envelope"));
    }

}
