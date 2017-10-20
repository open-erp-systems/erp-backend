package com.jukusoft.erp.app.server.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.app.server.AppServer;
import com.jukusoft.erp.lib.logger.HzLogger;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.module.IModule;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class DefaultAppServer implements AppServer {

    //vert.x options
    protected VertxOptions vertxOptions = null;

    //instance of vert.x
    protected Vertx vertx = null;

    //hazelcast instance
    protected HazelcastInstance hazelcastInstance = null;

    //vert.x cluster manager
    protected ClusterManager clusterManager = null;

    //logger
    protected ILogging logger = null;

    @Override
    public void start() {
        //create an new hazelcast instance
        Config config = new Config();
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        //create new vert.x cluster manager
        this.clusterManager = new HazelcastClusterManager(this.hazelcastInstance);

        //create new vertx.io options
        this.vertxOptions = new VertxOptions();

        //use clustered mode of vert.x
        this.vertxOptions.setClustered(true);

        //set cluster manager
        this.vertxOptions.setClusterManager(this.clusterManager);

        //create clustered vertx. instance
        Vertx.clusteredVertx(this.vertxOptions, res -> {
            if (res.succeeded()) {
                this.vertx = res.result();

                //initialize application
                initApp();
            } else {
                // failed!

                System.exit(1);
            }
        });
    }

    protected void initApp () {
        //create logger
        this.logger = new HzLogger(this.hazelcastInstance, this.clusterManager.getNodeID());

        this.logger.info("start_app_server", "Start application server: " + this.clusterManager.getNodeID());
    }

    @Override
    public <T extends IModule> void addModule(T module, Class<T> cls) {

    }

    @Override
    public <T extends IModule> void removeModule(Class<T> cls) {

    }

    @Override
    public void shutdown() {
        this.logger.info("shutdown_app_server", "Shutdown application server: " + this.clusterManager.getNodeID());

        this.vertx.close(res -> {
            if (res.succeeded()) {
                System.out.println("app server was shutting down successfully.");
            } else {
                System.out.println("Cannot shutdown app server.");
            }
        });
    }

}
