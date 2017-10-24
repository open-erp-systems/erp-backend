package com.jukusoft.erp.core.service;

import com.jukusoft.erp.core.service.test.TestPage;
import com.jukusoft.erp.lib.service.AbstractService;

public class TestService extends AbstractService {

    @Override
    public void start() throws Exception {
        //register handler
        addApi(new TestPage());
    }

    @Override
    public void stop() throws Exception {

    }

}
