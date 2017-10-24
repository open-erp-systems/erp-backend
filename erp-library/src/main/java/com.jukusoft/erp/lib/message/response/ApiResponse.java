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

    //meta information
    protected String sessionID = "";

    public enum RESPONSE_TYPE {
        JSON, CONTENT
    }

    protected RESPONSE_TYPE type = RESPONSE_TYPE.JSON;

    public ApiResponse (long messageID, String sessionID, String eventName) {
        this.messageID = messageID;
        this.sessionID = sessionID;
        this.eventName = eventName;
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

    public void setStatusCode (ResponseType type) {
        this.statusCode = type;
    }

    public long getMessageID () {
        return this.messageID;
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

            case "raw":
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
