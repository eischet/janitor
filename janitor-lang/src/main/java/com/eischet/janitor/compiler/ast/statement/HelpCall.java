// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.HasNameDocs;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * A call of the built-in {@code help} with a name or a member path, e.g. {@code help(foo)} or {@code help(config.timeout)}.
 * <p>
 * Documentation belongs to names ({@link NameDoc}), but a function only sees the values of its arguments. So the compiler
 * turns such calls into this node, which looks up the documentation of the name where the name lives: in the scope that
 * holds the variable, then along the documented members, or in a module made from a script ({@link HasNameDocs}).
 * Without documentation, or when {@code help} is not the built-in function, it is an ordinary call, which returns the HELP
 * text of the value.
 * </p>
 */
public class HelpCall extends Statement implements Expression {

    /** The name of the built-in function. */
    public static final String FUNCTION_NAME = "help";

    private final List<Expression> path;
    private final FunctionCallStatement call;

    /**
     * Constructor.
     * @param location where
     * @param path the argument and its parts, from the outside in: for {@code a.b.c}, the expressions {@code a}, {@code a.b} and {@code a.b.c}
     * @param call the ordinary call of help, for when there is no documentation
     */
    public HelpCall(final Location location, final List<Expression> path, final FunctionCallStatement call) {
        super(location);
        this.path = List.copyOf(path);
        this.call = call;
    }

    /**
     * Checks whether a call can look up the documentation of its argument by name.
     *
     * @param functionName the name of the called function
     * @param arguments the arguments of the call
     * @return the parts of the argument (see {@link #namePath}), or null if the call is not {@code help(name)}
     */
    public static @Nullable List<Expression> pathOfHelpArgument(final @Nullable String functionName, final @Nullable ArgumentList arguments) {
        if (!FUNCTION_NAME.equals(functionName) || arguments == null || arguments.getArguments().size() != 1) {
            return null;
        }
        final ArgumentList.Argument argument = arguments.getArguments().getFirst();
        return argument.getName() != null ? null : namePath(argument.getExpression());
    }

    /**
     * Splits an expression like {@code a.b.c} into its parts {@code a}, {@code a.b} and {@code a.b.c}.
     *
     * @param expression an expression
     * @return the parts, or null if the expression is not a name followed by member names
     */
    public static @Nullable List<Expression> namePath(final Expression expression) {
        final List<Expression> parts = new ArrayList<>();
        Expression current = expression;
        while (current instanceof MemberAccessExpression member) {
            parts.addFirst(member);
            current = member.getExpression();
        }
        if (!(current instanceof Identifier)) {
            return null;
        }
        parts.addFirst(current);
        return parts;
    }

    /**
     * @param path the parts of a name path, as returned by {@link #namePath}
     * @return the names, e.g. {@code ["a", "b", "c"]} for {@code a.b.c}
     */
    public static @NotNull List<String> names(final @NotNull List<Expression> path) {
        final List<String> names = new ArrayList<>(path.size());
        for (final Expression part : path) {
            names.add(part instanceof MemberAccessExpression member ? member.getIdentifier() : ((Identifier) part).getText());
        }
        return names;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException {
        evaluate(process);
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        final JanitorObject builtin = process.getEnvironment().getBuiltinScope().getVariable(FUNCTION_NAME);
        if (builtin != null && process.lookup(FUNCTION_NAME) == builtin) {
            final NameDoc doc = lookupDoc(process);
            final String help = doc == null ? null : doc.render();
            if (help != null) {
                return process.getBuiltins().string(help);
            }
        }
        return call.evaluate(process);
    }

    private @Nullable NameDoc lookupDoc(final JanitorScriptProcess process) throws JanitorRuntimeException {
        final List<String> names = names(path);
        final ResultAndScope scoped = process.lookupScopedVar(names.getFirst());
        NameDoc doc = scoped == null || scoped.getScope() == null ? null : scoped.getScope().getNameDoc(names.getFirst());
        for (int i = 1; i < names.size(); i++) {
            NameDoc member = doc == null ? null : doc.member(names.get(i));
            if (member == null && path.get(i - 1).evaluate(process) instanceof HasNameDocs owner) {
                member = owner.getNameDoc(names.get(i));
            }
            if (member == null) {
                return null;
            }
            doc = member;
        }
        return doc;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("call", call)
                .endObject();
    }

}
