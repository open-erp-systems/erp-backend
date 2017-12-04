package com.jukusoft.erp.core.module.base.service.menu;

import com.jukusoft.data.repository.MenuRepository;
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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.Sync;

public class MenuController extends AbstractController {

    @InjectRepository
    protected MenuRepository menuRepository;

    @Route(routes = "/list-menus")
    public void listMenus (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //first, check if request contains menuID
        if (!req.getData().has("menuID")) {
            response.setStatusCode(StatusCode.BAD_REQUEST);

            //log
            getLogger().warn(req.getMessageID(), "list_menus", "request doesnt contains menuID: " + req.toString());

            //call handler
            handler.handle(Future.succeededFuture(response));

            return;
        }

        //get menuID
        int menuID = req.getData().getInt("menuID");

        //list menus from database
        menuRepository.listMenusByMenuID(menuID, Sync.fiberHandler(res -> {
            if (!res.succeeded()) {
                response.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);
                handler.handle(Future.succeededFuture(response));

                getLogger().warn(req.getMessageID(), "list_menus_api", "Couldnt get menu list by repository: " + res.cause().getMessage());

                return;
            }

            //get database rows
            JsonArray rows = res.result();

            //create new menu array, which is sended to client
            JsonArray menuArray = new JsonArray();

            //first, get all entries with parentID = -1
            for (int i = 0; i < rows.size(); i++) {
                //get row
                JsonObject row = rows.getJsonObject(i);

                int parentID = row.getInteger("parentID");

                if (parentID == -1) {
                    //it isnt an sub menu

                    System.out.println("check menu: " + row.getString("title"));

                    //check login permissions
                    if (!checkLogin(req.getUserID(), row.getInteger("login_required"))) {
                        getLogger().debug(req.getMessageID(), "list_menus_api", "Dont show menu, because user isnt logged in: " + row.getString("title"));

                        //login is required and user isnt logged in, so dont show this menu
                        continue;
                    }

                    //check permissions
                    if (!checkPermissions(req, row.getString("permissions"))) {
                        getLogger().debug(req.getMessageID(), "list_menus_api", "Dont show menu, because user doesnt have permission: " + row.getString("title"));

                        //user doesnt have permissions, so dont show this menu
                        continue;
                    }

                    //show menu
                    JsonObject json = new JsonObject();
                    json.put("title", row.getString("title"));
                    json.put("event", row.getString("event_name"));
                    json.put("shortcut", row.getString("shortcut"));
                    json.put("order", row.getInteger("order"));

                    //get id
                    int id = row.getInteger("id");

                    //add sub menus
                    this.addSubMenusToJson(json, id, req, req.getUserID(), rows);

                    //add menu to array
                    menuArray.add(json);
                }
            }

            response.setStatusCode(StatusCode.OK);
            response.getData().put("menu", menuArray);
            handler.handle(Future.succeededFuture(response));
        }));
    }

    protected void addSubMenusToJson (JsonObject json, int id, ApiRequest request, long userID, JsonArray rows) {
        JsonArray subMenus = new JsonArray();

        //search for menu entries with parentID = id
        for (int i = 0; i < rows.size(); i++) {
            //get row
            JsonObject row = rows.getJsonObject(i);

            //get parent id
            int parentID = row.getInteger("parentID");

            if (parentID != id) {
                //menu doesnt belongs to this parent menu
                continue;
            }

            System.out.println("check menu: " + row.getString("title"));

            //check login permissions
            if (!checkLogin(userID, row.getInteger("login_required"))) {
                //login is required and user isnt logged in, so dont show this menu
                continue;
            }

            //check permissions
            if (!checkPermissions(request, row.getString("permissions"))) {
                //user doesnt have permissions, so dont show this menu
                continue;
            }

            //show menu
            JsonObject entry = new JsonObject();
            entry.put("title", row.getString("title"));
            entry.put("event", row.getString("event_name"));
            entry.put("shortcut", row.getString("shortcut"));
            entry.put("order", row.getInteger("order"));

            //get id
            int id1 = row.getInteger("id");

            //add sub menus
            this.addSubMenusToJson(entry, id1, request, userID, rows);

            //add menu entry to array
            subMenus.add(entry);
        }

        json.put("submenus", subMenus);
    }

    protected boolean checkLogin (long userID, int loginRequired) {
        if (loginRequired == 0) {
            //login isnt required
            return true;
        }

        if (userID > 0) {
            //user is logged in, because -1 means guest and positive number an logged in user
            return true;
        }

        return false;
    }

    protected boolean checkPermissions (ApiRequest request, String permissions) {
        if (permissions.isEmpty()) {
            //no permissions required
            return true;
        }

        String[] requiredPermissions = permissions.split("|");

        //check, if user has one of this permissions
        for (int i = 0; i < requiredPermissions.length; i++) {
            boolean permitted = getContext().checkPermission(request, requiredPermissions[i]);

            if (permitted) {
                return true;
            }
        }

        return false;
    }

}
