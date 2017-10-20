package com.jukusoft.erp.lib.message;

public enum ResponseType {

    NOT_FOUND,

    OK,

    BAD_REQUEST,

    WRONG_PERMISSIONS,

    SERVICE_UNAVAILABLE,

    UNKNOWN;

    public static ResponseType getByString (String str) {
        switch (str.trim().toUpperCase()) {
            case "NOT_FOUND":
                return NOT_FOUND;

            case "OK":

                return OK;

            case "BAD_REQUEST":

                return BAD_REQUEST;

            case "WRONG_PERMISSIONS":

                return WRONG_PERMISSIONS;

            case "SERVICE_UNAVAILABLE":

                return SERVICE_UNAVAILABLE;

            default:

                return UNKNOWN;
        }
    }

}
