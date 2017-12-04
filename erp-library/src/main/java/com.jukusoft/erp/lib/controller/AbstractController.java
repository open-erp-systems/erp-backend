package com.jukusoft.erp.lib.controller;

import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class AbstractController implements IController {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of module context
    protected AppContext context = null;

    //instance of logger
    protected ILogging logger = null;

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    /**
     * get instance of event bus
     *
     * @return instance of event bus
     */
    public EventBus getEventBus () {
        return this.vertx.eventBus();
    }

    public AppContext getContext () {
        return this.context;
    }

    @Override
    public ILogging getLogger() {
        if (this.logger == null) {
            throw new IllegalStateException("service isnt initialized.");
        }

        return this.logger;
    }

    @Override
    public void init(Vertx vertx, AppContext context, ILogging logger) {
        if (vertx == null) {
            throw new NullPointerException("vertx cannot be null.");
        }

        if (context == null) {
            throw new NullPointerException("app context cannot be null.");
        }

        if (logger == null) {
            throw new NullPointerException("logger cannot be null.");
        }

        this.vertx = vertx;
        this.context = context;
        this.logger = logger;
    }

}
