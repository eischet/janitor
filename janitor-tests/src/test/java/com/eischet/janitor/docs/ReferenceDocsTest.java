// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docs;

import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps the language reference in {@code docs/reference} in line with the code: generated sections must be up to date,
 * every built-in member needs a HELP text, every module and global must be described, and links must resolve.
 * <p>
 * To regenerate the generated sections, run:
 * {@code mvn -pl janitor-tests -am test -Dtest=ReferenceDocsTest -Djanitor.docs.regenerate=true -Dsurefire.failIfNoSpecifiedTests=false}
 */
public class ReferenceDocsTest extends JanitorTest {

    private static final String REGENERATE = "janitor.docs.regenerate";
    private static final String HOW_TO_REGENERATE = "mvn -pl janitor-tests -am test -Dtest=ReferenceDocsTest -D" + REGENERATE
            + "=true -Dsurefire.failIfNoSpecifiedTests=false";
    private static final Pattern LINK = Pattern.compile("\\]\\(([^)\\s]+)\\)");
    private static final Pattern MODULE_DIR = Pattern.compile("<module>([^<]+)</module>");

    @Test
    void generatedSectionsAreUpToDate() throws Exception {
        final boolean regenerate = Boolean.getBoolean(REGENERATE);
        final ReferenceGenerator generator = new ReferenceGenerator();
        final Map<String, String> sections = generator.generateSections();
        final Set<String> used = new HashSet<>();
        final List<String> stale = new ArrayList<>();
        for (final Path file : ReferenceFiles.markdownFiles()) {
            final String name = file.getFileName().toString();
            final String current = ReferenceFiles.read(file);
            final String expected = name.equals(ReferenceGenerator.MODULE_TYPES_FILE)
                    ? generator.generateModuleTypesFile()
                    : ReferenceGenerator.applySections(current, sections, used);
            if (!expected.equals(current)) {
                if (regenerate) {
                    Files.writeString(file, expected);
                    log.warn("regenerated {}", file);
                } else {
                    stale.add(name);
                }
            }
        }
        final Path moduleTypes = ReferenceFiles.directory().resolve(ReferenceGenerator.MODULE_TYPES_FILE);
        if (!Files.exists(moduleTypes)) {
            if (regenerate) {
                Files.writeString(moduleTypes, generator.generateModuleTypesFile());
            } else {
                stale.add(ReferenceGenerator.MODULE_TYPES_FILE);
            }
        }
        assertEquals(List.of(), stale, "generated parts of the reference are out of date; regenerate them with: " + HOW_TO_REGENERATE);
        final Set<String> unused = new TreeSet<>(sections.keySet());
        unused.removeAll(used);
        assertEquals(Set.of(), unused, "generated sections that no file of the reference contains");
    }

    @Test
    void builtinMembersHaveHelpTexts() throws Exception {
        final ReferenceGenerator generator = new ReferenceGenerator();
        generator.generateSections();
        assertEquals(List.of(), generator.getProblems(),
                "every member of a built-in type needs a HELP text that starts with its call form, e.g. \"name(arguments): \" or \"name: \"");
    }

    @Test
    void everyModuleIsDocumented() {
        final Path repository = ReferenceFiles.directory().getParent().getParent();
        final Matcher modules = MODULE_DIR.matcher(ReferenceFiles.read(repository.resolve("janitor-modules/pom.xml")));
        final Set<String> moduleArtifacts = new TreeSet<>();
        while (modules.find()) {
            moduleArtifacts.add("com.eischet.janitor.modules:" + modules.group(1));
        }
        final Set<String> documented = ReferenceGenerator.MODULES.stream().map(ReferenceGenerator.ModuleInfo::artifact)
                .filter(artifact -> artifact.startsWith("com.eischet.janitor.modules:"))
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(moduleArtifacts, documented, "ReferenceGenerator.MODULES must list every module of janitor-modules/pom.xml");

        final String modulesMd = ReferenceFiles.read(ReferenceFiles.directory().resolve("modules.md"));
        for (final ReferenceGenerator.ModuleInfo module : ReferenceGenerator.MODULES) {
            assertTrue(modulesMd.contains("<a id=\"module-" + module.name() + "\"></a>"), "modules.md must describe module " + module.name());
        }
    }

    @Test
    void everyGlobalIsDocumented() {
        final String builtinsMd = ReferenceFiles.read(ReferenceFiles.directory().resolve("builtins.md"));
        for (final String name : new ReferenceGenerator().builtinGlobals()) {
            assertTrue(builtinsMd.contains("<a id=\"global-" + name + "\"></a>"), "builtins.md must describe the global " + name);
        }
    }

    @Test
    void linksResolve() {
        final Map<String, Set<String>> anchors = new HashMap<>();
        for (final Path file : ReferenceFiles.markdownFiles()) {
            anchors.put(file.getFileName().toString(), ReferenceFiles.anchors(ReferenceFiles.read(file)));
        }
        final List<String> broken = new ArrayList<>();
        for (final Path file : ReferenceFiles.markdownFiles()) {
            final String name = file.getFileName().toString();
            final String markdown = ReferenceFiles.read(file);
            final Matcher link = LINK.matcher(markdown);
            while (link.find()) {
                final String target = link.group(1);
                if (target.startsWith("http://") || target.startsWith("https://") || target.startsWith("mailto:")) {
                    continue;
                }
                final int hash = target.indexOf('#');
                final String targetFile = hash < 0 ? target : target.substring(0, hash);
                final String anchor = hash < 0 ? null : target.substring(hash + 1);
                final String resolvedFile = targetFile.isEmpty() ? name : targetFile;
                if (!anchors.containsKey(resolvedFile)) {
                    broken.add(name + ": " + target + " (no such file)");
                } else if (anchor != null && !anchors.get(resolvedFile).contains(anchor)) {
                    broken.add(name + ": " + target + " (no such anchor)");
                }
            }
        }
        assertEquals(List.of(), broken, "broken links in the reference");
    }

    @Test
    void packagedIndexListsAllFiles() {
        final Set<String> files = ReferenceFiles.markdownFiles().stream().map(p -> p.getFileName().toString()).collect(Collectors.toCollection(TreeSet::new));
        assertEquals(files, new TreeSet<>(JanitorDocs.files()), "janitor-docs.properties must list every file of docs/reference");
        assertFalse(JanitorDocs.version().isBlank());
        for (final String file : JanitorDocs.files()) {
            assertTrue(JanitorDocs.read(file).isPresent(), "missing resource " + file);
        }
    }
}
