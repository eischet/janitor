// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.files;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
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
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
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
 * </p>
 * <p>
 * Every method is described by a {@link Signature}, which writes its HELP text and binds its arguments.
 * </p>
 */
public class FilesModule extends JanitorComposed<FilesModule> implements JanitorModule {

    private static final DispatchTable<FilesModule> dispatcher = new DispatchTable<>(FilesModule::new, false);

    static {
        dispatcher.setMetaData(com.eischet.janitor.api.Janitor.MetaData.HELP, "Access to the file system.");
    }

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("files", FilesModule::new).withDispatcher(() -> FilesModule.dispatcher);

    private static final String UTF_8 = "UTF-8";
    private static final ScriptType BINARY = new ScriptType.Builtin("binary");
    private static final ScriptType ELEMENT = ScriptType.of(JElement.DISPATCH);

    private static final Signature EXISTS = Signature.method("exists")
            .param("path", ScriptType.STRING)
            .returns(ScriptType.BOOL)
            .help("Returns true if a file or folder exists at the path.");
    private static final Signature WRITE = Signature.method("write")
            .param("path", ScriptType.STRING)
            .param("text", ScriptType.STRING)
            .optional("charset", ScriptType.STRING, UTF_8)
            .help("Writes the text to a file, replacing its contents. Returns null.");
    private static final Signature READ = Signature.method("read")
            .param("path", ScriptType.STRING)
            .optional("charset", ScriptType.STRING, UTF_8)
            .returns(ScriptType.STRING)
            .help("Returns the contents of a file as text.");
    private static final Signature READ_XML = Signature.method("readXml")
            .param("path", ScriptType.STRING)
            .optional("charset", ScriptType.STRING, UTF_8)
            .returns(ELEMENT)
            .help("Reads an XML file and returns its root element.");
    private static final Signature WRITE_BINARY = Signature.method("writeBinary")
            .param("path", ScriptType.STRING)
            .param("data", BINARY)
            .help("Writes binary data to a file, replacing its contents. Returns null.");
    private static final Signature READ_BINARY = Signature.method("readBinary")
            .param("path", ScriptType.STRING)
            .returns(BINARY)
            .help("Returns the contents of a file as binary data.");
    private static final Signature LIST = Signature.method("list")
            .param("folder", ScriptType.STRING)
            .returns(ScriptType.listOf(ScriptType.STRING))
            .help("Returns a list of the names of the entries in a folder; empty if the folder does not exist.");
    private static final Signature MKDIRS = Signature.method("mkdirs")
            .param("folder", ScriptType.STRING)
            .returns(ScriptType.BOOL)
            .help("Creates a folder including missing parent folders; returns true if it was created.");
    private static final Signature NORMALIZE = Signature.method("normalize")
            .param("path", ScriptType.STRING)
            .returns(ScriptType.STRING)
            .help("Returns the canonical absolute form of the path.");
    private static final Signature LASTMOD = Signature.method("lastmod")
            .param("path", ScriptType.STRING)
            .returns(ScriptType.DATETIME)
            .help("Returns the last modification time of a file as a date-time, or null if it does not exist.");
    private static final Signature DELETE = Signature.method("delete")
            .param("path", ScriptType.STRING)
            .returns(ScriptType.BOOL)
            .help("Deletes a file or an empty folder; returns true if it was deleted.");
    private static final Signature MOVE = Signature.method("move")
            .param("source", ScriptType.STRING)
            .param("target", ScriptType.STRING)
            .help("Moves a file; fails if the target already exists.");
    private static final Signature COPY = Signature.method("copy")
            .param("source", ScriptType.STRING)
            .param("target", ScriptType.STRING)
            .help("Copies a file; fails if the target already exists.");
    private static final Signature ZIP = Signature.method("Zip")
            .param("path", ScriptType.STRING)
            .help("Creates a new ZIP file at the path and returns it for adding entries; it is closed when the script ends.");

    static {
        dispatcher.addMethod(EXISTS, FilesModule::fileExists);
        dispatcher.addMethod(WRITE, FilesModule::writeString);
        dispatcher.addMethod(READ, FilesModule::readString);
        dispatcher.addMethod(READ_XML, FilesModule::readXml);
        dispatcher.addMethod(WRITE_BINARY, FilesModule::writeBinary);
        dispatcher.addMethod(READ_BINARY, FilesModule::readBinary);
        dispatcher.addMethod(LIST, FilesModule::list);
        dispatcher.addMethod(MKDIRS, FilesModule::mkdir);
        dispatcher.addMethod(NORMALIZE, FilesModule::normalize);
        dispatcher.addMethod(LASTMOD, FilesModule::lastMod);
        dispatcher.addMethod(DELETE, FilesModule::delete);
        dispatcher.addVoidMethod(MOVE, FilesModule::move);
        dispatcher.addVoidMethod(COPY, FilesModule::copy);
        dispatcher.addMethod(ZIP, FilesModule::zip);
    }

    private JanitorObject zip(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final String zipFilename = args.requiredString("path");
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
    public JBool fileExists(final JanitorScriptProcess runningScript, final SignatureArguments arguments) throws JanitorRuntimeException {
        final File f = new File(arguments.requiredString("path"));
        return Janitor.toBool(f.exists());
    }

    /**
     * Script method {@code files.write(path, text, charset)}: writes text to a file, in UTF-8 by default.
     * @param runningScript the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public JNull writeString(final JanitorScriptProcess runningScript, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            Files.writeString(
                    Path.of(arguments.requiredString("path")),
                    arguments.requiredString("text"),
                    Charset.forName(arguments.requiredString("charset"))
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
    public JanitorObject readString(final JanitorScriptProcess runningScript, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            return runningScript.getBuiltins().nullableString(
                    Files.readString(
                            Path.of(arguments.requiredString("path")),
                            Charset.forName(arguments.requiredString("charset"))
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
    public JElement readXml(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            final String contents = Files.readString(
                Path.of(arguments.requiredString("path")),
                Charset.forName(arguments.requiredString("charset"))
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
    public JNull writeBinary(final JanitorScriptProcess runningScript, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            Files.write(
                    Path.of(arguments.requiredString("path")),
                    arguments.required("data", JBinary.class).janitorGetHostValue()
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
    public JanitorObject readBinary(final JanitorScriptProcess runningScript, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            return runningScript.getBuiltins().binary(
                    Files.readAllBytes(
                            Path.of(arguments.requiredString("path"))
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
    public JanitorObject list(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        final String folderName = arguments.requiredString("folder");
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
    public JBool mkdir(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        return Janitor.toBool(new File(arguments.requiredString("folder")).mkdirs());
    }

    private JanitorObject lastMod(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        final File file = new File(arguments.requiredString("path"));
        if (!file.exists()) {
            return JNull.NULL;
        }
        // File.lastModified() returns milliseconds; this used to read them as seconds, which gave dates in the far future
        final LocalDateTime date = DateTimeUtils.fromSystemTimeMillis(file.lastModified());
        return process.getBuiltins().dateTime(date);
    }

    private JString normalize(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        final String fileName = arguments.requiredString("path");
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
    public JanitorObject delete(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        return Janitor.toBool(new File(arguments.requiredString("path")).delete());
    }

    /**
     * Script method {@code files.move(source, target)}: moves a file.
     * @param process the running script process
     * @param arguments the call arguments
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public void move(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            Files.move(Path.of(arguments.requiredString("source")), Path.of(arguments.requiredString("target")));
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
    public void copy(final JanitorScriptProcess process, final SignatureArguments arguments) throws JanitorRuntimeException {
        try {
            Files.copy(Path.of(arguments.requiredString("source")), Path.of(arguments.requiredString("target")));
        } catch (IOException e) {
            throw new JanitorNativeException(process, "error copying file", e);
        }
    }


}
