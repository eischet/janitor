package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JElement extends JanitorComposed<JElement> {

    public static final DispatchTable<JElement> DISPATCH = new DispatchTable<>(null);

    static {
        DISPATCH.addStringProperty("name", JElement::getName, JElement::setName);
        DISPATCH.addListProperty("children", JElement::getChildren);
        DISPATCH.addObjectProperty("attrs", JElement::getAttributes);
        DISPATCH.addStringProperty("text", JElement::getText, JElement::setText);
    }

    protected @Nullable String name;
    protected @Nullable JMap attributes;
    protected @Nullable JList children;
    protected @Nullable String text;

    public JElement() {
        super(DISPATCH);
    }

    public JElement(final @Nullable String name) {
        super(DISPATCH);
        this.name = name;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(final @Nullable String name) {
        this.name = name;
    }

    public @NotNull JMap getAttributes() {
        if (attributes == null) {
            attributes = Janitor.map();
        }
        return attributes;
    }

    public @NotNull JList getChildren() {
        if (children == null) {
            children = Janitor.list();
        }
        return children;
    }

    public @Nullable String getText() {
        return text;
    }

    public void setText(@Nullable final String text) {
        this.text = text;
    }

    public @NotNull JElement requireFirstChild(final String name) throws IllegalArgumentException {
        final JElement child = firstChild(name);
        if (child == null) {
            throw new IllegalArgumentException("Missing child element '" + name + "'");
        }
        return child;
    }

    public @Nullable JElement firstChild(final String name) {
        if (children != null) {
            for (final JanitorObject child : children) {
                if (child instanceof JElement && name.equals(((JElement) child).getName())) {
                    return (JElement) child;
                }
            }
        }
        return null;
    }

    public @Nullable String optionalChildText(final String childName) {
        @Nullable final JElement child = firstChild(childName);
        return child == null ? null : child.getText();
    }

}
