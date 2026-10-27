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
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyIdentity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A script loads an entity and adds an entry to one of its {@link JoinedList}s with {@code add}: the join record must be saved once, and both the
 * script and Java code must see the list with the new entry, each entry exactly once.
 */
class JoinedListScriptAddTestCase extends JanitorTest {

    public static class Team extends JanitorComposed<Team> implements OrmEntity, ForeignKeyIdentity<Team> {
        static final ForeignKeyNull<Team> NULL = new ForeignKeyNull<>(Team.class);
        static final EntityDispatchTable<Team, Teams> DISPATCH = new EntityDispatchTable<>(Team.class, Team::new, NULL, Teams::getTeamDao);

        static {
            DISPATCH.dbTable("s_team", "team_id", "team_key", "team_name", "seq_s_team");
            DISPATCH.addLongColumn("id", "team_id", Team::getId, Team::setId);
            DISPATCH.addStringColumn("key", "team_key", Team::getKey, Team::setKey, 20);
            DISPATCH.addStringColumn("name", "team_name", Team::getName, Team::setName, 50);
            // what scripts see: the list itself, so that they can call add on it
            DISPATCH.addObjectProperty("members", self -> self.getMembers().asJanitorObject());
        }

        private long id;
        private String key;
        private String name;
        private final JoinedList<Membership, Teams, MembershipDao, Member> members;

        Team(final Teams source) {
            super(DISPATCH);
            members = new JoinedList<>(this, Membership.class, () -> source, Teams::getMembershipDao,
                    (conn, dao) -> dao.findByColumnValue(conn, "team_id", getId()), Membership::getMember);
        }

        JoinedList<Membership, Teams, MembershipDao, Member> getMembers() { return members.lazyLoad(); }

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
            DISPATCH.dbTable("s_member", "member_id", "member_key", "member_name", "seq_s_member");
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

    /** The join record: a member belongs to a team. */
    public static class Membership implements OrmJoined {
        static final OrmDispatchTable<Membership, Teams> DISPATCH = new OrmDispatchTable<>(Membership.class, Membership::new);

        static {
            DISPATCH.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "s_membership");
            DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of("team_id", "member_id"));
            Team.DISPATCH.addReference(DISPATCH, "team", "team_id", Membership::getTeam, Membership::setTeam, membership -> membership.source);
            Member.DISPATCH.addReference(DISPATCH, "member", "member_id", Membership::getMember, Membership::setMember, membership -> membership.source);
        }

        private final Teams source;
        private ForeignKey<Team> team = Team.NULL;
        private ForeignKey<Member> member = Member.NULL;

        Membership(final Teams source) { this.source = source; }

        ForeignKey<Team> getTeam() { return team; }
        void setTeam(final ForeignKey<Team> team) { this.team = team; }
        ForeignKey<Member> getMember() { return member; }
        void setMember(final ForeignKey<Member> member) { this.member = member; }
    }

    public static class TeamDao extends GenericDao<Team, Teams> {
        TeamDao(final Teams c) { super(new DispatchTable<TeamDao>(false), c, Team.class, Team.DISPATCH, () -> new Team(c)); }
        @Override public @NotNull Class<Team> getEntityClass() { return Team.class; }
        @Override public @NotNull String getEntityClassName() { return "Team"; }
    }

    public static class MemberDao extends GenericDao<Member, Teams> {
        MemberDao(final Teams c) { super(new DispatchTable<MemberDao>(false), c, Member.class, Member.DISPATCH, Member::new); }
        @Override public @NotNull Class<Member> getEntityClass() { return Member.class; }
        @Override public @NotNull String getEntityClassName() { return "Member"; }
    }

    public static class MembershipDao extends JoinDao<Membership> {
        private final Teams collection;

        MembershipDao(final Teams c) {
            super(Membership.class, new DispatchTable<MembershipDao>(false), c, Membership.DISPATCH, () -> new Membership(c));
            this.collection = c;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Membership createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException {
            final Membership membership = new Membership(collection);
            membership.setTeam((ForeignKey<Team>) parentEntity);
            membership.setMember((ForeignKey<Member>) addedEntity);
            return membership;
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

    private SimpleDataManager manager;
    private Teams teams;
    private Team team;
    private Member bob;

    /** A team with the member alice; bob is not a member yet. */
    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        teams = new Teams(manager);
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
            final Member newBob = new Member();
            newBob.setKey("BOB");
            teams.getMemberDao().insert(conn, newBob);
            final Membership membership = new Membership(teams);
            membership.setTeam(newTeam);
            membership.setMember(alice);
            teams.getMembershipDao().insert(conn, membership);
        });
        team = manager.callTransaction(conn -> teams.getTeamDao().findByKey(conn, "T1"));
        bob = manager.callTransaction(conn -> teams.getMemberDao().findByKey(conn, "BOB"));
    }

    private JanitorObject addBob(final String script) throws Exception {
        return evaluate(script, g -> {
            g.bind("team", team);
            g.bind("bob", bob);
        });
    }

    private List<String> membersInDatabase() throws DatabaseError {
        return manager.callTransaction(conn -> teams.getMembershipDao().findByColumnValue(conn, "team_id", team.getId())).stream()
                .map(membership -> membership.getMember().resolveOrNull().getKey()).sorted().toList();
    }

    private List<String> membersInMemory() {
        return team.getMembers().getMainJoinedObjects().map(fk -> fk.resolveOrNull().getKey()).sorted().toList();
    }

    @Test
    void addSavesTheJoinRecord() throws Exception {
        addBob("team.members.add(bob);");
        assertEquals(List.of("ALICE", "BOB"), membersInDatabase());
    }

    @Test
    void javaSeesEachEntryOnceAfterAdd() throws Exception {
        assertEquals(List.of("ALICE"), membersInMemory()); // loaded before the script runs, like an entity from a cache
        addBob("team.members.add(bob);");
        assertEquals(List.of("ALICE", "BOB"), membersInMemory());
    }

    @Test
    void scriptSeesTheAddedEntry() throws Exception {
        final JanitorObject size = addBob("team.members.add(bob); return team.members.size();");
        assertEquals(2, ((JInt) size).getValue());
    }

    @Test
    void scriptIteratesOverTheAddedEntry() throws Exception {
        final JanitorObject count = addBob("""
                team.members.add(bob);
                n = 0;
                for (m in team.members) { n = n + 1; }
                return n;""");
        assertEquals(2, ((JInt) count).getValue());
    }

}
