package com.jukusoft.erp.lib.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.io.IOException;
import java.util.List;

public interface Database {

    /**
    * create database connection
     *
     * @param configFile path to database configuration file
     * @param handler callback handler to execute, after connection was established or has failed
     *
     * @throws IOException if configuration file couldnt be readed
    */
    public void connect (String configFile, Handler<Future<Void>> handler) throws IOException;

    /**
    * check, if database connection is established
     *
     * @return if database connection is established
    */
    public boolean isConnected ();

    /**
    * get table prefix
     *
     * @return table prefix
    */
    public String getPrefix ();

    /**
    * add prefix to table name and get table name
     *
     * @param tableName table name without prefix
     *
     * @return full table name in database
    */
    public String getTableName (String tableName);

    /**
    * read row from database
     *
     * @param sql sql query
     * @param params query parameters
     *
     * @return result row or null, if row doesnt exists
    */
    public JsonObject getRow (String sql, JsonArray params);

    /**
    * read row from database
     *
     * @param sql sql query
     *
     * @return result row or null, if row doesnt exists
    */
    public JsonObject getRow (String sql);

    /**
    * query data
     *
     * @param sql sql query
     * @param params query parameters
     *
     * @return query result
    */
    public ResultSet query (String sql, JsonArray params);

    /**
     * query data
     *
     * @param sql sql query
     *
     * @return query result
     */
    public ResultSet query (String sql);

    /**
    * list rows from database
     *
     * @param sql sql query
     * @param params query parameters
     *
     * @return list with all rows
    */
    public List<JsonObject> listRows (String sql, JsonArray params);

    /**
     * list rows from database
     *
     * @param sql sql query
     *
     * @return list with all rows
     */
    public List<JsonObject> listRows (String sql);

    /**
     * execute update query
     *
     * @param sql sql update query
     * @param params query params
     *
     * @return update result
     */
    public UpdateResult update (String sql, JsonArray params);

    /**
     * execute update query
     *
     * @param sql sql update quer
     *
     * @return update result
     */
    public UpdateResult update (String sql);

    /**
    * start new transaction
    */
    public void startTransaction ();

    /**
    * commit transaction
    */
    public void endTransaction ();

    /**
    * rollback transaction
    */
    public void rollbackTransaction ();

    /**
    * close database connection
    */
    public void close ();

}
