package com.eischet.janitor;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.env.JStringClass;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaDataTestCase extends JanitorTest {

    @Test
    void checkStringClassDocstring() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        rt.compile("test", "print(help(''));").run();
        assertEquals(JStringClass.STRING_CLASS + "\n", rt.getAllOutput());
    }

    @Test
    void checkStringLengthDocstring() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        rt.compile("test", "print(help(''.length));").run();
        assertEquals(JStringClass.STRING_LENGTH + "\n", rt.getAllOutput());
    }

    static class Simpleton extends JanitorComposed<Simpleton> {
        private static final DispatchTable<Simpleton> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.setMetaData(Janitor.MetaData.NAME, "Simpleton");
            DISPATCH.setMetaData(Janitor.MetaData.HELP, "a very simple type of class");
            DISPATCH.addBooleanProperty("stoopid", it -> true)
                    .setMetaData(Janitor.MetaData.HELP, "this is beyond help");
        }

        public Simpleton() {
            super(DISPATCH);
        }
    }

    /**
     * Another function blatantly stolen from the Python language: dir(object).
     * @throws JanitorCompilerException when it feels like
     * @throws JanitorRuntimeException when it fells like
     */
    @Test
    void checkDir() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        rt.compile("test", "print(dir(simpy));")
                .run(g -> g.bind("simpy", new Simpleton()));
        assertEquals("[stoopid]\n", rt.getAllOutput());
    }

    /* this cannot work right now, because simply.stoopid will return false, which does not
       have the meta-data that is present on the property. Figure out how to do this properly,
       or alternatively decide not to do it.
       One thing I do NOT want to do is manipulate the AST when "help" is in the call tree,
       for example, because that would be despicable.
    @Test
    void checkPropertyAttributes() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        rt.compile("test", "print(help(simpy.stoopid));")
                .run(g -> g.bind("simpy", new Simpleton()));
        assertEquals("[stoopid]\n", rt.getAllOutput());
    }
     */


}
