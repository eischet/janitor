package com.eischet.janitor.mods.files;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.modules.files.FilesModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * End-to-end regression test for ZipFile's automatic cleanup wiring: a script that creates a
 * files.Zip(...), adds an entry, and never calls close() itself (forgotten, or because it threw
 * partway through) used to leave the underlying FileOutputStream/ZipOutputStream open until GC
 * finalized them. ZipFile now implements JanitorCleanupRequired, so binding it to a script variable
 * registers it with the process (see Scope.setVariable() -> process.registerCleanable()), and
 * RunningScriptProcess.run()'s finally block finalizes the archive automatically -- synchronously,
 * before run() returns -- once the whole script process ends, with no explicit close() needed.
 */
public class FilesZipAutoCleanupTestCase extends JanitorTest {

    @Test
    @EnabledOnOs(OS.WINDOWS) // the "is the file still open" check below relies on Windows file locking
    public void zipIsAutomaticallyClosedWhenTheScriptNeverCallsClose(@TempDir final Path tempDir) throws JanitorCompilerException, JanitorRuntimeException, Exception {
        final Path source = tempDir.resolve("payload.txt");
        Files.writeString(source, "hello, zip");
        final Path zipPath = tempDir.resolve("out.zip");

        @Language("Janitor") final String script = """
                import files;
                zip = files.Zip(zipPath);
                zip.addFile(sourcePath, "payload.txt");
                return zip;
                // deliberately no zip.close() call
                """;
        evaluate(script,
                env -> env.addModule(FilesModule.REGISTRATION),
                globals -> {
                    globals.bind("zipPath", zipPath.toString());
                    globals.bind("sourcePath", source.toString());
                });

        // If the script-level auto-cleanup didn't run, Windows would still hold the zip file's
        // FileOutputStream open and refuse to delete it.
        assertDoesNotThrow(() -> Files.delete(zipPath), "the zip file must have been closed automatically when the script process ended");
    }

}
