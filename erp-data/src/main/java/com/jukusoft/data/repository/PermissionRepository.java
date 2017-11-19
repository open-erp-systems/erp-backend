package com.jukusoft.data.repository;

import com.jukusoft.data.entity.Group;
import com.jukusoft.data.entity.GroupMember;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.database.AbstractMySQLRepository;
import com.jukusoft.erp.lib.permission.PermissionStates;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRepository extends AbstractMySQLRepository {

    @InjectCache(name = "group-permissions-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache groupPermCache;

    public void listPermissionsByGroup (long groupID, Handler<AsyncResult<Map<String,PermissionStates>>> handler) {
        if (groupID <= 0) {
            throw new IllegalArgumentException("groupID cannot be <= 0.");
        }

        if (this.groupPermCache == null) {
            throw new NullPointerException("group permission cache cannot be null.");
        }

        //check, if result is already in cache
        if (this.groupPermCache.contains("group-permissions-" + groupID)) {
            //get cache object
            JsonArray jsonArray = this.groupPermCache.getArray("group-permissions-" + groupID);

            //convert rows to list
            Map<String,PermissionStates> map = this.createMapFromJSONArray(jsonArray);

            handler.handle(Future.succeededFuture(map));

            return;
        }
    }

    protected Map<String,PermissionStates> createMapFromJSONArray (JsonArray array) {
        Map<String,PermissionStates> map = new HashMap<>();

        return map;
    }

    public void listPermissionsByUser (long userID, Handler<AsyncResult<Map<String,PermissionStates>>> handler) {
        //
    }

}
