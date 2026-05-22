package com.eischet.janitor.modules.commonmark;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.commonmark.node.OrderedList;

public class CMOrderedList extends CMListBlock {
    private static final WrapperDispatchTable<OrderedList> dispatch = new WrapperDispatchTable<>();

    static {
        // TODO: method accept
        dispatch.addNullableIntegerProperty("markerStartNumber",
            node -> node.janitorGetHostValue().getMarkerStartNumber(),
            (self, value) -> self.janitorGetHostValue().setMarkerStartNumber(value)
        );
        dispatch.addStringProperty("markerDelimiter",
            node -> node.janitorGetHostValue().getMarkerDelimiter(),
            (self, value) -> self.janitorGetHostValue().setMarkerDelimiter(value)
        );
        dispatch.addIntegerProperty("startNumber",
            node -> node.janitorGetHostValue().getStartNumber(),
            (self, value) -> self.janitorGetHostValue().setStartNumber(value)
        );
        // TODO: property delimiter
    }

    public CMOrderedList(final OrderedList node) {
        super(Dispatcher.inherit(CMListBlock.dispatch, dispatch), node);
    }

    public CMOrderedList() {
        this(new OrderedList());
    }
}
