package com.jukusoft.erp.lib.session;

import java.util.UUID;

public class SessionIDGenerator {

    /**
    * generate new unique session ID
     *
     * @return unique session id of type string
    */
    public static String generateSessionID () {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
