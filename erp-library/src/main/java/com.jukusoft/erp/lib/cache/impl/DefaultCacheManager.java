package com.jukusoft.erp.lib.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.cache.CacheManager;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.logging.ILogging;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheManager implements CacheManager {

    //local cache directory
    protected File localCacheDir = null;

    //instance of hazelcast
    protected HazelcastInstance hazelcastInstance = null;

    //instance of logger
    protected ILogging logger = null;

    //map with all cache instances
    protected Map<String,ICache> cacheMap = new ConcurrentHashMap<>();

    public DefaultCacheManager (File localCacheDir, HazelcastInstance hazelcastInstance, ILogging logger) {
        this.localCacheDir = localCacheDir;
        this.hazelcastInstance = hazelcastInstance;
        this.logger = logger;
    }

    @Override
    public ICache getCache(String cacheName) {
        return this.cacheMap.get(cacheName);
    }

    @Override
    public ICache createCache(String cacheName, CacheTypes type) {
        //first check, if cache already exists
        if (this.getCache(cacheName) != null) {
            throw new IllegalStateException("cache '" + cacheName + "' does already exists.");
        }

        ICache cache = null;

        switch (type) {
            case FILE_CACHE:
                cache = new FileSystemCache(localCacheDir.getAbsolutePath() + "/" + cacheName.toLowerCase() + "/", cacheName, this.logger);
                break;

            case LOCAL_MEMORY_CACHE:
                cache = new LocalMemoryCache(this.logger);
                break;

            case HAZELCAST_CACHE:
                cache = new HazelcastCache(this.hazelcastInstance, cacheName, this.logger);
                break;

            default:
                throw new IllegalArgumentException("Unknown cache type: " + type);
        }

        this.cacheMap.put(cacheName, cache);

        return cache;
    }

    @Override
    public void removeCache(String cacheName) {
        ICache cache = this.getCache(cacheName);

        //remove cache from map
        this.cacheMap.remove(cacheName);

        //cleanUp cache if neccessary
        if (cache != null) {
            cache.cleanUp();
        }
    }

}
