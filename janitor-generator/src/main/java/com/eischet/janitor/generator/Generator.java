package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.generator.writing.DefaultJavaWriter;
import com.eischet.janitor.generator.writing.JavaWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    public JavaWriter getJavaWriter() {
        return javaWriter;
    }

    public void setJavaWriter(final JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) throws NullPointerException {
        Objects.requireNonNull(path);
        if (!path.endsWith("/")) {
            this.path = path + "/";
        } else {
            this.path = path;
        }
    }

    public String getJavaPackageName() {
        return javaPackageName;
    }

    public void setJavaPackageName(final String javaPackageName) {
        Objects.requireNonNull(javaPackageName);
        this.javaPackageName = javaPackageName;
    }

    public List<GeneratedClass> getGeneratedClasses() {
        return generatedClasses;
    }

    public Generator addClass(final GeneratedClass generatedClass) {
        generatedClasses.add(generatedClass);
        return this;
    }

    public void write() {
        for (final GeneratedClass gc : generatedClasses) {
            final String fullPath = path + javaPackageName.replace(".", "/") + "/" + gc.getName() + ".java";
            final String contents = gc.generate(this);
            javaWriter.write(fullPath, contents);
        }
    }

    public GeneratedClass newClass(final String name) {
        final GeneratedClass gc = new GeneratedClass(this, name);
        addClass(gc);
        return gc;
    }

}
