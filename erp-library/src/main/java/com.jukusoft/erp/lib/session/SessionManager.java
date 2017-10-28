package com.jukusoft.erp.lib.session;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.session.impl.HzJCacheSessionManager;
import com.jukusoft.erp.lib.session.impl.HzMapSessionManager;

public interface SessionManager {

    /**
    * get session by session id
     *
     * @return instance of session or null, if session doesnt exists
    */
    public Session getSession (String ssid);

    /**
    * check, if session exists
     *
     * @return true, if session exists
    */
    public boolean exists (String ssid);

    /**
    * generate an new session
     *
     * @return instance of new session
    */
    public Session generateNewSession ();

    public static SessionManager createHzJCacheSessionManager (HazelcastInstance hazelcastInstance) {
        return new HzJCacheSessionManager(hazelcastInstance);
    }

    public static SessionManager createHzMapSessionManager (HazelcastInstance hazelcastInstance) {
        return new HzMapSessionManager(hazelcastInstance);
    }

}
