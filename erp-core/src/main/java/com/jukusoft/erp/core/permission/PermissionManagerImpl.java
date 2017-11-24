package com.jukusoft.erp.core.permission;

import com.jukusoft.data.repository.GroupRepository;
import com.jukusoft.data.repository.PermissionRepository;
import com.jukusoft.erp.lib.permission.PermissionManager;
import io.vertx.core.Future;
import io.vertx.ext.sync.Sync;

public class PermissionManagerImpl implements PermissionManager {

    protected PermissionRepository permissionRepository = null;

    protected long PERMISSION_TIMEOUT = 500;

    public PermissionManagerImpl (GroupRepository groupRepository, PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public boolean hasPermission(long userID, String permissionName) {
        return Sync.awaitResult(h -> {
            permissionRepository.listPermissionsByUser(userID, res -> {
                if (!res.succeeded()) {
                    h.handle(Future.failedFuture(res.cause()));
                    return;
                }

                boolean hasPermission = res.result().contains(permissionName);
                h.handle(Future.succeededFuture(hasPermission));
            });
        });
    }

}
