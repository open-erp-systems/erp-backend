package com.jukusoft.erp.lib.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.json.JsonObject;

public class HazelcastCache implements ICache {

    protected IMap<String,String> cacheMap = null;

    public HazelcastCache (HazelcastInstance hazelcastInstance, String cacheName, ILogging logger) {
        this.cacheMap = hazelcastInstance.getMap("cache-" + cacheName);
    }

    @Override
    public void put(String key, JsonObject data) {
        if (data == null) {
            throw new NullPointerException("json object cannot be null.");
        }

        this.cacheMap.put(key, data.encode());
    }

    @Override
    public void remove(String key) {
        this.cacheMap.remove(key);
    }

    @Override
    public void removeAll() {
        this.cacheMap.clear();
    }

    @Override
    public boolean contains(String key) {
        return this.cacheMap.containsKey(key);
    }

    @Override
    public JsonObject get(String key) {
        return new JsonObject(this.cacheMap.get(key));
    }

    @Override
    public void cleanUp() {
        this.cacheMap.clear();
    }

}
