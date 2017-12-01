package com.jukusoft.erp.lib.gateway;

import com.jukusoft.erp.lib.message.response.ApiResponse;

public interface ResponseHandler {

    public void handleResponse (ApiResponse res);

    public void responseFailed ();

}
