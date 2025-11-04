package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class MemberAccessExpression extends AstNode implements Expression {

    private final boolean guarded;
    private final Expression expression;
    private final String identifier;

    public MemberAccessExpression(final Location location, final Expression expression, final String identifier, final boolean guarded) {
        super(location);
        this.guarded = guarded;
        this.expression = expression;
        this.identifier = identifier;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        final JanitorObject object = expression.evaluate(process);
        // "null?.anything" must return null
        if (guarded && Janitor.NULL == object) {
            return Janitor.NULL;
        }
        @Nullable final JanitorObject attribute = object.janitorGetAttribute(process, identifier, false);
        if (attribute != null) {
            return attribute;
        }
        if (object instanceof JMap) {
            // for ease of use, {}.foo returns null instead of throwing an exception! I.e. Maps are always using guarded lookups.
            return Janitor.NULL;
        }
        // try unpacking the object, so we can look inside property objects and the like...
        for (final JanitorObject unpacked : object.janitorUnpackAll()) {
            if (guarded && Janitor.NULL == unpacked) {
                return Janitor.NULL;
            }
            @Nullable final JanitorObject unpackedAttribute = unpacked.janitorGetAttribute(process, identifier, false);
            if (unpackedAttribute != null) {
                return unpackedAttribute;
            }
            if (unpacked instanceof JMap) {
                // for ease of use, {}.foo returns null instead of throwing an exception! I.e. Maps are always using guarded lookups.
                return Janitor.NULL;
            }
        }
        throw new JanitorNameException(process, "member not found: " + identifier + "; guarded: " + guarded +"; on: " + object + " [" + simpleClassNameOf(object) + "]");
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("identifier", identifier)
                .optional("expression", expression)
                .optional("guarded", guarded)
                .endObject();
    }

}
