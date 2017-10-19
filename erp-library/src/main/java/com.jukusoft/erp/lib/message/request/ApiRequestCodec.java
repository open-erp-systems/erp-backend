package com.jukusoft.erp.lib.message.request;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.json.JSONObject;

public class ApiRequestCodec implements MessageCodec<ApiRequest, ApiRequest> {

    @Override
    public void encodeToWire(Buffer buffer, ApiRequest req) {
        //create new json object
        JSONObject json = new JSONObject();

        //put data
        json.put("event", req.eventName);
        json.put("data", req.data);
        json.put("ackID", req.ackID);
        json.put("cluster-message-id", req.getMessageID());

        //encode json object to string
        String jsonToStr = json.toString();

        // Length of JSON: is NOT characters count
        int length = jsonToStr.getBytes().length;

        // Write data into given buffer
        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public ApiRequest decodeFromWire(int position, Buffer buffer) {
        // My custom message starting from this *position* of buffer
        int _pos = position;

        // Length of JSON
        int length = buffer.getInt(_pos);

        // Get JSON string by it`s length
        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(_pos+=4, _pos+=length);
        JSONObject json = new JSONObject(jsonStr);

        ApiRequest req = new ApiRequest();

        req.eventName = json.getString("event");
        req.data = json.getJSONObject("data");
        req.ackID = json.getString("ackID");
        req.messageID = json.getLong("cluster-message-id");

        return req;
    }

    @Override
    public ApiRequest transform(ApiRequest apiRequest) {
        return apiRequest;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

}
