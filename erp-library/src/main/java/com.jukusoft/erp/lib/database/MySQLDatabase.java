package com.jukusoft.erp.lib.database;

import com.jukusoft.erp.lib.utils.FileUtils;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MySQLDatabase {

    //instance of vertx
    protected Vertx vertx = null;

    //sql client
    protected SQLClient client = null;

    //sql connection
    protected SQLConnection connection = null;

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
