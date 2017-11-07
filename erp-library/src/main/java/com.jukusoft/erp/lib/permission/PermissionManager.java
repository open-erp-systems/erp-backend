package com.jukusoft.erp.lib.permission;

public interface PermissionManager {

    /**
    * check, if user has permission
     *
     * @param userID id of user
     * @param permissionName name of permission
     *
     * @return true, if user has permission or false, if not
    */
    public boolean hasPermission (long userID, String permissionName);

}
