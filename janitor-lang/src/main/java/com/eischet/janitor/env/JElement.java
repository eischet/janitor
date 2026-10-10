// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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

/** An element of an XML document, as seen by scripts: it has a name, attributes, text, and child elements. */
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

    /**
     * @return the name of this element, or null
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Sets the name of this element.
     * @param name the new name, or null
     */
    public void setName(final @Nullable String name) {
        this.name = name;
    }

    /**
     * @return the attributes of this element, as a map
     */
    public @NotNull JMap getAttributes() {
        if (attributes == null) {
            attributes = Janitor.map();
        }
        return attributes;
    }

    /**
     * @return the child elements, as a list
     */
    public @NotNull JList getChildren() {
        if (children == null) {
            children = Janitor.list();
        }
        return children;
    }

    /**
     * @return the text of this element, or null
     */
    public @Nullable String getText() {
        return text;
    }

    /**
     * Sets the text of this element.
     * @param text the new text, or null
     */
    public void setText(@Nullable final String text) {
        this.text = text;
    }

    /**
     * Finds the first child element with the given name.
     * @param name the name of the child
     * @return the child
     * @throws IllegalArgumentException if there is no such child
     */
    public @NotNull JElement requireFirstChild(final String name) throws IllegalArgumentException {
        final JElement child = firstChild(name);
        if (child == null) {
            throw new IllegalArgumentException("Missing child element '" + name + "'");
        }
        return child;
    }

    /**
     * Finds the first child element with the given name.
     * @param name the name of the child
     * @return the child, or null if there is none
     */
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

    /**
     * Gets the text of the first child element with the given name.
     * @param childName the name of the child
     * @return the text, or null if there is no such child
     */
    public @Nullable String optionalChildText(final String childName) {
        @Nullable final JElement child = firstChild(childName);
        return child == null ? null : child.getText();
    }

}
