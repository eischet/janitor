package com.eischet.janitor.modules.files;

import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.logging.JanitorLogger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile extends JanitorComposed<ZipFile> implements JanitorCleanupRequired {

    private static final JanitorLogger log = JanitorLogger.getLogger(ZipFile.class);

    private static final DispatchTable<ZipFile> dispatcher = new DispatchTable<>(null);

    static {
        dispatcher.addMethod("addFile", (self, process, arguments) -> {
            final String filePath = arguments.getString(0).janitorGetHostValue();
            final String zipEntryName = arguments.getOptionalStringValue(1, null);
            try {
                self.addFile(filePath, zipEntryName);
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error adding file to zip " + self.filename, e);
            }
            return self;
        });
        dispatcher.addMethod("addText", (self, process, arguments) -> {
            final String text = arguments.getRequiredStringValue(0);
            final String zipEntryName = arguments.getRequiredStringValue(1);
            final String encoding = arguments.getOptionalStringValue(3, null);
            try {
                self.addText(text, zipEntryName, encoding);
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error adding file to zip " + self.filename, e);
            }
            return self;
        });
        dispatcher.addMethod("addBinary", (self, process, arguments) -> {
            final JBinary bytes = arguments.getRequired(0, JBinary.class);
            final String zipEntryName = arguments.getRequiredStringValue(1);
            try {
                self.addBinary(bytes.janitorGetHostValue(), zipEntryName);
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error adding file to zip " + self.filename, e);
            }
            return self;
        });

        dispatcher.addVoidMethod("close", (self, process, arguments) -> {
            try {
                self.close();
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error closing zip file " + self.filename, e);
            }
        });

        dispatcher.addMethod("setLevel", (self, process, arguments) -> {
            self.zos.setLevel(arguments.getRequiredIntValue(0));
            return self;
        });

        dispatcher.addMethod("setMethod", (self, process, arguments) -> {
            self.zos.setMethod(arguments.getRequiredIntValue(0));
            // LATER: we should map levels from string or something like that!
            return self;
        });

        dispatcher.addMethod("setComment", (self, process, arguments) -> {
            self.zos.setComment(arguments.getRequiredStringValue(0));
            return self;
        });

    }

    private final String filename;
    private final FileOutputStream fos;
    private final ZipOutputStream zos;

    public ZipFile(final String filename) throws FileNotFoundException {
        super(dispatcher);
        this.filename = filename;
        this.fos = new FileOutputStream(filename);
        this.zos = new ZipOutputStream(fos);
    }

    /**
     * Closes the underlying streams, finalizing the zip file's central directory. Safe to call more
     * than once: both {@link ZipOutputStream#close()} and {@link FileOutputStream#close()} already
     * no-op on a stream that's already closed, so a script calling this explicitly and then letting
     * {@link #janitorCleanup()} call it again at process end (or vice versa) is not an error.
     * <p>
     * Unlike a self-healing resource (e.g. the httpclient module's JanitorHttpClient), a ZipFile
     * cannot be reopened after this: putNextEntry()/write() on an already-finalized ZipOutputStream
     * throws. Callers must be done adding entries before calling close().
     */
    public void close() throws IOException {
        zos.close();
        fos.close();
    }

    /**
     * Safety net for scripts that forget to call close(), or that throw an exception partway through
     * building the zip (which would otherwise leak the underlying FileOutputStream/ZipOutputStream
     * until GC finalizes them). This runs exactly once, at the true end of the whole script process
     * (see AbstractScriptProcess.processCleanups(), invoked from RunningScriptProcess.run()'s
     * finally block) -- deliberately not tied to ordinary scope-leave timing (janitorLeaveScope()),
     * which can fire earlier than the script's actual intent (e.g. on variable rebinding, or a nested
     * block scope exiting) and, since a ZipFile can't self-heal after being closed, would risk
     * finalizing the archive while the script still meant to keep adding entries to it.
     */
    @Override
    public void janitorCleanup() {
        try {
            close();
        } catch (IOException e) {
            log.warn("error auto-closing zip file {} during process cleanup", filename, e);
        }
    }

    public void addFile(String filePath, String zipEntryName) throws IOException {
        final File file = new File(filePath);
        final ZipEntry zipEntry = new ZipEntry(zipEntryName == null ? file.getName() : zipEntryName);
        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(zipEntry);
            fis.transferTo(zos);
            zos.closeEntry();
        }
    }

    public void addText(String text, String zipEntryName, @Nullable String encoding) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zos.putNextEntry(zipEntry);
        zos.write(text.getBytes(encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding)));
        zos.closeEntry();
    }

    public void addBinary(byte[] bytes, String zipEntryName) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zos.putNextEntry(zipEntry);
        zos.write(bytes);
        zos.closeEntry();
    }



}
