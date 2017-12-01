package com.jukusoft.erp.lib.route;

import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;

@FunctionalInterface
public interface RouteHandlerWithoutReturn {

    /**
     * handle request
     */
    public void handle (ApiRequest req, ApiResponse res);

}
