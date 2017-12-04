package com.jukusoft.erp.lib.cache.impl;

import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalMemoryCache implements ICache {

    //map with all cache entries
    protected Map<String,JsonObject> cacheMap = new ConcurrentHashMap<>();

    //map with last access date (unix timestamp)
    protected Map<String,Long> accessMap = new ConcurrentHashMap<>();

    //time to live for cache objects (30 seconds)
    protected long TTL = 30 * 1000;

    public LocalMemoryCache (ILogging logger) {
        //
    }

    @Override
    public void put(String key, JsonObject data) {
        if (data == null) {
            throw new NullPointerException("json object cannot be null.");
        }

        this.cacheMap.put(key, data);

        //update last access timestamp
        this.accessMap.put(key, System.currentTimeMillis());
    }

    @Override
    public void remove(String key) {
        this.cacheMap.remove(key);
        this.accessMap.remove(key);
    }

    @Override
    public void removeAll() {
        this.cacheMap.clear();
        this.accessMap.clear();
    }

    @Override
    public boolean contains(String key) {
        return this.cacheMap.get(key) != null;
    }

    @Override
    public JsonObject get(String key) {
        JsonObject value = this.cacheMap.get(key);

        if (value != null) {
            this.accessMap.put(key, System.currentTimeMillis());
        }

        return value;
    }

    @Override
    public void cleanUp() {
        //get current unix timestamp
        long now = System.currentTimeMillis();

        //list with all keys to remove
        List<String> removeKeys = new ArrayList<>();

        //iterate through all entries
        for (Map.Entry<String,Long> entry : this.accessMap.entrySet()) {
            //check, if cache entry is outdated
            if (entry.getValue() + TTL < now) {
                //add entry to temporary list to avoid ConcurrentModificationException
                removeKeys.add(entry.getKey());
            }
        }

        //iterate through list with all entries, which should be removed from cache
        for (String key : removeKeys) {
            //remove entry from cache
            this.remove(key);
        }
    }

}
