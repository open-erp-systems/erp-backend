package com.jukusoft.erp.lib.permission;

import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.database.Database;
import com.jukusoft.erp.lib.database.InjectDatabase;
import com.jukusoft.erp.lib.service.IService;
import com.jukusoft.erp.lib.utils.JsonUtils;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionService implements IService {

    @InjectCache(name = "group-perm-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache groupPermCache;

    @InjectCache(name = "user-perm-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache userPermCache;

    @InjectCache(name = "group-members-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache groupMembersCache;

    @InjectDatabase
    protected Database database;

    public PermissionService (ICache cache, Database database) {
        this.groupPermCache = cache;
        this.userPermCache = cache;
        this.groupMembersCache = cache;
        this.database = database;
    }

    @Override
    public void start() {

    }

    public Map<String,PermissionStates> listGroupPermissionStates (long groupID) {
        if (groupID <= 0) {
            throw new IllegalArgumentException("groupID cannot be <= 0.");
        }

        if (this.groupPermCache == null) {
            throw new NullPointerException("group permission cache cannot be null.");
        }

        //check, if result is already in cache
        if (this.groupPermCache.contains("group-permission-states-" + groupID)) {
            //get cache object
            JsonArray jsonArray = this.groupPermCache.getArray("group-permission-states-" + groupID);

            //convert rows to list
            Map<String,PermissionStates> map = this.createMapFromJSONArray(jsonArray);

            return map;
        }


        //load permissions from database
        JsonArray rows = database.listRowsAsArray("SELECT * FROM `{prefix}group_permissions` WHERE `groupID` = '" + groupID + "'; ");

        //cache rows
        this.groupPermCache.putArray("group-permission-states-" + groupID, rows);

        //convert rows to list
        Map<String,PermissionStates> map = this.createMapFromJSONArray(rows);

        return map;
    }

    protected Map<String,PermissionStates> createMapFromJSONArray (JsonArray array) {
        Map<String,PermissionStates> map = new HashMap<>();

        for (int i = 0; i < array.size(); i++) {
            //get entry
            JsonObject json = array.getJsonObject(i);

            String permission = json.getString("permission");
            PermissionStates state = PermissionStates.valueOf(json.getString("value"));

            map.put(permission, state);
        }

        return map;
    }

    public JsonArray listGroupPermissions (long groupID) {
        if (groupID <= 0) {
            throw new IllegalArgumentException("groupID cannot be <= 0.");
        }

        if (this.groupPermCache == null) {
            throw new NullPointerException("group permission cache cannot be null.");
        }

        if (this.groupPermCache.contains("group-permissions-" + groupID)) {
            return this.groupPermCache.getArray("group-permissions-" + groupID);
        } else {
            Map<String,PermissionStates> map = this.listGroupPermissionStates(groupID);

            //convert map to json array
            JsonArray array = new JsonArray();

            for (Map.Entry<String,PermissionStates> entry : map.entrySet()) {
                if (entry.getValue() == PermissionStates.ALLOW) {
                    array.add(entry.getKey());
                }
            }

            //put array to cache
            this.groupPermCache.putArray("group-permissions-" + groupID, array);

            return array;
        }
    }

    protected long[] listGroupIDsOfUser (long userID) {
        //first, check if list is in cache
        if (this.groupMembersCache.contains("user-groupIDs-" + userID)) {
            //get json array
            JsonArray array = this.groupMembersCache.getArray("user-groupIDs-" + userID);

            return createGroupIDsArray(array);
        }

        //execute sql query to list rows of table
        List<JsonObject> rows = database.listRows("SELECT * FROM `" + database.getTableName("group_members") + "` WHERE `userID` = '" + userID + "'; ");

        //create new json array
        JsonArray groups = new JsonArray();

        //iterate through all rows
        for (JsonObject row : rows) {
            //add row to json array
            groups.add(row.getLong("groupID"));
        }

        //cache result
        this.groupMembersCache.putArray("user-groupIDs-" + userID, groups);

        return createGroupIDsArray(groups);
    }

    private long[] createGroupIDsArray (JsonArray array) {
        long[] array1 = new long[array.size()];

        for (int i = 0; i < array.size(); i++) {
            array1[i] = array.getLong(i);
        }

        return array1;
    }

    public Map<String,PermissionStates> listUserPermissionStates (long userID) {
        //get groups of user
        long[] groupIDs = this.listGroupIDsOfUser(userID);

        //create new hashmap for permissions
        Map<String,PermissionStates> permMap = new HashMap<>();

        for (long groupID : groupIDs) {
            Map<String,PermissionStates> map = this.listGroupPermissionStates(groupID);
            mergePermissions(map, permMap);
        }

        return permMap;
    }

    public JsonArray listUserPermissions (long userID) {
        if (this.userPermCache.contains("user-permissions-" + userID)) {
            return this.userPermCache.getArray("user-permissions-" + userID);
        }

        //get group permission states
        Map<String,PermissionStates> permMap = this.listUserPermissionStates(userID);

        //create new empty json array
        JsonArray array = new JsonArray();

        for (Map.Entry<String,PermissionStates> entry : permMap.entrySet()) {
            if (entry.getValue() == PermissionStates.ALLOW) {
                array.add(entry.getKey());
            }
        }

        //cache array
        this.userPermCache.putArray("user-permissions-" + userID, array);

        return array;
    }

    private void mergePermissions (Map<String,PermissionStates> permMap, Map<String,PermissionStates> resultMap) {
        for (Map.Entry<String,PermissionStates> entry : permMap.entrySet()) {
            String permission = entry.getKey();
            PermissionStates value = entry.getValue();

            if (resultMap.containsKey(permission)) {
                //we have to merge values

                PermissionStates oldValue = resultMap.get(permission);

                if (oldValue == PermissionStates.NEVER) {
                    //we dont have to override anyting
                    continue;
                }

                if (value == PermissionStates.NEVER) {
                    //always override
                    resultMap.put(permission, value);

                    continue;
                }

                if (oldValue == PermissionStates.DISALLOW) {
                    //override value
                    resultMap.put(permission, value);
                } else if (oldValue == PermissionStates.ALLOW) {
                    //we dont have to do anything
                }
            } else {
                //set new value
                resultMap.put(permission, value);
            }
        }
    }

    @Override
    public void stop() {

    }

}
