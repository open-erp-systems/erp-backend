package com.jukusoft.erp.core.module;

import com.jukusoft.erp.core.module.test.TestPage;
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
        addApi(new TestPage());
    }

    @Override
    public void stop() throws Exception {

    }

}
