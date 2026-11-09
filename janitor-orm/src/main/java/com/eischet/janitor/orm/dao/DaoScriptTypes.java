// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.metadata.ScriptType.Parameter;

import java.util.List;

import static com.eischet.janitor.api.metadata.ScriptType.*;

/**
 * The types of the script methods that every DAO has, for {@link com.eischet.janitor.api.Janitor.MetaData#RETURNS} and
 * {@link com.eischet.janitor.api.Janitor.MetaData#PARAMETERS}. {@link ScriptType#ENTITY} stands for the entities of the DAO
 * (see {@link GenericDao#dispatcherFor(com.eischet.janitor.api.types.dispatch.Dispatcher, com.eischet.janitor.api.types.dispatch.Dispatcher)}).
 */
final class DaoScriptTypes {

    /** A list of entities. */
    static final ScriptType ENTITIES = listOf(ENTITY);

    /** The condition of select and count: a lambda that tests an entity (a filter map works too, but tells tools nothing). */
    static final List<Parameter> CONDITION = parameters(parameter("condition", function(BOOL, ENTITY)));

    /** One entity, or a map to fill one from. */
    static final List<Parameter> ONE_ENTITY = parameters(parameter("entity", ENTITY));

    /** An ID. */
    static final List<Parameter> ID = parameters(parameter("id", INT));

    /** A key. */
    static final List<Parameter> KEY = parameters(parameter("key", STRING));

    /** A list of IDs. */
    static final List<Parameter> IDS = parameters(parameter("ids", LIST));

    /** The SQL and the callback of queryForEach. */
    static final List<Parameter> QUERY_FOR_EACH = parameters(parameter("sql", STRING), parameter("callback", function(ANY, ENTITY, INT)));

    private DaoScriptTypes() {
    }
}
