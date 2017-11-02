package com.jukusoft.erp.app.server;

import com.jukusoft.erp.lib.module.IModule;

public interface AppServer {

    /**
    * start microservice application server
    */
    public void start (AppStartListener listener);

    /**
    * set number of threads for event loop pool
     *
     * @param nOfThreads number of threads for event loop pool
    */
    public void setEventLoopPoolSize (int nOfThreads);

    /**
     * set number of threads for worker pool
     *
     * @param nOfThreads number of threads for worker pool
     */
    public void setWorkerPoolSize (int nOfThreads);

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
