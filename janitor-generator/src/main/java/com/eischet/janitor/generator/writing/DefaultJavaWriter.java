package com.eischet.janitor.generator.writing;

import com.eischet.janitor.api.errors.runtime.JanitorError;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class DefaultJavaWriter implements JavaWriter {
    @Override
    public void write(@NotNull final String filename, @NotNull final String contents) throws JanitorError {
        final Path path = Path.of(filename);
        try {
            Files.writeString(path, Objects.requireNonNull(contents));
        } catch (final IOException e) {
            throw new JanitorError("error writing to " + path.toAbsolutePath(), e);
        }
    }
}
