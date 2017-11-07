package com.jukusoft.erp.core.permission;

import com.jukusoft.data.repository.GroupRepository;
import com.jukusoft.data.repository.PermissionRepository;
import com.jukusoft.erp.lib.permission.PermissionManager;

public class PermissionManagerImpl implements PermissionManager {

    public PermissionManagerImpl (GroupRepository groupRepository, PermissionRepository permissionRepository) {
        //
    }

    @Override
    public boolean hasPermission(long userID, String permissionName) {
        return false;
    }

}
