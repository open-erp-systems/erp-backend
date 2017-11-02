package com.jukusoft.erp.server;

public interface IServer {

    /**
    * start server
    */
    public void start ();

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
    * shutdown server
    */
    public void stutdown ();

}
