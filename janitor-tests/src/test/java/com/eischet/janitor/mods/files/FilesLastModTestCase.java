// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.files;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.modules.files.FilesModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * files.lastmod(path) returns the time of the last change in the host's time zone, or null for a missing file. It used to
 * read the milliseconds of File.lastModified() as seconds.
 */
public class FilesLastModTestCase extends JanitorTest {

    private static JanitorObject lastmod(final Path path) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh(env -> env.addModule(FilesModule.REGISTRATION))
                .compile("test", "import files; files.lastmod(path)")
                .run(globals -> globals.bind("path", path.toString()));
    }

    @Test
    void timeOfTheLastChange(@TempDir final Path dir) throws Exception {
        final Path file = dir.resolve("example.txt");
        Files.writeString(file, "x");
        final long millis = Instant.parse("2024-03-05T13:30:15Z").toEpochMilli(); // whole seconds: not every file system stores milliseconds
        assertTrue(file.toFile().setLastModified(millis));
        final JanitorObject result = lastmod(file);
        assertInstanceOf(JDateTime.class, result);
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DateTimeUtils.getZoneId()), ((JDateTime) result).janitorGetHostValue());
    }

    @Test
    void missingFileGivesNull(@TempDir final Path dir) throws Exception {
        final Path missing = dir.resolve("missing.txt");
        assertFalse(new File(missing.toString()).exists());
        assertSame(JNull.NULL, lastmod(missing));
    }
}
