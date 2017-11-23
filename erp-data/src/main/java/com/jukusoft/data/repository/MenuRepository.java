package com.jukusoft.data.repository;

import com.jukusoft.data.entity.Group;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.database.AbstractMySQLRepository;
import com.jukusoft.erp.lib.utils.JsonUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class MenuRepository extends AbstractMySQLRepository {

    @InjectCache(name = "menu-cache", type = CacheTypes.HAZELCAST_CACHE)
    protected ICache menuCache;

    public void listMenusByMenuID (int menuID, Handler<AsyncResult<JsonArray>> handler) {
        //check, if cache is available
        if (this.menuCache == null) {
            throw new NullPointerException("menu cache cannot be null.");
        }

        if (menuID <= 0) {
            throw new IllegalArgumentException("menuID has to be greater than 0. Current value: " + menuID);
        }

        //check, if menu entries are in cache
        if (this.menuCache.contains("menu-" + menuID)) {
            handler.handle(Future.succeededFuture(this.menuCache.getArray("menu-" + menuID)));
            return;
        }

        //read menus from database
        getMySQLDatabase().listRows("SELECT * FROM `{prefix}menu` WHERE `menuID` = '" + menuID + "' ORDER BY `order`; ", res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //get rows
            List<JsonObject> rows = res.result();

            //convert list to json array
            JsonArray rowsArray = JsonUtils.convertJsonObjectListToArray(rows);

            //cache rows
            this.menuCache.putArray("menu-" + menuID, rowsArray);

            //call handler
            handler.handle(Future.succeededFuture(rowsArray));
        });
    }

}
