package com.jukusoft.erp.app.server.impl;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.app.server.AppServer;
import com.jukusoft.erp.app.server.AppStartListener;
import com.jukusoft.erp.lib.cache.CacheManager;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.context.AppContextImpl;
import com.jukusoft.erp.lib.database.DatabaseManager;
import com.jukusoft.erp.lib.database.MySQLDatabase;
import com.jukusoft.erp.lib.database.impl.DatabaseManagerImpl;
import com.jukusoft.erp.lib.logger.HzLogger;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.request.ApiRequestCodec;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.message.response.ApiResponseCodec;
import com.jukusoft.erp.lib.module.IModule;
import com.jukusoft.erp.lib.session.SessionManager;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.io.File;
import java.io.IOException;
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
    protected List<IModule> deployedModules = new ArrayList<>();
    protected Map<Class<?>,IModule> moduleMap = new HashMap<>();

    //vertx eventbus
    protected EventBus eventBus = null;

    //app context
    protected AppContext context = null;

    //instance of session manager
    protected SessionManager sessionManager = null;

    //instance of mysql database
    protected MySQLDatabase database = null;

    //database manager
    protected DatabaseManager dbManager = null;

    //instance of cache manager
    protected CacheManager cacheManager = null;

    @Override
    public void start(AppStartListener listener) {
        System.out.println("start local hazelcast instance now...");

        //create an new hazelcast instance
        Config config = new Config();

        //disable hazelcast logging
        config.setProperty("hazelcast.logging.type", "none");

        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        config.getCacheConfigs().put("session-cache", cacheConfig);

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

                if (this.vertx == null) {
                    throw new NullPointerException("vertx cannot be null.");
                }

                //create logger
                this.logger = new HzLogger(this.hazelcastInstance, this.clusterManager.getNodeID());

                //create new cache manager
                this.cacheManager = CacheManager.createDefaultCacheManager(new File("./cache/"), this.hazelcastInstance, this.logger);

                //create database client and connect to database
                try {
                    this.connectToMySQL(res1 -> {
                        if (!res1.succeeded()) {
                            logger.error("database_error", "Couldnt connect to database: " + res1.cause());
                            System.exit(1);
                        }

                        logger.info("database_connection", "Connected to database successfully.");

                        //create database manager
                        this.dbManager = new DatabaseManagerImpl(this.vertx, this.database, this.hazelcastInstance);

                        //initialize application
                        initApp();

                        listener.afterStartup(this, true);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                // failed!

                listener.afterStartup(null, false);
                //startFuture.fail(res.cause());
            }
        });
    }

    protected void initApp () {
        this.logger.info("start_app_server", "Start application server: " + this.clusterManager.getNodeID());

        //get eventbus
        this.eventBus = vertx.eventBus();

        //register codec for api request & response message
        eventBus.registerDefaultCodec(ApiRequest.class, new ApiRequestCodec());
        eventBus.registerDefaultCodec(ApiResponse.class, new ApiResponseCodec());

        //create new session manager
        this.sessionManager = SessionManager.createHzMapSessionManager(this.hazelcastInstance);

        //create app content
        this.context = new AppContextImpl(this.vertx, this.logger, this.hazelcastInstance, this.sessionManager, this.dbManager, this.cacheManager);
    }

    @Override
    public <T extends IModule> void deployModule(final T module, final Class<T> cls) {
        if (module == null) {
            throw new NullPointerException("module cannot be null.");
        }

        if (this.vertx == null) {
            throw new IllegalStateException("vertx isnt initialized yet, start server first (maybe server is starting asynchronous and hasnt finished loading?).");
        }

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
    public <T extends IModule> void undeployModule(Class<T> cls) {
        //check, if module exists
        if (!this.moduleMap.containsKey(cls)) {
            return;
        }

        //get module
        IModule module = this.moduleMap.get(cls);

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

    protected void connectToMySQL (Handler<Future<Void>> handler) throws IOException {
        this.database = new MySQLDatabase(this.vertx);

        logger.info("database_connection", "try to connect to database...");

        //connect to database asynchronous
        this.database.connect("./config/mysql.cfg", handler);
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
