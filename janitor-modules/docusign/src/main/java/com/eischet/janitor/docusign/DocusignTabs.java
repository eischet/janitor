package com.eischet.janitor.docusign;

import com.docusign.esign.model.Tabs;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignTabs extends JanitorComposed<DocusignTabs> {

    public static final DispatchTable<DocusignTabs> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addMethod("addSignHere", (self, process, args) -> {
            DocusignSignHere signHere = args.getRequired(0, DocusignSignHere.class);
            self.wrapped.addSignHereTabsItem(signHere.getWrapped());
            return self;
        });
    }

    private final Tabs wrapped;

    public DocusignTabs() {
        this(new Tabs());
    }

    public DocusignTabs(final Tabs wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    public Tabs getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignTabs [wrapped=" + wrapped + "]";
    }
}
