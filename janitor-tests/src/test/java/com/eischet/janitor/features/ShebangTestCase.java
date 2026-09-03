package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A '#' starting the very first line of a script is ignored, like a Unix shebang line, so scripts can be
 * made directly executable (e.g. "#!/usr/bin/env janitor"). See Janitor.g4's SHEBANG lexer rule.
 */
public class ShebangTestCase extends JanitorTest {

    @Test
    void aLeadingShebangLineIsIgnored() throws JanitorRuntimeException, JanitorCompilerException {
        final JanitorObject result = evaluate("""
                #!/usr/bin/env janitor
                return 1 + 1;
                """);
        assertEquals("2", result.janitorToString());
    }

    @Test
    void anyHashPrefixedFirstLineIsIgnoredNotJustAProperShebang() throws JanitorRuntimeException, JanitorCompilerException {
        final JanitorObject result = evaluate("""
                # just some leading hash-prefixed line, not necessarily "#!"
                return 42;
                """);
        assertEquals("42", result.janitorToString());
    }

    @Test
    void aShebangOnItsOwnWithNoFollowingCodeCompilesToAnEmptyScript() throws JanitorRuntimeException, JanitorCompilerException {
        final JanitorObject result = evaluate("#!/usr/bin/env janitor\n");
        assertEquals("null", result.janitorToString());
    }

    @Test
    void hashIsStillNotAllowedAnywhereButTheFirstLine() {
        // '#' has no meaning at all outside of that first-line special case -- confirms the lexer
        // predicate is correctly scoped to line 1, not a general end-of-line comment character.
        // (The compiler recovers from the unrecognized '#' token rather than failing outright, so this
        // surfaces as a runtime error from the garbled resulting statement, not a JanitorCompilerException.)
        assertThrows(Exception.class, () -> evaluate("""
                x = 1;
                # not a shebang, this is line 2
                return x;
                """));
    }

}
