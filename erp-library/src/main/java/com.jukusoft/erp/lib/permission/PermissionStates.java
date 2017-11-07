package com.jukusoft.erp.lib.permission;

public enum PermissionStates {

    //allow, user has permission
    ALLOW,

    //disallow, user doesnt have permission, but can be access, if another of his groups has this permission
    DISALLOW,

    //if an other group grants this permission, reset this permission, user dont get this permission, independent from other ALLOW permissions
    NEVER

}
