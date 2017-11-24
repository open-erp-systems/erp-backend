package com.jukusoft.erp.lib.cache;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.cache.impl.DefaultCacheManager;
import com.jukusoft.erp.lib.logging.ILogging;

import java.io.File;
import java.util.List;

public interface CacheManager {

    /**
    * get instance of cache
     *
     * @param cacheName name of cache
     *
     * @return instance of cache or null, if cache doesnt exists
    */
    public ICache getCache (String cacheName);

    /**
    * check, if cache is present
     *
     * @param cacheName name of cache
    */
    public boolean containsCache (String cacheName);

    /**
    * crate an new cache instance
     *
     * @param cacheName name of cache
     * @param type type of cache
     *
     * @return instance of new cache
    */
    public ICache createCache (String cacheName, CacheTypes type);

    /**
    * remove cache and cleanup values
     *
     * @param cacheName name of cache
    */
    public void removeCache (String cacheName);

    /**
    * list names of all caches
     *
     * @return list with all cache names
    */
    public List<String> listCacheNames ();

    /**
    * clean up outdated entries from all caches
    */
    public void cleanUp ();

    /**
    * create default cache manager
     *
     * @param localCacheDir local cache directory
     * @param hazelcastInstance instance of hazelcast
    */
    public static CacheManager createDefaultCacheManager (File localCacheDir, HazelcastInstance hazelcastInstance, ILogging logger) {
        return new DefaultCacheManager(localCacheDir, hazelcastInstance, logger);
    }

}
