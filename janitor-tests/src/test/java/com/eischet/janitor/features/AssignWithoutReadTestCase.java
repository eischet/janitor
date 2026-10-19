// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Assigning to a property, with "=" or with apply, must not read the property first.
 * The getter may be expensive or fail, e.g. when it lazily loads data that does not exist yet.
 */
public class AssignWithoutReadTestCase extends JanitorTest {

    private static class Attachment extends JanitorComposed<Attachment> {
        private static final DispatchTable<Attachment> DISPATCH = new DispatchTable<>(Attachment::new);

        static {
            DISPATCH.addObjectProperty("data",
                    self -> Janitor.nullableBinary(self.getData()),
                    (self, value) -> {
                        if (value instanceof JBinary binary) {
                            self.data = binary.janitorGetHostValue();
                        } else if (value instanceof JString string) {
                            self.data = string.janitorGetHostValue().getBytes(StandardCharsets.UTF_8);
                        } else {
                            throw new JanitorError("invalid assignment: you cannot assign " + value);
                        }
                    },
                    () -> Janitor.nullableBinary(null)
            );
        }

        private byte[] data;
        private int reads;

        Attachment() {
            super(DISPATCH);
        }

        byte[] getData() {
            reads++;
            if (data == null) {
                throw new IllegalStateException("lazy loading failed: not stored yet");
            }
            return data;
        }
    }

    @Test
    void assignmentDoesNotReadTheProperty() throws Exception {
        final Attachment att = new Attachment();
        evaluate("att.data = 'foo';", g -> g.bind("att", att));
        assertEquals("foo", new String(att.data, StandardCharsets.UTF_8));
        assertEquals(0, att.reads);
    }

    @Test
    void applyDoesNotReadTheProperty() throws Exception {
        final Attachment att = new Attachment();
        evaluate("att.apply({data: 'foo'});", g -> g.bind("att", att));
        assertEquals("foo", new String(att.data, StandardCharsets.UTF_8));
        assertEquals(0, att.reads);
    }

    @Test
    void readingAfterAssignmentSeesTheNewValue() throws Exception {
        final Attachment att = new Attachment();
        assertEquals("foo", evaluate("att.data = 'foo'; return att.data.toString();", g -> g.bind("att", att)).janitorToString());
        assertEquals(1, att.reads);
    }

    @Test
    void failingGetterCanStillBeCaughtWhenReading() throws Exception {
        final Attachment att = new Attachment();
        assertEquals("caught", evaluate("try { x = att.data; return 'not caught'; } catch (e) { return 'caught'; }", g -> g.bind("att", att)).janitorToString());
    }

}
