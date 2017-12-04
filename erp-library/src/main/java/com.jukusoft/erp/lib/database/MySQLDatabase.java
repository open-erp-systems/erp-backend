package com.jukusoft.erp.lib.database;

import com.jukusoft.erp.lib.utils.FileUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.vertx.ext.sync.Sync.awaitResult;

public class MySQLDatabase implements Database {

    //instance of vertx
    protected Vertx vertx = null;

    //sql client
    protected SQLClient client = null;

    //sql connection
    protected SQLConnection connection = null;

    //table prefix
    protected String prefix = "";

    public MySQLDatabase (Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void connect (String configFile, Handler<Future<Void>> handler) throws IOException {
        //check, if config file exists
        if (!new File(configFile).exists()) {
            throw new FileNotFoundException("Couldnt found database config file '" + configFile + "'.");
        }

        //read file content
        String content = FileUtils.readFile(configFile, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(content);

        //get mysql connection paramters
        String host = json.getString("host");
        int port = json.getInt("port");
        String username = json.getString("username");
        String password = json.getString("password");
        String database = json.getString("database");
        int maxPoolSize = 30;

        if (json.has("max_pool_size")) {
            maxPoolSize = json.getInt("max_pool_size");
        }

        String urlAdd = "&profileSQL=true";
        boolean logging = false;

        if (json.has("logging")) {
            logging = json.getBoolean("logging");

            if (!logging) {
                urlAdd = "";
            }
        }

        this.prefix = json.getString("prefix");

        //mysql config
        JsonObject config = new JsonObject();

        //https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html
        config.put("url", "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC&zeroDateTimeBehavior=convertToNull" + urlAdd);
        config.put("driver_class", "com.mysql.cj.jdbc.Driver");
        config.put("max_pool_size", maxPoolSize);
        config.put("user", username);
        config.put("password", password);

        this.client = JDBCClient.createShared(this.vertx, config);

        System.out.println("try to connect to database: " + config.getString("url"));

        this.client.getConnection(res -> {
            if (res.succeeded()) {
                //save connection
                this.connection = res.result();

                //http://vertx.io/docs/vertx-jdbc-client/java/

                //http://vertx.io/docs/guide-for-java-devs/

                //call handler
                handler.handle(Future.succeededFuture());
            } else {
                // Failed to get connection - deal with it
                handler.handle(Future.failedFuture(res.cause()));

                System.err.println("Couldnt connect to mysql database: " + config.getString("url"));
            }
        });
    }

    @Override
    public boolean isConnected () {
        return this.connection != null;
    }

    public SQLConnection getConnection() {
        return this.connection;
    }

    @Override
    public String getPrefix () {
        return this.prefix;
    }

    @Override
    public String getTableName (String tableName) {
        return getPrefix() + tableName;
    }

    public void getRow (String sql, Handler<AsyncResult<JsonObject>> handler) {
        this.connection.query(sql, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                throw new IllegalStateException("Couldnt execute query to read row: " + sql);
            }

            //get result set
            ResultSet rs = res.result();

            //check, if no row was found
            if (rs.getRows().isEmpty()) {
                handler.handle(Future.succeededFuture(null));
                return;
            }

            //get first row
            JsonObject row = rs.getRows().get(0);

            handler.handle(Future.succeededFuture(row));
        });
    }

    public void getRow (String sql, JsonArray params, Handler<AsyncResult<JsonObject>> handler) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        this.connection.queryWithParams(sql1, params, res -> {
            if (!res.succeeded()) {
                if (res.cause() != null) {
                    res.cause().printStackTrace();
                }

                handler.handle(Future.failedFuture(res.cause()));
                throw new IllegalStateException("Couldnt execute query to read row: " + sql1 + ", exception: " + res.cause());
            }

            //get result set
            ResultSet rs = res.result();

            //check, if no row was found
            if (rs.getRows().isEmpty()) {
                handler.handle(Future.succeededFuture(null));
                return;
            }

            //get first row
            JsonObject row = rs.getRows().get(0);

            handler.handle(Future.succeededFuture(row));
        });
    }

    @Override
    public JsonObject getRow (String sql, JsonArray params) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        //query (with fiber for non-blocking event loop)
        ResultSet rs = awaitResult(h -> this.connection.queryWithParams(sql1, params, h));

        //check, if no row was found
        if (rs.getRows().isEmpty()) {
            return null;
        }

        //get first row
        JsonObject row = rs.getRows().get(0);

        return row;
    }

    @Override
    public JsonObject getRow (String sql) {
        return this.getRow(sql, new JsonArray());
    }

    public void query (String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
        this.connection.queryWithParams(sql, params, handler);
    }

    @Override
    public ResultSet query (String sql, JsonArray params) {
        return awaitResult(h -> this.query(sql, params, h));
    }

    @Override
    public ResultSet query (String sql) {
        return this.query(sql, new JsonArray());
    }

    public void listRows (String sql, Handler<AsyncResult<List<JsonObject>>> handler) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        this.connection.query(sql1, res -> {
            if (!res.succeeded()) {
                if (res.cause() != null) {
                    res.cause().printStackTrace();
                }

                throw new IllegalStateException("Couldnt execute query to read row: " + sql1);
            }

            //get result set
            ResultSet rs = res.result();

            //get first row
            List<JsonObject> rows = rs.getRows();

            handler.handle(Future.succeededFuture(rows));
        });
    }

    public void listRows (String sql, JsonArray params, Handler<AsyncResult<List<JsonObject>>> handler) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        this.connection.queryWithParams(sql1, params, res -> {
            if (!res.succeeded()) {
                if (res.cause() != null) {
                    res.cause().printStackTrace();
                }

                throw new IllegalStateException("Couldnt execute query to read row: " + sql1);
            }

            //get result set
            ResultSet rs = res.result();

            //get first row
            List<JsonObject> rows = rs.getRows();

            handler.handle(Future.succeededFuture(rows));
        });
    }

    @Override
    public List<JsonObject> listRows (String sql, JsonArray params) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        ResultSet rs = awaitResult(h -> this.connection.queryWithParams(sql1, params, h));

        //get first row
        List<JsonObject> rows = rs.getRows();

        return rows;
    }

    @Override
    public List<JsonObject> listRows (String sql) {
        String sql1 = sql.replace("{prefix}", this.getPrefix());

        ResultSet rs = awaitResult(h -> this.connection.query(sql1, h));

        //get first row
        List<JsonObject> rows = rs.getRows();

        return rows;
    }

    public void update (String sql, Handler<AsyncResult<UpdateResult>> handler) {
        sql = sql.replace("{prefix}", this.getPrefix());

        this.connection.update(sql, handler);
    }

    @Override
    public UpdateResult update (String sql, JsonArray params) {
        final String sql1 = sql.replace("{prefix}", this.getPrefix());

        return awaitResult(h -> this.connection.updateWithParams(sql1, params, h));
    }

    @Override
    public UpdateResult update (String sql) {
        return awaitResult(h -> this.update(sql, h));
    }

    public void setAutoCommit (boolean value, Handler<Void> done) {
        this.connection.setAutoCommit(value, res -> {
            if (!res.succeeded()) {
                throw new IllegalStateException("Couldnt set auto commit.");
            }

            done.handle(null);
        });
    }

    public void startTransation (Handler<AsyncResult<Void>> done) {
        this.connection.setAutoCommit(false, res -> {
            if (res.failed()) {
                done.handle(Future.failedFuture(res.cause()));
                throw new RuntimeException(res.cause());
            }

            done.handle(Future.succeededFuture());
            //done.handle(null);
        });
    }

    @Override
    public void startTransaction () {
        Void void1 = awaitResult(h -> startTransation(h));
    }

    public void endTransation (Handler<AsyncResult<Void>> done) {
        this.connection.commit(res -> {
            if (res.failed()) {
                done.handle(Future.failedFuture(res.cause()));
                throw new RuntimeException(res.cause());
            }

            done.handle(Future.succeededFuture());
            //done.handle(null);
        });
    }

    @Override
    public void endTransaction () {
        Void void1 = awaitResult(h -> endTransation(h));
    }

    public void rollbackTransaction (Handler<AsyncResult<Void>> done) {
        this.connection.rollback(res -> {
            if (res.failed()) {
                done.handle(Future.failedFuture(res.cause()));
                throw new RuntimeException(res.cause());
            }

            done.handle(Future.succeededFuture());
        });
    }

    @Override
    public void rollbackTransaction () {
        Void void1 = awaitResult(h -> this.rollbackTransaction(h));
    }

    @Override
    public void close () {
        this.connection.close();
    }

}
