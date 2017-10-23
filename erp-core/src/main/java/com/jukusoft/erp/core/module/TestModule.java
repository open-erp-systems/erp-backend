package com.jukusoft.erp.core.module;

import com.jukusoft.erp.lib.logging.ILogging;
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
                getLogger().debug(event.body().getMessageID(), "test_module_handle", "handle received message (event: " + event.address() + "): " + event.body());

                //get message
                ApiRequest req = event.body();

                //send new api answer
                ApiResponse res = new ApiResponse(req.getMessageID());

                //reply to api request
                event.reply(res);
            }
        });
    }

    @Override
    public void stop() throws Exception {

    }

}
