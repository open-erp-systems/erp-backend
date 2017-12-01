package com.jukusoft.erp.server.message;

import com.jukusoft.erp.lib.message.StatusCode;
import io.vertx.core.json.JsonObject;

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
    public static String generateResponse (String event, JsonObject jsonData, String sessionID, String externalID, StatusCode type) {
        if (jsonData == null) {
            throw new NullPointerException("json data cannot be null.");
        }

        JsonObject json = new JsonObject();

        //add header information
        json.put("event", event);
        json.put("messageID", externalID);

        //set status code
        json.put("statusCode", type.getValue());
        json.put("status", type.name().toLowerCase());

        //put data
        json.put("data", jsonData);

        //put session id
        json.put("ssid", sessionID);

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
    public static String generateResponse (String event, String sessionID, String externalID, StatusCode type) {
        return generateResponse(event, new JsonObject(), sessionID, externalID, type);
    }

}
