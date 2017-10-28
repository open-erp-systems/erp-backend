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

public class MySQLDatabase {

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
        config.put("url", "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC" + urlAdd);
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

    public boolean isConnected () {
        return this.connection != null;
    }

    public SQLConnection getConnection() {
        return this.connection;
    }

    public String getPrefix () {
        return this.prefix;
    }

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
        this.connection.queryWithParams(sql, params, res -> {
            if (!res.succeeded()) {
                if (res.cause() != null) {
                    res.cause().printStackTrace();
                }

                handler.handle(Future.failedFuture(res.cause()));
                throw new IllegalStateException("Couldnt execute query to read row: " + sql + ", exception: " + res.cause());
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

    public void query (String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
        this.connection.queryWithParams(sql, params, handler);
    }

    public void listRows (String sql, Handler<AsyncResult<List<JsonObject>>> handler) {
        this.connection.query(sql, res -> {
            if (!res.succeeded()) {
                if (res.cause() != null) {
                    res.cause().printStackTrace();
                }

                throw new IllegalStateException("Couldnt execute query to read row: " + sql);
            }

            //get result set
            ResultSet rs = res.result();

            //get first row
            List<JsonObject> rows = rs.getRows();

            handler.handle(Future.succeededFuture(rows));
        });
    }

    public void update (String sql, Handler<AsyncResult<UpdateResult>> handler) {
        this.connection.update(sql, handler);
    }

    public void setAutoCommit (boolean value) {
        this.connection.setAutoCommit(value, res -> {
            if (!res.succeeded()) {
                throw new IllegalStateException("Couldnt set auto commit.");
            }
        });
    }

    public void startTransation (Handler<ResultSet> done) {
        this.connection.setAutoCommit(false, res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }

            done.handle(null);
        });
    }

    public void endTransation (Handler<ResultSet> done) {
        this.connection.commit(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }

            done.handle(null);
        });
    }

    public void close () {
        //
    }

}
