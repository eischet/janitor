// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Copying must not evaluate read-only attributes, which may load themselves (e.g. from join tables). */
public class EntityDuplicateLazyTestCase extends JanitorTest {

    static class Holder implements OrmEntity {
        static final ForeignKeyNull<Holder> NULL = new ForeignKeyNull<>(Holder.class);
        static final EntityDispatchTable<Holder, EntityDispatchTableTestCase.TestCollection> DISPATCH =
                new EntityDispatchTable<>(Holder.class, up -> new Holder(), NULL, up -> null);
        static final List<Long> IDS_SEEN_BY_GETTER = new ArrayList<>();

        static {
            DISPATCH.addLongColumn("id", "holder_id", Holder::getId, Holder::setId);
            DISPATCH.addObjectProperty("children", self -> {
                IDS_SEEN_BY_GETTER.add(self.getId());
                return Janitor.list(java.util.stream.Stream.of(Janitor.string("child of " + self.getId())));
            });
        }

        private long id;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return null; }
        @Override public void setKey(final String key) { }
        @Override public String getName() { return null; }
        @Override public void setName(final String name) { }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    @Test
    void readOnlyAttributesAreNotEvaluatedOnTheCopy() {
        final Holder original = new Holder();
        original.setId(42);
        Holder.IDS_SEEN_BY_GETTER.clear();
        final Holder copy = Holder.DISPATCH.duplicate(new EntityDispatchTableTestCase.TestCollection(), original);
        assertEquals(42, copy.getId());
        assertEquals(List.of(), Holder.IDS_SEEN_BY_GETTER, "a read-only getter may load by id; on a copy it must wait until the copy is used");
    }
}
