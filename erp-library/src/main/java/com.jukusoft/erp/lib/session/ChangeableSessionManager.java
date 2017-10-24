package com.jukusoft.erp.lib.session;

import com.jukusoft.erp.lib.session.impl.Session;

public interface ChangeableSessionManager extends SessionManager {

    public void putSession (String ssid, Session session);

}
