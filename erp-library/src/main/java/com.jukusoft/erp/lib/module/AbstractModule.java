package com.jukusoft.erp.lib.module;

import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public abstract class AbstractModule implements IModule {

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

    @Override
    public ILogging getLogger() {
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

    @Override
    public void start(Handler<Future<IModule>> handler) throws Exception {
        if (handler == null) {
            throw new NullPointerException("start future cannot be null.");
        }

        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(Future.failedFuture(e));
        }

        handler.handle(Future.succeededFuture(this));
        //startFuture.complete(AbstractModule.this);
    }

    @Override
    public void stop(Handler<Future<Void>> handler) throws Exception {
        try {
            this.stop();
            handler.handle(Future.succeededFuture());
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
    }

    /**
     * start module
     *
     * @throws Exception
     */
    public abstract void start() throws Exception;

    /**
     * stop module
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;

}
