// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Transient properties are visible to scripts, but never written to JSON, so objects that point at each other can be written.
 */
public class JsonTransientPropertiesTestCase extends JanitorTest {

    @Test
    public void objectsPointingAtEachOtherCanBeWritten() throws JsonException {
        final Ticket ticket = Ticket.withActions(3);
        assertEquals("{\"id\":4711,\"title\":\"Drucker\"}", Ticket.DISPATCH.writeToJson(ticket));
        assertEquals("{\"id\":2,\"ticketId\":4711}", Action.DISPATCH.writeToJson(ticket.actions.get(1)));
        assertEquals("{\"id\":4711,\"title\":\"Drucker\"}", Janitor.writeJson(JsonWriteOptions.COMPACT, out -> Ticket.DISPATCH.writeToJson(out, ticket)));
    }

    @Test
    public void notPartOfTheSchema() throws JsonException {
        assertEquals("{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"number\"},\"title\":{\"type\":\"string\"}}}",
                Janitor.writeJson(JsonWriteOptions.COMPACT, Ticket.DISPATCH::writeSchemaToJson));
        assertEquals("{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"number\"},\"ticketId\":{\"type\":\"number\"}}}",
                Janitor.writeJson(JsonWriteOptions.COMPACT, Action.DISPATCH::writeSchemaToJson));
    }

    @Test
    public void scriptsStillSeeTheProperties() throws Exception {
        final Ticket ticket = Ticket.withActions(3);
        assertEquals(3L, evaluate("ticket.actions.size()", g -> g.bind("ticket", ticket)).janitorGetHostValue());
        assertEquals("Drucker", evaluate("ticket.actions[2].ticket.title", g -> g.bind("ticket", ticket)).janitorGetHostValue());
        assertEquals("Drucker", evaluate("ticket.latestAction.ticket.title", g -> g.bind("ticket", ticket)).janitorGetHostValue());
    }

    @Test
    public void nullIsSeenAsNull() throws Exception {
        final Action orphan = new Action(null, 9);
        assertEquals(true, evaluate("action.ticket == null", g -> g.bind("action", orphan)).janitorGetHostValue());
        assertEquals("{\"id\":9}", Action.DISPATCH.writeToJson(orphan));
    }

    @Test
    public void scriptsWriteThemWithoutTheTransientProperties() throws Exception {
        final Ticket ticket = Ticket.withActions(2);
        assertEquals("[{\"id\":4711,\"title\":\"Drucker\"},{\"id\":1,\"ticketId\":4711}]",
                evaluate("[ticket, ticket.actions[0]].toJson()", g -> g.bind("ticket", ticket)).janitorGetHostValue());
    }

    private static class Ticket extends JanitorComposed<Ticket> {
        public static final DispatchTable<Ticket> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addIntegerProperty("id", self -> self.id, (self, value) -> self.id = value);
            DISPATCH.addStringProperty("title", self -> self.title, (self, value) -> self.title = value);
            DISPATCH.addTransientListProperty("actions", self -> Janitor.list(self.actions.stream()));
            DISPATCH.addTransientObjectProperty("latestAction", self -> self.actions.isEmpty() ? null : self.actions.getLast(), Action.DISPATCH);
        }

        int id = 4711;
        String title = "Drucker";
        final List<Action> actions = new ArrayList<>();

        Ticket() {
            super(DISPATCH);
        }

        static Ticket withActions(final int count) {
            final Ticket ticket = new Ticket();
            for (int i = 1; i <= count; i++) {
                ticket.actions.add(new Action(ticket, i));
            }
            return ticket;
        }
    }

    private static class Action extends JanitorComposed<Action> {
        public static final DispatchTable<Action> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addIntegerProperty("id", self -> self.id, (self, value) -> self.id = value);
            DISPATCH.addNullableIntegerProperty("ticketId", self -> self.ticket == null ? null : self.ticket.id, (self, value) -> { });
            DISPATCH.addTransientObjectProperty("ticket", self -> self.ticket);
        }

        final Ticket ticket;
        int id;

        Action(final Ticket ticket, final int id) {
            super(DISPATCH);
            this.ticket = ticket;
            this.id = id;
        }
    }

}
