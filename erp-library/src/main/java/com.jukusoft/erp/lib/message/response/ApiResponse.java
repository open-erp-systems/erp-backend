package com.jukusoft.erp.lib.message.response;

import com.jukusoft.erp.lib.message.ResponseType;
import org.json.JSONObject;

public class ApiResponse {

    //name of event
    protected String eventName = "";

    //json data
    protected JSONObject data = new JSONObject();

    protected String ackID = "";

    protected ResponseType statusCode = ResponseType.OK;

    protected long messageID = 0;

    public ApiResponse (long messageID) {
        this.messageID = messageID;
    }

    public String getEvent () {
        return this.eventName;
    }

    public JSONObject getData () {
        return this.data;
    }

    public void setData (JSONObject json) {
        this.data = json;
    }

    public ResponseType getStatusCode () {
        return this.statusCode;
    }

    public long getMessageID () {
        return this.messageID;
    }

    @Override
    public String toString () {
        return "api response (event: " + eventName + ", messageID: " + messageID + ", statusCode: " + statusCode.name() + ", data: " + this.data.toString() + ")";
    }

}
