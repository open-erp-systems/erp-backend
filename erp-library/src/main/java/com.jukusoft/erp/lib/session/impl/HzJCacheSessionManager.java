package com.jukusoft.erp.lib.session.impl;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.cache.ICache;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.session.ChangeableSessionManager;
import org.json.JSONObject;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class HzJCacheSessionManager implements ChangeableSessionManager {

    //session cache
    private ICache<String,String> sessionCache = null;

    public HzJCacheSessionManager(HazelcastInstance hazelcastInstance) {
        //initialize cache
        this.initCache(hazelcastInstance);
    }

    /**
    * initialize JCache
     *
     * @param hazelcastInstance instance of hazelcast
    */
    protected void initCache (HazelcastInstance hazelcastInstance) {
        //get name of hazelcast instance
        String instanceName = hazelcastInstance.getName();

         /*
        * use new JSR107 standard caching api for java with hazelcast connector,
        * so the JCache is distributed in the hazelcast cluster
        */
        CachingProvider cachingProvider = Caching.getCachingProvider();

        // Create Properties instance pointing to a named HazelcastInstance
        Properties properties = new Properties();
        properties.setProperty( HazelcastCachingProvider.HAZELCAST_INSTANCE_NAME,
                instanceName );

        URI cacheManagerName = null;

        try {
            cacheManagerName = new URI( "com.hazelcast.cache.HazelcastCachingProvider" );//com.hazelcast.cache.HazelcastCachingProvider my-cache-manager
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        /*
        * JCache Configuration
        */
        CacheManager cacheManager = cachingProvider
                .getCacheManager(cacheManagerName, null, properties);

        MutableConfiguration<Long,String> configuration = new MutableConfiguration<Long,String>();

        //enable jcache management and statistics, so jcache is shown in hazelcast mancenter
        configuration.setManagementEnabled(true);
        configuration.setStatisticsEnabled(true);

        //remove entries after 60 minutes without access
        configuration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 60l)));

        Cache<Long,String> cache = null;

        try {
            cache = cacheManager.createCache("session-cache", configuration);
        } catch (Exception e) {
            e.printStackTrace();

            cache = cacheManager.getCache("session-cache");

            //enable jcache management and statistics, so jcache is shown in hazelcast mancenter
            cache.getConfiguration(MutableConfiguration.class).setManagementEnabled(true);
            cache.getConfiguration(MutableConfiguration.class).setStatisticsEnabled(true);
        }

        //ICache extends Cache interface, provides more functionality
        this.sessionCache = cache.unwrap(ICache.class);
    }

    @Override
    public Session getSession(String ssid) {
        if (!this.exists(ssid)) {
            return null;
        }

        //create session instance from cache
        String jsonStr = this.sessionCache.get(ssid);
        Session session = Session.createFromJSON(new JSONObject(jsonStr), this);

        return session;
    }

    @Override
    public boolean exists(String ssid) {
        return this.sessionCache.containsKey(ssid);
    }

    @Override
    public Session generateNewSession() {
        //generate new sessionID
        String sessionID = this.generateNewSessionID();

        //create new session
        Session session = new Session(sessionID);

        //save session
        this.sessionCache.put(sessionID, session.toJSON().toString());

        return session;
    }

    protected String generateNewSessionID () {
        return SessionIDGenerator.generateSessionID();
    }

    @Override
    public void putSession(String ssid, Session session) {
        this.sessionCache.put(ssid, session.toJSON().toString());
    }
}
