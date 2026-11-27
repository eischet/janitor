// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorPermissionException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Member;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Membership;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Team;
import com.eischet.janitor.orm.entity.JoinedListScriptAddTestCase.Teams;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A collection decides whether scripts may read through its DAOs: all script methods that fetch records ask it first. What a script was
 * handed stays usable, including the references of those entities, and so does writing and Java code that calls the DAOs directly.
 */
class ScriptReadCheckTestCase extends JanitorTest {

    private static final MetaDataKey<Boolean> READER = new MetaDataKey<>("READER", Boolean.class);

    /** Lets a script read only with the right. */
    private static class GuardedTeams extends Teams {
        GuardedTeams(final DataManager dataManager) {
            super(dataManager);
        }

        @Override
        public void checkScriptRead(final @NotNull JanitorScriptProcess process) throws JanitorRuntimeException {
            process.require(READER);
        }
    }

    private SimpleDataManager manager;
    private Teams teams;
    private Team team;

    /** A team with the member alice. */
    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        teams = new GuardedTeams(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_s_team start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_s_member start with 1"));
            conn.update(UpdateStatement.of("create table s_team (team_id bigint primary key, team_key varchar(20), team_name varchar(50))"));
            conn.update(UpdateStatement.of("create table s_member (member_id bigint primary key, member_key varchar(20), member_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table s_membership (
                      team_id bigint not null references s_team(team_id),
                      member_id bigint not null references s_member(member_id),
                      primary key (team_id, member_id))"""));
            final Team newTeam = new Team(teams);
            newTeam.setKey("T1");
            teams.getTeamDao().insert(conn, newTeam);
            final Member alice = new Member();
            alice.setKey("ALICE");
            teams.getMemberDao().insert(conn, alice);
            final Membership membership = new Membership(teams);
            membership.setTeam(newTeam);
            membership.setMember(alice);
            teams.getMembershipDao().insert(conn, membership);
        });
        team = manager.callTransaction(conn -> teams.getTeamDao().findByKey(conn, "T1"));
    }

    private void run(final String script, final Consumer<JanitorScriptProcess> setup) throws Exception {
        final Team newTeam = new Team(teams);
        newTeam.setKey("T2");
        OutputCatchingTestRuntime.fresh().compileAndRun("test", script, process -> {
            setup.accept(process);
            process.bind("teamDao", teams.getTeamDao());
            process.bind("membershipDao", teams.getMembershipDao());
            process.bind("team", team);
            process.bind("newTeam", newTeam);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "teamDao.findById(1);",
            "teamDao.getById(1);",
            "teamDao.findByIds([1]);",
            "teamDao.findByKey(\"T1\");",
            "teamDao.getByKey(\"T1\");",
            "teamDao.findAll();",
            "teamDao.getAll();",
            "teamDao.select(t -> t.key == \"T1\");",
            "teamDao.count(t -> t.key == \"T1\");",
            "teamDao.queryForEach(\"select team_id from s_team\", (t, id) -> null);",
            "teamDao.select(\"select team_id from s_team\");",
            "membershipDao.select(m -> m.team == 1);",
            "membershipDao.count(m -> m.team == 1);",
    })
    void readingIsRefused(final String script) {
        assertThrows(JanitorPermissionException.class, () -> run(script, process -> process.setMetaData(READER, false)));
    }

    @Test
    void readingWithTheRightWorks() throws Exception {
        run("""
                assert(teamDao.findById(1).key == "T1");
                assert(teamDao.findByKey("T1") != null);
                assert(teamDao.findAll().size() == 1);
                assert(teamDao.select(t -> t.key == "T1").size() == 1);
                assert(teamDao.count(t -> t.key == "T1") == 1);
                assert(membershipDao.count(m -> m.team == 1) == 1);
                assert(teamDao.queryForEach("select team_id from s_team", (t, id) -> null) == 1);""", process -> process.setMetaData(READER, true));
    }

    @Test
    void whatTheScriptWasHandedStaysUsable() throws Exception {
        run("""
                assert(team.key == "T1");
                assert(team.members.size() == 1);""", process -> process.setMetaData(READER, false));
    }

    @Test
    void writingIsNotRefused() throws Exception {
        run("teamDao.insert(newTeam);", process -> process.setMetaData(READER, false));
        assertEquals(List.of("T1", "T2"), manager.callTransaction(conn -> teams.getTeamDao().findAll(conn, null)).stream().map(Team::getKey).sorted().toList());
    }

    @Test
    void javaCodeIsNotChecked() throws Exception {
        assertEquals("T1", manager.callTransaction(conn -> teams.getTeamDao().findByKey(conn, "T1")).getKey());
    }

    @Test
    void aCollectionWithoutCheckLetsScriptsRead() throws Exception {
        teams = new Teams(manager);
        run("assert(teamDao.findByKey(\"T1\") != null);", process -> { });
    }
}
