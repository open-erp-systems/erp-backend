package com.jukusoft.erp.lib.session.impl;

import com.jukusoft.erp.lib.json.JsonLoadable;
import com.jukusoft.erp.lib.json.JsonSerializable;
import com.jukusoft.erp.lib.session.ChangeableSessionManager;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class Session implements JsonSerializable, JsonLoadable {

    //unique id of session
    protected final String sessionID;

    //session attributes
    protected Map<String,Object> attributes = new HashMap<>();

    //created unix timestamp
    protected long created = 0;

    private ChangeableSessionManager sessionManager = null;

    //flag, if user is logged in
    protected boolean isLoggedIn = false;

    //userID of -1, if user isnt logged in
    protected long userID = -1;

    /**
    * default constructor
     *
     * @param sessionID unique session id
    */
    public Session (String sessionID) {
        this.sessionID = sessionID;

        this.created = System.currentTimeMillis();
    }

    /**
    * get unique ID of session
     *
     * @return unique session id
    */
    public String getSessionID () {
        return this.sessionID;
    }

    /**
    * set a session attribute
     *
     * @param key key
     * @param value value
    */
    public <T> void putAttribute (String key, T value) {
        this.attributes.put(key, value);
    }

    /**
    * remove attribute by key
     *
     * @param key key of attribute
    */
    public void removeAttribute (String key) {
        this.attributes.remove(key);
    }

    /**
    * get attribute
    */
    public <T> T getAttribute (String key, Class<T> expectedClass) {
        if (!this.attributes.containsKey(key)) {
            return null;
        }

        return expectedClass.cast(this.attributes.get(key));
    }

    public boolean containsAttribute (String key) {
        return this.attributes.containsKey(key);
    }

    /**
    * writes session attributes to cache
    */
    public void flush () {
        this.sessionManager.putSession(this.sessionID, this);
    }

    public boolean isLoggedIn () {
        return this.isLoggedIn;
    }

    public long getUserID () {
        return this.userID;
    }

    @Override
    public JsonObject toJSON() {
        //create new json object
        JsonObject json = new JsonObject();

        //add session ID
        json.put("session-id", this.sessionID);

        //add created timestamp
        json.put("created", this.created);

        //add user information
        json.put("is-logged-in", this.isLoggedIn);
        json.put("user-id", this.userID);

        //add meta information
        JSONArray jsonArray = new JSONArray();

        for (Map.Entry<String,Object> entry : attributes.entrySet()) {
            JsonObject json1 = new JsonObject();
            json1.put("key", entry.getKey());
            json1.put("value", entry.getValue());

            jsonArray.put(json1);
        }

        json.put("meta", jsonArray);

        return json;
    }

    @Override
    public void loadFromJSON(JsonObject json) {
        //this.sessionID = json.getString("session-id");

        this.created = json.getLong("created");

        JsonArray jsonArray = json.getJsonArray("meta");

        //get user information
        this.isLoggedIn = json.getBoolean("is-logged-in");
        this.userID = json.getLong("user-id");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject json1 = jsonArray.getJsonObject(i);

            String key = json1.getString("key");
            String value = json1.getString("value");
        }
    }

    public static Session createFromJSON (JsonObject json, ChangeableSessionManager sessionManager) {
        //create new session with session id
        Session session = new Session(json.getString("session-id"));

        //load meta information
        session.loadFromJSON(json);

        session.sessionManager = sessionManager;

        return session;
    }

}
