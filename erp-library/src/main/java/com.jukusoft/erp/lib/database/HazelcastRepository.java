package com.jukusoft.erp.lib.database;

import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Vertx;

public interface HazelcastRepository {

    /**
     * initialize repository
     *
     * @param vertx instance of vertx
     * @param hazelcastInstance instance of hazelcast
     */
    public void init (Vertx vertx, HazelcastInstance hazelcastInstance);

}
