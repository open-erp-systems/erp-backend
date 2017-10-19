package com.jukusoft.erp.lib.message.response;

import com.jukusoft.erp.lib.message.request.ApiRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ApiResponseCodec implements MessageCodec<ApiResponse, ApiResponse> {

    @Override
    public void encodeToWire(Buffer buffer, ApiResponse apiResponse) {

    }

    @Override
    public ApiResponse decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public ApiResponse transform(ApiResponse apiResponse) {
        return apiResponse;
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
