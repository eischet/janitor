package com.eischet.janitor.generator.writing;

import com.eischet.janitor.api.errors.runtime.JanitorError;
import org.jetbrains.annotations.NotNull;

/**
 * Helper interface for writing out generated source code files.
 * This is an interface to help with testing; most real world uses will involve the DefaultJavaWriter.
 */
public interface JavaWriter {

    /**
     * Write the contents to the file.
     * @param filename the file name
     * @param contents the contents
     */
    void write(@NotNull String filename, @NotNull String contents) throws JanitorError;

}
