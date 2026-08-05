package com.eischet.janitor.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiResponse;
import com.docusign.esign.model.Envelope;
import com.docusign.esign.model.EnvelopeSummary;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignEnvelopesApi extends JanitorComposed<DocusignEnvelopesApi> {

    public static final DispatchTable<DocusignEnvelopesApi> DISPATCHER = new DispatchTable<>();

    static {
        // EnvelopesApi.CreateEnvelopeOptions
        DISPATCHER.addMethod("CreateEnvelopeOptions", (self, process, arguments) ->
                DocusignModule.enableApply(process, new DocusignCreateEnvelopeOptions(self.getCreateEnvelopeOptions()), arguments));
        DISPATCHER.addMethod("createEnvelope", (self, process, arguments) -> {
            final String accountId = arguments.getRequiredStringValue(0);
            final DocusignEnvelopeDefinition envDef = arguments.getRequired(1, DocusignEnvelopeDefinition.class);
            try {
                final EnvelopeSummary result = self.envelopesApi.createEnvelope(
                        accountId, envDef.getWrapped());
                return new DocusignEnvelopeSummary(result);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "Failed to create envelope", e);
            }

        });

        DISPATCHER.addMethod("getEnvelope", (self, process, arguments) -> {
            final String accountId = arguments.getRequiredStringValue(0);
            final String envelopeId = arguments.getRequiredStringValue(1);
            final JMap optionsMap = arguments.size() > 2 ? arguments.getRequired(2, JMap.class) : null;
            final EnvelopesApi.GetEnvelopeOptions options = self.envelopesApi.new GetEnvelopeOptions();
            final DocusignGetEnvelopeOptions docusignOptions = new DocusignGetEnvelopeOptions(options);
            if (optionsMap != null) {
                optionsMap.applyTo(process, docusignOptions);
            }
            try {
                final Envelope response = self.envelopesApi.getEnvelope(accountId, envelopeId, options);
                return new DocusignEnvelope(response);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "Failed to get document", e);
            }
        });


        DISPATCHER.addMethod("getDocumentWithHttpInfo", (self, process, arguments) -> {
            final String accountId = arguments.getRequiredStringValue(0);
            final String envelopeId = arguments.getRequiredStringValue(1);
            final String documentId = arguments.getRequiredStringValue(2);
            final JMap optionsMap = arguments.size() > 3 ? arguments.getRequired(3, JMap.class) : null;
            final EnvelopesApi.GetDocumentOptions options = self.envelopesApi.new GetDocumentOptions();
            final DocusignGetDocumentOptions docusignOptions = new DocusignGetDocumentOptions(options);
            if (optionsMap != null) {
                optionsMap.applyTo(process, docusignOptions);
            }
            try {
                final ApiResponse<byte[]> response = self.envelopesApi.getDocumentWithHttpInfo(accountId, envelopeId, documentId, options);
                final byte[] data = response.getData();
                return Janitor.nullableBinary(data);
            } catch (Exception e) {
                throw new JanitorNativeException(process, "Failed to get document", e);
            }
        });

    }

    protected final EnvelopesApi envelopesApi;

    public DocusignEnvelopesApi(final EnvelopesApi envelopesApi) {
        super(DISPATCHER);
        this.envelopesApi = envelopesApi;
    }

    public DocusignEnvelopesApi(final ApiClient apiClient) {
        super(DISPATCHER);
        this.envelopesApi = new EnvelopesApi(apiClient);
    }

    public EnvelopesApi.CreateEnvelopeOptions getCreateEnvelopeOptions() {
        return envelopesApi.new CreateEnvelopeOptions(); // I learned about this syntax just now.
    }

}
