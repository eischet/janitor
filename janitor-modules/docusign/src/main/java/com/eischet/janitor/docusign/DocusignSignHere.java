// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.SignHere;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign sign here. */
public class DocusignSignHere extends JanitorComposed<DocusignSignHere> {

    public static final DispatchTable<DocusignSignHere> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("anchorString", self -> self.wrapped.getAnchorString(), (self, value) -> self.wrapped.setAnchorString(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorString: The text in the document at which the tab is placed; writable.");
        DISPATCHER.addStringProperty("anchorXOffset", self -> self.wrapped.getAnchorXOffset(), (self, value) -> self.wrapped.setAnchorXOffset(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorXOffset: The horizontal offset of the tab from the anchor text, in anchorUnits; writable.");
        DISPATCHER.addStringProperty("anchorYOffset", self -> self.wrapped.getAnchorYOffset(), (self, value) -> self.wrapped.setAnchorYOffset(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorYOffset: The vertical offset of the tab from the anchor text, in anchorUnits; writable.");
        DISPATCHER.addStringProperty("anchorUnits", self -> self.wrapped.getAnchorUnits(), (self, value) -> self.wrapped.setAnchorUnits(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorUnits: The unit of the anchor offsets, e.g. \"pixels\", \"inches\", \"mms\" or \"cms\"; writable.");
        DISPATCHER.addStringProperty("documentId", self -> self.wrapped.getDocumentId(), (self, value) -> self.wrapped.setDocumentId(value))
                .setMetaData(Janitor.MetaData.HELP, "documentId: The ID of the document that the tab is placed on; writable.");
        DISPATCHER.addStringProperty("pageNumber", self -> self.wrapped.getPageNumber(), (self, value) -> self.wrapped.setPageNumber(value))
                .setMetaData(Janitor.MetaData.HELP, "pageNumber: The number of the page that the tab is placed on, as a string; writable.");
        DISPATCHER.addStringProperty("recipientId", self -> self.wrapped.getRecipientId(), (self, value) -> self.wrapped.setRecipientId(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientId: The ID of the recipient who signs at the tab; writable.");
        DISPATCHER.addStringProperty("tabLabel", self -> self.wrapped.getTabLabel(), (self, value) -> self.wrapped.setTabLabel(value))
                .setMetaData(Janitor.MetaData.HELP, "tabLabel: The label of the tab; writable.");
        DISPATCHER.addStringProperty("tabId", self -> self.wrapped.getTabId(), (self, value) -> self.wrapped.setTabId(value))
                .setMetaData(Janitor.MetaData.HELP, "tabId: The ID of the tab, which DocuSign assigns; writable.");
        DISPATCHER.addStringProperty("optional", self -> self.wrapped.getOptional(), (self, value) -> self.wrapped.setOptional(value))
                .setMetaData(Janitor.MetaData.HELP, "optional: \"true\" if signing at this tab is optional; writable.");
        DISPATCHER.addStringProperty("agreementAttribute", self -> self.wrapped.getAgreementAttribute(), (self, value) -> self.wrapped.setAgreementAttribute(value));
        DISPATCHER.addStringProperty("agreementAttributeLocked", self -> self.wrapped.getAgreementAttributeLocked(), (self, value) -> self.wrapped.setAgreementAttributeLocked(value));
        DISPATCHER.addStringProperty("anchorAllowWhiteSpaceInCharacters", self -> self.wrapped.getAnchorAllowWhiteSpaceInCharacters(), (self, value) -> self.wrapped.setAnchorAllowWhiteSpaceInCharacters(value));
        DISPATCHER.addStringProperty("anchorCaseSensitive", self -> self.wrapped.getAnchorCaseSensitive(), (self, value) -> self.wrapped.setAnchorCaseSensitive(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorCaseSensitive: \"true\" if the anchor text is matched case-sensitively; writable.");
        DISPATCHER.addStringProperty("anchorHorizontalAlignment", self -> self.wrapped.getAnchorHorizontalAlignment(), (self, value) -> self.wrapped.setAnchorHorizontalAlignment(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorHorizontalAlignment: The horizontal alignment of the tab relative to the anchor text; writable.");
        DISPATCHER.addStringProperty("anchorIgnoreIfNotPresent", self -> self.wrapped.getAnchorIgnoreIfNotPresent(), (self, value) -> self.wrapped.setAnchorIgnoreIfNotPresent(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorIgnoreIfNotPresent: \"true\" to ignore the tab if the anchor text is not found in the document; writable.");
        DISPATCHER.addStringProperty("anchorMatchWholeWord", self -> self.wrapped.getAnchorMatchWholeWord(), (self, value) -> self.wrapped.setAnchorMatchWholeWord(value))
                .setMetaData(Janitor.MetaData.HELP, "anchorMatchWholeWord: \"true\" if the anchor text only matches whole words; writable.");
        DISPATCHER.addStringProperty("anchorTabProcessorVersion", self -> self.wrapped.getAnchorTabProcessorVersion(), (self, value) -> self.wrapped.setAnchorTabProcessorVersion(value));
        DISPATCHER.addStringProperty("caption", self -> self.wrapped.getCaption(), (self, value) -> self.wrapped.setCaption(value));
        DISPATCHER.addStringProperty("conditionalParentLabel", self -> self.wrapped.getConditionalParentLabel(), (self, value) -> self.wrapped.setConditionalParentLabel(value))
                .setMetaData(Janitor.MetaData.HELP, "conditionalParentLabel: The label of the tab whose value decides whether this tab is shown; writable.");
        DISPATCHER.addStringProperty("conditionalParentValue", self -> self.wrapped.getConditionalParentValue(), (self, value) -> self.wrapped.setConditionalParentValue(value))
                .setMetaData(Janitor.MetaData.HELP, "conditionalParentValue: The value of the conditional parent tab that makes this tab shown; writable.");
        DISPATCHER.addStringProperty("customTabId", self -> self.wrapped.getCustomTabId(), (self, value) -> self.wrapped.setCustomTabId(value))
                .setMetaData(Janitor.MetaData.HELP, "customTabId: The ID of the custom tab that this tab is based on; writable.");
        DISPATCHER.addStringProperty("formOrder", self -> self.wrapped.getFormOrder(), (self, value) -> self.wrapped.setFormOrder(value));
        DISPATCHER.addStringProperty("formPageLabel", self -> self.wrapped.getFormPageLabel(), (self, value) -> self.wrapped.setFormPageLabel(value));
        DISPATCHER.addStringProperty("formPageNumber", self -> self.wrapped.getFormPageNumber(), (self, value) -> self.wrapped.setFormPageNumber(value));
        DISPATCHER.addStringProperty("handDrawRequired", self -> self.wrapped.getHandDrawRequired(), (self, value) -> self.wrapped.setHandDrawRequired(value))
                .setMetaData(Janitor.MetaData.HELP, "handDrawRequired: \"true\" if the signer must draw the signature by hand; writable.");
        DISPATCHER.addStringProperty("height", self -> self.wrapped.getHeight(), (self, value) -> self.wrapped.setHeight(value));
        DISPATCHER.addStringProperty("isSealSignTab", self -> self.wrapped.getIsSealSignTab(), (self, value) -> self.wrapped.setIsSealSignTab(value));
        DISPATCHER.addStringProperty("mergeFieldXml", self -> self.wrapped.getMergeFieldXml(), (self, value) -> self.wrapped.setMergeFieldXml(value));
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The name of the tab; writable.");
        DISPATCHER.addStringProperty("recipientIdGuid", self -> self.wrapped.getRecipientIdGuid(), (self, value) -> self.wrapped.setRecipientIdGuid(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientIdGuid: The globally unique ID of the recipient who signs at the tab; writable.");
        DISPATCHER.addStringProperty("scaleValue", self -> self.wrapped.getScaleValue(), (self, value) -> self.wrapped.setScaleValue(value))
                .setMetaData(Janitor.MetaData.HELP, "scaleValue: The scaling factor for the size of the signature, as a string, e.g. \"0.5\"; writable.");
        DISPATCHER.addStringProperty("source", self -> self.wrapped.getSource(), (self, value) -> self.wrapped.setSource(value));
        DISPATCHER.addStringProperty("stampType", self -> self.wrapped.getStampType(), (self, value) -> self.wrapped.setStampType(value))
                .setMetaData(Janitor.MetaData.HELP, "stampType: The kind of mark that is placed, e.g. \"signature\" or \"stamp\"; writable.");
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The status of the tab; writable.");
        DISPATCHER.addStringProperty("tabFullyQualifiedPath", self -> self.wrapped.getTabFullyQualifiedPath(), (self, value) -> self.wrapped.setTabFullyQualifiedPath(value));
        DISPATCHER.addStringProperty("tabOrder", self -> self.wrapped.getTabOrder(), (self, value) -> self.wrapped.setTabOrder(value))
                .setMetaData(Janitor.MetaData.HELP, "tabOrder: The position of the tab in the order in which the signer visits the tabs, as a string; writable.");
        DISPATCHER.addStringProperty("tabType", self -> self.wrapped.getTabType(), (self, value) -> self.wrapped.setTabType(value))
                .setMetaData(Janitor.MetaData.HELP, "tabType: The type of the tab; writable.");
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value))
                .setMetaData(Janitor.MetaData.HELP, "templateLocked: \"true\" if the sender may not change the tab taken from a template; writable.");
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value))
                .setMetaData(Janitor.MetaData.HELP, "templateRequired: \"true\" if the sender may not remove the tab taken from a template; writable.");

        // TODO: Add wrappers for complex object properties:
        // - PropertyMetadata: anchorCaseSensitiveMetadata, anchorHorizontalAlignmentMetadata, anchorIgnoreIfNotPresentMetadata, anchorMatchWholeWordMetadata, anchorStringMetadata, anchorTabProcessorVersionMetadata, anchorUnitsMetadata, anchorXOffsetMetadata, anchorYOffsetMetadata, captionMetadata, conditionalParentLabelMetadata, conditionalParentValueMetadata, customTabIdMetadata, documentIdMetadata, formOrderMetadata, formPageLabelMetadata, formPageNumberMetadata, heightMetadata, mergeFieldMetadata, nameMetadata, optionalMetadata, pageNumberMetadata, recipientIdGuidMetadata, recipientIdMetadata, scaleValueMetadata, smartContractInformationMetadata, sourceMetadata, stampTypeMetadata, statusMetadata, tabIdMetadata, tabLabelMetadata, tabOrderMetadata, tabTypeMetadata, templateLockedMetadata, templateRequiredMetadata, tooltipMetadata, widthMetadata, xPositionMetadata, yPositionMetadata
        // - SmartContractInformation: smartContractInformation
        // - TabPosition: tabPosition
        // - ErrorDetails: errorDetails
        // - MergeField: mergeField
    }

    private final SignHere wrapped;

    public DocusignSignHere() {
        super(DISPATCHER);
        this.wrapped = new SignHere();
    }

    /**
     * @return the wrapped DocuSign object
     */
    public SignHere getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignSignHere [wrapped=" + wrapped + "]";
    }
}
