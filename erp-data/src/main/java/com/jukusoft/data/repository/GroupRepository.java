package com.jukusoft.data.repository;

import com.jukusoft.data.entity.Group;
import com.jukusoft.data.entity.GroupMember;
import com.jukusoft.data.entity.User;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.database.AbstractMySQLRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class GroupRepository extends AbstractMySQLRepository {

    @InjectCache(name = "group-cache", type = CacheTypes.LOCAL_MEMORY_CACHE)
    protected ICache groupCache;

    @InjectCache(name = "group-members-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache groupMembersCache;

    public void getGroupByID (long groupID, Handler<AsyncResult<Group>> handler) {
        if (this.groupCache == null) {
            throw new NullPointerException("group cache cannot be null.");
        }

        //check, if group is in cache
        if (this.groupCache.contains("group-" + groupID)) {
            handler.handle(Future.succeededFuture(new Group(this.groupCache.get("group-" + groupID))));
            return;
        }

        //read group from database
        getMySQLDatabase().getRow("SELECT * FROM `" + getMySQLDatabase().getTableName("groups") + "` WHERE `groupID` = '" + groupID + "';", res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //create DAO group from row
            Group group = new Group(res.result());

            //add group to cache
            this.groupCache.put("group-" + groupID, group.toJSON());

            handler.handle(Future.succeededFuture(group));
        });
    }

    public void getGroupsOfUser (long userID, Handler<AsyncResult<List<GroupMember>>> handler) {
        JsonArray params = new JsonArray();
        params.add(userID);

        //check, if result is already in cache
        if (this.groupMembersCache.contains("user-groups-" + userID)) {
            //get cache object
            JsonObject cacheObj = this.groupMembersCache.get("user-groups-" + userID);

            //get json array
            JsonArray jsonArray = cacheObj.getJsonArray("groups");

            //convert rows to list
            List<GroupMember> list = this.createListFromRows(jsonArray);

            handler.handle(Future.succeededFuture(list));

            return;
        }

        //execute sql query to list rows of table
        getMySQLDatabase().listRows("SELECT * FROM `" + getMySQLDatabase().getTableName("group_members") + "` LEFT JOIN `" + getMySQLDatabase().getTableName("groups") + "` ON (`" + getMySQLDatabase().getTableName("group_members") + "`.`groupID` = `" + getMySQLDatabase().getTableName("groups") + "`.`groupID`) WHERE `userID` = ?; ", params, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //create new json array
            JsonArray rows = new JsonArray();

            for (JsonObject row : res.result()) {
                //add row to json array
                rows.add(row);
            }

            //cache result
            JsonObject cacheObj = new JsonObject();
            cacheObj.put("groups", rows);
            this.groupMembersCache.put("user-groups-" + userID, cacheObj);

            //convert rows to list
            List<GroupMember> list = this.createListFromRows(rows);

            handler.handle(Future.succeededFuture(list));
        });
    }

    public void listGroupIDsOfUser (long userID, Handler<AsyncResult<long[]>> handler) {
        //first, check if list is in cache
        if (this.groupMembersCache.contains("user-groupIDs-" + userID)) {
            //get json array
            JsonArray array = this.groupMembersCache.getArray("user-groupIDs-" + userID);

            handler.handle(Future.succeededFuture(createGroupIDsArray(array)));

            return;
        }

        //execute sql query to list rows of table
        getMySQLDatabase().listRows("SELECT * FROM `" + getMySQLDatabase().getTableName("group_members") + "` WHERE `userID` = '" + userID + "'; ", res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //create new json array
            JsonArray groups = new JsonArray();

            //iterate through all rows
            for (JsonObject row : res.result()) {
                //add row to json array
                groups.add(row.getLong("groupID"));
            }

            //cache result
            this.groupMembersCache.putArray("user-groupIDs-" + userID, groups);

            handler.handle(Future.succeededFuture(createGroupIDsArray(groups)));
        });

        //TODO: read from database
    }

    private long[] createGroupIDsArray (JsonArray array) {
        long[] array1 = new long[array.size()];

        for (int i = 0; i < array.size(); i++) {
            array1[i] = array.getLong(i);
        }

        return array1;
    }

    /**
    * converts rows to list with group membership instances
     *
     * @return list with group membership instances
    */
    protected List<GroupMember> createListFromRows (JsonArray rows) {
        //create new empty list
        List<GroupMember> list = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            //get row
            JsonObject row = rows.getJsonObject(i);

            GroupMember member = new GroupMember(row);

            try {
                Group group = new Group(row);
                member.setGroup(group);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //add membership to list
            list.add(member);
        }

        return list;
    }

    private void saveInCache (long groupID, Group group) {
        this.groupCache.put("group-" + groupID, group.toJSON());
    }

}
