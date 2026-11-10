// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A collection decides whether scripts may write through its DAOs: all script methods that write ask it first, and nothing reaches the
 * database if it says no. Reading, and Java code that calls the DAOs directly, are not affected.
 */
class ScriptWriteCheckTestCase {

    private static final MetaDataKey<Boolean> WRITER = new MetaDataKey<>("WRITER", Boolean.class);

    /** Lets a script write only with the right. */
    private static class GuardedTeams extends Teams {
        GuardedTeams(final DataManager dataManager) {
            super(dataManager);
        }

        @Override
        public void checkScriptWrite(final @NotNull JanitorScriptProcess process) throws JanitorRuntimeException {
            process.require(WRITER);
        }
    }

    private SimpleDataManager manager;
    private Teams teams;
    private Team team;
    private Member alice;
    private Member bob;

    /** A team with the member alice; bob is not a member yet. */
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
            final Member newAlice = new Member();
            newAlice.setKey("ALICE");
            teams.getMemberDao().insert(conn, newAlice);
            final Member newBob = new Member();
            newBob.setKey("BOB");
            teams.getMemberDao().insert(conn, newBob);
            final Membership membership = new Membership(teams);
            membership.setTeam(newTeam);
            membership.setMember(newAlice);
            teams.getMembershipDao().insert(conn, membership);
        });
        team = manager.callTransaction(conn -> teams.getTeamDao().findByKey(conn, "T1"));
        alice = manager.callTransaction(conn -> teams.getMemberDao().findByKey(conn, "ALICE"));
        bob = manager.callTransaction(conn -> teams.getMemberDao().findByKey(conn, "BOB"));
    }

    private Team newTeam() {
        final Team newTeam = new Team(teams);
        newTeam.setKey("T2");
        return newTeam;
    }

    private Membership membershipOf(final Member member) {
        final Membership membership = new Membership(teams);
        membership.setTeam(team);
        membership.setMember(member);
        return membership;
    }

    private void run(final String script, final Consumer<JanitorScriptProcess> setup) throws Exception {
        OutputCatchingTestRuntime.fresh().compileAndRun("test", script, process -> {
            setup.accept(process);
            process.bind("teamDao", teams.getTeamDao());
            process.bind("membershipDao", teams.getMembershipDao());
            process.bind("team", team);
            process.bind("newTeam", newTeam());
            process.bind("bob", bob);
            process.bind("newMembership", membershipOf(bob));
            process.bind("oldMembership", membershipOf(alice));
        });
    }

    private void assertRefused(final String script) {
        assertThrows(JanitorPermissionException.class, () -> run(script, process -> process.setMetaData(WRITER, false)));
    }

    private List<String> teamKeys() throws DatabaseError {
        return manager.callTransaction(conn -> teams.getTeamDao().findAll(conn, null)).stream().map(Team::getKey).sorted().toList();
    }

    private List<String> teamNames() throws DatabaseError {
        return manager.callTransaction(conn -> teams.getTeamDao().findAll(conn, null)).stream().map(Team::getName).toList();
    }

    private List<String> members() throws DatabaseError {
        return manager.callTransaction(conn -> teams.getMembershipDao().findByColumnValue(conn, "team_id", team.getId())).stream()
                .map(membership -> membership.getMember().resolveOrNull().getKey()).sorted().toList();
    }

    @Test
    void insertIsRefused() throws Exception {
        assertRefused("teamDao.insert(newTeam);");
        assertEquals(List.of("T1"), teamKeys());
    }

    @Test
    void updateIsRefused() throws Exception {
        team.setName("changed");
        assertRefused("teamDao.update(team);");
        assertEquals(Collections.singletonList(null), teamNames());
    }

    @Test
    void joinInsertIsRefused() throws Exception {
        assertRefused("membershipDao.insert(newMembership);");
        assertEquals(List.of("ALICE"), members());
    }

    @Test
    void joinUpdateIsRefused() throws Exception {
        assertRefused("membershipDao.update(oldMembership);");
        assertEquals(List.of("ALICE"), members());
    }

    @Test
    void joinMergeIsRefused() throws Exception {
        assertRefused("membershipDao.merge(newMembership);");
        assertEquals(List.of("ALICE"), members());
    }

    @Test
    void joinDeleteIsRefused() throws Exception {
        assertRefused("membershipDao.delete(oldMembership);");
        assertEquals(List.of("ALICE"), members());
    }

    @Test
    void addToAJoinedListIsRefused() throws Exception {
        assertRefused("team.members.add(bob);");
        assertEquals(List.of("ALICE"), members());
    }

    private static final String QUERY_FOR_EACH = "assert(teamDao.queryForEach(\"select team_id from s_team\", (t, id) -> null) == 1);";

    @Test
    void sqlFromTheScriptIsRefusedLikeAWrite() {
        assertRefused(QUERY_FOR_EACH);
    }

    @Test
    void sqlFromTheScriptWorksWithTheRight() throws Exception {
        run(QUERY_FOR_EACH, process -> process.setMetaData(WRITER, true));
    }

    @Test
    void aCollectionCanAllowSqlFromTheScriptButNotWrites() throws Exception {
        teams = new GuardedTeams(manager) {
            @Override
            public void checkScriptSql(final @NotNull JanitorScriptProcess process) {
            }
        };
        run(QUERY_FOR_EACH, process -> process.setMetaData(WRITER, false));
        assertRefused("teamDao.insert(newTeam);");
    }

    @Test
    void readingIsNotRefused() throws Exception {
        run("assert(teamDao.findByKey(\"T1\") != null);", process -> process.setMetaData(WRITER, false));
    }

    @Test
    void writingWithTheRightWorks() throws Exception {
        run("""
                teamDao.insert(newTeam);
                membershipDao.delete(oldMembership);
                membershipDao.insert(newMembership);""", process -> process.setMetaData(WRITER, true));
        assertEquals(List.of("T1", "T2"), teamKeys());
        assertEquals(List.of("BOB"), members());
    }

    @Test
    void javaCodeIsNotChecked() throws Exception {
        manager.executeTransaction(conn -> teams.getMembershipDao().insert(conn, membershipOf(bob)));
        assertEquals(List.of("ALICE", "BOB"), members());
    }

    @Test
    void aCollectionWithoutCheckLetsScriptsWrite() throws Exception {
        teams = new Teams(manager);
        run("teamDao.insert(newTeam);", process -> { });
        assertEquals(List.of("T1", "T2"), teamKeys());
    }
}
