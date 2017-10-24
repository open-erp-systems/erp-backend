package com.jukusoft.erp.lib.session;

public interface SessionManager {

    /**
    * get session by session id
     *
     * @return instance of session or null, if session doesnt exists
    */
    public Session getSession (String ssid);

    /**
    * check, if session exists
     *
     * @return true, if session exists
    */
    public boolean exists (String ssid);

    /**
    * generate an new session
     *
     * @return instance of new session
    */
    public Session generateNewSession ();

}
