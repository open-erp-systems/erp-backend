package com.jukusoft.erp.app.server.impl;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.session.SessionManager;
import io.vertx.core.Vertx;

public class AppContextImpl implements AppContext {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of logger
    protected ILogging logger = null;

    //instance of hazelcast
    protected HazelcastInstance hazelcastInstance = null;

    //instance of session manager
    protected SessionManager sessionManager = null;

    public AppContextImpl (Vertx vertx, ILogging logger, HazelcastInstance hazelcastInstance, SessionManager sessionManager) {
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

        this.vertx = vertx;
        this.logger = logger;
        this.hazelcastInstance = hazelcastInstance;
        this.sessionManager = sessionManager;
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

}
