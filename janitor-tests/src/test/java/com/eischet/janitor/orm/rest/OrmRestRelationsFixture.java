// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;

/**
 * Entities for the tests of sub-resources and links: teams and members (both with a key, read-only) that are connected by memberships (a join
 * table with the extra column "role"), and tasks that belong to a team (a sub-resource of the team, which can be created).
 * <p>
 * Teams have the links {@code /teams/{id}/members}, members have {@code /members/{id}/teams}; memberships can be created and deleted.
 * </p>
 */
public final class OrmRestRelationsFixture {

    private OrmRestRelationsFixture() {
    }

    public static class Team implements OrmEntity {
        static final ForeignKeyNull<Team> NULL = new ForeignKeyNull<>(Team.class);
        static final EntityDispatchTable<Team, RelationsCollection> DISPATCH = new EntityDispatchTable<>(Team.class, up -> new Team(), NULL, RelationsCollection::getTeamDao);

        static {
            DISPATCH.dbTable("r_team", "team_id", "team_key", "team_name", "seq_r_team");
            DISPATCH.addLongColumn("id", "team_id", Team::getId, Team::setId);
            DISPATCH.addStringColumn("key", "team_key", Team::getKey, Team::setKey, 20);
            DISPATCH.addStringColumn("name", "team_name", Team::getName, Team::setName, 50);
            RestOrm.expose(DISPATCH, "teams");
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

    public static class Member implements OrmEntity {
        static final ForeignKeyNull<Member> NULL = new ForeignKeyNull<>(Member.class);
        static final EntityDispatchTable<Member, RelationsCollection> DISPATCH = new EntityDispatchTable<>(Member.class, up -> new Member(), NULL, RelationsCollection::getMemberDao);

        static {
            DISPATCH.dbTable("r_member", "member_id", "member_key", "member_name", "seq_r_member");
            DISPATCH.addLongColumn("id", "member_id", Member::getId, Member::setId);
            DISPATCH.addStringColumn("key", "member_key", Member::getKey, Member::setKey, 20);
            DISPATCH.addStringColumn("name", "member_name", Member::getName, Member::setName, 50);
            RestOrm.expose(DISPATCH, "members");
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

    public static class Task implements OrmEntity {
        static final ForeignKeyNull<Task> NULL = new ForeignKeyNull<>(Task.class);
        static final EntityDispatchTable<Task, RelationsCollection> DISPATCH = new EntityDispatchTable<>(Task.class, up -> new Task(up), NULL, RelationsCollection::getTaskDao);

        static {
            DISPATCH.dbTable("r_task", "task_id", "task_key", "task_name", "seq_r_task");
            DISPATCH.addLongColumn("id", "task_id", Task::getId, Task::setId);
            DISPATCH.addStringColumn("key", "task_key", Task::getKey, Task::setKey, 20);
            DISPATCH.addStringColumn("name", "task_name", Task::getName, Task::setName, 50);
            RestOrm.subresource(DISPATCH.addReference("team", "team_id", Team.DISPATCH, Task::getTeam, Task::setTeam, task -> task.source), "tasks");
            RestOrm.allow(RestOrm.expose(DISPATCH, "tasks"), RestOperation.CREATE, RestOperation.UPDATE, RestOperation.DELETE);
        }

        private final RelationsCollection source;
        private long id;
        private String key;
        private String name;
        private ForeignKey<Team> team = Team.NULL;

        Task(final RelationsCollection source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        ForeignKey<Team> getTeam() { return team; }
        void setTeam(final ForeignKey<Team> team) { this.team = team; }
    }

    /** The join record: a member belongs to a team, in a role. */
    public static class Membership implements OrmJoined {
        static final OrmDispatchTable<Membership, RelationsCollection> DISPATCH = new OrmDispatchTable<>(Membership.class, Membership::new);

        static {
            DISPATCH.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "r_membership");
            DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of("team_id", "member_id"));
            Team.DISPATCH.addReference(DISPATCH, "team", "team_id", Membership::getTeam, Membership::setTeam, membership -> membership.source);
            Member.DISPATCH.addReference(DISPATCH, "member", "member_id", Membership::getMember, Membership::setMember, membership -> membership.source);
            DISPATCH.addStringColumn("role", "role", Membership::getRole, Membership::setRole, 20);
            RestOrm.link(DISPATCH, "team", "members", "member");
            RestOrm.link(DISPATCH, "member", "teams", "team");
            RestOrm.allow(DISPATCH, RestOperation.CREATE, RestOperation.DELETE);
        }

        private final RelationsCollection source;
        private ForeignKey<Team> team = Team.NULL;
        private ForeignKey<Member> member = Member.NULL;
        private String role;

        Membership(final RelationsCollection source) { this.source = source; }

        ForeignKey<Team> getTeam() { return team; }
        void setTeam(final ForeignKey<Team> team) { this.team = team; }
        ForeignKey<Member> getMember() { return member; }
        void setMember(final ForeignKey<Member> member) { this.member = member; }
        String getRole() { return role; }
        void setRole(final String role) { this.role = role; }
    }

    public static class TeamDao extends GenericDao<Team, RelationsCollection> {
        TeamDao(final RelationsCollection c) { super(new DispatchTable<TeamDao>(false), c, Team.class, Team.DISPATCH, Team::new); }
        @Override public @NotNull Class<Team> getEntityClass() { return Team.class; }
        @Override public @NotNull String getEntityClassName() { return "Team"; }
    }

    public static class MemberDao extends GenericDao<Member, RelationsCollection> {
        MemberDao(final RelationsCollection c) { super(new DispatchTable<MemberDao>(false), c, Member.class, Member.DISPATCH, Member::new); }
        @Override public @NotNull Class<Member> getEntityClass() { return Member.class; }
        @Override public @NotNull String getEntityClassName() { return "Member"; }
    }

    public static class TaskDao extends GenericDao<Task, RelationsCollection> {
        TaskDao(final RelationsCollection c) { super(new DispatchTable<TaskDao>(false), c, Task.class, Task.DISPATCH, () -> new Task(c)); }
        @Override public @NotNull Class<Task> getEntityClass() { return Task.class; }
        @Override public @NotNull String getEntityClassName() { return "Task"; }
    }

    public static class MembershipDao extends JoinDao<Membership> {
        MembershipDao(final RelationsCollection c) { super(Membership.class, new DispatchTable<MembershipDao>(false), c, Membership.DISPATCH, () -> new Membership(c)); }

        @Override
        public Membership createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException {
            throw new UnsupportedOperationException("not needed for the tests");
        }
    }

    public static class RelationsCollection extends OrmDaoCollection<RelationsCollection> implements Uplink {
        static final DispatchTable<RelationsCollection> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final TeamDao teamDao;
        private final MemberDao memberDao;
        private final TaskDao taskDao;
        private final MembershipDao membershipDao;

        RelationsCollection(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.teamDao = new TeamDao(this);
            this.memberDao = new MemberDao(this);
            this.taskDao = new TaskDao(this);
            this.membershipDao = new MembershipDao(this);
        }

        TeamDao getTeamDao() { return teamDao; }
        MemberDao getMemberDao() { return memberDao; }
        TaskDao getTaskDao() { return taskDao; }
        MembershipDao getMembershipDao() { return membershipDao; }
        @Override public DataManager getDataManager() { return dataManager; }
    }

    /** Creates a collection on a fresh in-memory database, with the tables but without rows. */
    static RelationsCollection newCollection() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        final RelationsCollection collection = new RelationsCollection(manager);
        manager.executeTransaction(conn -> {
            for (final String name : new String[]{"r_team", "r_member", "r_task"}) {
                conn.update(UpdateStatement.of("create sequence seq_" + name + " start with 1"));
            }
            conn.update(UpdateStatement.of("create table r_team (team_id bigint primary key, team_key varchar(20), team_name varchar(50))"));
            conn.update(UpdateStatement.of("create table r_member (member_id bigint primary key, member_key varchar(20), member_name varchar(50))"));
            conn.update(UpdateStatement.of("create table r_task (task_id bigint primary key, task_key varchar(20), task_name varchar(50), team_id bigint references r_team(team_id))"));
            conn.update(UpdateStatement.of("""
                    create table r_membership (
                      team_id bigint not null references r_team(team_id),
                      member_id bigint not null references r_member(member_id),
                      role varchar(20),
                      primary key (team_id, member_id))"""));
        });
        return collection;
    }
}
