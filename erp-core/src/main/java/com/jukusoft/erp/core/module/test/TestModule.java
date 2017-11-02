package com.jukusoft.erp.core.module.test;

import com.jukusoft.erp.core.module.test.service.TestService;
import com.jukusoft.erp.lib.module.AbstractModule;

public class TestModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //register handler
        addService(new TestService());
    }

    @Override
    public void stop() throws Exception {

    }

}
