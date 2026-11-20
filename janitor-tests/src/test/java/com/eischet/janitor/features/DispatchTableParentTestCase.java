package com.eischet.janitor.features;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tools that describe a dispatch table need its parent, to tell the inherited members from its own.
 */
public class DispatchTableParentTestCase {

    @Test
    public void extendedTableKnowsItsParent() {
        final DispatchTable<JanitorObject> parent = new DispatchTable<>(false);
        parent.addStringProperty("name", self -> "parent");
        final DispatchTable<JanitorObject> child = parent.extend(false);
        child.addStringProperty("extra", self -> "child");

        assertNull(parent.getParent());
        assertSame(parent, child.getParent());
        assertEquals(List.of("extra"), child.getDirectAttributes().stream().map(GenericDispatchTable.Attribute::name).toList());
        assertEquals(List.of("name", "extra"), child.streamAttributeNames().toList());
    }

    @Test
    public void tableWithExplicitParentKnowsIt() {
        final DispatchTable<JanitorObject> parent = new DispatchTable<>(false);
        final DispatchTable<JanitorObject> child = new DispatchTable<>(parent, self -> self, false);

        assertSame(parent, child.getParent());
    }
}
