package com.eischet.janitor.standalone.repl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplCommandLineTestCase {

    @Test
    void noArgumentsStartsTheReplWithNoOptions() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[0]);
        assertNull(cli.getError());
        assertFalse(cli.isHelpRequested());
        assertFalse(cli.isVerbose());
        assertFalse(cli.isPlain());
        assertNull(cli.getScriptFile());
        assertTrue(cli.getScriptArgs().isEmpty());
    }

    @Test
    void aBareFileNameIsTheScriptToRun() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"foo.janitor"});
        assertNull(cli.getError());
        assertEquals("foo.janitor", cli.getScriptFile());
        assertTrue(cli.getScriptArgs().isEmpty());
    }

    @Test
    void optionsBeforeTheScriptFileAreConsumedByTheLauncher() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-v", "-p", "foo.janitor"});
        assertNull(cli.getError());
        assertTrue(cli.isVerbose());
        assertTrue(cli.isPlain());
        assertEquals("foo.janitor", cli.getScriptFile());
        assertTrue(cli.getScriptArgs().isEmpty());
    }

    @Test
    void argumentsAfterTheScriptFileGoToTheScriptEvenIfTheyLookLikeOptions() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"foo.janitor", "-v", "bar", "--baz"});
        assertNull(cli.getError());
        assertEquals("foo.janitor", cli.getScriptFile());
        // note: -v before the script file would have set verbose; after it, it must NOT be consumed as an option.
        assertFalse(cli.isVerbose());
        assertEquals(List.of("-v", "bar", "--baz"), cli.getScriptArgs());
    }

    @Test
    void anUnrecognizedOptionBeforeTheScriptFileIsAnError() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-x", "foo.janitor"});
        assertNull(cli.getScriptFile());
        assertTrue(cli.getError() != null && cli.getError().contains("-x"));
    }

    @Test
    void helpFlagIsRecognized() {
        assertTrue(ReplCommandLine.parse(new String[]{"-h"}).isHelpRequested());
        assertTrue(ReplCommandLine.parse(new String[]{"--help"}).isHelpRequested());
    }

    @Test
    void aScriptFileNamedLikeAnOptionIsStillJustAFileNameWhenPassedAfterTheRealScript() {
        // e.g. "janitor build.janitor -p" -- the "-p" here is an argument to build.janitor, not the launcher's plain-console flag.
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"build.janitor", "-p"});
        assertEquals("build.janitor", cli.getScriptFile());
        assertFalse(cli.isPlain());
        assertEquals(List.of("-p"), cli.getScriptArgs());
    }

    @Test
    void interactiveFlagIsRecognizedBeforeTheScriptFile() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-i", "foo.janitor"});
        assertNull(cli.getError());
        assertTrue(cli.isInteractive());
        assertEquals("foo.janitor", cli.getScriptFile());
    }

    @Test
    void interactiveFlagCanBeCombinedWithOtherOptions() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-v", "-i", "foo.janitor", "a"});
        assertNull(cli.getError());
        assertTrue(cli.isVerbose());
        assertTrue(cli.isInteractive());
        assertEquals("foo.janitor", cli.getScriptFile());
        assertEquals(List.of("a"), cli.getScriptArgs());
    }

    @Test
    void interactiveFlagAfterTheScriptFileGoesToTheScriptInstead() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"foo.janitor", "-i"});
        assertFalse(cli.isInteractive());
        assertEquals(List.of("-i"), cli.getScriptArgs());
    }

    @Test
    void quietFlagIsRecognized() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-q"});
        assertNull(cli.getError());
        assertTrue(cli.isQuiet());
        assertNull(cli.getScriptFile());
    }

    @Test
    void quietFlagAfterTheScriptFileGoesToTheScriptInstead() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"foo.janitor", "-q"});
        assertFalse(cli.isQuiet());
        assertEquals(List.of("-q"), cli.getScriptArgs());
    }

    @Test
    void versionFlagIsRecognized() {
        assertTrue(ReplCommandLine.parse(new String[]{"-V"}).isVersionRequested());
        assertTrue(ReplCommandLine.parse(new String[]{"--version"}).isVersionRequested());
        // "-v" (lowercase, verbose) must not be confused with "-V" (uppercase, version):
        final ReplCommandLine lowercaseV = ReplCommandLine.parse(new String[]{"-v"});
        assertTrue(lowercaseV.isVerbose());
        assertFalse(lowercaseV.isVersionRequested());
    }

    @Test
    void allNewFlagsCanBeCombined() {
        final ReplCommandLine cli = ReplCommandLine.parse(new String[]{"-i", "-q", "-v", "foo.janitor"});
        assertNull(cli.getError());
        assertTrue(cli.isInteractive());
        assertTrue(cli.isQuiet());
        assertTrue(cli.isVerbose());
        assertEquals("foo.janitor", cli.getScriptFile());
    }

}
