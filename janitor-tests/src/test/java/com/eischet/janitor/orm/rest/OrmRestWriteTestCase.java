// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.HasModifiedDate;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.Versioned;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Creating, changing and deleting entities through the handler, against a real (H2) database. */
public class OrmRestWriteTestCase extends JanitorTest {

    // --- fixtures: an unversioned folder, and a versioned document in a folder; both may be written ---

    public static class Folder implements OrmEntity {
        static final ForeignKeyNull<Folder> NULL = new ForeignKeyNull<>(Folder.class);
        static final EntityDispatchTable<Folder, WriteCollection> DISPATCH = new EntityDispatchTable<>(Folder.class, up -> new Folder(), NULL, WriteCollection::getFolderDao);

        static {
            DISPATCH.dbTable("w_folder", "folder_id", "folder_key", "folder_name", "seq_w_folder");
            DISPATCH.addLongColumn("id", "folder_id", Folder::getId, Folder::setId);
            DISPATCH.addStringColumn("key", "folder_key", Folder::getKey, Folder::setKey, 20);
            DISPATCH.addStringColumn("name", "folder_name", Folder::getName, Folder::setName, 50);
            RestOrm.allow(RestOrm.expose(DISPATCH, "folders"), RestOperation.CREATE, RestOperation.UPDATE, RestOperation.DELETE);
        }

        private long id;
        private String key;
        private String name;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    public static class Doc implements OrmEntity, HasModifiedDate, Versioned {
        static final ForeignKeyNull<Doc> NULL = new ForeignKeyNull<>(Doc.class);
        static final EntityDispatchTable<Doc, WriteCollection> DISPATCH = new EntityDispatchTable<>(Doc.class, Doc::new, NULL, WriteCollection::getDocDao);

        static {
            DISPATCH.dbTable("w_doc", "doc_id", "doc_key", "doc_title", "seq_w_doc").versionColumn("version_no").modifiedColumn("modified_at");
            DISPATCH.addLongColumn("id", "doc_id", Doc::getId, Doc::setId);
            DISPATCH.addStringColumn("key", "doc_key", Doc::getKey, Doc::setKey, 30);
            DISPATCH.addStringColumn("name", "doc_title", Doc::getName, Doc::setName, 10);
            DISPATCH.addIntegerColumn("rank", "doc_rank", Doc::getRank, Doc::setRank);
            DISPATCH.addDateTimeColumn("due", "doc_due", Doc::getDue, Doc::setDue);
            RestOrm.hidden(DISPATCH.addLongColumn("modifiedBy", "doc_modified_by", Doc::getModifiedBy, Doc::setModifiedBy));
            RestOrm.hidden(DISPATCH.addStringColumn("secret", "doc_secret", Doc::getSecret, Doc::setSecret, 40));
            DISPATCH.addDateTimeColumn("modifiedDate", "modified_at", Doc::getModifiedDate, Doc::setModifiedDate);
            DISPATCH.addLongColumn("version", "version_no", Doc::getVersion, Doc::setVersion);
            DISPATCH.addReference("folder", "folder_id", Folder.DISPATCH, Doc::getFolder, Doc::setFolder, doc -> doc.source);
            RestOrm.allow(RestOrm.expose(DISPATCH, "docs"), RestOperation.CREATE, RestOperation.UPDATE, RestOperation.DELETE);
        }

        private final WriteCollection source;
        private long id;
        private String key;
        private String name;
        private int rank;
        private LocalDateTime due;
        private long modifiedBy;
        private String secret = "initial-secret";
        private LocalDateTime modifiedDate;
        private long version;
        private ForeignKey<Folder> folder = Folder.NULL;

        Doc(final WriteCollection source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        @Override public @Nullable LocalDateTime getModifiedDate() { return modifiedDate; }
        @Override public void setModifiedDate(final @Nullable LocalDateTime updateDate) { this.modifiedDate = updateDate; }
        @Override public long getVersion() { return version; }
        @Override public void setVersion(final long version) { this.version = version; }
        int getRank() { return rank; }
        void setRank(final int rank) { this.rank = rank; }
        long getModifiedBy() { return modifiedBy; }
        void setModifiedBy(final long modifiedBy) { this.modifiedBy = modifiedBy; }
        LocalDateTime getDue() { return due; }
        void setDue(final LocalDateTime due) { this.due = due; }
        String getSecret() { return secret; }
        void setSecret(final String secret) { this.secret = secret; }
        ForeignKey<Folder> getFolder() { return folder; }
        void setFolder(final ForeignKey<Folder> folder) { this.folder = folder; }
    }

    public static class FolderDao extends GenericDao<Folder, WriteCollection> {
        FolderDao(final WriteCollection c) { super(new DispatchTable<FolderDao>(false), c, Folder.class, Folder.DISPATCH, Folder::new); }
        @Override public @NotNull Class<Folder> getEntityClass() { return Folder.class; }
        @Override public @NotNull String getEntityClassName() { return "Folder"; }
    }

    public static class DocDao extends GenericDao<Doc, WriteCollection> {
        DocDao(final WriteCollection c) { super(new DispatchTable<DocDao>(false), c, Doc.class, Doc.DISPATCH, () -> new Doc(c)); }
        @Override public @NotNull Class<Doc> getEntityClass() { return Doc.class; }
        @Override public @NotNull String getEntityClassName() { return "Doc"; }
    }

    public static class WriteCollection extends OrmDaoCollection<WriteCollection> implements Uplink {
        static final DispatchTable<WriteCollection> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final FolderDao folderDao;
        private final DocDao docDao;

        WriteCollection(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.folderDao = new FolderDao(this);
            this.docDao = new DocDao(this);
        }

        FolderDao getFolderDao() { return folderDao; }
        DocDao getDocDao() { return docDao; }
        @Override public DataManager getDataManager() { return dataManager; }
        @Override public @NotNull Clock getClock() { return Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC); }
    }

    private SimpleDataManager manager;
    private WriteCollection collection;
    private OrmRestHandler<Object> handler;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        collection = new WriteCollection(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_w_folder start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_w_doc start with 1"));
            conn.update(UpdateStatement.of("create table w_folder (folder_id bigint primary key, folder_key varchar(20) unique, folder_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table w_doc (
                      doc_id bigint primary key, doc_key varchar(30), doc_title varchar(10), doc_rank integer, doc_due timestamp, doc_modified_by bigint, doc_secret varchar(40),
                      modified_at timestamp, version_no bigint, folder_id bigint references w_folder(folder_id))"""));
            final Folder inbox = new Folder();
            inbox.setKey("inbox");
            inbox.setName("Inbox");
            collection.getFolderDao().insert(conn, inbox);
            final Folder archive = new Folder();
            archive.setKey("archive");
            archive.setName("Archive");
            collection.getFolderDao().insert(conn, archive);
        });
        handler = OrmRestHandler.of(collection, RestPolicy.allowAll());
    }

    // --- helpers ---

    private RestResponse send(final String method, final String path, final @Nullable String body, final String... headers) {
        return send(handler, method, path, body, headers);
    }

    private static RestResponse send(final OrmRestHandler<Object> handler, final String method, final String path, final @Nullable String body, final String... headers) {
        final Map<String, String> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i += 2) {
            headerMap.put(headers[i], headers[i + 1]);
        }
        return handler.handle(new RestRequest<>(method, path, Map.of(), headerMap, body, null));
    }

    private static String compact(final RestResponse response) {
        assertNotNull(response.body(), "no body, status " + response.status());
        return response.body().replaceAll("\\s", "");
    }

    private static JMap json(final RestResponse response) throws Exception {
        return Janitor.current().parseJsonToMap(response.body());
    }

    private static long idOf(final RestResponse response) throws Exception {
        return ((Number) json(response).get(Janitor.string("id")).janitorGetHostValue()).longValue();
    }

    /** Creates a document and returns its id. */
    private long createDoc(final String key, final String name, final int rank) throws Exception {
        final RestResponse response = send("POST", "/docs", "{\"key\":\"" + key + "\",\"name\":\"" + name + "\",\"rank\":" + rank + "}");
        assertEquals(201, response.status(), response.body());
        return idOf(response);
    }

    private int count(final String path) throws Exception {
        return ((JMap) Janitor.current().parseJsonToMap(send("GET", path, null).body())).get(Janitor.string("total")).janitorGetHostValue() instanceof Number n ? n.intValue() : -1;
    }

    private Doc stored(final long id) throws DatabaseError {
        return manager.callTransaction(conn -> collection.getDocDao().findByIdIgnoringCache(conn, id));
    }

    // --- create ---

    @Test
    public void createsAnEntityAndAnswersWithWhatWasStored() throws Exception {
        final RestResponse response = send("POST", "/docs", "{\"key\":\"d1\",\"name\":\"First\",\"rank\":3,\"folder\":\"inbox\"}");
        assertEquals(201, response.status(), response.body());
        final long id = idOf(response);
        assertEquals("docs/" + id, response.headers().get("Location"));
        assertEquals("\"0\"", response.headers().get("ETag"));
        assertEquals("Fri, 2 Jan 2026 03:04:05 GMT", response.headers().get("Last-Modified"));
        final String body = compact(response);
        assertTrue(body.contains("\"key\":\"d1\"") && body.contains("\"name\":\"First\"") && body.contains("\"rank\":3"), body);
        assertTrue(body.contains("\"folder\":\"inbox\""), body);
        assertTrue(body.contains("\"version\":0"), body);
        assertFalse(body.contains("secret"), body);
        assertEquals(200, send("GET", "/docs/" + id, null).status(), "the entity is there");
        assertEquals("initial-secret", stored(id).getSecret(), "hidden properties are never touched by clients");
    }

    @Test
    public void readOnlyPropertiesInTheBodyAreIgnored() throws Exception {
        final RestResponse response = send("POST", "/docs", "{\"id\":999,\"version\":42,\"modifiedDate\":\"2001-01-01T00:00:00Z\",\"key\":\"d2\",\"name\":\"x\",\"rank\":1}");
        assertEquals(201, response.status(), response.body());
        assertNotEquals(999, idOf(response));
        assertTrue(compact(response).contains("\"version\":0"));
        assertTrue(compact(response).contains("\"modifiedDate\":\"2026-01-02T03:04:05Z\""));
    }

    @Test
    public void propertiesThatAreMissingKeepTheValuesOfANewEntity() throws Exception {
        final String body = compact(send("POST", "/docs", "{\"name\":\"x\"}"));
        assertTrue(body.contains("\"rank\":0") && body.contains("\"key\":null") && body.contains("\"folder\":null"), body);
    }

    @Test
    public void refusesBodiesThatAreNotAcceptable() throws Exception {
        assertEquals(400, send("POST", "/docs", null).status(), "no body");
        assertEquals(400, send("POST", "/docs", "  ").status(), "blank body");
        assertEquals(400, send("POST", "/docs", "{not json").status(), "malformed");
        assertEquals(400, send("POST", "/docs", "{\"rank\":1,\"nope\":1}").status(), "unknown property");
        assertEquals(400, send("POST", "/docs", "{\"rank\":1,\"secret\":\"mine\"}").status(), "hidden property");
        assertEquals(400, send("POST", "/docs", "{\"rank\":\"high\"}").status(), "wrong type");
        assertEquals(400, send("POST", "/docs", "{\"rank\":1,\"name\":\"far too long for ten\"}").status(), "longer than the column");
        assertEquals(400, send("POST", "/docs", "{\"rank\":1,\"folder\":\"nowhere\"}").status(), "reference to something that does not exist");
        assertEquals(0, count("/docs"), "nothing was created");
    }

    @Test
    public void dateTimesAreWrittenWithOrWithoutOffset() throws Exception {
        final long id = createDoc("dt", "dates", 1);
        final RestResponse withOffset = send("PATCH", "/docs/" + id, "{\"due\":\"2026-03-04T05:06:07Z\"}", "If-Match", "\"0\"");
        assertEquals(200, withOffset.status(), withOffset.body());
        assertEquals(LocalDateTime.of(2026, 3, 4, 5, 6, 7), stored(id).getDue());
        assertTrue(compact(withOffset).contains("\"due\":\"2026-03-04T05:06:07Z\""), compact(withOffset));
        final RestResponse withOtherOffset = send("PATCH", "/docs/" + id, "{\"due\":\"2026-03-04T07:06:07+02:00\"}", "If-Match", "\"1\"");
        assertEquals(200, withOtherOffset.status(), withOtherOffset.body());
        assertEquals(LocalDateTime.of(2026, 3, 4, 5, 6, 7), stored(id).getDue(), "the same moment");
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"due\":\"2026-05-06T07:08:09\"}", "If-Match", "\"2\"").status(), "local date-times are taken as they are");
        assertEquals(LocalDateTime.of(2026, 5, 6, 7, 8, 9), stored(id).getDue());
        assertEquals(400, send("PATCH", "/docs/" + id, "{\"due\":\"yesterday\"}", "If-Match", "\"3\"").status());
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"due\":null}", "If-Match", "\"3\"").status());
        assertNull(stored(id).getDue());
    }

    @Test
    public void nullIsRefusedForPropertiesThatCannotBeNull() throws Exception {
        final long id = createDoc("nn", "nonnull", 1);
        assertEquals(400, send("PATCH", "/docs/" + id, "{\"rank\":null}", "If-Match", "\"0\"").status());
        assertEquals(1, stored(id).getRank());
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"name\":null}", "If-Match", "\"0\"").status());
        assertNull(stored(id).getName());
    }

    @Test
    public void duplicateKeysAreConflicts() {
        assertEquals(201, send("POST", "/folders", "{\"key\":\"new\",\"name\":\"New\"}").status());
        assertEquals(409, send("POST", "/folders", "{\"key\":\"new\",\"name\":\"Again\"}").status());
    }

    @Test
    public void aRestrictionAlsoCoversWhatIsCreatedAndChanged() throws Exception {
        final long id = createDoc("mine", "mine", 1);
        final RestPolicy<Object> ownRows = new RestPolicy<Object>() {
            @Override public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return true; }
            @Override public @Nullable FilterExpression restriction(final @Nullable Object principal, final @NotNull ApiEntity entity) {
                return entity.path().equals("docs") ? FilterExpression.from("key", FilterOperator.STARTSWITH, "mine") : null;
            }
        };
        final OrmRestHandler<Object> restricted = OrmRestHandler.of(collection, ownRows);
        assertEquals(401, send(restricted, "POST", "/docs", "{\"key\":\"theirs\",\"rank\":1}").status(), "created outside of the restriction");
        assertEquals(1, count("/docs"), "and rolled back");
        assertEquals(201, send(restricted, "POST", "/docs", "{\"key\":\"mine2\",\"rank\":1}").status());
        assertEquals(401, send(restricted, "PATCH", "/docs/" + id, "{\"key\":\"theirs\"}", "If-Match", "\"0\"").status(), "changed to outside of the restriction");
        assertEquals("mine", stored(id).getKey(), "and rolled back");
        final long other = createDoc("other", "o", 1);
        assertEquals(404, send(restricted, "PATCH", "/docs/" + other, "{\"rank\":2}", "If-Match", "\"0\"").status(), "not visible: not found");
        assertEquals(404, send(restricted, "DELETE", "/docs/" + other, null, "If-Match", "\"0\"").status());
    }

    // --- update ---

    @Test
    public void ifMatchIsCheckedWhenTheClientSendsIt() throws Exception {
        final long id = createDoc("d3", "three", 3);
        final RestResponse stale = send("PATCH", "/docs/" + id, "{\"rank\":4}", "If-Match", "\"7\"");
        assertEquals(412, stale.status());
        assertEquals("\"0\"", stale.headers().get("ETag"), "tells the current version");
        assertEquals(3, stored(id).getRank(), "nothing changed");
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":4}", "If-Match", "\"0\"").status());
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":5}", "if-match", "W/\"1\", \"9\"").status());
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":6}", "If-Match", "*").status());
        assertEquals(6, stored(id).getRank());
        assertEquals(3, stored(id).getVersion());
    }

    @Test
    public void withoutIfMatchTheLastWriteWinsAndTheVersionStillCountsUp() throws Exception {
        final long id = createDoc("d3b", "three", 3);
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":4}").status());
        assertEquals(200, send("PUT", "/docs/" + id, "{\"rank\":5}").status());
        assertEquals(5, stored(id).getRank());
        assertEquals(2, stored(id).getVersion());
        assertEquals(204, send("DELETE", "/docs/" + id, null).status());
    }

    @Test
    public void entitiesCanInsistOnIfMatch() throws Exception {
        final long id = createDoc("d3c", "three", 3);
        RestOrm.requireIfMatch(Doc.DISPATCH);
        try {
            final RestResponse missing = send("PATCH", "/docs/" + id, "{\"rank\":4}");
            assertEquals(428, missing.status());
            assertEquals("\"0\"", missing.headers().get("ETag"), "tells the current version");
            assertEquals(428, send("PUT", "/docs/" + id, "{\"rank\":4}").status());
            assertEquals(428, send("DELETE", "/docs/" + id, null).status());
            assertEquals(3, stored(id).getRank());
            assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":4}", "If-Match", "\"0\"").status());
            assertEquals(201, send("POST", "/docs", "{\"rank\":1}").status(), "creating needs no version");
        } finally {
            Doc.DISPATCH.setMetaData(RestMetaData.REQUIRE_IF_MATCH, null);
        }
    }

    @Test
    public void patchChangesOnlyWhatIsGiven() throws Exception {
        final long id = createDoc("d4", "four", 4);
        final RestResponse response = send("PATCH", "/docs/" + id, "{\"name\":\"quattro\",\"folder\":\"archive\"}", "If-Match", "\"0\"");
        assertEquals(200, response.status(), response.body());
        assertEquals("\"1\"", response.headers().get("ETag"));
        final String body = compact(response);
        assertTrue(body.contains("\"name\":\"quattro\"") && body.contains("\"folder\":\"archive\"") && body.contains("\"rank\":4") && body.contains("\"key\":\"d4\""), body);
        assertTrue(body.contains("\"version\":1"), body);
    }

    @Test
    public void putReplacesEverything() throws Exception {
        final long id = createDoc("d5", "five", 5);
        send("PATCH", "/docs/" + id, "{\"folder\":\"inbox\"}", "If-Match", "\"0\"");
        assertEquals(400, send("PUT", "/docs/" + id, "{\"name\":\"x\"}", "If-Match", "\"1\"").status(), "rank is required");
        final RestResponse response = send("PUT", "/docs/" + id, "{\"rank\":9,\"name\":\"nine\"}", "If-Match", "\"1\"");
        assertEquals(200, response.status(), response.body());
        final String body = compact(response);
        assertTrue(body.contains("\"rank\":9") && body.contains("\"name\":\"nine\""), body);
        assertTrue(body.contains("\"key\":null") && body.contains("\"folder\":null"), "what is missing is set to null: " + body);
        assertEquals("initial-secret", stored(id).getSecret(), "hidden properties are not part of the replacement");
    }

    @Test
    public void ofTwoChangesBasedOnTheSameVersionOnlyTheFirstWins() throws Exception {
        final long id = createDoc("d6", "six", 6);
        assertEquals(200, send("PATCH", "/docs/" + id, "{\"rank\":1}", "If-Match", "\"0\"").status());
        assertEquals(412, send("PATCH", "/docs/" + id, "{\"rank\":2}", "If-Match", "\"0\"").status());
        assertEquals(1, stored(id).getRank());
    }

    @Test
    public void aBadReferenceLeavesTheEntityAlone() throws Exception {
        final long id = createDoc("d7", "seven", 7);
        assertEquals(400, send("PATCH", "/docs/" + id, "{\"rank\":8,\"folder\":\"nowhere\"}", "If-Match", "\"0\"").status());
        assertEquals(7, stored(id).getRank());
        assertEquals(0, stored(id).getVersion());
    }

    @Test
    public void unversionedEntitiesNeedNoIfMatch() throws Exception {
        final RestResponse created = send("POST", "/folders", "{\"key\":\"misc\",\"name\":\"Misc\"}");
        assertEquals(201, created.status(), created.body());
        assertNull(created.headers().get("ETag"));
        final long id = idOf(created);
        assertEquals(200, send("PATCH", "/folders/" + id, "{\"name\":\"Other\"}").status());
        assertEquals(200, send("PUT", "/folders/" + id, "{\"key\":\"misc\",\"name\":\"Third\"}").status());
        assertEquals(204, send("DELETE", "/folders/" + id, null).status());
    }

    @Test
    public void unknownIdsAreNotFound() {
        assertEquals(404, send("PATCH", "/docs/12345", "{\"rank\":1}", "If-Match", "*").status());
        assertEquals(404, send("PUT", "/docs/12345", "{\"rank\":1}", "If-Match", "*").status());
        assertEquals(404, send("DELETE", "/docs/12345", null, "If-Match", "*").status());
        assertEquals(400, send("DELETE", "/docs/abc", null).status());
    }

    // --- delete ---

    @Test
    public void deletesVersionedEntitiesWithIfMatch() throws Exception {
        final long id = createDoc("d8", "eight", 8);
        assertEquals(412, send("DELETE", "/docs/" + id, null, "If-Match", "\"3\"").status());
        assertNotNull(stored(id));
        final RestResponse deleted = send("DELETE", "/docs/" + id, null, "If-Match", "\"0\"");
        assertEquals(204, deleted.status());
        assertNull(deleted.body());
        assertNull(deleted.contentType());
        assertEquals(404, send("GET", "/docs/" + id, null).status());
    }

    @Test
    public void somethingThatIsStillReferencedCannotBeDeleted() throws Exception {
        createDoc("d9", "nine", 9);
        send("PATCH", "/docs/" + idOfDoc("d9"), "{\"folder\":\"inbox\"}", "If-Match", "\"0\"");
        final long inbox = idOf(send("GET", "/folders/by-key/inbox", null));
        assertEquals(409, send("DELETE", "/folders/" + inbox, null).status());
        assertEquals(200, send("GET", "/folders/" + inbox, null).status());
    }

    private long idOfDoc(final String key) throws Exception {
        return idOf(send("GET", "/docs/by-key/" + key, null));
    }

    // --- routing and policy ---

    @Test
    public void methodsThatTheEntityDoesNotOfferAreNotAllowed() {
        final RestResponse collectionPost = send("PUT", "/docs", "{}");
        assertEquals(405, collectionPost.status());
        assertEquals("GET, POST", collectionPost.headers().get("Allow"));
        assertEquals("GET, PUT, PATCH, DELETE", send("POST", "/docs/1", "{}").headers().get("Allow"));
        assertEquals(405, send("DELETE", "/docs/by-key/x", null).status());
    }

    @Test
    public void thePolicyDecidesAboutEveryWriteOperation() throws Exception {
        final long id = createDoc("d10", "ten", 10);
        for (final RestOperation denied : List.of(RestOperation.CREATE, RestOperation.UPDATE, RestOperation.DELETE)) {
            final OrmRestHandler<Object> limited = OrmRestHandler.of(collection, (principal, entity, operation) -> operation != denied);
            assertEquals(denied != RestOperation.CREATE ? 201 : 401, send(limited, "POST", "/docs", "{\"rank\":1}").status());
            assertEquals(denied != RestOperation.UPDATE ? 200 : 401, send(limited, "PATCH", "/docs/" + id, "{\"rank\":" + (denied == RestOperation.UPDATE ? 1 : 2) + "}", "If-Match", "*").status());
            assertEquals(denied != RestOperation.DELETE ? 204 : 401, send(limited, "DELETE", "/docs/" + (denied == RestOperation.DELETE ? id : createDoc("tmp" + denied, "t", 1)), null, "If-Match", "*").status());
        }
    }

    @Test
    public void deniedFieldsCannotBeWritten() throws Exception {
        final OrmRestHandler<Object> noRank = OrmRestHandler.of(collection, new RestPolicy<Object>() {
            @Override public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return true; }
            @Override public @NotNull java.util.Set<String> deniedFields(final @Nullable Object principal, final @NotNull ApiEntity entity) { return java.util.Set.of("rank"); }
        });
        assertEquals(400, send(noRank, "POST", "/docs", "{\"rank\":1}").status());
        final RestResponse created = send(noRank, "POST", "/docs", "{\"key\":\"nr\"}");
        assertEquals(201, created.status(), created.body());
        assertFalse(compact(created).contains("\"rank\""), "denied properties are also left out of the answer");
    }

    // --- the API description ---

    @Test
    @SuppressWarnings("unchecked")
    public void theDescriptionCoversTheOperationsThatAreOffered() {
        final Map<String, Object> document = OpenApiEmitter.of(OrmApiModel.of(collection)).build();
        final Map<String, Object> paths = (Map<String, Object>) document.get("paths");
        assertTrue(((Map<String, Object>) paths.get("/docs")).keySet().containsAll(List.of("get", "post")));
        final Map<String, Object> item = (Map<String, Object>) paths.get("/docs/{id}");
        assertTrue(item.keySet().containsAll(List.of("get", "put", "patch", "delete")), item.keySet().toString());
        final List<Object> parameters = (List<Object>) ((Map<String, Object>) item.get("patch")).get("parameters");
        assertTrue(parameters.stream().anyMatch(p -> "If-Match".equals(((Map<String, Object>) p).get("name"))), "versioned: If-Match is a parameter");
        final Map<String, Object> responses = (Map<String, Object>) ((Map<String, Object>) item.get("patch")).get("responses");
        assertTrue(responses.containsKey("412"));
        assertFalse(responses.containsKey("428"), "only for entities that insist on the header");
        assertEquals(Boolean.FALSE, ((Map<String, Object>) parameters.stream().filter(p -> "If-Match".equals(((Map<String, Object>) p).get("name"))).findFirst().orElseThrow()).get("required"));
        final Map<String, Object> folderItem = (Map<String, Object>) paths.get("/folders/{id}");
        final List<Object> folderParameters = (List<Object>) ((Map<String, Object>) folderItem.get("patch")).get("parameters");
        assertEquals(1, folderParameters.size(), "unversioned: only the id");
        assertFalse(((Map<String, Object>) ((Map<String, Object>) folderItem.get("patch")).get("responses")).containsKey("412"));
        final List<String> refs = new ArrayList<>();
        collectRefs(document, refs);
        for (final String ref : refs) {
            Object node = document;
            for (final String part : ref.substring(2).split("/")) {
                node = ((Map<?, ?>) node).get(part);
                assertNotNull(node, "dangling reference " + ref);
            }
        }
    }

    private static void collectRefs(final Object node, final List<String> refs) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                if ("$ref".equals(key)) {
                    refs.add((String) value);
                } else {
                    collectRefs(value, refs);
                }
            });
        } else if (node instanceof List<?> list) {
            list.forEach(element -> collectRefs(element, refs));
        }
    }
}
