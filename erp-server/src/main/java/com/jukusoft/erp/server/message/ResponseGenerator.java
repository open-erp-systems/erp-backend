package com.jukusoft.erp.server.message;

import com.jukusoft.erp.lib.message.ResponseType;
import org.json.JSONObject;

public class ResponseGenerator {

    /**
    * generate response data
     *
     * @param event event name
     * @param jsonData json data
     * @param type response type
     *
     * @return json string
    */
    public static String generateResponse (String event, JSONObject jsonData, ResponseType type) {
        if (jsonData == null) {
            throw new NullPointerException("json data cannot be null.");
        }

        JSONObject json = new JSONObject();

        json.put("event", event);

        //set status code
        json.put("statusCode", type.getValue());
        json.put("status", type.name().toLowerCase());

        //put data
        json.put("data", jsonData);

        return json.toString();
    }

    /**
     * generate response data
     *
     * @param event event name
     * @param type response type
     *
     * @return json string
     */
    public static String generateResponse (String event, ResponseType type) {
        return generateResponse(event, new JSONObject(), type);
    }

}
