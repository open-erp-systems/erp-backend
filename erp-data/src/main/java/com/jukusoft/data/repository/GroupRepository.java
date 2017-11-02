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

        getMySQLDatabase().listRows("SELECT * FROM `" + getMySQLDatabase().getTableName("group_members") + "` LEFT JOIN `" + getMySQLDatabase().getTableName("groups") + "` ON (`" + getMySQLDatabase().getTableName("group_members") + "`.`groupID` = `" + getMySQLDatabase().getTableName("groups") + "`.`groupID`) WHERE `userID` = ?; ", params, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            List<GroupMember> list = new ArrayList<>();

            for (JsonObject row : res.result()) {
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

            handler.handle(Future.succeededFuture(list));
        });
    }

    private void saveInCache (long groupID, Group group) {
        this.groupCache.put("group-" + groupID, group.toJSON());
    }

}
