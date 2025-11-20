package com.eischet.janitor.generator;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.generator.writing.CodeOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GeneratedField extends JanitorComposed<GeneratedField> {

    public static final DispatchTable<GeneratedField> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("name", GeneratedField::getName);
    }

    private final @NotNull GeneratedClass parent;
    private final @NotNull String name;
    private final @NotNull JavaType type;
    private boolean nullable = true;


    public GeneratedField(final @NotNull GeneratedClass parent, final @NotNull String name, final @NotNull JavaType type) {
        super(DISPATCH);
        this.parent = parent;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public JavaType getType() {
        return type;
    }

    public @NotNull GeneratedClass getParent() {
        return parent;
    }

    public void generateDispatch(final @NotNull CodeOutputStream out) {
        String dispatchMethod = type.getName(); // TODO: must be improved to better fit the actual dispatch table layout


        out.writeIndent().writeIndent().write("DISPATCH.add").write(dispatchMethod).write("Property(\"")
                        .write(name).write("\", ")
                        .write(parent.getName()).write("::get").capitalize(name);
        // TODO: next line only if writable:
        out.write(", ").write(parent.getName()).write("::set").capitalize(name);
        out.write(");").newline();
        // DISPATCH.addStringProperty("bar", Foo::getBar, Foo::setBar);
    }

    public void generateDeclaration(final @NotNull CodeOutputStream out) {
        out.writeIndent().write("private ");
        out.optional(!type.isPrimitive(), nullability(out, nullable));
        out.write(type.getName()).space().write(name);
        @Nullable final Consumer<CodeOutputStream> dve = type.defaultValueEmitter();
        if (dve != null) {
            out.space().write("= ");
            dve.accept(out);
        }
        out.write(";").newline();
    }

    public void generateGetter( final @NotNull CodeOutputStream out) {
        out.writeIndent().write("public ").optional(!type.isPrimitive(), nullability(out, nullable))
                .space().write(type.getName()).space().write("get").capitalize(name).write("() {").newline();
        out.writeIndent().writeIndent().write("return ").write(name).write(";").newline();
        out.writeIndent().write("}").newline().newline();
    }

    private Runnable nullability(final @NotNull CodeOutputStream out, final boolean nullable) {
        if (nullable) {
            return () -> out.write("@Nullable ");
        } else {
            return () -> out.write("@NotNull ");
        }
    }

    public void generateSetter( final @NotNull CodeOutputStream out) {
        out.writeIndent().write("public void set").capitalize(name)
                .write("(final ")
                .optional(!type.isPrimitive(), nullability(out, nullable))
                .write(type.getName()).space().write(name)
                .write(") {").newline();
        out.writeIndent().writeIndent().write("this.").write(name).write(" = ").write(name).write(";").newline();
        out.writeIndent().write("}").newline().newline();
    }

    public void generateWither( final @NotNull CodeOutputStream out) {
        out.writeIndent().write("public @NotNull ")
                .write(parent.getName()).space().write("with").capitalize(name).write("(final ")
                .optional(!type.isPrimitive(), nullability(out, nullable))
                .write(type.getName()).space().write(name).write(") {").newline();
        out.writeIndent().writeIndent().write("this.").write(name).write(" = ").write(name).write(";").newline();
        out.writeIndent().writeIndent().write("return this;").newline();
        out.writeIndent().write("}").newline().newline();
    }

}
