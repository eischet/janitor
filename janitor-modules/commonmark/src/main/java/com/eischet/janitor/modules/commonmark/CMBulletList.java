package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.BulletList;

public class CMBulletList extends CMListBlock {
    private static final WrapperDispatchTable<BulletList> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addStringProperty("marker",
            node -> node.janitorGetHostValue().getMarker(),
            (self, value) -> self.janitorGetHostValue().setMarker(value)
        );
        // TODO: property bulletMarker
    }

    public CMBulletList(final BulletList node) {
        super(Dispatcher.inherit(CMListBlock.dispatch, dispatch), node);
    }

    public CMBulletList() {
        this(new BulletList());
    }
}
