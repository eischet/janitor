// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.word;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import com.eischet.janitor.api.types.functions.SignatureArguments;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The "word" module: fills in, creates and reads Word documents (.docx), on top of Apache POI.
 * <pre>
 *   import word;
 *   doc = word.open("template.docx");
 *   doc.replace("{{customer}}", "ACME Corp.");
 *   doc.repeatRow("{{item}}", [{"{{item}}": "Toner", "{{price}}": "59.90"}]);
 *   doc.save("offer.docx");
 * </pre>
 * The layout of a document is best made in Word, as a template with placeholders that a script fills in; for simple
 * documents, a script can also build headings, paragraphs, tables and images itself.
 * <p>
 * {@code open}, {@code save} and {@code image} read and write arbitrary paths of the file system, so a host registers this
 * module only for scripts that may do that.
 * </p>
 */
public class WordModule extends JanitorComposed<WordModule> implements JanitorModule {

    public static final DispatchTable<WordModule> dispatcher = new DispatchTable<>(WordModule::new, false);

    static {
        dispatcher.setMetaData(Janitor.MetaData.HELP, "Fills in, creates and reads Word documents (.docx).");
    }

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("word", WordModule::new).withDispatcher(() -> WordModule.dispatcher);

    private static final Signature CREATE = Signature.method("create")
            .returns(ScriptType.of(WordDocument.DISPATCH))
            .help("Creates a new, empty document.");
    private static final Signature OPEN = Signature.method("open")
            .param("source", ScriptType.ANY)
            .returns(ScriptType.of(WordDocument.DISPATCH))
            .help("Opens an existing document (.docx) from a file path or from binary data, e.g. a template with placeholders. "
                  + "The source is read completely and is not changed; write the result with toBinary() or save().");

    static {
        dispatcher.addMethod(CREATE, (self, process, args) -> {
            final WordDocument document = new WordDocument(new XWPFDocument());
            process.registerCleanable(document);
            return document;
        });
        dispatcher.addMethod(OPEN, WordModule::open);
    }

    public WordModule() {
        super(dispatcher);
    }

    private JanitorObject open(final JanitorScriptProcess process, final SignatureArguments args) throws JanitorRuntimeException {
        final byte[] bytes = bytesOf(process, "open", args.get("source"));
        try {
            final WordDocument document = new WordDocument(new XWPFDocument(new ByteArrayInputStream(bytes)));
            process.registerCleanable(document);
            return document;
        } catch (IOException | RuntimeException e) {
            throw new JanitorNativeException(process, "open: this is not a Word document (.docx) that can be read", e);
        }
    }

    /**
     * Reads a source that scripts give as a file path or as binary data.
     *
     * @param process the running script
     * @param method  the name of the script method, for error messages
     * @param source  the file path or the binary data
     * @return the bytes
     * @throws JanitorRuntimeException if the source is neither, or the file cannot be read
     */
    static byte[] bytesOf(final JanitorScriptProcess process, final String method, final JanitorObject source) throws JanitorRuntimeException {
        if (source instanceof JBinary binary) {
            return binary.janitorGetHostValue();
        }
        if (source instanceof JString path) {
            try {
                return Files.readAllBytes(Path.of(path.janitorGetHostValue()));
            } catch (IOException e) {
                throw new JanitorNativeException(process, method + ": error reading file '" + path.janitorGetHostValue() + "'", e);
            }
        }
        throw new JanitorArgumentException(process, method + ": expected a file path or binary data, but got: " + source.janitorToString());
    }

}
