package com.eischet.janitor.docusign;

import com.docusign.esign.model.SignHere;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignSignHere extends JanitorComposed<DocusignSignHere> {

    public static final DispatchTable<DocusignSignHere> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("anchorString", self -> self.wrapped.getAnchorString(), (self, value) -> self.wrapped.setAnchorString(value));
        DISPATCHER.addStringProperty("anchorXOffset", self -> self.wrapped.getAnchorXOffset(), (self, value) -> self.wrapped.setAnchorXOffset(value));
        DISPATCHER.addStringProperty("anchorYOffset", self -> self.wrapped.getAnchorYOffset(), (self, value) -> self.wrapped.setAnchorYOffset(value));
        DISPATCHER.addStringProperty("anchorUnits", self -> self.wrapped.getAnchorUnits(), (self, value) -> self.wrapped.setAnchorUnits(value));
        DISPATCHER.addStringProperty("documentId", self -> self.wrapped.getDocumentId(), (self, value) -> self.wrapped.setDocumentId(value));
        DISPATCHER.addStringProperty("pageNumber", self -> self.wrapped.getPageNumber(), (self, value) -> self.wrapped.setPageNumber(value));
        DISPATCHER.addStringProperty("recipientId", self -> self.wrapped.getRecipientId(), (self, value) -> self.wrapped.setRecipientId(value));
        DISPATCHER.addStringProperty("tabLabel", self -> self.wrapped.getTabLabel(), (self, value) -> self.wrapped.setTabLabel(value));
        DISPATCHER.addStringProperty("tabId", self -> self.wrapped.getTabId(), (self, value) -> self.wrapped.setTabId(value));
        DISPATCHER.addStringProperty("optional", self -> self.wrapped.getOptional(), (self, value) -> self.wrapped.setOptional(value));
        DISPATCHER.addStringProperty("agreementAttribute", self -> self.wrapped.getAgreementAttribute(), (self, value) -> self.wrapped.setAgreementAttribute(value));
        DISPATCHER.addStringProperty("agreementAttributeLocked", self -> self.wrapped.getAgreementAttributeLocked(), (self, value) -> self.wrapped.setAgreementAttributeLocked(value));
        DISPATCHER.addStringProperty("anchorAllowWhiteSpaceInCharacters", self -> self.wrapped.getAnchorAllowWhiteSpaceInCharacters(), (self, value) -> self.wrapped.setAnchorAllowWhiteSpaceInCharacters(value));
        DISPATCHER.addStringProperty("anchorCaseSensitive", self -> self.wrapped.getAnchorCaseSensitive(), (self, value) -> self.wrapped.setAnchorCaseSensitive(value));
        DISPATCHER.addStringProperty("anchorHorizontalAlignment", self -> self.wrapped.getAnchorHorizontalAlignment(), (self, value) -> self.wrapped.setAnchorHorizontalAlignment(value));
        DISPATCHER.addStringProperty("anchorIgnoreIfNotPresent", self -> self.wrapped.getAnchorIgnoreIfNotPresent(), (self, value) -> self.wrapped.setAnchorIgnoreIfNotPresent(value));
        DISPATCHER.addStringProperty("anchorMatchWholeWord", self -> self.wrapped.getAnchorMatchWholeWord(), (self, value) -> self.wrapped.setAnchorMatchWholeWord(value));
        DISPATCHER.addStringProperty("anchorTabProcessorVersion", self -> self.wrapped.getAnchorTabProcessorVersion(), (self, value) -> self.wrapped.setAnchorTabProcessorVersion(value));
        DISPATCHER.addStringProperty("caption", self -> self.wrapped.getCaption(), (self, value) -> self.wrapped.setCaption(value));
        DISPATCHER.addStringProperty("conditionalParentLabel", self -> self.wrapped.getConditionalParentLabel(), (self, value) -> self.wrapped.setConditionalParentLabel(value));
        DISPATCHER.addStringProperty("conditionalParentValue", self -> self.wrapped.getConditionalParentValue(), (self, value) -> self.wrapped.setConditionalParentValue(value));
        DISPATCHER.addStringProperty("customTabId", self -> self.wrapped.getCustomTabId(), (self, value) -> self.wrapped.setCustomTabId(value));
        DISPATCHER.addStringProperty("formOrder", self -> self.wrapped.getFormOrder(), (self, value) -> self.wrapped.setFormOrder(value));
        DISPATCHER.addStringProperty("formPageLabel", self -> self.wrapped.getFormPageLabel(), (self, value) -> self.wrapped.setFormPageLabel(value));
        DISPATCHER.addStringProperty("formPageNumber", self -> self.wrapped.getFormPageNumber(), (self, value) -> self.wrapped.setFormPageNumber(value));
        DISPATCHER.addStringProperty("handDrawRequired", self -> self.wrapped.getHandDrawRequired(), (self, value) -> self.wrapped.setHandDrawRequired(value));
        DISPATCHER.addStringProperty("height", self -> self.wrapped.getHeight(), (self, value) -> self.wrapped.setHeight(value));
        DISPATCHER.addStringProperty("isSealSignTab", self -> self.wrapped.getIsSealSignTab(), (self, value) -> self.wrapped.setIsSealSignTab(value));
        DISPATCHER.addStringProperty("mergeFieldXml", self -> self.wrapped.getMergeFieldXml(), (self, value) -> self.wrapped.setMergeFieldXml(value));
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value));
        DISPATCHER.addStringProperty("recipientIdGuid", self -> self.wrapped.getRecipientIdGuid(), (self, value) -> self.wrapped.setRecipientIdGuid(value));
        DISPATCHER.addStringProperty("scaleValue", self -> self.wrapped.getScaleValue(), (self, value) -> self.wrapped.setScaleValue(value));
        DISPATCHER.addStringProperty("source", self -> self.wrapped.getSource(), (self, value) -> self.wrapped.setSource(value));
        DISPATCHER.addStringProperty("stampType", self -> self.wrapped.getStampType(), (self, value) -> self.wrapped.setStampType(value));
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value));
        DISPATCHER.addStringProperty("tabFullyQualifiedPath", self -> self.wrapped.getTabFullyQualifiedPath(), (self, value) -> self.wrapped.setTabFullyQualifiedPath(value));
        DISPATCHER.addStringProperty("tabOrder", self -> self.wrapped.getTabOrder(), (self, value) -> self.wrapped.setTabOrder(value));
        DISPATCHER.addStringProperty("tabType", self -> self.wrapped.getTabType(), (self, value) -> self.wrapped.setTabType(value));
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value));
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value));

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

    public SignHere getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignSignHere [wrapped=" + wrapped + "]";
    }
}
