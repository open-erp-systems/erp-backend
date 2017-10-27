package com.jukusoft.erp.lib.module;

import com.jukusoft.erp.lib.annotation.LoginRequired;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.database.InjectRepository;
import com.jukusoft.erp.lib.database.Repository;
import com.jukusoft.erp.lib.exception.RequiredRepositoryNotFoundException;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.service.IService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.json.JSONObject;

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
            /*System.out.println("method: " + method.getName());

            for (Annotation annotation : method.getDeclaredAnnotations()) {
                System.out.println("annotation found: " + annotation.getClass());
            }*/

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
    }

    protected <T extends IService> void injectRepositories (T target/*, Class<T> cls*/) {
        //iterate through all fields in class
        for (Field field : target.getClass().getDeclaredFields()) {
            //get annotation
            InjectRepository annotation = field.getAnnotation(InjectRepository.class);

            if (annotation != null && Repository.class.isAssignableFrom(field.getType())) {
                injectField(target, field, annotation.nullable());
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
    private void injectField(Object target, Field field, boolean nullable) {
        // check if component present
        if (context.getDatabaseManager().contains(field.getType())) {
            //set field accessible, so we can change value
            field.setAccessible(true);

            try {
                //set value of field
                field.set(target, context.getDatabaseManager().getRepositoryAsObject(field.getType()));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();

                throw new RuntimeException("Couldn't inject repository '" + field.getType() + "' in '"
                        + field.getDeclaringClass().getName() + "'. Exception: " + e.getLocalizedMessage());
            }
        } else if (!nullable) {
            throw new RequiredRepositoryNotFoundException("Repository '" + field.getType()
                    + "' is required by service '" + field.getDeclaringClass().getName() + "' but does not exist.");
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

                    try {
                        if (method.getParameterCount() > 2) {
                            method.invoke(page, event, req, res);
                        } else {
                            method.invoke(page, event, req);
                        }

                        getLogger().debug(req.getMessageID(), "reply_message", "reply to message: " + res);

                        //reply to api request
                        event.reply(res);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        event.fail(500, e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        event.fail(500, e.getLocalizedMessage());
                    } catch (Exception e) {
                        getLogger().warn(req.getMessageID(), "handler_exception", "Exception in " + page.getClass().getSimpleName() + "::" + method.getName() + ": " + e.getLocalizedMessage());

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
