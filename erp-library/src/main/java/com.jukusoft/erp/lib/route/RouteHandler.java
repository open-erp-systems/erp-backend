package com.jukusoft.erp.lib.route;

import com.jukusoft.erp.lib.exception.HandlerException;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;

@FunctionalInterface
public interface RouteHandler {

    /**
    * handle request
    */
    public ApiResponse handle (ApiRequest req, ApiResponse res) throws HandlerException;

}
