package com.jukusoft.erp.lib.message;

public enum StatusCode {

    NOT_FOUND(404),

    OK(200),

    BAD_REQUEST(400),

    WRONG_SESSION(400),

    FORBIDDEN(403),

    WRONG_PERMISSIONS(403),

    INTERNAL_SERVER_ERROR(500),

    SERVICE_UNAVAILABLE(503),

    UNKNOWN(500);

    private final int value;

    private StatusCode(int value) {
        this.value = value;
    }

    public int getValue () {
        return this.value;
    }

    public static StatusCode getByString (String str) {
        switch (str.trim().toUpperCase()) {
            case "NOT_FOUND":
                return NOT_FOUND;

            case "OK":

                return OK;

            case "BAD_REQUEST":

                return BAD_REQUEST;

            case "FORBIDDEN":

                return FORBIDDEN;

            case "WRONG_PERMISSIONS":

                return WRONG_PERMISSIONS;

            case "SERVICE_UNAVAILABLE":

                return SERVICE_UNAVAILABLE;

            default:

                return UNKNOWN;
        }
    }

}
