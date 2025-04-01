package com.eischet.janitor.commons;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JVoidMethod;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile extends JanitorComposed<ZipFile> {

    private static final DispatchTable<ZipFile> dispatcher = new DispatchTable<>();

    static {
        dispatcher.addVoidMethod("addFile", (self, process, arguments) -> {
            final String filePath = arguments.getString(0).janitorGetHostValue();
            final String zipEntryName = arguments.getOptionalStringValue(1, null);
            try {
                self.addFile(filePath, zipEntryName);
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error adding file to zip " + self.filename, e);
            }
        });
        dispatcher.addVoidMethod("close", (self, process, arguments) -> {
            try {
                self.close();
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error closing zip file " + self.filename, e);
            }
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



}
