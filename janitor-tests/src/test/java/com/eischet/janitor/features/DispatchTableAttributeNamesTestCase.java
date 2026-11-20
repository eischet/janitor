package com.eischet.janitor.features;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tools list the members of a dispatch table by its attribute names; a member that is defined again, in the same table or over its
 * parent's, must still appear only once, in the place where it first appeared.
 */
public class DispatchTableAttributeNamesTestCase {

    @Test
    public void memberRegisteredTwiceIsListedOnce() {
        final DispatchTable<JanitorObject> table = new DispatchTable<>(false);
        table.addStringProperty("name", self -> "first");
        table.addStringProperty("other", self -> "other");
        table.addStringProperty("name", self -> "second");

        assertEquals(List.of("name", "other"), table.streamAttributeNames().toList());
    }

    @Test
    public void memberOverridingTheParentsIsListedOnce() {
        final DispatchTable<JanitorObject> parent = new DispatchTable<>(false);
        parent.addStringProperty("name", self -> "parent");
        parent.addStringProperty("other", self -> "other");
        final DispatchTable<JanitorObject> child = parent.extend(false);
        child.addStringProperty("name", self -> "child");
        child.addStringProperty("extra", self -> "extra");

        assertEquals(List.of("name", "other", "extra"), child.streamAttributeNames().toList());
    }

    @Test
    public void inheritedDispatcherListsSharedMembersOnce() {
        final DispatchTable<JanitorObject> parent = new DispatchTable<>(false);
        parent.addStringProperty("name", self -> "parent");
        parent.addStringProperty("other", self -> "other");
        final DispatchTable<JanitorObject> child = new DispatchTable<>(false);
        child.addStringProperty("name", self -> "child");
        child.addStringProperty("extra", self -> "extra");

        final Dispatcher<JanitorObject> inherited = Dispatcher.inherit(parent, child);
        assertEquals(List.of("name", "extra", "other"), inherited.streamAttributeNames().toList());
    }
}
