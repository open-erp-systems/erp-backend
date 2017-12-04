package com.jukusoft.erp.core.module.base.service.permission;

import com.jukusoft.data.repository.PermissionRepository;
import com.jukusoft.erp.lib.database.InjectRepository;
import com.jukusoft.erp.lib.message.StatusCode;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.controller.AbstractController;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

public class PermissionController extends AbstractController {

    @InjectRepository
    protected PermissionRepository permissionRepository;

    @Route(routes = "/list-my-permissions")
    public void listPermissions (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //first get userID
        long userID = req.getUserID();

        //list all permissions of user
        this.permissionRepository.listPermissionsByUser(userID, res -> {
            if (!res.succeeded()) {
                getLogger().warn(req.getMessageID(), "list_permissions", "Internal Server Error, cannot get permissions of user: " + res.cause().getMessage());

                response.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);
                handler.handle(Future.succeededFuture(response));

                return;
            }

            response.setStatusCode(StatusCode.OK);
            response.getData().put("permissions", res.result());
            handler.handle(Future.succeededFuture(response));
        });
    }

}
