package com.jukusoft.erp.lib.context;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.cache.CacheManager;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.database.DatabaseManager;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.permission.PermissionManager;
import com.jukusoft.erp.lib.permission.PermissionService;
import com.jukusoft.erp.lib.session.SessionManager;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

public class AppContextImpl implements AppContext {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of logger
    protected ILogging logger = null;

    //instance of hazelcast
    protected HazelcastInstance hazelcastInstance = null;

    //instance of session manager
    protected SessionManager sessionManager = null;

    //instance of database manager
    protected DatabaseManager dbManager = null;

    //instance of cache manager
    protected CacheManager cacheManager = null;

    //instance of permission manager
    protected PermissionManager permissionManager = null;

    protected PermissionService permissionService = null;

    public AppContextImpl (Vertx vertx, ILogging logger, HazelcastInstance hazelcastInstance, SessionManager sessionManager, DatabaseManager dbManager, CacheManager cacheManager, PermissionManager permissionManager, PermissionService permissionService) {
        if (vertx == null) {
            throw new NullPointerException("vertx cannot be null.");
        }

        if (logger == null) {
            throw new NullPointerException("logger cannot be null.");
        }

        if (hazelcastInstance == null) {
            throw new NullPointerException("hazelcast instance cannot be null.");
        }

        if (sessionManager == null) {
            throw new NullPointerException("session manager cannot be null.");
        }

        if (dbManager == null) {
            throw new NullPointerException("database manager cannot be null.");
        }

        if (cacheManager == null) {
            throw new NullPointerException("cache manager cannot be null.");
        }

        if (permissionManager == null) {
            throw new NullPointerException("permission manager cannot be null.");
        }

        if (permissionService == null) {
            throw new NullPointerException("permission service cannot be null.");
        }

        this.vertx = vertx;
        this.logger = logger;
        this.hazelcastInstance = hazelcastInstance;
        this.sessionManager = sessionManager;
        this.dbManager = dbManager;
        this.cacheManager = cacheManager;
        this.permissionManager = permissionManager;
        this.permissionService = permissionService;
    }

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    @Override
    public ILogging getLogger() {
        return this.logger;
    }

    @Override
    public HazelcastInstance getHazelcast() {
        return this.hazelcastInstance;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public DatabaseManager getDatabaseManager() {
        return this.dbManager;
    }

    @Override
    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    @Override
    public void setPermissionManager(PermissionManager permissionManager) {
        if (permissionManager == null) {
            throw new NullPointerException("permission manager cannot be null.");
        }

        this.permissionManager = permissionManager;
    }

    @Override
    public PermissionService getPermissionService() {
        return this.permissionService;
    }

    @Override
    public void setPermissionService(PermissionService service) {
        this.permissionService = service;
    }

    @Override
    public boolean checkPermission(ApiRequest request, String permission) {
        //initialize permissions, if neccessary
        if (request.getPermissions().size() == 0) {
            getLogger().debug(request.getMessageID(), "initialize_permissions", "load permissions for userID: " + request.getUserID());

            //load permissions
            JsonArray permArray = this.permissionService.listUserPermissions(request.getUserID());

            //cache permissions in cache object
            request.setPermissions(permArray.getList());
        }

        return request.hasPermission(permission);
    }

}
