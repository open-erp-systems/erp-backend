package com.jukusoft.erp.core.module;

import com.jukusoft.erp.core.module.test.TestPage;
import com.jukusoft.erp.lib.module.AbstractModule;

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
