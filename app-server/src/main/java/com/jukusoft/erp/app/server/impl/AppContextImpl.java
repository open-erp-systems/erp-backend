package com.jukusoft.erp.app.server.impl;

import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Vertx;

public class AppContextImpl implements AppContext {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of logger
    protected ILogging logger = null;

    public AppContextImpl (Vertx vertx, ILogging logger) {
        if (vertx == null) {
            throw new NullPointerException("vertx cannot be null.");
        }

        if (logger == null) {
            throw new NullPointerException("logger cannot be null.");
        }

        this.vertx = vertx;
        this.logger = logger;
    }

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    @Override
    public ILogging getLogger() {
        return this.logger;
    }

}
