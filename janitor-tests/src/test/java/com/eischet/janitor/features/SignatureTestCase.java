// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A native method that is described by a {@link Signature} gets its HELP text and parameter metadata from it, and its arguments
 * are bound by the signature, by the same rules as for script functions, before the method runs.
 */
public class SignatureTestCase extends JanitorTest {

    private static final Signature WRITE = Signature.method("write")
            .param("path", ScriptType.STRING)
            .param("text", ScriptType.STRING)
            .optional("charset", ScriptType.STRING, "UTF-8")
            .optional("append", ScriptType.BOOL, false)
            .optional("retries", ScriptType.INT, 3)
            .returns(ScriptType.STRING)
            .help("Writes the text.");

    private static final Signature JOIN = Signature.method("join")
            .param("first", ScriptType.ANY)
            .rest("more", ScriptType.ANY)
            .options("format")
            .help("Joins things.");

    private static final class Probe extends JanitorComposed<Probe> {
        public static final DispatchTable<Probe> DISPATCH = new DispatchTable<>();
        private int calls = 0;

        static {
            DISPATCH.addMethod(WRITE, (self, process, args) -> {
                self.calls++;
                return Janitor.string(args.requiredString("path") + "|" + args.string("text") + "|" + args.string("charset")
                                      + "|" + args.bool("append") + "|" + args.integer("retries") + "|" + args.has("charset"));
            });
            DISPATCH.addMethod(JOIN, (self, process, args) -> Janitor.string(args.get("first") + " " + args.rest("more") + " " + args.options("format")));
        }

        private Probe() {
            super(DISPATCH);
        }
    }

    private String call(final Probe probe, final @Language("Janitor") String script) throws Exception {
        return evaluate(script, g -> g.bind("probe", probe)).janitorToString();
    }

    private String argumentError(final Probe probe, final @Language("Janitor") String script) {
        return assertThrows(JanitorArgumentException.class, () -> evaluate(script, g -> g.bind("probe", probe))).getText();
    }

    @Test
    void theSignatureWritesTheHelpTextAndTheMetadata() {
        assertEquals("write(path, text, charset = \"UTF-8\", append = false, retries = 3): Writes the text.",
                Probe.DISPATCH.getMetaData("write", Janitor.MetaData.HELP));
        assertEquals(List.of("path", "text", "charset", "append", "retries"),
                Probe.DISPATCH.getMetaData("write", Janitor.MetaData.PARAMETERS).stream().map(ScriptType.Parameter::name).toList());
        assertEquals(ScriptType.STRING, Probe.DISPATCH.getMetaData("write", Janitor.MetaData.RETURNS));
        assertEquals("join(first, *more, **format): Joins things.", Probe.DISPATCH.getMetaData("join", Janitor.MetaData.HELP));
    }

    @Test
    void argumentsAreBoundByPositionOrNameWithDefaults() throws Exception {
        final Probe probe = new Probe();
        assertEquals("a|b|UTF-8|false|3|false", call(probe, "return probe.write('a', 'b');"));
        assertEquals("a|b|latin1|true|3|true", call(probe, "return probe.write('a', 'b', 'latin1', true);"));
        assertEquals("a|b|UTF-8|true|5|false", call(probe, "return probe.write(text = 'b', path = 'a', retries = 5, append = true);"));
        assertEquals("1 [2, 3] {sep=-}", call(probe, "return probe.join(1, 2, 3, sep = '-');"));
        assertEquals("1 [] {}", call(probe, "return probe.join(first = 1);"));
    }

    @Test
    void wrongCallsFailBeforeTheMethodRuns() {
        final Probe probe = new Probe();
        assertTrue(argumentError(probe, "probe.write('a', 'b', charst = 'x');").contains("unexpected named argument 'charst'"));
        assertTrue(argumentError(probe, "probe.write('a');").contains("missing argument 'text'"));
        assertTrue(argumentError(probe, "probe.write('a', 'b', 'c', true, 1, 2);").contains("at most 5"));
        assertTrue(argumentError(probe, "probe.write('a', 'b', path = 'c');").contains("more than one value for 'path'"));
        assertEquals(0, probe.calls);
    }

    @Test
    void valuesOfTheWrongTypeNameTheParameter() {
        final Probe probe = new Probe();
        assertTrue(argumentError(probe, "probe.write('a', 'b', append = 'yes');").contains("argument 'append' must be a boolean"));
        assertTrue(argumentError(probe, "probe.write(null, 'b');").contains("argument 'path' must be a string"));
    }

    @Test
    void anInvalidSignatureFailsWhenItIsBuilt() {
        assertThrows(IllegalArgumentException.class, () -> Signature.method("f").optional("a", ScriptType.ANY, 1).param("b", ScriptType.ANY).help("."));
        assertThrows(IllegalArgumentException.class, () -> Signature.method("f").param("a", ScriptType.ANY).param("a", ScriptType.ANY).help("."));
        assertThrows(IllegalArgumentException.class, () -> Signature.method("f").optional("a", ScriptType.ANY, List.of()).help("."));
    }
}
