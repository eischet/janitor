package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.functions.EvaluatedArgument;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * List of expressions.
 * Used in function calls and array literals, for example.
 */
public class ArgumentList extends AstNode implements JsonExportableObject {

    public class Argument {
        final @Nullable String name;
        final Expression expression;

        public Argument(@Nullable final String name, final Expression expression) {
            this.name = name;
            this.expression = expression;
        }

        public @Nullable String getName() {
            return name;
        }

        public Expression getExpression() {
            return expression;
        }
    }
    private final List<Argument> arguments = new LinkedList<>();

    /**
     * Constructor.
     * @param location where
     */
    public ArgumentList(final Location location) {
        super(location);
    }

    public JCallArgs toCallArguments(final String identifier, final JanitorScriptProcess process) throws JanitorRuntimeException {
        final List<EvaluatedArgument> evaluatedArguments = new LinkedList<>();
        for (final Argument argument : arguments) {
            evaluatedArguments.add(new EvaluatedArgument(argument.name, argument.expression.evaluate(process).janitorUnpack()));
        }
        JCallArgs args = new JCallArgs(process, identifier, evaluatedArguments);
        return args;

        /* OLD:
        final List<JanitorObject> finishedArgs;
        final List<JanitorObject> args = new ArrayList<>(expressionList.size());
        for (int i = 0; i < expressionList.size(); i++) {
            args.add(expressionList.get(i).evaluate(process).janitorUnpack());
        }
        finishedArgs = args;
        process.trace(() -> "args: " + finishedArgs);
        final JCallArgs callArgs = new JCallArgs(identifier, process, finishedArgs);
        return callArgs;
         */
        // TODO: add positional arguments
    }


    /**
     * Add an expression to the list.
     * @param expression what
     * @return this
     */
    public ArgumentList addExpression(final Expression expression) {
        arguments.add(new Argument(null, expression));
        return this;
    }

    public ArgumentList addNamedExpression(final String name, final Expression expression) {
        arguments.add(new Argument(name, expression));
        return this;
    }

    /**
     * Get the number of expressions in the list.
     * @return number of expressions
     */
    public int length() {
        return arguments.size();
    }

    /**
     * Get an expression from the list.
     * @param index which
     * @return the expression
     */
    public Expression get(final int index) {
        return arguments.get(index).expression;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this));
        // TODO: fix this, or simply remove it all .optional("args", arguments)
        producer.endObject();

    }

}
