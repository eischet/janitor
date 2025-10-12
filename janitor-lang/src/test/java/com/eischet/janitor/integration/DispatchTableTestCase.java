package com.eischet.janitor.integration;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.TestEnv;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DispatchTableTestCase extends JanitorTest {

    /**
     * Java Booleans might be null, and need to be mapped differently than booleans in Janitor.
     * Test that Boolean values, including null, are mapped properly
     *
     * @throws JanitorRuntimeException on errors
     */
    @Test
    public void nullableBoolean() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final SimpleObject so = new SimpleObject();

        /*
         * simplify script execution for the rest of the test.
         */
        final TestEnv.ScriptConsumer play = (script) -> {
            try {
                final RunnableScript runnableScript = rt.compile("test", script);
                runnableScript.run(g -> g.bind("obj", so));
            } catch (JanitorCompilerException e) {
                throw new RuntimeException(e); // RuntimeConsumers allow Runtime Errors, but not compile errors, so let's just convert
            }
        };

        // Script --> Java:
        assertNull(so.frobnicate); // regular Java default
        play.accept("obj.frobnicate = false;");
        assertEquals(Boolean.FALSE, so.getFrobnicate());
        play.accept("obj.frobnicate = true;");
        assertEquals(Boolean.TRUE, so.getFrobnicate());
        play.accept("obj.frobnicate = null;");
        assertNull(so.frobnicate);

        // Java --> Script:
        so.setFrobnicate(false);
        play.accept("assert(obj.frobnicate == false);");
        play.accept("assert(not obj.frobnicate);");
        so.setFrobnicate(true);
        play.accept("assert(obj.frobnicate == true);");
        play.accept("assert(not not obj.frobnicate);");
        so.setFrobnicate(null);
        play.accept("assert(obj.frobnicate == null);");
        play.accept("assert(not obj.frobnicate);");
        play.accept("assert(obj.frobnicate != false);");
        play.accept("assert(obj.frobnicate != true);");
    }

    /**
     * A simple test object with a dispatch table.
     */
    private static class SimpleObject extends JanitorComposed<SimpleObject> {

        private static final DispatchTable<SimpleObject> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addNullableBooleanProperty("frobnicate", SimpleObject::getFrobnicate, SimpleObject::setFrobnicate);
            DISPATCH.addStringProperty("foo", SimpleObject::getFoo, SimpleObject::setFoo);
        }

        private Boolean frobnicate;
        private String foo;

        public SimpleObject() {
            super(DISPATCH);
        }

        public Boolean getFrobnicate() {
            return frobnicate;
        }

        public void setFrobnicate(final Boolean frobnicate) {
            this.frobnicate = frobnicate;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(final String foo) {
            this.foo = foo;
        }
    }

    @Test
    void invalidAssignment() throws JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final SimpleObject so = new SimpleObject();

        /*
         * simplify script execution for the rest of the test.
         */
        final TestEnv.ScriptConsumer play = (script) -> {
            try {
                final RunnableScript runnableScript = rt.compile("test", script);
                runnableScript.run(g -> g.bind("obj", so));
            } catch (JanitorCompilerException e) {
                throw new RuntimeException(e); // RuntimeConsumers allow Runtime Errors, but not compile errors, so let's just convert
            }
        };

        assertThrows(JanitorArgumentException.class, () -> play.accept("obj.foo = 17;"));

    }

}
