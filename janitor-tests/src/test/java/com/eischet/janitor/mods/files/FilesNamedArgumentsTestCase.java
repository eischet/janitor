// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.files;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.files.FilesModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The methods of the files module take their arguments by position or by name, and the charset defaults to UTF-8.
 */
public class FilesNamedArgumentsTestCase extends JanitorTest {

    private static JanitorObject run(final String script, final Path file) throws Exception {
        return OutputCatchingTestRuntime.fresh(env -> env.addModule(FilesModule.REGISTRATION))
                .compileAndRun("test", "import files; " + script, globals -> globals.bind("path", file.toString()));
    }

    @Test
    void theCharsetCanBeGivenByName(@TempDir final Path dir) throws Exception {
        final Path file = dir.resolve("text.txt");
        run("files.write(path, 'Grüße', charset = 'ISO-8859-1');", file);
        assertArrayEquals("Grüße".getBytes(StandardCharsets.ISO_8859_1), Files.readAllBytes(file));
        assertEquals("Grüße", run("return files.read(charset = 'ISO-8859-1', path = path);", file).janitorToString());
        run("files.write(path, 'Grüße');", file);
        assertArrayEquals("Grüße".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(file));
    }

    @Test
    void aMisspelledNameFailsBeforeAnythingIsWritten(@TempDir final Path dir) {
        final Path file = dir.resolve("text.txt");
        final JanitorArgumentException e = assertThrows(JanitorArgumentException.class, () -> run("files.write(path, 'x', charst = 'UTF-8');", file));
        assertTrue(e.getText().contains("unexpected named argument 'charst'"), e.getText());
        assertFalse(Files.exists(file));
    }
}
