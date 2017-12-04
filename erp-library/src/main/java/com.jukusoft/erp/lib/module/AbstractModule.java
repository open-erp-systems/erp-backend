package com.jukusoft.erp.lib.module;

import com.jukusoft.erp.lib.annotation.InjectLogger;
import com.jukusoft.erp.lib.annotation.LoginRequired;
import com.jukusoft.erp.lib.annotation.PermissionRequired;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.database.*;
import com.jukusoft.erp.lib.exception.HandlerException;
import com.jukusoft.erp.lib.exception.RequiredRepositoryNotFoundException;
import com.jukusoft.erp.lib.exception.RequiredServiceNotFoundException;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.StatusCode;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.route.RouteHandler;
import com.jukusoft.erp.lib.route.RouteHandlerWithoutReturn;
import com.jukusoft.erp.lib.controller.IController;
import com.jukusoft.erp.lib.service.IService;
import com.jukusoft.erp.lib.service.InjectService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractModule implements IModule {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of module context
    protected AppContext context = null;

    //instance of logger
    protected ILogging logger = null;

    //map with all services
    protected Map<Class<?>,IService> serviceMap = new ConcurrentHashMap<>();

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    /**
     * get instance of event bus
     *
     * @return instance of event bus
     */
    public EventBus getEventBus () {
        return this.vertx.eventBus();
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

    protected <T extends Repository> void addRepository (T repository, Class<T> cls) {
        //inject app context
        this.injectAppContext(repository);

        //inject logger instance
        this.injectLogger(repository);

        //inject database
        this.injectDatabase(repository);

        //inject repositories
        this.injectRepositories(repository);

        //inject caches
        this.injectCaches(repository);

        context.getDatabaseManager().addRepository(repository, cls);
    }

    protected <T extends Repository> T getRepository (Class<T> cls) {
        return context.getDatabaseManager().getRepository(cls);
    }

    protected <T extends IController> void addController(T page) {
        getLogger().debug("init_controller", "Initialize controller '" + page.getClass().getSimpleName() + "'.");

        //initialize service
        page.init(getVertx(), this.context, getLogger());

        getLogger().debug("search_api_routes", "search for new routes in class " + page.getClass().getSimpleName());

        //get class of object
        Class cls = page.getClass();

        //list all available methods in class
        Method[] methods = cls.getDeclaredMethods();

        for (Method method : methods) {
            //check, if method has annotation @Route
            if (method.isAnnotationPresent(Route.class)) {
                for (String route : method.getAnnotation(Route.class).routes()) {
                    getLogger().debug("module_route_detected", "new route found: " + route + " --> " + cls.getCanonicalName());

                    registerHandler(route, page, method);
                }
            }
        }

        //inject logger
        this.injectLogger(page);

        //inject database
        this.injectDatabase(page);

        //inject repositories
        this.injectRepositories(page);

        //inject services
        this.injectServices(page);

        //inject caches
        this.injectCaches(page);
    }

    public <T extends IService> void addService (T service, Class<T> cls) {
        //inject app context
        this.injectAppContext(service);

        //inject logger instance
        this.injectLogger(service);

        //inject database
        this.injectDatabase(service);

        //inject repositories
        this.injectRepositories(service);

        //inject services
        this.injectServices(service);

        //inject caches
        this.injectCaches(service);

        //start service
        service.start();

        //put servive to map
        this.serviceMap.put(cls, service);
    }

    protected <T extends IService> T getService (Class<T> cls) {
        IService service = this.serviceMap.get(cls);

        if (service == null) {
            throw new IllegalStateException("service " + cls.getName() + " isnt registered yet. Add with addService() first.");
        }

        return cls.cast(service);
    }

    protected <T> void injectRepositories (T target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectRepository annotation = field.getAnnotation(InjectRepository.class);

            if (annotation != null && Repository.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_repository", "try to inject repository '" + field.getType().getSimpleName() + "' in class: " + target.getClass().getSimpleName());
                injectRepositoryField(target, field, annotation.nullable());
            }
        }
    }

    protected <T> void injectServices (T target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectService annotation = field.getAnnotation(InjectService.class);

            if (annotation != null && IService.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_service", "try to inject service '" + field.getType().getSimpleName() + "' in class: " + target.getClass().getSimpleName());
                injectServiceField(target, field, annotation.nullable());
            }
        }
    }

    protected void injectAppContext (Object target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectAppContext annotation = field.getAnnotation(InjectAppContext.class);

            if (annotation != null && AppContext.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_app_context", "try to inject app context '" + field.getType().getSimpleName() + "' in class: " + target.getClass().getSimpleName());

                //set field accessible, so we can change value
                field.setAccessible(true);

                //set value of field
                try {
                    field.set(target, this.context);

                    getLogger().debug("inject_app_context", "set value successfully: " + field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    getLogger().warn("inject_app_context_error", "cannot set injected value: " + target.getClass().getSimpleName() + "." + field.getType());
                }
            }
        }
    }

    protected void injectLogger (Object target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectLogger annotation = field.getAnnotation(InjectLogger.class);

            if (annotation != null && ILogging.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_logger", "try to inject logger '" + field.getType().getSimpleName() + "' in class: " + target.getClass().getSimpleName());

                //set field accessible, so we can change value
                field.setAccessible(true);

                //set value of field
                try {
                    field.set(target, this.logger);

                    getLogger().debug("inject_logger", "set value successfully: " + field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    getLogger().warn("inject_logger_error", "cannot set injected value: " + target.getClass().getSimpleName() + "." + field.getType());
                }
            }
        }
    }

    protected void injectDatabase (Object target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectDatabase annotation = field.getAnnotation(InjectDatabase.class);

            if (annotation != null && Database.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_database", "try to inject database '" + field.getType().getSimpleName() + "' in class: " + target.getClass().getSimpleName());

                //set field accessible, so we can change value
                field.setAccessible(true);

                //set value of field
                try {
                    field.set(target, this.context.getDatabaseManager().getMainDatabase());

                    getLogger().debug("inject_database", "set value successfully: " + field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    getLogger().warn("inject_database_error", "cannot set injected value: " + target.getClass().getSimpleName() + "." + field.getType());
                }
            }
        }
    }

    protected void injectCaches (Object target) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectCache annotation = field.getAnnotation(InjectCache.class);

            if (annotation != null && ICache.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_cache", "try to inject cache '" + field.getType().getSimpleName() + "' (name: " + annotation.name() + ") in class: " + target.getClass().getSimpleName());
                injectCacheField(target, field, annotation.name(), annotation.type(), annotation.nullable());
            }
        }
    }

    /**
     * Injects value of field in given service
     *
     * @param target
     *            The object whose field should be injected.
     * @param field
     *            The field.
     * @param nullable
     *            Whether the field can be null.
     */
    private void injectRepositoryField(Object target, Field field, boolean nullable) {
        // check if component present
        if (context.getDatabaseManager().contains(field.getType())) {
            //set field accessible, so we can change value
            field.setAccessible(true);

            try {
                Object value = context.getDatabaseManager().getRepositoryAsObject(field.getType());

                if (value == null) {
                    if (nullable) {
                        getLogger().debug("inject_repository", "Repository '" + field.getType().getSimpleName() + "' doesnt exists.");
                    } else {
                        throw new NullPointerException("injected object cannot be null.");
                    }
                } else {
                    //set value of field
                    field.set(target, value);

                    getLogger().debug("inject_repository", "set value successfully: " + field.getType());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();

                throw new RuntimeException("Couldn't inject repository '" + field.getType() + "' in '"
                        + field.getDeclaringClass().getName() + "'. Exception: " + e.getLocalizedMessage());
            }
        } else if (!nullable) {
            throw new RequiredRepositoryNotFoundException("Repository '" + field.getType()
                    + "' is required by service '" + field.getDeclaringClass().getName() + "' but does not exist.");
        } else {
            getLogger().warn("inject_repository", "Repository doesnt exists: " + field.getType().getSimpleName());
        }
    }

    /**
     * Injects value of field in given service
     *
     * @param target
     *            The object whose field should be injected.
     * @param field
     *            The field.
     * @param nullable
     *            Whether the field can be null.
     */
    private void injectServiceField(Object target, Field field, boolean nullable) {
        // check if component present
        if (this.serviceMap.get(field.getType()) != null) {
            //set field accessible, so we can change value
            field.setAccessible(true);

            try {
                Object value = this.serviceMap.get(field.getType());

                if (value == null) {
                    if (nullable) {
                        getLogger().debug("inject_service", "Service '" + field.getType().getSimpleName() + "' doesnt exists.");
                    } else {
                        throw new NullPointerException("injected object cannot be null.");
                    }
                } else {
                    //set value of field
                    field.set(target, value);

                    getLogger().debug("inject_service", "set value successfully: " + field.getType());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();

                throw new RuntimeException("Couldn't inject service '" + field.getType() + "' in '"
                        + field.getDeclaringClass().getName() + "'. Exception: " + e.getLocalizedMessage());
            }
        } else if (!nullable) {
            throw new RequiredServiceNotFoundException("Service '" + field.getType()
                    + "' is required by class '" + field.getDeclaringClass().getName() + "' but does not exist.");
        } else {
            getLogger().warn("inject_service", "Service doesnt exists: " + field.getType().getSimpleName());
        }
    }

    private void injectCacheField (Object target, Field field, String cacheName, CacheTypes cacheType, boolean nullable) {
        ICache cache = null;

        //check, if cache is present
        if (context.getCacheManager().containsCache(cacheName)) {
            //get cache
            cache = context.getCacheManager().getCache(cacheName);
        } else {
            //create new cache
            cache = context.getCacheManager().createCache(cacheName, cacheType);
        }

        //set field accessible, so we can change value
        field.setAccessible(true);

        try {
            Object value = cache;

            if (value == null) {
                if (nullable) {
                    getLogger().debug("inject_cache", "cache '" + cacheName + "' doesnt exists.");
                } else {
                    throw new NullPointerException("injected object cannot be null.");
                }
            } else {
                //set value of field
                field.set(target, value);

                getLogger().debug("inject_cache", "set value successfully: " + cacheName + " in class " + field.getDeclaringClass().getSimpleName() + ".");
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();

            throw new RuntimeException("Couldn't inject cache '" + field.getType() + "' (name: " + cacheName + ", type: " + cacheType + ") in '"
                    + field.getDeclaringClass().getName() + "'. Exception: " + e.getLocalizedMessage());
        }
    }

    private <T extends IController> void registerHandler (String eventName, T page, Method method) {
        //register handler
        getEventBus().consumer(eventName, Sync.fiberHandler((Message<ApiRequest> event) -> {
            try {
                getLogger().debug(event.body().getMessageID(), "consume_message", "consume message: " + event.body());

                //get message
                ApiRequest req = event.body();

                //check, if login is required
                if (method.isAnnotationPresent(LoginRequired.class)) {
                    //check, if user is logged in
                    if (!req.isLoggedIn()) {
                        ApiResponse res = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());

                        //set forbidden status code
                        res.setStatusCode(StatusCode.FORBIDDEN);

                        //reply to api request
                        event.reply(res);

                        return;
                    }
                }

                //check required permissions
                if (method.isAnnotationPresent(PermissionRequired.class)) {
                    //get annotation
                    PermissionRequired annotation = method.getAnnotation(PermissionRequired.class);

                    //check permissions
                    for (String permissionName : annotation.requiredPermissions()) {
                        if (!context.checkPermission(req, permissionName)) {
                            //user doesnt have the permission to see this page / use this api

                            ApiResponse res = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());

                            //set forbidden status code
                            res.setStatusCode(StatusCode.FORBIDDEN);

                            //reply to api request
                            event.reply(res);

                            return;
                        }
                    }
                }

                if (method.getReturnType() == ApiResponse.class) {
                    //create new api answer
                    ApiResponse res = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());

                    try {
                        res = (ApiResponse) method.invoke(page, event, req, res);

                        getLogger().debug(req.getMessageID(), "reply_message", "reply to message: " + res);

                        //reply to api request
                        event.reply(res);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        event.fail(500, e.getLocalizedMessage());

                        return;
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        event.fail(500, e.getLocalizedMessage());

                        return;
                    } catch (Exception e) {
                        getLogger().warn(req.getMessageID(), "handler_exception", "Exception in " + page.getClass().getSimpleName() + "::" + method.getName() + ": " + e.getLocalizedMessage());

                        e.printStackTrace();

                        event.fail(500, e.getLocalizedMessage());
                    }
                } else {
                    //create new api answer
                    ApiResponse res = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());

                    Handler<AsyncResult<ApiResponse>> handler = new Handler<AsyncResult<ApiResponse>>() {
                        @Override
                        public void handle(AsyncResult<ApiResponse> event1) {
                            if (!event1.succeeded()) {
                                //send error message
                                res.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);

                                getLogger().warn(req.getMessageID(), "reply_message", "reply error message: " + res + ", cause: " + event1.cause());

                                //reply to api request
                                event.reply(res);
                            } else {
                                getLogger().debug(req.getMessageID(), "reply_message", "reply to message: " + res);

                                //reply to api request
                                event.reply(event1.result());
                            }
                        }
                    };

                    try {
                        if (method.getParameterCount() > 3) {
                            method.invoke(page, event, req, res, handler);
                        } else {
                            method.invoke(page, event, req, handler);
                            throw new IllegalStateException("method " + page.getClass().getSimpleName() + "::" + method.getName() + " has wrong parameter count (should be 3, but is " + method.getParameterCount() + ").");
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();

                        //reply to api request
                        res.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        event.fail(500, e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();

                        //reply to api request
                        res.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        event.fail(500, e.getLocalizedMessage());
                    } catch (Exception e) {
                        getLogger().warn(req.getMessageID(), "handler_exception", "Exception in " + page.getClass().getSimpleName() + "::" + method.getName() + ": " + e.getLocalizedMessage());

                        //reply to api request
                        res.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                throw e;
            }
        }));
    }

    /**
    * add route handler
    */
    public void addRoute (final String eventName, RouteHandler handler) {
        if (eventName == null || eventName.isEmpty()) {
            throw new NullPointerException("event name cannot be null or empty");
        }

        if (handler == null) {
            throw new NullPointerException("handler cannot be null.");
        }

        //register route
        getEventBus().consumer(eventName, Sync.fiberHandler((Message<ApiRequest> event) -> {
            try {
                //get message
                ApiRequest req = event.body();

                //create new api answer
                ApiResponse res = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());

                try {
                    //handle request
                    res = handler.handle(req, res);
                } catch (HandlerException e) {
                    e.printStackTrace();

                    getLogger().warn(req.getMessageID(), "internal_server_error", "request: " + req.toString() + ", exception: " + e.getMessage());

                    //send internal server error
                    event.reply(this.generateInternalServerError(req));

                    return;
                }

                if (res == null) {
                    getLogger().warn(req.getMessageID(), "reply_message", "Api request result is null.");

                    //generate error messsage
                    ApiResponse errorResult = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());
                    errorResult.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);

                    //send error message
                    event.reply(res);

                    return;
                }

                //log answer
                getLogger().debug(req.getMessageID(), "reply_message", "reply to message: " + res);

                //reply to api request
                event.reply(res);
            } catch (Exception e) {
                e.printStackTrace();

                throw e;
            }
        }));
    }

    /**
    * add route, but dont use an return statement
     *
     * @param eventName event name
     * @param handler instance of handler
    */
    public void addRoute (final String eventName, RouteHandlerWithoutReturn handler) {
        this.addRoute(eventName, (req, res) -> {
            //call handler
            handler.handle(req, res);

            return res;
        });
    }

    private ApiResponse generateInternalServerError (ApiRequest req) {
        //generate error messsage
        ApiResponse errorResult = new ApiResponse(req.getMessageID(), req.getExternalID(), req.getSessionID(), req.getEvent());
        errorResult.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);

        return errorResult;
    }

    @Override
    public void start(Handler<Future<IModule>> handler) throws Exception {
        if (handler == null) {
            throw new NullPointerException("start future cannot be null.");
        }

        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(Future.failedFuture(e));
        }

        handler.handle(Future.succeededFuture(this));
        //startFuture.complete(AbstractModule.this);
    }

    @Override
    public void stop(Handler<Future<Void>> handler) throws Exception {
        try {
            this.stop();
            handler.handle(Future.succeededFuture());
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
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
