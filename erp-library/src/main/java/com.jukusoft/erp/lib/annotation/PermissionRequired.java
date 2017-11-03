package com.jukusoft.erp.lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks methods, which can only executed, if player has specific permissions
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionRequired {

    /**
    * array with all names of required permissions to use this method.
     * Else an error 403 - FORBIDDEN will be raised.
    */
    String[] requiredPermissions ();

}
