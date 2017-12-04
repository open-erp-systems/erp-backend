package com.jukusoft.erp.core.module.test;

import com.jukusoft.erp.core.module.test.service.TestController;
import com.jukusoft.erp.lib.module.AbstractModule;

public class TestModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //register handler
        addController(new TestController());
    }

    @Override
    public void stop() throws Exception {

    }

}
