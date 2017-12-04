package com.jukusoft.erp.core.module.base.service.cache;

import com.jukusoft.erp.lib.annotation.PermissionRequired;
import com.jukusoft.erp.lib.cache.CacheManager;
import com.jukusoft.erp.lib.message.StatusCode;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.controller.AbstractController;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class CacheController extends AbstractController {

    @Route(routes = "/clear-cache")
    @PermissionRequired(requiredPermissions = "CAN_CLEAR_CACHE")
    public void clearCache (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //get cache manager
        CacheManager cacheManager = getContext().getCacheManager();

        //get list with all cache names
        List<String> cacheNames = cacheManager.listCacheNames();

        JsonArray cacheArray = new JsonArray();

        //clear caches
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).cleanUp();
            cacheArray.add(cacheName);
        }

        response.setStatusCode(StatusCode.OK);
        response.getData().put("caches", cacheArray);
        handler.handle(Future.succeededFuture(response));
    }

}
