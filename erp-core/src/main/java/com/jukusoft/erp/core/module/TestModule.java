package com.jukusoft.erp.core.module;

import com.jukusoft.erp.lib.module.AbstractModule;

public class TestModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        System.err.println("start test module.");
    }

    @Override
    public void stop() throws Exception {

    }

}
