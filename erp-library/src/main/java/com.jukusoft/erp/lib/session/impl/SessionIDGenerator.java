package com.jukusoft.erp.lib.session.impl;

import java.util.UUID;

public class SessionIDGenerator {

    /**
    * generate new unique session ID
     *
     * @return unique session id of type string
    */
    protected static String generateSessionID () {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
