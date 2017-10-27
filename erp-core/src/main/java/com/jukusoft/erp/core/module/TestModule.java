package com.jukusoft.erp.core.module;

import com.jukusoft.erp.core.module.test.TestService;
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
