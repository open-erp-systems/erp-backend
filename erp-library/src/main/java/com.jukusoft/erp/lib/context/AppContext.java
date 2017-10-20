package com.jukusoft.erp.lib.context;

import com.jukusoft.erp.lib.logging.ILogging;
import io.vertx.core.Vertx;

public interface AppContext {

    public Vertx getVertx ();

    public ILogging getLogger ();

}
