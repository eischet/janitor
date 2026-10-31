// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.files;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.env.JElement;
import com.eischet.janitor.env.JanitorXmlParser;
import com.eischet.janitor.toolbox.time.DateTimeUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * File system access for scripts: read/write/list/delete/zip, arbitrary absolute paths.
 * <p>
 * This module is deliberately unrestricted -- {@code normalize()} only resolves via
 * {@code getCanonicalPath()}, with no containment check against a base directory. That's intentional:
 * like {@code os}, it is a privileged module that is only ever available to a script if the host
 * application explicitly registers it (never auto-registered). Other embedded/polyglot runtimes
 * (GraalVM Polyglot, Luau) follow the same pattern at a similar granularity -- gate whole capabilities
 * behind explicit opt-in (GraalVM's {@code allowIO}/{@code allowCreateProcess}/...) or omit unsafe
 * functionality from the sandbox entirely, rather than restricting what a granted capability can
 * reach. The host is responsible for not registering this module for untrusted scripts.
 */
public class FilesModule extends JanitorComposed<FilesModule> implements JanitorModule {

    private static final DispatchTable<FilesModule> dispatcher = new DispatchTable<>(FilesModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("files", FilesModule::new);

    static {
        dispatcher.addMethod("exists", FilesModule::fileExists)
                .setMetaData(Janitor.MetaData.HELP, "exists(path): Returns true if a file or folder exists at the path.");
        dispatcher.addMethod("write", FilesModule::writeString)
                .setMetaData(Janitor.MetaData.HELP, "write(path, text, charset): Writes the text to a file, replacing its contents; charset defaults to UTF-8. Returns null.");
        dispatcher.addMethod("read", FilesModule::readString)
                .setMetaData(Janitor.MetaData.HELP, "read(path, charset): Returns the contents of a file as text; charset defaults to UTF-8.");
        dispatcher.addMethod("readXml", FilesModule::readXml)
                .setMetaData(Janitor.MetaData.HELP, "readXml(path, charset): Reads an XML file and returns its root element; charset defaults to UTF-8.");
        dispatcher.addMethod("writeBinary", FilesModule::writeBinary)
                .setMetaData(Janitor.MetaData.HELP, "writeBinary(path, data): Writes binary data to a file, replacing its contents. Returns null.");
        dispatcher.addMethod("readBinary", FilesModule::readBinary)
                .setMetaData(Janitor.MetaData.HELP, "readBinary(path): Returns the contents of a file as binary data.");
        dispatcher.addMethod("list", FilesModule::list)
                .setMetaData(Janitor.MetaData.HELP, "list(folder): Returns a list of the names of the entries in a folder; empty if the folder does not exist.");
        dispatcher.addMethod("mkdirs", FilesModule::mkdir)
                .setMetaData(Janitor.MetaData.HELP, "mkdirs(folder): Creates a folder including missing parent folders; returns true if it was created.");
        dispatcher.addMethod("normalize", FilesModule::normalize)
                .setMetaData(Janitor.MetaData.HELP, "normalize(path): Returns the canonical absolute form of the path.");
        dispatcher.addMethod("lastmod", FilesModule::lastMod)
                .setMetaData(Janitor.MetaData.HELP, "lastmod(path): Returns the last modification time of a file as a date-time, or null if it does not exist.");

        dispatcher.addMethod("delete", FilesModule::delete)
                .setMetaData(Janitor.MetaData.HELP, "delete(path): Deletes a file or an empty folder; returns true if it was deleted.");
        dispatcher.addVoidMethod("move", FilesModule::move)
                .setMetaData(Janitor.MetaData.HELP, "move(source, target): Moves a file; fails if the target already exists.");
        dispatcher.addVoidMethod("copy", FilesModule::copy)
                .setMetaData(Janitor.MetaData.HELP, "copy(source, target): Copies a file; fails if the target already exists.");

        dispatcher.addMethod("Zip", FilesModule::zip)
                .setMetaData(Janitor.MetaData.HELP, "Zip(path): Creates a new ZIP file at the path and returns it for adding entries; it is closed when the script ends.");

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

    /**
     * Script method {@code files.exists(path)}: checks whether a file or folder exists.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return true if it exists
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JBool fileExists(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final File f = new File(arguments.require(1).getString(0).janitorGetHostValue());
        return Janitor.toBool(f.exists());
    }

    /**
     * Script method {@code files.write(path, text, charset)}: writes text to a file, in UTF-8 by default.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code files.read(path, charset)}: reads a file as text, in UTF-8 by default.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return the text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JanitorObject readString(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return runningScript.getBuiltins().nullableString(
                    Files.readString(
                            Path.of(arguments.require(1, 2).getString(0).janitorGetHostValue()),
                            Charset.forName(arguments.getOptionalStringValue(1, "UTF-8"))
                    )
            );
        } catch (IOException e) {
            throw new JanitorNativeException(runningScript, "error reading file", e);
        }
    }

    /**
     * Script method {@code files.readXml(path, charset)}: reads an XML file.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the root element
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JElement readXml(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final String contents = Files.readString(
                Path.of(arguments.require(1, 2).getString(0).janitorGetHostValue()),
                Charset.forName(arguments.getOptionalStringValue(1, "UTF-8"))
            );
            return JanitorXmlParser.parseXml(contents);
        } catch (IOException | XMLStreamException e) {
            throw new JanitorNativeException(process, "error reading file", e);
        }
    }

    /**
     * Parses an XML text.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the root element
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JElement parseXml(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final String contents = arguments.require(1).getRequiredStringValue(0);
            return JanitorXmlParser.parseXml(contents);
        } catch (XMLStreamException e) {
            throw new JanitorNativeException(process, "error reading file", e);
        }
    }

    /**
     * Script method {@code files.writeBinary(path, data)}: writes binary data to a file.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code files.readBinary(path)}: reads a file as binary data.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return the data
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code files.list(folder)}: lists the names of the files in a folder.
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the names; empty if the folder does not exist
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code files.mkdirs(folder)}: creates a folder, including any missing parent folders.
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the folder was created
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JBool mkdir(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String folderName = arguments.require(1).getRequiredStringValue(0);
        return Janitor.toBool(new File(folderName).mkdirs());
    }

    private JanitorObject lastMod(JanitorScriptProcess process, JCallArgs arguments) throws JanitorRuntimeException {
        final String fileName = arguments.require(1).getRequiredStringValue(0);
        final File file = new File(fileName);
        if (!file.exists()) {
            return JNull.NULL;
        }
        // File.lastModified() returns milliseconds; this used to read them as seconds, which gave dates in the far future
        final LocalDateTime date = DateTimeUtils.fromSystemTimeMillis(file.lastModified());
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

    /**
     * Script method {@code files.delete(path)}: deletes a file or an empty folder.
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if it was deleted
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JanitorObject delete(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String fileName = arguments.require(1).getRequiredStringValue(0);
        final boolean success = new File(fileName).delete();
        return Janitor.toBool(success);
    }

    /**
     * Script method {@code files.move(source, target)}: moves a file.
     * @param process the running script process
     * @param arguments the call arguments
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code files.copy(source, target)}: copies a file.
     * @param process the running script process
     * @param arguments the call arguments
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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
