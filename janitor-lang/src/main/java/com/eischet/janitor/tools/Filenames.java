package com.eischet.janitor.tools;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Filenames {

    public static @Nullable String cutFilename(@Nullable String filename, int maxLength) {
        if (filename == null) {
            return null;
        }
        File file = new File(filename.trim());
        filename = file.getName();
        if (filename.length() <= maxLength) {
            return filename;
        }
        String[] parts = filename.split("\\.(?=[^\\.]+$)");
        if (parts.length > 1) {
            filename = parts[0].substring(0, maxLength - parts[1].length() - 1).trim() + "." + parts[1].trim();
        } else {
            filename = parts[0].substring(0, maxLength).trim();
        }
        return filename;
    }


}
