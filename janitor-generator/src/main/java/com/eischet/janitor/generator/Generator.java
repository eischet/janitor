// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.generator.writing.DefaultJavaWriter;
import com.eischet.janitor.generator.writing.JavaWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/** Generates Java classes, e.g. entities with their dispatch tables, and writes them to files. */
public class Generator extends JanitorComposed<Generator> {

    public static final DispatchTable<Generator> DISPATCH = new DispatchTable<>(Generator::new);

    static {

    }

    private JavaWriter javaWriter = new DefaultJavaWriter();
    private String path;
    private String javaPackageName;
    private final List<GeneratedClass> generatedClasses = new LinkedList<>();
    private final List<JavaType> externalTypes = new LinkedList<>();

    public Generator() {
        super(DISPATCH);
    }

    /**
     * @return the writer that stores the generated files
     */
    public JavaWriter getJavaWriter() {
        return javaWriter;
    }

    /**
     * Sets the writer that stores the generated files.
     * @param javaWriter the writer
     */
    public void setJavaWriter(final JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
    }

    /**
     * @return the directory that the files are written to, with a trailing slash
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the directory that the files are written to.
     * @param path the directory
     * @throws NullPointerException if the path is null
     */
    public void setPath(final String path) throws NullPointerException {
        Objects.requireNonNull(path);
        if (!path.endsWith("/")) {
            this.path = path + "/";
        } else {
            this.path = path;
        }
    }

    /**
     * @return the package of the generated classes
     */
    public String getJavaPackageName() {
        return javaPackageName;
    }

    /**
     * Sets the package of the generated classes.
     * @param javaPackageName the package name
     */
    public void setJavaPackageName(final String javaPackageName) {
        Objects.requireNonNull(javaPackageName);
        this.javaPackageName = javaPackageName;
    }

    /**
     * @return the classes that are going to be generated
     */
    public List<GeneratedClass> getGeneratedClasses() {
        return generatedClasses;
    }

    /**
     * Adds a class to generate.
     * @param generatedClass the class
     * @return this generator
     */
    public Generator addClass(final GeneratedClass generatedClass) {
        generatedClasses.add(generatedClass);
        return this;
    }

    /** Generates all classes, and writes them as files. */
    public void write() {
        for (final GeneratedClass gc : generatedClasses) {
            final String fullPath = path + javaPackageName.replace(".", "/") + "/" + gc.getName() + ".java";
            final String contents = gc.generate(this);
            javaWriter.write(fullPath, contents);
        }
    }

    /**
     * Creates a class to generate, and adds it to this generator.
     * @param name the name of the class
     * @return the new class
     */
    public GeneratedClass newClass(final String name) {
        final GeneratedClass gc = new GeneratedClass(this, name);
        addClass(gc);
        return gc;
    }

}
