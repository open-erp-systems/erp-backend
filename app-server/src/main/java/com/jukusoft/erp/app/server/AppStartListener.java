package com.jukusoft.erp.app.server;

@FunctionalInterface
public interface AppStartListener {

    public void afterStartup (AppServer server, boolean success);

}
