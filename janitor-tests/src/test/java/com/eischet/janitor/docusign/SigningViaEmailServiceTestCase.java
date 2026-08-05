package com.eischet.janitor.docusign;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SigningViaEmailServiceTestCase extends JanitorTest {

    // Based on https://github.com/docusign/code-examples-java/blob/master/src/main/java/com/docusign/controller/eSignature/services/SigningViaEmailService.java

    @Language("Janitor")
    private static final String SCRIPT = """
// import docusign; // TODO: import should work, but does not, can't see the issue right now

PDF_DOCUMENT_FILE_NAME = "World_Wide_Corp_lorem.pdf"
PDF_DOCUMENT_NAME = "Lorem Ipsum";
ANCHOR_OFFSET_Y = "10" // I have no idea why these are strings, but it's in the original API.
ANCHOR_OFFSET_X = "20"

signerTabs = docusign.Tabs()

signerTabs.addSignHere(docusign.SignHere({
    anchorString: "**signature_1**",
    anchorUnits: "pixels",
    anchorYOffset: ANCHOR_OFFSET_Y,
    anchorXOffset: ANCHOR_OFFSET_X
}))

signerTabs.addSignHere(docusign.SignHere({
    anchorString: "/sn1/",
    anchorUnits: "pixels",
    anchorYOffset: ANCHOR_OFFSET_Y,
    anchorXOffset: ANCHOR_OFFSET_X
}))

cc = docusign.CarbonCopy({email: 'bar@example.com', name: 'Jane Doe', recipientId: '2', routingOrder: '2'})

signer = docusign.Signer({email: 'foo@example.com', name: 'John Doe', recipientId: '1', routingOrder: '1', tabs: signerTabs})

env = docusign.EnvelopeDefinition({
    emailSubject: "Please sign this document set",
    recipients: docusign.Recipients().addSigner(signer).addCarbonCopy(cc),
    status: "sent",
})

env.addDocument(docusign.Document({
    documentBase64: "",
    name: PDF_DOCUMENT_FILE_NAME,
    fileExtension: "pdf",
    order: "1"
}))

return env
""";

    @Test
    void test() throws JanitorRuntimeException, JanitorCompilerException {
        final JanitorObject result = evaluate(SCRIPT, g -> g.bind("docusign", new DocusignModule()));
        Assertions.assertInstanceOf(DocusignEnvelopeDefinition.class, result);


    }

}
