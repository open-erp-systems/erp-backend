package com.jukusoft.erp.app.server;

import com.jukusoft.erp.lib.module.IModule;
import io.vertx.core.Future;

public interface AppServer {

    /**
    * start microservice application server
    */
    public void start (AppStartListener listener);

    /**
    * add and start module
     *
     * @param module instance of module
     * @param cls class name of module
    */
    public <T extends IModule> void deployModule (T module, Class<T> cls);

    /**
     * stop and remove module
     *
     * @param cls class name of module
     */
    public <T extends IModule> void undeployModule (Class<T> cls);

    /**
    * shutdown microservice application server
    */
    public void shutdown ();

}
