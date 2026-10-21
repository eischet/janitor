// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.files;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.modules.files.FilesModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Zip.addText(text, entryName, encoding) uses the encoding from the third argument, UTF-8 without one. It used to read the
 * encoding from a fourth argument, so the given encoding was ignored.
 */
public class ZipAddTextTestCase extends JanitorTest {

    /** a-umlaut, built from its code so that no tool can change the source text */
    private static final String AE = String.valueOf((char) 0xE4);

    private static void run(final String source, final Path zip) throws JanitorCompilerException, JanitorRuntimeException {
        OutputCatchingTestRuntime.fresh(env -> env.addModule(FilesModule.REGISTRATION))
                .compile("test", source)
                .run(globals -> globals.bind("path", zip.toString()));
    }

    private static byte[] entry(final Path zip, final String name) throws Exception {
        try (ZipFile file = new ZipFile(zip.toFile())) {
            return file.getInputStream(file.getEntry(name)).readAllBytes();
        }
    }

    @Test
    void encodingIsTheThirdArgument(@TempDir final Path dir) throws Exception {
        final Path zip = dir.resolve("texts.zip");
        run("import files; z = files.Zip(path); z.addText(\"" + AE + "\", \"latin1.txt\", \"ISO-8859-1\"); z.addText(\"" + AE + "\", \"utf8.txt\"); z.close();", zip);
        assertArrayEquals(new byte[]{(byte) 0xE4}, entry(zip, "latin1.txt"));
        assertArrayEquals(new byte[]{(byte) 0xC3, (byte) 0xA4}, entry(zip, "utf8.txt"));
    }

    @Test
    void numberOfArguments(@TempDir final Path dir) {
        final Path zip = dir.resolve("args.zip");
        assertThrows(JanitorArgumentException.class, () -> run("import files; z = files.Zip(path); try { z.addText(\"x\"); } finally { z.close(); }", zip));
        assertThrows(JanitorArgumentException.class,
                () -> run("import files; z = files.Zip(path); try { z.addText(\"x\", \"x.txt\", \"UTF-8\", 1); } finally { z.close(); }", zip));
    }
}
