package com.jukusoft.erp.core.module;

import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.module.AbstractModule;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

public class TestModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //register handler
        getEventBus().consumer("/test", new Handler<Message<ApiRequest>>() {
            @Override
            public void handle(Message<ApiRequest> event) {
                //get message
                ApiRequest req = event.body();

                //send new api answer
                ApiResponse res = new ApiResponse(req.getMessageID());

                //send response
                getEventBus().send(req.getEvent(), res);
            }
        });
    }

    @Override
    public void stop() throws Exception {

    }

}
