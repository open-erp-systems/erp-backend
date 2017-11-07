package com.jukusoft.erp.lib.context;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.cache.CacheManager;
import com.jukusoft.erp.lib.database.DatabaseManager;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.permission.PermissionManager;
import com.jukusoft.erp.lib.session.SessionManager;
import io.vertx.core.Vertx;

public interface AppContext {

    public Vertx getVertx ();

    public ILogging getLogger ();

    /**
    * get hazelcast instance
     *
     * @return instance of hazelcast
    */
    public HazelcastInstance getHazelcast ();

    /**
    * get instance of session manager
     *
     * @return instance of session manager
    */
    public SessionManager getSessionManager ();

    /**
    * get instance of database manager
     *
     * @return instance of database manager
    */
    public DatabaseManager getDatabaseManager ();

    /**
    * get instance of cache manager
     *
     * @return instance of cache manager
    */
    public CacheManager getCacheManager ();

    /**
    * get instance of permission manager
     *
     * @return instance of permission manager
    */
    public PermissionManager getPermissionManager ();

    /**
    * set instance of permission manager
     *
     * @param permissionManager instance of permission manager
    */
    public void setPermissionManager (PermissionManager permissionManager);

}
