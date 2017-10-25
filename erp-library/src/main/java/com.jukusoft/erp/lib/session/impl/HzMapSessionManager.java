package com.jukusoft.erp.lib.session.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.jukusoft.erp.lib.session.ChangeableSessionManager;
import com.jukusoft.erp.lib.session.SessionManager;
import org.json.JSONObject;

public class HzMapSessionManager implements ChangeableSessionManager {

    //map with all sessions
    protected IMap<String,String> sessionMap = null;

    /**
    * default constructor
     *
     * @param hazelcastInstance instance of hazelcast
    */
    public HzMapSessionManager (HazelcastInstance hazelcastInstance) {
        //get map
        this.sessionMap = hazelcastInstance.getMap("session-cache");
    }

    @Override
    public Session getSession(String ssid) {
        if (!this.exists(ssid)) {
            return null;
        }

        //create session instance from cache
        String jsonStr = this.sessionMap.get(ssid);
        Session session = Session.createFromJSON(new JSONObject(jsonStr), this);

        return session;
    }

    @Override
    public boolean exists(String ssid) {
        return this.sessionMap.containsKey(ssid);
    }

    @Override
    public Session generateNewSession() {
        //generate new sessionID
        String sessionID = this.generateNewSessionID();

        //create new session
        Session session = new Session(sessionID);

        //save session
        this.sessionMap.put(sessionID, session.toJSON().toString());

        return session;
    }

    protected String generateNewSessionID () {
        return SessionIDGenerator.generateSessionID();
    }

    @Override
    public void putSession(String ssid, Session session) {
        this.sessionMap.put(ssid, session.toJSON().toString());
    }

}
