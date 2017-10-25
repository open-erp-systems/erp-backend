package com.jukusoft.erp.lib.service;

import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public interface IService {

    /**
     * get a reference to the Vert.x instance that deployed this verticle
     *
     * @return reference to the Vert.x instance
     */
    public Vertx getVertx ();

    /**
     * get instance of logger
     */
    public ILogging getLogger ();

    /**
     * get instance of event bus
     *
     * @return instance of event bus
     */
    public EventBus getEventBus ();

    /**
     * initialize module
     */
    public void init (Vertx vertx, AppContext context, ILogging logger);

}
