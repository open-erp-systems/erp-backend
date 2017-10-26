package com.jukusoft.erp.lib.database;

import io.vertx.core.Vertx;

public interface MySQLRepository extends Repository {

    /**
    * initialize repository
     *
     * @param vertx instance of vertx
     * @param database instance of database
    */
    public void init (Vertx vertx, MySQLDatabase database);

}
