package com.jukusoft.erp.server.gateway;

import com.jukusoft.erp.lib.gateway.ApiGateway;
import com.jukusoft.erp.lib.gateway.ResponseHandler;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.request.ApiRequestCodec;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.message.response.ApiResponseCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.json.JSONObject;

public class DefaultApiGateway implements ApiGateway {

    //instance of vert.x
    protected Vertx vertx = null;

    //clusered vert.x eventbus
    protected EventBus eventBus = null;

    //logger
    protected ILogging logger = null;

    protected DeliveryOptions deliveryOptions = null;

    /**
    * default constructor
     *
     * @param vertx instance of vert.x
    */
    public DefaultApiGateway (Vertx vertx, ILogging logger) {
        this.vertx = vertx;
        this.logger = logger;

        this.deliveryOptions = new DeliveryOptions();

        //set timeout of 3 seconds
        this.deliveryOptions.setSendTimeout(3 * 1000);

        //get eventbus
        this.eventBus = vertx.eventBus();

        //register codec for api request & response message
        eventBus.registerDefaultCodec(ApiRequest.class, new ApiRequestCodec());
        eventBus.registerDefaultCodec(ApiResponse.class, new ApiResponseCodec());
    }

    @Override
    public void handleRequestAsync(ApiRequest request, ResponseHandler handler) {
        //send message into cluster
        this.eventBus.send(request.getEvent(), request, reply -> {
            if (reply.succeeded()) {
                handler.handleResponse((ApiResponse) reply.result().body());
            } else {
                handler.responseFailed();
            }
        });
    }

}
