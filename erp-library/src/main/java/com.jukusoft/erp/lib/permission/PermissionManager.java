package com.jukusoft.erp.lib.permission;

public interface PermissionManager {

    public enum STATES {
        //allow, user has permission
        ALLOW,

        //disallow, user doesnt have permission, but can be access, if another of his groups has this permission
        DISALLOW,

        //if an other group grants this permission, reset this permission, user dont get this permission, independent from other ALLOW permissions
        NEVER
    };

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
