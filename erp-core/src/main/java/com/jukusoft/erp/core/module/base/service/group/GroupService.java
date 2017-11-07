package com.jukusoft.erp.core.module.base.service.group;

import com.jukusoft.data.entity.Group;
import com.jukusoft.data.entity.GroupMember;
import com.jukusoft.data.repository.GroupRepository;
import com.jukusoft.erp.lib.database.InjectRepository;
import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.route.Route;
import com.jukusoft.erp.lib.service.AbstractService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class GroupService extends AbstractService {

    @InjectRepository
    protected GroupRepository groupRepository;

    @Route(routes = "/list-my-groups")
    public void listMyGroups (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //first get user ID
        long userID = req.getUserID();

        //get an list with all groups where user is member
        this.groupRepository.getGroupsOfUser(userID, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //list with all groups where user is a member
            List<GroupMember> groupList = res.result();

            response.setStatusCode(ResponseType.OK);

            JsonArray array = new JsonArray();

            for (GroupMember groupMembership : groupList) {
                JsonObject json = new JsonObject();

                json.put("groupID", groupMembership.getGroup().getGroupID());
                json.put("name", groupMembership.getGroup().getName());
                json.put("descrption", groupMembership.getGroup().getDescription());
                json.put("color", groupMembership.getGroup().getColorHex());
                json.put("is_owner", groupMembership.isOwner());
                json.put("maingroup", groupMembership.isMainGroup());
                json.put("activated", groupMembership.getGroup().isActivated());

                //add group to list
                array.add(json);
            }

            response.getData().put("group_count", groupList.size());
            response.getData().put("groups", array);

            handler.handle(Future.succeededFuture(response));
        });
    }

    @Route(routes = "/list-my-groupIDs")
    public void listMyGroupIDs (Message<ApiRequest> event, ApiRequest req, ApiResponse response, Handler<AsyncResult<ApiResponse>> handler) {
        //first get user ID
        long userID = req.getUserID();

        //get an list with all groupIDs where user is member
        this.groupRepository.listGroupIDsOfUser(userID, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //list with all groups where user is a member
            long[] groupIDs = res.result();

            //set successful status code of request
            response.setStatusCode(ResponseType.OK);

            JsonArray array = new JsonArray();

            for (long groupID : groupIDs) {
                array.add(groupID);
            }

            response.getData().put("group_count", groupIDs.length);
            response.getData().put("groupIDs", array);

            handler.handle(Future.succeededFuture(response));
        });
    }

}
