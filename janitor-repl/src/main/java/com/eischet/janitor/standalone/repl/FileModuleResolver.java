// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.standalone.repl;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptModuleException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.ScriptSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves {@code import "NAME" as alias;} to script files, for the standalone interpreter.
 * <ul>
 *     <li>{@code "./foo"} and {@code "../lib/foo"} are relative to the directory of the importing script, or to the working
 *     directory for code typed into the interactive shell.</li>
 *     <li>Absolute paths are used as they are.</li>
 *     <li>Any other name, e.g. {@code "foo"} or {@code "util/strings"}, is looked up in the directories of the search path, in
 *     order. The directory of the importing script is not part of it: local modules are imported with {@code "./"}.</li>
 * </ul>
 * The file name extension {@code .janitor} is added unless the name already has it. Each file is loaded once and the module
 * is shared by all scripts that import it; when the file changes, the next import loads it again. A module that imports
 * itself, directly or through others, is an error.
 */
public class FileModuleResolver implements ModuleResolver {

    /** The file name extension of Janitor scripts. */
    public static final String EXTENSION = ".janitor";

    /** The environment variable with the search path, directories separated by {@link File#pathSeparator}. */
    public static final String SEARCH_PATH_VARIABLE = "JANITOR_PATH";

    private final List<Path> searchPath;
    private final Map<Path, Loaded> loaded = new HashMap<>();
    private final Set<Path> loading = new HashSet<>();

    private record Loaded(FileTime modified, JanitorModule module) {
    }

    /**
     * @param searchPath the directories for names that are neither relative nor absolute, in the order they are searched
     */
    public FileModuleResolver(final @NotNull List<Path> searchPath) {
        this.searchPath = List.copyOf(searchPath);
    }

    /**
     * The search path: first the extra directories (e.g. from the command line), then the directories from
     * {@value #SEARCH_PATH_VARIABLE}, or {@code ~/.janitor/modules} if that variable is not set.
     *
     * @param extraDirectories directories to search first
     * @param variable the value of {@value #SEARCH_PATH_VARIABLE}, or null
     * @param userHome the user's home directory
     * @return the search path
     */
    public static @NotNull List<Path> defaultSearchPath(final @NotNull List<Path> extraDirectories, final @Nullable String variable, final @NotNull Path userHome) {
        final List<Path> result = new ArrayList<>(extraDirectories);
        if (variable == null || variable.isBlank()) {
            result.add(userHome.resolve(".janitor").resolve("modules"));
        } else {
            for (final String entry : variable.split(File.pathSeparator)) {
                if (!entry.isBlank()) {
                    result.add(Path.of(entry.trim()));
                }
            }
        }
        return result;
    }

    /**
     * @return the directories searched for names that are neither relative nor absolute
     */
    public @NotNull List<Path> getSearchPath() {
        return searchPath;
    }

    @Override
    public synchronized @Nullable JanitorModule resolveModuleByStringName(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        final Path file = find(process, name);
        final FileTime modified;
        try {
            modified = Files.getLastModifiedTime(file);
        } catch (IOException e) {
            throw new JanitorScriptModuleException(process, "Cannot read module '" + name + "' from " + file, e);
        }
        final Loaded cached = loaded.get(file);
        if (cached != null && cached.modified().equals(modified)) {
            return cached.module();
        }
        if (!loading.add(file)) {
            throw new JanitorScriptModuleException(process, "Module '" + name + "' imports itself: " + file);
        }
        try {
            final JanitorModule module = load(process, name, file);
            loaded.put(file, new Loaded(modified, module));
            return module;
        } finally {
            loading.remove(file);
        }
    }

    private JanitorModule load(final JanitorScriptProcess process, final String name, final Path file) throws JanitorRuntimeException {
        final String source;
        try {
            source = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JanitorScriptModuleException(process, "Cannot read module '" + name + "' from " + file, e);
        }
        try {
            final RunnableScript compiled = process.getRuntime().compile(file.getFileName().toString(), source, file.toString());
            final ResultAndScope result = compiled.runAndKeepGlobals(process.getRuntime(), g -> {
            });
            return new ScriptModule.ModuleFromScope(result.getScope(), compiled.getDocumentation());
        } catch (JanitorCompilerException e) {
            throw new JanitorScriptModuleException(process, e);
        }
    }

    /**
     * Finds the file for a module name.
     *
     * @return the absolute, normalized path of an existing file
     * @throws JanitorNameException if there is no such file
     */
    private Path find(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        final String fileName = name.endsWith(EXTENSION) ? name : name + EXTENSION;
        final List<Path> candidates = new ArrayList<>();
        try {
            final Path path = Path.of(fileName);
            if (isRelative(name)) {
                candidates.add(baseDirectory(process).resolve(path));
            } else if (path.isAbsolute()) {
                candidates.add(path);
            } else {
                for (final Path directory : searchPath) {
                    candidates.add(directory.resolve(path));
                }
            }
        } catch (InvalidPathException e) {
            throw new JanitorNameException(process, "Module not found: '" + name + "' is not a valid file name");
        }
        for (final Path candidate : candidates) {
            final Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(normalized)) {
                try {
                    return normalized.toRealPath();
                } catch (IOException e) {
                    return normalized;
                }
            }
        }
        final String looked = candidates.isEmpty() ? "the search path is empty"
                : "looked for " + candidates.stream().map(c -> c.toAbsolutePath().normalize().toString()).collect(Collectors.joining(", "));
        throw new JanitorNameException(process, "Module not found: '" + name + "' (" + looked + ")");
    }

    private static boolean isRelative(final String name) {
        return name.startsWith("./") || name.startsWith("../") || name.startsWith(".\\") || name.startsWith("..\\");
    }

    /**
     * The directory of the importing script, which is the script of the import statement that is running now, or the working
     * directory if that script did not come from a file.
     */
    private static Path baseDirectory(final JanitorScriptProcess process) {
        final Location location = process.getCurrentLocation();
        final ScriptSource importer = location == null ? null : location.getModule();
        final String origin = importer == null ? null : importer.getOrigin();
        if (origin != null) {
            try {
                final Path parent = Path.of(origin).toAbsolutePath().getParent();
                if (parent != null) {
                    return parent;
                }
            } catch (InvalidPathException ignored) {
                // the origin is not a file name; fall back to the working directory
            }
        }
        return Path.of("").toAbsolutePath();
    }
}
