package com.jukusoft.erp.app.server;

import com.jukusoft.erp.app.server.impl.DefaultAppServer;
import com.jukusoft.erp.core.module.TestModule;

public class Main {

    public static void main (String[] args) {
        //create new app server
        AppServer server = new DefaultAppServer();

        //start app server
        server.start();

        //add modules
        server.deployModule(new TestModule(), TestModule.class);
    }

}
