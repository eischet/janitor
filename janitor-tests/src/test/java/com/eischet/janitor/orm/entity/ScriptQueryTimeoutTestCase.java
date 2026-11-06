// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Team;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Teams;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A query that a script runs gets the time its run time limit leaves it as query timeout, so a slow query cannot hold the script
 * beyond its limit.
 */
public class ScriptQueryTimeoutTestCase extends JanitorTest {

    private SimpleDataManager manager;
    private Teams teams;

    /** Twenty teams. */
    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        teams = new Teams(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_s_team start with 1"));
            conn.update(UpdateStatement.of("create table s_team (team_id bigint primary key, team_key varchar(20), team_name varchar(50))"));
            for (int i = 1; i <= 20; i++) {
                final Team team = new Team(teams);
                team.setKey("T" + i);
                teams.getTeamDao().insert(conn, team);
            }
        });
    }

    private void run(final String script, final Duration maxRunTime) throws Exception {
        OutputCatchingTestRuntime.fresh().compile("test", script).run(process -> {
            process.setMaxRunTime(maxRunTime);
            process.bind("teamDao", teams.getTeamDao());
        });
    }


    @Test
    void slowQueryStopsAtTheLimit() {
        // counts up to three billion without finding a row, which takes far longer than the limit
        final long start = System.nanoTime();
        final Exception error = assertThrows(Exception.class, () -> run(
                "teamDao.queryForEach(\"select x from system_range(1, 3000000000) where mod(x, 1000000007) = -1\", (team, id) -> {});", Duration.ofSeconds(1)));
        final long millis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(millis < 3000, "the query ran " + millis + " ms with a limit of 1 s: " + error);
        assertTrue(isTimeout(error), "expected a query timeout, but got " + error);
    }

    @Test
    void queryWithinTheLimitRuns() throws Exception {
        run("count = 0; teamDao.queryForEach(\"select team_id from s_team\", (team, id) -> { count = count + 1; }); assert(count == 20);"
                + " assert(teamDao.count(t -> t.key == \"T1\") == 1); assert(teamDao.select(t -> t.key == \"T2\").size() == 1);", Duration.ofSeconds(10));
    }

    @Test
    void remainingRunTime() throws Exception {
        final AtomicReference<JanitorScriptProcess> seen = new AtomicReference<>();
        OutputCatchingTestRuntime.fresh().compile("test", "1;").run(seen::set);
        assertNull(seen.get().getRemainingRunTime());

        final AtomicReference<Duration> remaining = new AtomicReference<>();
        OutputCatchingTestRuntime.fresh().compile("test", "probe();").run(process -> {
            process.setMaxRunTime(Duration.ofSeconds(5));
            process.bind("probe", new JNativeMethod((p, args) -> {
                remaining.set(p.getRemainingRunTime());
                return Janitor.NULL;
            }));
        });
        assertNotNull(remaining.get());
        assertTrue(remaining.get().compareTo(Duration.ZERO) > 0 && remaining.get().compareTo(Duration.ofSeconds(5)) <= 0, "remaining: " + remaining.get());
    }

    private static boolean isTimeout(final Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof DatabaseError databaseError && databaseError.isTimeout()) {
                return true;
            }
        }
        return false;
    }
}
