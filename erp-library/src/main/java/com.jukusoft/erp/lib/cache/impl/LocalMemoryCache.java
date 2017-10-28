package com.jukusoft.erp.lib.cache.impl;

import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalMemoryCache implements ICache {

    //map with all cache entries
    protected Map<String,JsonObject> cacheMap = new ConcurrentHashMap<>();

    public LocalMemoryCache (ILogging logger) {
        //
    }

    @Override
    public void put(String key, JsonObject data) {
        this.cacheMap.put(key, data);
    }

    @Override
    public void remove(String key) {
        this.cacheMap.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return this.cacheMap.get(key) != null;
    }

    @Override
    public JsonObject get(String key) {
        return this.cacheMap.get(key);
    }

    @Override
    public void cleanUp() {
        this.cacheMap.clear();
    }

}
