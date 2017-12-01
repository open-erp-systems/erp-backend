package com.jukusoft.erp.lib.message.response;

import com.jukusoft.erp.lib.message.StatusCode;
import io.vertx.core.json.JsonObject;

public class ApiResponse {

    //name of event
    protected String eventName = "";

    //json data
    protected JsonObject data = new JsonObject();

    protected String ackID = "";

    protected StatusCode statusCode = StatusCode.OK;

    protected long messageID = 0;

    //external ack ID
    protected String externalID = "";

    //meta information
    protected String sessionID = "";

    public enum RESPONSE_TYPE {
        JSON, CONTENT
    }

    protected RESPONSE_TYPE type = RESPONSE_TYPE.JSON;

    public ApiResponse (long messageID, String externalID, String sessionID, String eventName) {
        this.messageID = messageID;
        this.externalID = externalID;
        this.sessionID = sessionID;
        this.eventName = eventName;
    }

    public String getEvent () {
        return this.eventName;
    }

    public JsonObject getData () {
        return this.data;
    }

    public void setData (JsonObject json) {
        this.data = json;
    }

    public StatusCode getStatusCode () {
        return this.statusCode;
    }

    public void setStatusCode (StatusCode type) {
        this.statusCode = type;
    }

    public long getMessageID () {
        return this.messageID;
    }

    public String getExternalID () {
        return this.externalID;
    }

    public String getSessionID () {
        return this.sessionID;
    }

    public RESPONSE_TYPE getType() {
        return this.type;
    }

    public void setType (RESPONSE_TYPE type) {
        this.type = type;
    }

    public static RESPONSE_TYPE getTypeByString (String str) {
        switch (str.toLowerCase()) {
            case "json":
                return RESPONSE_TYPE.JSON;

            case "content":
                return RESPONSE_TYPE.CONTENT;

            default:
                throw new IllegalArgumentException("Unknown type: " + str);
        }
    }

    @Override
    public String toString () {
        return "api response (event: " + eventName + ", messageID: " + messageID + ", statusCode: " + statusCode.name() + ", data: " + this.data.toString() + ")";
    }

}
