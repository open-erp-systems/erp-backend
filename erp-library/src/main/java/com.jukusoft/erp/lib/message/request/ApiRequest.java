package com.jukusoft.erp.lib.message.request;

import org.json.JSONObject;

public class ApiRequest {

    //name of event
    protected String eventName = "";

    //json data
    protected JSONObject data = null;

    protected String ackID = "";

    //cluster wide unique message id for logging
    protected long messageID = 0;

    //meta information
    protected JSONObject meta = new JSONObject();
    protected String sessionID = "";
    protected boolean isLoggedIn = false;
    protected long userID = -1;

    /**
    * default constructor
     *
     * @param event event name
     * @param data json data
    */
    public ApiRequest (String event, JSONObject data, long messageID, String sessionID, boolean isLoggedIn, long userID) {
        this.eventName = event;
        this.data = data;
        this.messageID = messageID;
        this.sessionID = sessionID;
        this.isLoggedIn = isLoggedIn;
        this.userID = userID;
    }

    protected ApiRequest () {
        //
    }

    public String getEvent () {
        return this.eventName;
    }

    public JSONObject getData () {
        return this.data;
    }

    public long getMessageID () {
        return this.messageID;
    }

    public JSONObject getMeta () {
        return this.meta;
    }

    public String getSessionID () {
        return this.sessionID;
    }

    public boolean isLoggedIn () {
        return this.isLoggedIn;
    }

    public long getUserID () {
        return this.userID;
    }

    public String getIP () {
        return getMeta().getString("host");
    }

    public int getPort () {
        return getMeta().getInt("port");
    }

    @Override
    public String toString () {
        if (data.has("password")) {
            //dont log data to hide password
            return "api request (event: " + eventName + ", messageID: " + messageID + ", data: *********)";
        } else {
            return "api request (event: " + eventName + ", messageID: " + messageID + ", data: " + this.data.toString() + ")";
        }
    }

}
