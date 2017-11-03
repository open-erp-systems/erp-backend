package com.jukusoft.erp.lib.module;

import com.jukusoft.erp.lib.annotation.LoginRequired;
import com.jukusoft.erp.lib.annotation.PermissionRequired;
import com.jukusoft.erp.lib.cache.CacheTypes;
import com.jukusoft.erp.lib.cache.ICache;
import com.jukusoft.erp.lib.cache.InjectCache;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.database.InjectAppContext;
import com.jukusoft.erp.lib.database.InjectRepository;
import com.jukusoft.erp.lib.database.Repository;
import com.jukusoft.erp.lib.exception.CacheNotFoundException;
import com.jukusoft.erp.lib.exception.RequiredRepositoryNotFoundException;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.service.IService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractModule implements IModule {

    //instance of vertx
    protected Vertx vertx = null;

    //instance of module context
    protected AppContext context = null;

    //instance of logger
    protected ILogging logger = null;

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

        //inject caches
        this.injectCaches(repository);

        context.getDatabaseManager().addRepository(repository, cls);
    }

    protected <T extends IService> void addService(T page) {
        getLogger().debug("init_service", "Initialize service '" + page.getClass().getSimpleName() + "'.");

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

        //inject repositories
        this.injectRepositories(page);

        //inject caches
        this.injectCaches(page);
    }

    protected <T extends IService> void injectRepositories (T target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectRepository annotation = field.getAnnotation(InjectRepository.class);

            if (annotation != null && Repository.class.isAssignableFrom(field.getType())) {
                getLogger().debug("inject_repository", "try to inject repository '" + field.getType().getSimpleName() + "' in service: " + target.getClass().getSimpleName());
                injectRepositoryField(target, field, annotation.nullable());
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
                    getLogger().warn("inject_repository_error", "cannot set injected value: " + target.getClass().getSimpleName() + "." + field.getType());
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

    private <T extends IService> void registerHandler (String event, T page, Method method) {
        //register handler
        getEventBus().consumer(event, new Handler<Message<ApiRequest>>() {
            @Override
            public void handle(Message<ApiRequest> event) {
                getLogger().debug(event.body().getMessageID(), "consume_message", "consume message: " + event.body());

                //get message
                ApiRequest req = event.body();

                //check, if login is required
                if (method.isAnnotationPresent(LoginRequired.class)) {
                    //check, if user is logged in
                    if (!req.isLoggedIn()) {
                        ApiResponse res = new ApiResponse(req.getMessageID(), req.getSessionID(), req.getEvent());

                        //set forbidden status code
                        res.setStatusCode(ResponseType.FORBIDDEN);

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
                        if (!context.getPermissionManager().hasPermission(req.getUserID(), permissionName)) {
                            //user doesnt have the permission to see this page / use this api

                            ApiResponse res = new ApiResponse(req.getMessageID(), req.getSessionID(), req.getEvent());

                            //set forbidden status code
                            res.setStatusCode(ResponseType.FORBIDDEN);

                            //reply to api request
                            event.reply(res);

                            return;
                        }
                    }
                }

                if (method.getReturnType() == ApiResponse.class) {
                    //create new api answer
                    ApiResponse res = new ApiResponse(req.getMessageID(), req.getSessionID(), req.getEvent());

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
                    ApiResponse res = new ApiResponse(req.getMessageID(), req.getSessionID(), req.getEvent());

                    Handler<AsyncResult<ApiResponse>> handler = new Handler<AsyncResult<ApiResponse>>() {
                        @Override
                        public void handle(AsyncResult<ApiResponse> event1) {
                            if (!event1.succeeded()) {
                                //send error message
                                res.setStatusCode(ResponseType.INTERNAL_SERVER_ERROR);

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
                        res.setStatusCode(ResponseType.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        event.fail(500, e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();

                        //reply to api request
                        res.setStatusCode(ResponseType.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        event.fail(500, e.getLocalizedMessage());
                    } catch (Exception e) {
                        getLogger().warn(req.getMessageID(), "handler_exception", "Exception in " + page.getClass().getSimpleName() + "::" + method.getName() + ": " + e.getLocalizedMessage());

                        //reply to api request
                        res.setStatusCode(ResponseType.INTERNAL_SERVER_ERROR);
                        event.reply(res);

                        e.printStackTrace();
                    }
                }
            }
        });
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
