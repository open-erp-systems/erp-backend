package com.jukusoft.erp.lib.module;

import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

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
    public void start(Future<Void> startFuture) throws Exception {
        if (startFuture == null) {
            throw new NullPointerException("start future cannot be null.");
        }

        System.err.println("try to start abstract module.");

        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            startFuture.fail(e);
        }

        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        this.stop();
        stopFuture.complete();
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
