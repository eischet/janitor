package com.eischet.janitor.generator.writing;

import com.eischet.janitor.api.errors.runtime.JanitorError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

public class TestWriter implements JavaWriter {

    private final Map<@NotNull String, @NotNull String> generatedFiles = new HashMap<>();

    @Override
    public void write(@NotNull final String filename, @NotNull final String contents) throws JanitorError {
        generatedFiles.put(filename, contents);
    }

    @VisibleForTesting
    public @Unmodifiable Map<@NotNull String, @NotNull String> getGeneratedFiles() {
        return Map.copyOf(generatedFiles);
    }

}
