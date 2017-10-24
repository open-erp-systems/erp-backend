package com.jukusoft.erp.app.server;

import com.jukusoft.erp.lib.service.IService;

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
    public <T extends IService> void deployModule (T module, Class<T> cls);

    /**
     * stop and remove module
     *
     * @param cls class name of module
     */
    public <T extends IService> void undeployModule (Class<T> cls);

    /**
    * shutdown microservice application server
    */
    public void shutdown ();

}
