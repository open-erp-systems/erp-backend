package com.jukusoft.erp.core.module.loginform;

import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.module.AbstractModule;
import io.vertx.core.eventbus.Message;

public class LoginFormService {

    public ApiResponse loginForm (Message<ApiRequest> event, ApiRequest req, ApiResponse res) {
        res.setType(ApiResponse.RESPONSE_TYPE.CONTENT);

        String content =    "<html>" +
                "               <head>" +
                "                   <title>Login Form</title>" +
                "               </head>" +
                "               <body>" +
                "                   <form action=\"/try-login?ssid=" + req.getSessionID() + "\" method=\"POST\">" +
                "                       Username: <input type=\"text\" name=\"username\" /><br />" +
                "                       Password: <input type=\"password\" name=\"password\" /><br />" +
                "                       <br />" +
                "                       <input type=\"submit\" name=\"submitbutton\" value=\"Login\" />" +
                "                   </form>" +
                "               </body>" +
                "           </html>";

        res.getData().put("content", content);

        return res;
    }

}
