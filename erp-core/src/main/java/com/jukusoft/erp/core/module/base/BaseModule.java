package com.jukusoft.erp.core.module.base;

import com.jukusoft.data.repository.GroupRepository;
import com.jukusoft.data.repository.UserRepository;
import com.jukusoft.erp.core.module.base.service.group.GroupService;
import com.jukusoft.erp.core.module.base.service.login.LoginService;
import com.jukusoft.erp.core.module.base.service.loginform.LoginFormService;
import com.jukusoft.erp.lib.module.AbstractModule;

public class BaseModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //add repositories
        addRepository(new UserRepository(), UserRepository.class);
        addRepository(new GroupRepository(), GroupRepository.class);

        //add services
        addService(new LoginFormService());
        addService(new LoginService());
        addService(new GroupService());
    }

    @Override
    public void stop() throws Exception {

    }

}
