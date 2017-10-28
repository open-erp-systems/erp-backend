package com.jukusoft.erp.core.module.base.service;

import com.jukusoft.data.entity.User;
import com.jukusoft.data.repository.UserRepository;
import com.jukusoft.erp.lib.database.InjectRepository;
import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.service.AbstractService;
import com.jukusoft.erp.lib.session.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

public class LoginService extends AbstractService {

    @InjectRepository
    protected UserRepository userRepository;

    @Route(routes = "/try-login")
    public void tryLogin (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //validate username
        if (!req.getData().has("username")) {
            getLogger().warn(req.getMessageID(), "failed_login", "username wasnt set.");

            response.setStatusCode(ResponseType.BAD_REQUEST);
            handler.handle(Future.succeededFuture(response));
            return;
        }

        //get username
        String username = req.getData().getString("username");

        //get password
        if (!req.getData().has("password")) {
            getLogger().warn(req.getMessageID(), "failed_login", "password wasnt set.");

            response.setStatusCode(ResponseType.BAD_REQUEST);
            handler.handle(Future.succeededFuture(response));
            return;
        }

        //get password
        String password = req.getData().getString("password");

        getLogger().info(req.getMessageID(), "try_login", "try login '" + username + "' (IP: " + req.getIP() + ").");

        if (userRepository == null) {
            throw new NullPointerException("user repository cannot be null.");
        }

        //find user by username
        userRepository.getUserByUsername(username, res -> {
            if (!res.succeeded()) {
                generateFailedMessage("Couldnt find user. Maybe its an internal problem, please contact administrator.", response);
                handler.handle(Future.succeededFuture(response));
                return;
            }

            //get user
            User user = res.result();

            if (user == null) {
                getLogger().warn(req.getMessageID(), "try_login", "Couldnt found username '" + username + "'.");

                generateFailedMessage("User doesnt exists.", response);
                handler.handle(Future.succeededFuture(response));

                return;
            }

            getLogger().info(req.getMessageID(), "try_login", "username '" + username + "' was found (userID: " + user.getUserID() + ").");

            //check password
            userRepository.checkPassword(user.getUserID(), password, result -> {
                if (!result.succeeded()) {
                    getLogger().warn(req.getMessageID(), "try_login", "Exception occurred: " + result.cause());
                    generateFailedMessage("Internal Server Error! Cannot check password.", response);
                    handler.handle(Future.succeededFuture(response));

                    return;
                }

                boolean value = result.result();

                if (value == true) {
                    //login successfully
                    getLogger().info(req.getMessageID(), "login", "Login user successfully: " + username + " (userID: " + user.getUserID() + ").");

                    //get session
                    Session session = getContext().getSessionManager().getSession(req.getSessionID());

                    //set logged in state
                    session.login(user.getUserID(), user.getUsername());

                    //save session
                    session.flush();

                    generateSuccessMessage("Login successful!", response);
                    handler.handle(Future.succeededFuture(response));
                } else {
                    //login failed
                    getLogger().info(req.getMessageID(), "login", "Login failed (wrong password): " + username + " (userID: " + user.getUserID() + ").");

                    generateFailedMessage("Wrong password!", response);
                    handler.handle(Future.succeededFuture(response));
                }
            });
        });
    }

    @Route(routes = "/isloggedin")
    public void isLoggedIn (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //get session
        Session session = getContext().getSessionManager().getSession(req.getSessionID());

        response.getData().put("is-logged-in", session.isLoggedIn());

        handler.handle(Future.succeededFuture(response));
    }

    protected void generateFailedMessage (String message, ApiResponse response) {
        response.setStatusCode(ResponseType.OK);
        response.getData().put("login_state", "failed");
        response.getData().put("login_message", message);
    }

    protected void generateSuccessMessage (String message, ApiResponse response) {
        response.setStatusCode(ResponseType.OK);
        response.getData().put("login_state", "success");
        response.getData().put("login_message", message);
    }

}
