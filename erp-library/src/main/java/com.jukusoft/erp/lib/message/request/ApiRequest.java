package com.jukusoft.erp.lib.message.request;

import com.jukusoft.erp.lib.json.JSONSerializable;
import org.json.JSONObject;

public class ApiRequest {

    //name of event
    protected String eventName = "";

    //json data
    protected JSONObject data = null;

    protected String ackID = "";

    //cluster wide unique message id for logging
    protected long messageID = 0;

    /**
    * default constructor
     *
     * @param event event name
     * @param data json data
    */
    public ApiRequest (String event, JSONObject data, long messageID) {
        this.eventName = event;
        this.data = data;
        this.messageID = messageID;
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

    @Override
    public String toString () {
        return "api request (event: " + eventName + ", messageID: " + messageID + ", data: " + this.data.toString() + ")";
    }

}
