package com.eischet.janitor.commons;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.runtime.DateTimeUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class FilesModule extends JanitorComposed<FilesModule> implements JanitorModule {

    private static final DispatchTable<FilesModule> dispatcher = new DispatchTable<>();
    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("files", FilesModule::new);

    static {
        dispatcher.addMethod("exists", FilesModule::fileExists);
        dispatcher.addMethod("write", FilesModule::writeString);
        dispatcher.addMethod("read", FilesModule::readString);
        dispatcher.addMethod("writeBinary", FilesModule::writeBinary);
        dispatcher.addMethod("readBinary", FilesModule::readBinary);
        dispatcher.addMethod("list", FilesModule::list);
        dispatcher.addMethod("mkdirs", FilesModule::mkdir);
        dispatcher.addMethod("normalize", FilesModule::normalize);
        dispatcher.addMethod("lastmod", FilesModule::lastMod);

        dispatcher.addMethod("delete", FilesModule::delete);
        dispatcher.addVoidMethod("move", FilesModule::move);
        dispatcher.addVoidMethod("copy", FilesModule::copy);

        dispatcher.addMethod("Zip", FilesModule::zip);

    }

    private JanitorObject zip(JanitorScriptProcess process, JCallArgs args) throws JanitorRuntimeException {
        final String zipFilename = args.getRequiredStringValue(0);
        try {
            return new ZipFile(zipFilename);
        } catch (Exception e) {
            throw new JanitorNativeException(process, "error creating zip file " + zipFilename, e);
        }
    }


    public FilesModule() {
        super(dispatcher);
    }

    public JBool fileExists(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final File f = new File(arguments.require(1).getString(0).janitorGetHostValue());
        return Janitor.toBool(f.exists());
    }

    public JNull writeString(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            Files.writeString(
                    Path.of(arguments.require(2, 3).getString(0).janitorGetHostValue()),
                    arguments.getString(1).janitorGetHostValue(),
                    Charset.forName(arguments.getOptionalStringValue(2, "UTF-8"))
            );
            return JNull.NULL;
        } catch (IOException e) {
            throw new JanitorNativeException(runningScript, "error writing to file", e);
        }
    }

    public JanitorObject readString(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return runningScript.getBuiltins().nullableString(
                    Files.readString(
                            Path.of(arguments.require(1, 2).getString(0).janitorGetHostValue()),
                            Charset.forName(arguments.getOptionalStringValue(2, "UTF-8"))
                    )
            );
        } catch (IOException e) {
            throw new JanitorNativeException(runningScript, "error reading file", e);
        }
    }

    public JNull writeBinary(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            Files.write(
                    Path.of(arguments.require(2).getString(0).janitorGetHostValue()),
                    arguments.getRequired(1, JBinary.class).janitorGetHostValue()
            );
            return JNull.NULL;
        } catch (IOException e) {
            throw new JanitorNativeException(runningScript, "error writing to file", e);
        }
    }

    public JanitorObject readBinary(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return runningScript.getBuiltins().binary(
                    Files.readAllBytes(
                            Path.of(arguments.require(1).getString(0).janitorGetHostValue())
                    )
            );
        } catch (IOException e) {
            throw new JanitorNativeException(runningScript, "error reading file", e);
        }
    }

    public JanitorObject list(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String folderName = arguments.require(1).getRequiredStringValue(0);
        // TODO: final String glob = arguments.getOptionalStringValue(1, ""); ...
        final JList result = process.getBuiltins().list();
        final String[] listing = new File(folderName).list();
        if (listing != null) {
            for (final String name : listing) {
                result.add(process.getBuiltins().nullableString(name));
            }
        }
        return result;
    }

    public JBool mkdir(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String folderName = arguments.require(1).getRequiredStringValue(0);
        return Janitor.toBool(new File(folderName).mkdirs());
    }

    private JanitorObject lastMod(JanitorScriptProcess process, JCallArgs arguments) throws JanitorRuntimeException {
        final String fileName = arguments.require(1).getRequiredStringValue(0);
        final long lastModified = new File(fileName).lastModified();
        final LocalDateTime date = DateTimeUtilities.localFromEpochSeconds(lastModified);
        return process.getBuiltins().dateTime(date);
    }

    private JString normalize(JanitorScriptProcess process, JCallArgs arguments) throws JanitorRuntimeException {
        final String fileName = arguments.require(1).getRequiredStringValue(0);
        final String normalized;
        try {
            normalized = new File(fileName).getCanonicalPath();
        } catch (IOException e) {
            throw new JanitorNativeException(process, "error normalizing file name '" + fileName + "'", e);
        }
        return process.getBuiltins().string(normalized);
    }

    public JanitorObject delete(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String fileName = arguments.require(1).getRequiredStringValue(0);
        final boolean success = new File(fileName).delete();
        return Janitor.toBool(success);
    }

    public void move(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String source = arguments.getRequiredStringValue(0);
        final String target = arguments.getRequiredStringValue(1);
        try {
            Files.move(Path.of(source), Path.of(target));
        } catch (IOException e) {
            throw new JanitorNativeException(process, "error moving file", e);
        }
    }

    public void copy(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String source = arguments.getRequiredStringValue(0);
        final String target = arguments.getRequiredStringValue(1);
        try {
            Files.copy(Path.of(source), Path.of(target));
        } catch (IOException e) {
            throw new JanitorNativeException(process, "error copying file", e);
        }
    }


}
