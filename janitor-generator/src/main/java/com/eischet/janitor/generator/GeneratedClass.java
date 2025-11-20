package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.generator.types.SimpleBuiltinType;
import com.eischet.janitor.generator.writing.CodeOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class GeneratedClass extends JanitorComposed<GeneratedClass> implements JavaType {

    public static final DispatchTable<GeneratedClass> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("name", GeneratedClass::getName);
    }


    private final @NotNull Generator generator;
    private final @NotNull String name;
    private final @NotNull List<GeneratedField> fields = new LinkedList<>();

    public GeneratedClass(final @NotNull Generator generator, final @NotNull String name) {
        super(DISPATCH);
        this.generator = generator;
        this.name = name;
    }

    public String generate(final Generator generator) {
        final var out = new CodeOutputStream();
        out.write("package ").write(generator.getJavaPackageName()).write(";").newline();
        out.emptyLine();

        out.writeLine("import com.eischet.janitor.api.types.dispatch.DispatchTable;");
        // TODO: import all required external types, sorted by package
        out.writeLine("import org.jetbrains.annotations.NotNull;");
        out.writeLine("import org.jetbrains.annotations.Nullable;");

        out.newline();
        out.write("public class ").write(name).space().startBlock();

        out.emptyLine();
        out.write("    public static final DispatchTable<").write(name).write("> DISPATCH = new DispatchTable<>();").newline();
        out.emptyLine();
        out.writeIndent().write("static {").newline();
        for (final var field : fields) {
            field.generateDispatch(out);
        }
        // TODO: iterate all fields and create DISPATCH entries where possible
        // TODO: add an optional static block
        out.writeIndent().write("}").newline();
        out.emptyLine();

        for (final var field : fields) {
            field.generateDeclaration(out);
        }
        out.emptyLine();

        // TODO: constructor

        for (final var field : fields) {
            field.generateGetter(out);
            field.generateSetter(out);
            field.generateWither(out);
        }

        out.endBlock();
        return out.toString();
    }


    @Override
    public String getPackageName() {
        return generator.getJavaPackageName();
    }

    @Override
    public String getName() {
        return name;
    }

    public @NotNull @Unmodifiable List<GeneratedField> getFields() {
        return List.copyOf(fields);
    }


    public GeneratedClass addField(final @NotNull JavaType type,
                                   final @NotNull String name) {
        return addField(type, name, null);
    }

    public GeneratedClass addField(final @NotNull JavaType type,
                                   final @NotNull String name,
                                   final @Nullable Consumer<@NotNull GeneratedField> customizer) {
        final var field = new GeneratedField(this, name, type);
        fields.add(field);
        if (customizer != null) {
            customizer.accept(field);
        }
        return this;
    }



}
