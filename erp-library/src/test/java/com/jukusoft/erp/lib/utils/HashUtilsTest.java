package com.jukusoft.erp.lib.utils;

import com.hazelcast.util.HashUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HashUtilsTest {

    @Test
    public void testHashSHA256 () {
        String password = "hello";
        String salt = "";

        //expected hash (generated with PHP SHA 256)
        String expectedHash = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"; //"2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e730";

        assertEquals(expectedHash.length(), HashUtils.computeSHA256Hash(password, salt).length());
        assertEquals(expectedHash, HashUtils.computeSHA256Hash(password, salt));
    }

}
