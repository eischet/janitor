package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class MemberCallExpression extends Statement implements Expression {

    private final boolean guarded;
    private final Expression expression;
    private final String identifier;
    private final ArgumentList args;

    public MemberCallExpression(final Location location, final Expression expression, final String identifier, final ArgumentList args, final boolean guarded) {
        super(location);
        this.guarded = guarded;
        this.expression = expression;
        this.identifier = identifier;
        this.args = args;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        evaluate(process);
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        final JanitorObject object = expression.evaluate(process);
        // "null?.anything(anything)" must return null; and don't bother evaluation the args.
        if (guarded && Janitor.NULL == object) {
            return Janitor.NULL;
        }
        @Nullable final JanitorObject attribute = object.janitorGetAttribute(process, identifier, false);
        if (attribute == null) {
            throw new JanitorNameException(process, "member not found: " + identifier + "; on: " + object + "[" + simpleClassNameOf(object) + "]");
        }
        @NotNull final JanitorObject existingAttribute = attribute;
        if (existingAttribute instanceof JCallable callable) {
            return callable.call(process, args == null ? JCallArgs.empty(identifier, process) : args.toCallArguments(identifier, process));
        }
        throw new JanitorNameException(process, "member is not callable: " + identifier +
                                                "; on: " + object + "[" + simpleClassNameOf(object) + "] = " +
                                                existingAttribute + " [" + simpleClassNameOf(existingAttribute) + "]");
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("identifier", identifier)
                .optional("expression", expression)
                .optional("args", args)
                .optional("guarded", guarded)
                .endObject();
    }

}
