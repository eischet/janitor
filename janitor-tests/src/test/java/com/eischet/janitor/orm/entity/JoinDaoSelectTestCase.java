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
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyIdentity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Filters on the records of a join table: {@code dao.select(link -> ...)} and {@code dao.count(link -> ...)} in scripts, and
 * {@link JoinDao#findByFilter} in Java, with conditions on the join table's own columns and on fields of the referenced entities.
 */
class JoinDaoSelectTestCase extends JanitorTest {

    public static class Team extends JanitorComposed<Team> implements OrmEntity, ForeignKeyIdentity<Team> {
        static final ForeignKeyNull<Team> NULL = new ForeignKeyNull<>(Team.class);
        static final EntityDispatchTable<Team, Teams> DISPATCH = new EntityDispatchTable<>(Team.class, up -> new Team(), NULL, Teams::getTeamDao);

        static {
            DISPATCH.dbTable("f_team", "team_id", "team_key", "team_name", "seq_f_team");
            DISPATCH.addLongColumn("id", "team_id", Team::getId, Team::setId);
            DISPATCH.addStringColumn("key", "team_key", Team::getKey, Team::setKey, 20);
            DISPATCH.addStringColumn("name", "team_name", Team::getName, Team::setName, 50);
        }

        private long id;
        private String key;
        private String name;

        Team() {
            super(DISPATCH);
        }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        @Override public @NotNull Team getIdentity() { return this; }
    }

    public static class Member extends JanitorComposed<Member> implements OrmEntity, ForeignKeyIdentity<Member> {
        static final ForeignKeyNull<Member> NULL = new ForeignKeyNull<>(Member.class);
        static final EntityDispatchTable<Member, Teams> DISPATCH = new EntityDispatchTable<>(Member.class, up -> new Member(), NULL, Teams::getMemberDao);

        static {
            DISPATCH.dbTable("f_member", "member_id", "member_key", "member_name", "seq_f_member");
            DISPATCH.addLongColumn("id", "member_id", Member::getId, Member::setId);
            DISPATCH.addStringColumn("key", "member_key", Member::getKey, Member::setKey, 20);
            DISPATCH.addStringColumn("name", "member_name", Member::getName, Member::setName, 50);
        }

        private long id;
        private String key;
        private String name;

        Member() {
            super(DISPATCH);
        }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        @Override public @NotNull Member getIdentity() { return this; }
    }

    /**
     * The join record: a member belongs to a team, in a role.
     */
    public static class Membership implements OrmJoined {
        static final OrmDispatchTable<Membership, Teams> DISPATCH = new OrmDispatchTable<>(Membership.class, Membership::new);

        static {
            DISPATCH.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "f_membership");
            DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of("team_id", "member_id"));
            Team.DISPATCH.addReference(DISPATCH, "team", "team_id", Membership::getTeam, Membership::setTeam, membership -> membership.source);
            Member.DISPATCH.addReference(DISPATCH, "member", "member_id", Membership::getMember, Membership::setMember, membership -> membership.source);
            DISPATCH.addStringColumn("role", "member_role", Membership::getRole, Membership::setRole, 20);
        }

        private final Teams source;
        private ForeignKey<Team> team = Team.NULL;
        private ForeignKey<Member> member = Member.NULL;
        private @Nullable String role;

        Membership(final Teams source) { this.source = source; }

        ForeignKey<Team> getTeam() { return team; }
        void setTeam(final ForeignKey<Team> team) { this.team = team; }
        ForeignKey<Member> getMember() { return member; }
        void setMember(final ForeignKey<Member> member) { this.member = member; }
        @Nullable String getRole() { return role; }
        void setRole(final @Nullable String role) { this.role = role; }
    }

    public static class TeamDao extends GenericDao<Team, Teams> {
        TeamDao(final Teams c) { super(new DispatchTable<TeamDao>(false), c, Team.class, Team.DISPATCH, Team::new); }
        @Override public @NotNull Class<Team> getEntityClass() { return Team.class; }
        @Override public @NotNull String getEntityClassName() { return "Team"; }
    }

    public static class MemberDao extends GenericDao<Member, Teams> {
        MemberDao(final Teams c) { super(new DispatchTable<MemberDao>(false), c, Member.class, Member.DISPATCH, Member::new); }
        @Override public @NotNull Class<Member> getEntityClass() { return Member.class; }
        @Override public @NotNull String getEntityClassName() { return "Member"; }
    }

    public static class MembershipDao extends JoinDao<Membership> {
        MembershipDao(final Teams c) {
            super(Membership.class, new DispatchTable<MembershipDao>(false), c, Membership.DISPATCH, () -> new Membership(c));
        }

        @Override
        public Membership createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException {
            throw new UnsupportedOperationException();
        }
    }

    public static class Teams extends OrmDaoCollection<Teams> implements Uplink {
        static final DispatchTable<Teams> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final TeamDao teamDao;
        private final MemberDao memberDao;
        private final MembershipDao membershipDao;

        Teams(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.teamDao = new TeamDao(this);
            this.memberDao = new MemberDao(this);
            this.membershipDao = new MembershipDao(this);
        }

        TeamDao getTeamDao() { return teamDao; }
        MemberDao getMemberDao() { return memberDao; }
        MembershipDao getMembershipDao() { return membershipDao; }
        @Override public DataManager getDataManager() { return dataManager; }
    }

    private Teams teams;
    private Team red;

    /**
     * Two teams: RED with ALICE (lead) and BOB (member), BLUE with BOB (lead) and CAROL (no role).
     */
    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        teams = new Teams(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_f_team start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_f_member start with 1"));
            conn.update(UpdateStatement.of("create table f_team (team_id bigint primary key, team_key varchar(20), team_name varchar(50))"));
            conn.update(UpdateStatement.of("create table f_member (member_id bigint primary key, member_key varchar(20), member_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table f_membership (
                      team_id bigint not null references f_team(team_id),
                      member_id bigint not null references f_member(member_id),
                      member_role varchar(20),
                      primary key (team_id, member_id))"""));
            final Team newRed = team(conn, "RED");
            final Team blue = team(conn, "BLUE");
            final Member alice = member(conn, "ALICE");
            final Member bob = member(conn, "BOB");
            final Member carol = member(conn, "CAROL");
            membership(conn, newRed, alice, "lead");
            membership(conn, newRed, bob, "member");
            membership(conn, blue, bob, "lead");
            membership(conn, blue, carol, null);
        });
        red = manager.callTransaction(conn -> teams.getTeamDao().findByKey(conn, "RED"));
    }

    private Team team(final com.eischet.dbxs.DatabaseConnection conn, final String key) throws DatabaseError {
        final Team team = new Team();
        team.setKey(key);
        teams.getTeamDao().insert(conn, team);
        return team;
    }

    private Member member(final com.eischet.dbxs.DatabaseConnection conn, final String key) throws DatabaseError {
        final Member member = new Member();
        member.setKey(key);
        teams.getMemberDao().insert(conn, member);
        return member;
    }

    private void membership(final com.eischet.dbxs.DatabaseConnection conn, final Team team, final Member member, final @Nullable String role) throws DatabaseError {
        final Membership membership = new Membership(teams);
        membership.setTeam(team);
        membership.setMember(member);
        membership.setRole(role);
        teams.getMembershipDao().insert(conn, membership);
    }

    private JanitorObject run(final String script) throws Exception {
        return evaluate(script, g -> {
            g.bind("memberships", teams.getMembershipDao());
            g.bind("red", red);
        });
    }

    /**
     * The selected memberships as "TEAM/MEMBER", in the order the query returned them.
     */
    private List<String> pairs(final JanitorObject result) {
        return ((JList) result).stream()
                .map(it -> (Membership) it)
                .map(m -> m.getTeam().resolveOrNull().getKey() + "/" + m.getMember().resolveOrNull().getKey())
                .toList();
    }

    @Test
    void selectsByOwnColumn() throws Exception {
        assertEquals(List.of("RED/ALICE", "BLUE/BOB"), pairs(run("return memberships.select(m -> m.role == \"lead\");")));
    }

    @Test
    void selectsByReferencedEntity() throws Exception {
        assertEquals(List.of("RED/ALICE", "RED/BOB"), pairs(run("return memberships.select(m -> m.team == red);")));
    }

    @Test
    void selectsByFieldOfReferencedEntity() throws Exception {
        assertEquals(List.of("RED/BOB", "BLUE/BOB"), pairs(run("return memberships.select(m -> m.member.key == \"BOB\");")));
    }

    @Test
    void combinesConditions() throws Exception {
        assertEquals(List.of("BLUE/BOB"), pairs(run("return memberships.select(m -> m.member.key == \"BOB\" and m.role == \"lead\");")));
        assertEquals(List.of("RED/ALICE", "BLUE/BOB", "BLUE/CAROL"), pairs(run("return memberships.select(m -> m.role == \"lead\" or m.team.key == \"BLUE\");")));
    }

    @Test
    void selectsMissingValues() throws Exception {
        assertEquals(List.of("BLUE/CAROL"), pairs(run("return memberships.select(m -> m.role == null);")));
    }

    @Test
    void counts() throws Exception {
        assertEquals(2L, run("return memberships.count(m -> m.team.key == \"BLUE\");").janitorGetHostValue());
        assertEquals(0L, run("return memberships.count(m -> m.member.key == \"NOBODY\");").janitorGetHostValue());
    }

    @Test
    void selectsAndCountsByFilterMap() throws Exception {
        assertEquals(List.of("BLUE/BOB"), pairs(run("""
                return memberships.select({logic: "and", filters: [
                    {field: "member.key", operator: "eq", valueString: "BOB"},
                    {field: "role", operator: "eq", valueString: "lead"}
                ]});""")));
        assertEquals(2L, run("return memberships.count({field: \"team.key\", operator: \"eq\", valueString: \"BLUE\"});").janitorGetHostValue());
    }

    @Test
    void findsByFilterExpressionInJava() throws Exception {
        final List<Membership> found = teams.getDataManager().callTransaction(conn -> teams.getMembershipDao().findByFilter(conn,
                FilterExpression.from("member.key", FilterOperator.STARTSWITH, "c"), null));
        assertEquals(1, found.size());
        assertEquals("CAROL", found.getFirst().getMember().resolveOrNull().getKey());
    }

    @Test
    void knowsItsFilterFields() {
        final MembershipDao dao = teams.getMembershipDao();
        assertTrue(dao.isFilterField("role"));
        assertTrue(dao.isFilterField("team"));
        assertTrue(dao.isFilterField("team.key"));
        assertFalse(dao.isFilterField("color"));
        assertFalse(dao.isFilterField("role.key"));
        assertFalse(dao.isFilterField("team.color"));
    }

    @Test
    void rejectsUnknownFields() {
        assertThrows(JanitorArgumentException.class, () -> run("return memberships.select(m -> m.color == \"red\");"));
    }

    @Test
    void rejectsSomethingElseThanALambda() {
        assertThrows(JanitorArgumentException.class, () -> run("return memberships.select(\"role = 'lead'\");"));
    }

}
