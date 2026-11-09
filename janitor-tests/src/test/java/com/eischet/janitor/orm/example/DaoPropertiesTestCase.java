// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.TestDb;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.scopes.ScriptBindings;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.tooling.JanitorCompletions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The DAOs of a collection as properties of its dispatch table, with the dispatchers of the DAOs for editors. */
public class DaoPropertiesTestCase extends JanitorTest {

    @Test
    void daosArePropertiesWithTheirDispatchers() throws Exception {
        final ExampleDaoCollection collection = new ExampleDaoCollection(TestDb.newManager());
        assertEquals(List.of(), collection.getUndeclaredDaos());
        // the declared type has the members of the DAO
        final Dispatcher<?> type = ExampleDaoCollection.DISPATCH.getMetaData("Article", Janitor.MetaData.DISPATCHER);
        assertNotNull(type);
        assertEquals(collection.getArticleDao().getDispatcher().streamAttributeNames().sorted().toList(),
                type.streamAttributeNames().sorted().toList());
        assertEquals("Article: The DAO of Article.", ExampleDaoCollection.DISPATCH.getMetaData("Article", Janitor.MetaData.HELP));
        assertSame(collection.getArticleDao(), OutputCatchingTestRuntime.fresh().compile("test", "return example.Article;")
                .run(process -> process.bind("example", collection)));
    }

    @Test
    void editorsKnowTheEntitiesOfADao() {
        final JanitorCompletions completions = new JanitorCompletions(OutputCatchingTestRuntime.fresh());
        final List<ScriptBindings.Declaration> declared = ScriptBindings.<Object>create()
                .declare("example", "ExampleDaoCollection", ExampleDaoCollection.DISPATCH, "The DAOs.", context -> null)
                .declarations();
        final String lambda = "example.Article.select(a -> a.";
        final JanitorCompletions.Completion name = completions.complete(lambda, 1, lambda.length(), declared).stream()
                .filter(c -> c.name().equals("name")).findFirst().orElseThrow();
        assertEquals("Article", name.owners());
        final String loop = "for (a in example.Article.findAll()) {\n    a.";
        assertTrue(completions.complete(loop, 2, 6, declared).stream().anyMatch(c -> c.name().equals("shortCode")));
        final String byId = "example.Article.findById(1).";
        assertTrue(completions.complete(byId, 1, byId.length(), declared).stream().anyMatch(c -> c.name().equals("shortCode")));
    }
}
