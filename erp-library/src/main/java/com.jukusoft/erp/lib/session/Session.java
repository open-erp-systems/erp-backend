package com.jukusoft.erp.lib.session;

import com.jukusoft.erp.lib.json.JSONLoadable;
import com.jukusoft.erp.lib.json.JSONSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Session implements JSONSerializable, JSONLoadable {

    //unique id of session
    protected final String sessionID;

    //session attributes
    protected Map<String,Object> attributes = new HashMap<>();

    /**
    * default constructor
     *
     * @param sessionID unique session id
    */
    public Session (String sessionID) {
        this.sessionID = sessionID;
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

    @Override
    public JSONObject toJSON() {
        //create new json object
        JSONObject json = new JSONObject();

        //add session ID
        json.put("session-id", this.sessionID);

        //add meta information
        JSONArray jsonArray = new JSONArray();

        for (Map.Entry<String,Object> entry : attributes.entrySet()) {
            JSONObject json1 = new JSONObject();
            json1.put("key", entry.getKey());
            json1.put("value", entry.getValue());
        }

        json.put("meta", jsonArray);

        return json;
    }

    @Override
    public void loadFromJSON(JSONObject json) {
        //this.sessionID = json.getString("session-id");

        JSONArray jsonArray = json.getJSONArray("meta");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json1 = jsonArray.getJSONObject(i);

            String key = json1.getString("key");
            String value = json1.getString("value");
        }
    }
}
