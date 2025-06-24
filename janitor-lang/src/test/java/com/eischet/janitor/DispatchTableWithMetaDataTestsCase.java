package com.eischet.janitor;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DispatchTableWithMetaDataTestsCase {

    private static final MetaDataKey<Integer> MAX_LENGTH = new MetaDataKey<>("max_length", Integer.class);

    private static final MetaDataKey<String> COLUMN_NAME = new MetaDataKey<>("column_name", String.class);


    public static class SomeObject extends JanitorComposed<SomeObject> {

        public static final DispatchTable<SomeObject> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addStringProperty("name", SomeObject::getName, SomeObject::setName).setMetaData(MAX_LENGTH, 10);
            DISPATCH.addLongProperty("width", SomeObject::getWidth, SomeObject::setWidth).setMetaData(COLUMN_NAME, "parent_col");
            DISPATCH.addBooleanProperty("enabled", SomeObject::isEnabled, SomeObject::setEnabled);
        }

        private String name;
        private long width;
        private boolean enabled;

        public SomeObject() {
            super(DISPATCH);
        }

        public SomeObject(final Dispatcher<SomeObject> childDispatch) {
            super(childDispatch);
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getWidth() {
            return width;
        }

        public void setWidth(final long width) {
            this.width = width;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class SomeChildObject extends SomeObject {
        public static final DispatchTable<SomeChildObject> DISPATCH = new DispatchTable<>(SomeObject.DISPATCH, it -> it);

        static {
            DISPATCH.addStringProperty("nickname", SomeChildObject::getNickname, SomeChildObject::setNickname);
            DISPATCH.override("width").setMetaData(COLUMN_NAME, "child_col");
        }

        public SomeChildObject() {
            super(Dispatcher.inherit(SomeObject.DISPATCH, DISPATCH));
        }

        public String nickname;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(final String nickname) {
            this.nickname = nickname;
        }
    }

    @Test
    void checkMetaDataMechanics() {
        final var so = new SomeObject();
        final var soc = new SomeChildObject();
        // Check that the MAX_LENGTH entry is available on the parent and child classes
        assertEquals(10, SomeObject.DISPATCH.getMetaData("name", MAX_LENGTH));
        assertEquals(10, so.getDispatcher().getMetaData("name", MAX_LENGTH));
        assertEquals(10, soc.getDispatcher().getMetaData("name", MAX_LENGTH));
        // Check that the COLUMN_NAME entry is available AND DIFFERENT on parent and child
        assertEquals("parent_col", so.getDispatcher().getMetaData("width", COLUMN_NAME));
        assertEquals("child_col", soc.getDispatcher().getMetaData("width", COLUMN_NAME));


    }

    /**
     * Prove that JSON writing takes into account both child and parent fields.
     * <p>
     * It's safe to use JSON strings here, because the order of fields in the generated JSON is predetermined by how the dispatch table is constructed.
     * It would be a wise idea not to depend on that kind of behaviour too much, i.e. outside of unit tests.
     * </p>
     * @throws JsonException on errors
     */
    @Test
    void checkJsonWriting() throws JsonException {
        final var soc = new SomeChildObject();
        assertEquals("{}", soc.toJson(TestEnv.env));
        soc.setNickname("nicky");
        assertEquals("{\"nickname\":\"nicky\"}", soc.toJson(TestEnv.env));
        soc.setNickname(null);
        assertEquals("{}", soc.toJson(TestEnv.env));
        soc.setName("Nicholas");
        assertEquals("{\"name\":\"Nicholas\"}", soc.toJson(TestEnv.env));
        soc.setWidth(10);
        assertEquals("{\"name\":\"Nicholas\",\"width\":10}", soc.toJson(TestEnv.env));
        soc.setNickname("nicky");
        assertEquals("{\"name\":\"Nicholas\",\"width\":10,\"nickname\":\"nicky\"}", soc.toJson(TestEnv.env));
    }


}
