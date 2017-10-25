package com.jukusoft.erp.core.module.base;

import com.jukusoft.erp.core.module.base.service.LoginService;
import com.jukusoft.erp.core.module.base.service.loginform.LoginFormService;
import com.jukusoft.erp.lib.module.AbstractModule;

public class BaseModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        addApi(new LoginFormService());
        addApi(new LoginService());
    }

    @Override
    public void stop() throws Exception {

    }

}
