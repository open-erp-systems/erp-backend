package com.jukusoft.erp.core.module.base;

import com.jukusoft.data.repository.GroupRepository;
import com.jukusoft.data.repository.MenuRepository;
import com.jukusoft.data.repository.PermissionRepository;
import com.jukusoft.data.repository.UserRepository;
import com.jukusoft.erp.core.module.base.service.cache.CacheController;
import com.jukusoft.erp.core.module.base.service.group.GroupController;
import com.jukusoft.erp.core.module.base.service.login.LoginController;
import com.jukusoft.erp.core.module.base.service.loginform.LoginFormController;
import com.jukusoft.erp.core.module.base.service.menu.MenuController;
import com.jukusoft.erp.core.module.base.service.permission.PermissionController;
import com.jukusoft.erp.core.permission.PermissionManagerImpl;
import com.jukusoft.erp.lib.module.AbstractModule;

public class BaseModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //add repositories
        addRepository(new UserRepository(), UserRepository.class);
        addRepository(new GroupRepository(), GroupRepository.class);
        addRepository(new PermissionRepository(), PermissionRepository.class);
        addRepository(new MenuRepository(), MenuRepository.class);

        //set permission manager
        context.setPermissionManager(new PermissionManagerImpl(getRepository(GroupRepository.class), getRepository(PermissionRepository.class)));

        //add services
        addController(new LoginFormController());
        addController(new LoginController());
        addController(new GroupController());
        addController(new MenuController());
        addController(new PermissionController());
        addController(new CacheController());
    }

    @Override
    public void stop() throws Exception {

    }

}
