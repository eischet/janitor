// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Options for listing messages. Scripts pass them as a map, e.g.
 * {@code client.messages(user, "inbox", {select: ["id", "subject", "uniqueBody"], top: 20, filter: "isRead eq false"})}.
 */
public class MessageQuery extends JanitorComposed<MessageQuery> {

    /** The properties loaded when the caller does not choose any. */
    public static final List<String> DEFAULT_SELECT = List.of("id", "subject", "sender", "categories");

    public static final DispatchTable<MessageQuery> DISPATCH = new DispatchTable<>(MessageQuery::new);

    static {
        DISPATCH.addListOfStringsProperty("select", MessageQuery::getSelect, MessageQuery::setSelect);
        DISPATCH.addIntegerProperty("top", MessageQuery::getTop, MessageQuery::setTop);
        DISPATCH.addStringProperty("orderBy", MessageQuery::getOrderBy, MessageQuery::setOrderBy);
        DISPATCH.addStringProperty("filter", MessageQuery::getFilter, MessageQuery::setFilter);
        DISPATCH.addStringProperty("bodyType", MessageQuery::getBodyType, MessageQuery::setBodyType);
    }

    private @NotNull List<String> select = DEFAULT_SELECT;
    private int top = 10;
    private @Nullable String orderBy = "receivedDateTime asc";
    private @Nullable String filter;
    private @Nullable String bodyType;

    public MessageQuery() {
        super(DISPATCH);
    }

    /**
     * @return the properties to load ("$select"); "uniqueBody" and "body" are only returned when selected
     */
    public @NotNull List<String> getSelect() {
        return select;
    }

    public MessageQuery setSelect(final @Nullable List<String> select) {
        this.select = select == null || select.isEmpty() ? DEFAULT_SELECT : List.copyOf(select);
        return this;
    }

    /**
     * @return the page size ("$top"); all pages are read regardless
     */
    public int getTop() {
        return top;
    }

    public MessageQuery setTop(final int top) {
        this.top = top;
        return this;
    }

    public @Nullable String getOrderBy() {
        return orderBy;
    }

    public MessageQuery setOrderBy(final @Nullable String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * @return an OData filter ("$filter"), e.g. "isRead eq false"
     */
    public @Nullable String getFilter() {
        return filter;
    }

    public MessageQuery setFilter(final @Nullable String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * @return "text" or "html" to request body and unique body in that format (header
     * {@code Prefer: outlook.body-content-type}); null leaves the Graph default, which is HTML
     */
    public @Nullable String getBodyType() {
        return bodyType;
    }

    public MessageQuery setBodyType(final @Nullable String bodyType) {
        this.bodyType = bodyType;
        return this;
    }
}
