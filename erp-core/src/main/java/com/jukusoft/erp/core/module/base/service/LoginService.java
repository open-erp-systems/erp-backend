package com.jukusoft.erp.core.module.base.service;

import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.service.AbstractService;
import io.vertx.core.eventbus.Message;

public class LoginService extends AbstractService {

    @Route(routes = "/try-login")
    public ApiResponse tryLogin (Message<ApiRequest> event, ApiRequest req, ApiResponse res) {
        //validate username
        if (!req.getData().has("username")) {
            getLogger().warn(req.getMessageID(), "failed_login", "username wasnt set.");

            res.setStatusCode(ResponseType.BAD_REQUEST);
            return res;
        }

        //get username
        String username = req.getData().getString("username");

        //get password
        if (!req.getData().has("password")) {
            getLogger().warn(req.getMessageID(), "failed_login", "password wasnt set.");

            res.setStatusCode(ResponseType.BAD_REQUEST);
            return res;
        }

        //get password
        String password = req.getData().getString("password");

        getLogger().info("try_login", "try login '" + username + "' (IP: " + req.getIP() + ").");

        return res;
    }

}
