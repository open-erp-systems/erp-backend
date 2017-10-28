package com.jukusoft.erp.lib.session;

public interface ChangeableSessionManager extends SessionManager {

    public void putSession (String ssid, Session session);

}
