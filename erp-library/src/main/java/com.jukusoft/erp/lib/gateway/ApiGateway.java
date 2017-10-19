package com.jukusoft.erp.lib.gateway;

import com.jukusoft.erp.lib.message.request.ApiRequest;
import org.json.JSONObject;

public interface ApiGateway {

    /**
    * handle request
     *
     * @param request request
     * @param handler response handler
    */
    public void handleRequestAsync (ApiRequest request, ResponseHandler handler);

}
