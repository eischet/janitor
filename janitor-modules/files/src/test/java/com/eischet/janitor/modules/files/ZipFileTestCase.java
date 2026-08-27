package com.eischet.janitor.modules.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression test for the addFile() FileInputStream leak: there was no try-with-resources/finally
 * around the source file's FileInputStream, so if zos.putNextEntry() (e.g. a duplicate entry name) or
 * fis.transferTo() threw, fis.close() was never reached. Since scripts can call addFile() in a loop,
 * this was a realistic path to file-handle exhaustion.
 * <p>
 * This is verified indirectly via a Windows-specific file-locking effect: while a FileInputStream on a
 * file is still open, Windows refuses to delete that file. Triggering the leaking exception path and
 * then immediately trying to delete the source file distinguishes "fis leaked" (delete fails) from
 * "fis was closed" (delete succeeds) -- there's no portable, non-flaky way to directly observe an open
 * file descriptor from within the JVM, so this test only runs on Windows.
 */
public class ZipFileTestCase {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void addFileClosesTheSourceStreamEvenWhenPutNextEntryThrows(@TempDir final Path tempDir) throws Exception {
        final Path payload = tempDir.resolve("payload.txt");
        Files.writeString(payload, "hello");
        final Path zipPath = tempDir.resolve("out.zip");

        final ZipFile zip = new ZipFile(zipPath.toString());
        try {
            zip.addFile(payload.toString(), "entry.txt");
            // Adding the same entry name again makes putNextEntry() throw (duplicate entry) -- the
            // exact path that used to leak the FileInputStream, since fis.close() sat after
            // putNextEntry()/transferTo()/closeEntry() instead of in a finally/try-with-resources.
            assertThrows(ZipException.class, () -> zip.addFile(payload.toString(), "entry.txt"));

            // If the FileInputStream from the failed addFile() call above leaked, Windows still holds
            // an open handle on payload.txt and refuses to delete it.
            assertDoesNotThrow(() -> Files.delete(payload), "payload.txt's FileInputStream must have been closed, even though addFile() threw");
        } finally {
            zip.close();
        }
    }

    @Test
    void closeCanBeCalledMoreThanOnceWithoutThrowing(@TempDir final Path tempDir) throws Exception {
        // janitorCleanup() (see below) may call close() after a script already called it explicitly,
        // or vice versa -- both must be harmless no-ops the second time around.
        final ZipFile zip = new ZipFile(tempDir.resolve("out.zip").toString());
        zip.close();
        assertDoesNotThrow(zip::close);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void janitorCleanupClosesTheUnderlyingStreams(@TempDir final Path tempDir) throws Exception {
        // Regression test for ZipFile now implementing JanitorCleanupRequired: a script that forgets
        // to call close() (or throws before reaching it) must still have its zip file's
        // FileOutputStream/ZipOutputStream closed automatically -- see janitorCleanup()'s Javadoc for
        // why this is tied to process-end cleanup rather than ordinary scope-leave timing.
        final Path zipPath = tempDir.resolve("out.zip");
        final ZipFile zip = new ZipFile(zipPath.toString());
        zip.addText("hello", "entry.txt", null);

        zip.janitorCleanup(); // no explicit close() call

        // If the FileOutputStream were left open, Windows would refuse to delete out.zip.
        assertDoesNotThrow(() -> Files.delete(zipPath), "janitorCleanup() must close the underlying streams");
    }

}
