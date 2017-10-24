package com.jukusoft.erp.app.server.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.app.server.AppServer;
import com.jukusoft.erp.app.server.AppStartListener;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.logger.HzLogger;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.request.ApiRequestCodec;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.message.response.ApiResponseCodec;
import com.jukusoft.erp.lib.service.IService;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //list & map with deployed modules
    protected List<IService> deployedModules = new ArrayList<>();
    protected Map<Class<?>,IService> moduleMap = new HashMap<>();

    protected EventBus eventBus = null;

    @Override
    public void start(AppStartListener listener) {
        System.out.println("start local hazelcast instance now...");

        //create an new hazelcast instance
        Config config = new Config();

        //disable hazelcast logging
        config.setProperty("hazelcast.logging.type", "none");

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

                listener.afterStartup(this, true);
            } else {
                // failed!

                listener.afterStartup(null, false);
                //startFuture.fail(res.cause());
            }
        });
    }

    protected void initApp () {
        //create logger
        this.logger = new HzLogger(this.hazelcastInstance, this.clusterManager.getNodeID());

        this.logger.info("start_app_server", "Start application server: " + this.clusterManager.getNodeID());

        //get eventbus
        this.eventBus = vertx.eventBus();

        //register codec for api request & response message
        eventBus.registerDefaultCodec(ApiRequest.class, new ApiRequestCodec());
        eventBus.registerDefaultCodec(ApiResponse.class, new ApiResponseCodec());
    }

    @Override
    public <T extends IService> void deployModule(final T module, final Class<T> cls) {
        if (module == null) {
            throw new NullPointerException("module cannot be null.");
        }

        if (this.vertx == null) {
            throw new IllegalStateException("vertx isnt initialized yet, start server first (maybe server is starting asynchronous and hasnt finished loading?).");
        }

        //TODO: create new app context
        AppContext context = new AppContextImpl(this.vertx, this.logger);

        //first, initialize module
        module.init(this.vertx, context, this.logger);

        //start module
        try {
            module.start(future -> {
                try {
                    if (future.succeeded()) {
                        logger.info("deploy_module", "Module '" + cls.getSimpleName() + "' was deployed successfully.");

                        //add module to list and map
                        this.deployedModules.add(module);
                        this.moduleMap.put(cls, module);
                    } else {
                        logger.warn("deploy_module_error", "Module '" + cls.getSimpleName() + "' couldnt be deployed, cause: " + future.cause());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("deploy_module_exception", "Cannot deploy module '" + cls.getCanonicalName() + "': " + e.getLocalizedMessage());
        }
    }

    @Override
    public <T extends IService> void undeployModule(Class<T> cls) {
        //check, if module exists
        if (!this.moduleMap.containsKey(cls)) {
            return;
        }

        //get module
        IService module = this.moduleMap.get(cls);

        //remove module from list and map
        this.deployedModules.remove(module);
        this.moduleMap.remove(cls);

        //stop module
        try {
            module.stop(res -> {
                if (res.succeeded()) {
                    logger.info("undeploy_module", "Module '" + cls.getSimpleName() + "' was undeployed successfully.");
                } else {
                    logger.warn("undeploy_module_error", "Module '" + cls.getSimpleName() + "' couldnt be undeployed, cause: " + res.cause());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("undeploy_module_exception", "Cannot undeploy module '" + cls.getCanonicalName() + "': " + e.getLocalizedMessage());
        }
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
