package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Unary operation.
 */
public abstract class UnaryOperation extends AstNode implements Expression {
    protected final Expression parameter;
    private final UnaryOperationDelegate functor;

    /**
     * Constructor.
     * @param location where
     * @param parameter what
     * @param functor how
     */
    protected UnaryOperation(final Location location, final Expression parameter, final UnaryOperationDelegate functor) {
        super(location);
        this.parameter = parameter;
        this.functor = functor;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        final JanitorObject variable = parameter.evaluate(process).janitorUnpack();
        process.setCurrentLocation(getLocation());
        return functor.perform(process, variable);
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", parameter)
                .endObject();

    }

}
