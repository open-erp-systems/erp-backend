package com.jukusoft.erp.lib.database;

import com.jukusoft.erp.lib.database.MySQLDatabase;
import com.jukusoft.erp.lib.database.MySQLRepository;
import io.vertx.core.Vertx;

public class AbstractMySQLRepository implements MySQLRepository {

    //instance of mysql database
    private MySQLDatabase mySQLDatabase = null;

    @Override
    public void init(Vertx vertx, MySQLDatabase database) {
        this.mySQLDatabase = mySQLDatabase;
    }

    protected MySQLDatabase getMySQLDatabase () {
        return this.mySQLDatabase;
    }

}
