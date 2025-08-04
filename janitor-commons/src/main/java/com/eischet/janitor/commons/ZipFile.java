package com.eischet.janitor.commons;

import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile extends JanitorComposed<ZipFile> {

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

    public void close() throws IOException {
        zos.close();
        fos.close();
    }

    public void addFile(String filePath, String zipEntryName) throws IOException {
        final File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(zipEntryName == null ? file.getName() : zipEntryName);
        zos.putNextEntry(zipEntry);
        fis.transferTo(zos);
        zos.closeEntry();
        fis.close();
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
